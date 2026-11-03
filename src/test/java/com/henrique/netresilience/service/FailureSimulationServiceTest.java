
package com.henrique.netresilience.service;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;

import com.henrique.netresilience.model.result.FailureSimulationResult;
import com.henrique.netresilience.model.result.RouteResult;

import com.henrique.netresilience.model.scenario.FailureScenario;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FailureSimulationServiceTest {

    private final FailureSimulationService service =
            new FailureSimulationService();

    @Test
    void shouldPreserveResultsWhenScenarioHasNoFailures() {
        Graph graph = createDiamondGraph();

        FailureScenario scenario = new FailureScenario();

        FailureSimulationResult result = service.simulate(
                graph, "A", "D", scenario
        );

        assertEquals(
                4.0,
                result.getOriginalRoute().orElseThrow().getTotalWeight()
        );

        assertEquals(
                4.0,
                result.getAffectedRoute().orElseThrow().getTotalWeight()
        );

        assertEquals(
                0.0,
                result.getRouteCostDifference().orElseThrow(),
                0.0001
        );

        assertEquals(1, result.getOriginalComponents().getComponentCount());
        assertEquals(1, result.getAffectedComponents().getComponentCount());
    }

    @Test
    void shouldFindAlternativeRouteAfterEdgeFailure() {
        Graph graph = createDiamondGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableEdge("E1");

        FailureSimulationResult result = service.simulate(
                graph, "A", "D", scenario
        );

        RouteResult originalRoute = result.getOriginalRoute()
                .orElseThrow();

        RouteResult affectedRoute = result.getAffectedRoute()
                .orElseThrow();

        assertEquals(4.0, originalRoute.getTotalWeight(), 0.0001);
        assertEquals(8.0, affectedRoute.getTotalWeight(), 0.0001);

        assertEquals(
                List.of("A", "C", "D"),
                affectedRoute.getNodes()
                        .stream()
                        .map(node -> node.getId())
                        .toList()
        );

        assertEquals(
                4.0,
                result.getRouteCostDifference().orElseThrow(),
                0.0001
        );

        assertTrue(result.isRouteAvailableAfter());
    }

    @Test
    void shouldIdentifyDisconnectedComponentsAfterEdgeFailure() {
        Graph graph = createLineGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableEdge("E2");

        FailureSimulationResult result = service.simulate(
                graph, "A", "C", scenario
        );

        assertTrue(result.isRouteLost());
        assertTrue(result.getAffectedRoute().isEmpty());

        assertEquals(1, result.getOriginalComponents().getComponentCount());
        assertEquals(2, result.getAffectedComponents().getComponentCount());

        assertTrue(result.getRouteCostDifference().isEmpty());
    }

    @Test
    void shouldRemoveFailedNodeAndItsIncidentEdges() {
        Graph graph = createDiamondGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableNode("B");

        FailureSimulationResult result = service.simulate(
                graph, "A", "D", scenario
        );

        RouteResult affectedRoute = result.getAffectedRoute()
                .orElseThrow();

        assertEquals(8.0, affectedRoute.getTotalWeight(), 0.0001);

        assertEquals(
                List.of("A", "C", "D"),
                affectedRoute.getNodes()
                        .stream()
                        .map(node -> node.getId())
                        .toList()
        );

        assertEquals(1, result.getAffectedComponents().getComponentCount());
    }

    @Test
    void shouldReturnNoRouteWhenSourceIsUnavailable() {
        Graph graph = createLineGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableNode("A");

        FailureSimulationResult result = service.simulate(
                graph, "A", "C", scenario
        );

        assertTrue(result.isRouteAvailableBefore());
        assertFalse(result.isRouteAvailableAfter());
        assertTrue(result.isRouteLost());

        assertEquals(1, result.getAffectedComponents().getComponentCount());
    }

    @Test
    void shouldReturnNoRouteWhenTargetIsUnavailable() {
        Graph graph = createLineGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableNode("C");

        FailureSimulationResult result = service.simulate(
                graph, "A", "C", scenario
        );

        assertTrue(result.isRouteAvailableBefore());
        assertFalse(result.isRouteAvailableAfter());
        assertTrue(result.isRouteLost());
    }

    @Test
    void shouldRejectUnknownNodeInScenario() {
        Graph graph = createLineGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableNode("UNKNOWN");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.simulate(graph, "A", "C", scenario)
        );
    }

    @Test
    void shouldRejectUnknownEdgeInScenario() {
        Graph graph = createLineGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableEdge("UNKNOWN");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.simulate(graph, "A", "C", scenario)
        );
    }

    @Test
    void shouldNotModifyOriginalGraphDuringSimulation() {
        Graph graph = createDiamondGraph();

        FailureScenario scenario = new FailureScenario();
        scenario.addUnavailableNode("B");

        service.simulate(graph, "A", "D", scenario);

        // Original graph must remain unchanged.
        assertEquals(4, graph.nodeCount());
        assertEquals(4, graph.edgeCount());

        assertTrue(graph.findNode("B").isPresent());
        assertTrue(graph.findEdge("E1").isPresent());
    }

    private Graph createLineGraph() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 2.0);
        graph.addEdge("E2", "B", "C", 3.0);

        return graph;
    }

    private Graph createDiamondGraph() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");
        graph.addNode("D", "Node D");

        graph.addEdge("E1", "A", "B", 2.0);
        graph.addEdge("E2", "B", "D", 2.0);
        graph.addEdge("E3", "A", "C", 4.0);
        graph.addEdge("E4", "C", "D", 4.0);

        return graph;
    }
}
