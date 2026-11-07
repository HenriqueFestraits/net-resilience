
package com.henrique.netresilience.service;

import com.henrique.netresilience.model.graph.Graph;

import com.henrique.netresilience.model.result.FailureSimulationResult;
import com.henrique.netresilience.model.result.RouteResult;
import com.henrique.netresilience.model.result.NetworkResilienceMetrics;

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

        assertEquals(
                0.0,
                result.getRouteCostChangePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(1, result.getOriginalComponents().getComponentCount());
        assertEquals(1, result.getAffectedComponents().getComponentCount());

        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        assertEquals(4, metrics.getOriginalNodeCount());
        assertEquals(4, metrics.getAvailableNodeCount());

        assertEquals(4, metrics.getOriginalEdgeCount());
        assertEquals(4, metrics.getAvailableEdgeCount());

        assertEquals(4, metrics.getLargestAffectedComponentSize());

        assertEquals(
                100.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                100.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                100.0,
                metrics.getLargestComponentCoveragePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(0, metrics.getComponentCountChange());
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

        assertEquals(
                100.0,
                result.getRouteCostChangePercentage().orElseThrow(),
                0.0001
        );

        assertTrue(result.isRouteAvailableAfter());

        // Resilience metrics
        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        assertEquals(4, metrics.getOriginalNodeCount());
        assertEquals(4, metrics.getAvailableNodeCount());

        assertEquals(4, metrics.getOriginalEdgeCount());
        assertEquals(3, metrics.getAvailableEdgeCount());

        assertEquals(
                75.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                100.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                100.0,
                metrics.getLargestComponentCoveragePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(0, metrics.getComponentCountChange());
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
        assertTrue(result.getRouteCostChangePercentage().isEmpty());

        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        assertEquals(3, metrics.getAvailableNodeCount());
        assertEquals(1, metrics.getAvailableEdgeCount());

        assertEquals(2, metrics.getLargestAffectedComponentSize());

        assertEquals(
                50.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                100.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                200.0 / 3.0,
                metrics.getLargestComponentCoveragePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(1, metrics.getComponentCountChange());
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

        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        assertEquals(3, metrics.getAvailableNodeCount());
        assertEquals(2, metrics.getAvailableEdgeCount());

        assertEquals(
                75.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                50.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                75.0,
                metrics.getLargestComponentCoveragePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(0, metrics.getComponentCountChange());
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

        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        assertEquals(2, metrics.getAvailableNodeCount());
        assertEquals(1, metrics.getAvailableEdgeCount());

        assertEquals(
                200.0 / 3.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                50.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(2, metrics.getLargestAffectedComponentSize());
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

        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        assertEquals(2, metrics.getAvailableNodeCount());
        assertEquals(1, metrics.getAvailableEdgeCount());

        assertEquals(
                200.0 / 3.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                50.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(1, metrics.getAffectedComponentCount());
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

        FailureSimulationResult result = service.simulate(
                graph, "A", "D", scenario
        );

        // Original graph must remain unchanged.
        assertEquals(4, graph.nodeCount());
        assertEquals(4, graph.edgeCount());

        assertTrue(graph.findNode("B").isPresent());
        assertTrue(graph.findEdge("E1").isPresent());

        // The simulation must use an affected graph.
        assertEquals(
                3,
                result.getResilienceMetrics().getAvailableNodeCount()
        );

        assertEquals(
                2,
                result.getResilienceMetrics().getAvailableEdgeCount()
        );
    }

    @Test
    void shouldCalculateMetricsForMultipleSimultaneousFailures() {

        Graph graph = createDiamondGraph();

        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("B");
        scenario.addUnavailableEdge("E3");

        FailureSimulationResult result = service.simulate(
                graph, "A", "D", scenario
        );

        assertTrue(result.isRouteLost());
        assertTrue(result.getAffectedRoute().isEmpty());

        NetworkResilienceMetrics metrics = result.getResilienceMetrics();

        // B removes E1 and E2 implicitly.
        // E3 is also explicitly unavailable.
        assertEquals(3, metrics.getAvailableNodeCount());
        assertEquals(1, metrics.getAvailableEdgeCount());

        assertEquals(2, metrics.getAffectedComponentCount());
        assertEquals(2, metrics.getLargestAffectedComponentSize());

        assertEquals(
                75.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                25.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                50.0,
                metrics.getLargestComponentCoveragePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(1, metrics.getComponentCountChange());
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
