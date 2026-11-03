
package com.henrique.netresilience.model.result;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FailureSimulationResultTest {

    @Test
    void shouldStoreSimulationResultsAndCalculateCostDifference() {
        FailureSimulationResult result = createResult(
                true,
                Set.of("B"),
                Set.of("E2")
        );

        assertEquals("A", result.getSourceNodeId());
        assertEquals("C", result.getTargetNodeId());

        assertEquals(
                10.0,
                result.getOriginalRoute().orElseThrow().getTotalWeight()
        );

        assertEquals(
                15.0,
                result.getAffectedRoute().orElseThrow().getTotalWeight()
        );

        assertEquals(
                5.0,
                result.getRouteCostDifference().orElseThrow(),
                0.0001
        );

        assertTrue(result.isRouteAvailableBefore());
        assertTrue(result.isRouteAvailableAfter());
        assertFalse(result.isRouteLost());
    }

    @Test
    void shouldIdentifyWhenRouteIsLostAfterFailure() {
        FailureSimulationResult result = createResult(
                false,
                Set.of("B"),
                Set.of("E2")
        );

        assertTrue(result.isRouteAvailableBefore());
        assertFalse(result.isRouteAvailableAfter());
        assertTrue(result.isRouteLost());

        assertTrue(result.getAffectedRoute().isEmpty());
        assertTrue(result.getRouteCostDifference().isEmpty());
    }

    @Test
    void shouldKeepIndependentCopiesOfFailureIds() {
        Set<String> unavailableNodes = new LinkedHashSet<>();
        Set<String> unavailableEdges = new LinkedHashSet<>();

        unavailableNodes.add("B");
        unavailableEdges.add("E2");

        FailureSimulationResult result = createResult(
                true,
                unavailableNodes,
                unavailableEdges
        );

        unavailableNodes.add("C");
        unavailableEdges.add("E3");

        assertEquals(
                Set.of("B"),
                result.getUnavailableNodeIds()
        );

        assertEquals(
                Set.of("E2"),
                result.getUnavailableEdgeIds()
        );
    }

    @Test
    void shouldPreventExternalModificationOfFailureIds() {
        FailureSimulationResult result = createResult(
                true,
                Set.of("B"),
                Set.of("E2")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.getUnavailableNodeIds().add("C")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.getUnavailableEdgeIds().clear()
        );
    }

    @Test
    void shouldRejectRoutesWithDifferentEndpoints() {
        Graph graph = new Graph();

        Node a = graph.addNode("A", "Node A");
        Node b = graph.addNode("B", "Node B");
        Node c = graph.addNode("C", "Node C");

        Edge edge = graph.addEdge("E1", "B", "C", 5.0);

        RouteResult invalidRoute = new RouteResult(
                List.of(b, c),
                List.of(edge)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new FailureSimulationResult(
                        "A",
                        "C",
                        Optional.of(invalidRoute),
                        Optional.empty(),
                        new ConnectedComponentsResult(List.of(List.of(a, b, c))),
                        new ConnectedComponentsResult(List.of(List.of(a, b, c))),
                        new CriticalElementsResult(List.of(), List.of()),
                        new CriticalElementsResult(List.of(), List.of()),
                        Set.of(),
                        Set.of()
                )
        );
    }

    @Test
    void shouldRejectInvalidSourceId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createResultWithInvalidSource()
        );
    }

    private static FailureSimulationResult createResult(
            boolean routeAvailableAfter,
            Set<String> unavailableNodes,
            Set<String> unavailableEdges
    ) {
        Graph graph = new Graph();

        Node a = graph.addNode("A", "Node A");
        Node b = graph.addNode("B", "Node B");
        Node c = graph.addNode("C", "Node C");

        Edge e1 = graph.addEdge("E1", "A", "B", 4.0);
        Edge e2 = graph.addEdge("E2", "B", "C", 6.0);
        Edge e3 = graph.addEdge("E3", "A", "C", 15.0);

        RouteResult originalRoute = new RouteResult(
                List.of(a, b, c),
                List.of(e1, e2)
        );

        Optional<RouteResult> affectedRoute =
                routeAvailableAfter
                        ? Optional.of(new RouteResult(
                                List.of(a, c),
                                List.of(e3)
                        ))
                        : Optional.empty();

        ConnectedComponentsResult originalComponents =
                new ConnectedComponentsResult(
                        List.of(List.of(a, b, c))
                );

        ConnectedComponentsResult affectedComponents =
                routeAvailableAfter
                        ? new ConnectedComponentsResult(
                                List.of(List.of(a, b, c))
                        )
                        : new ConnectedComponentsResult(
                                List.of(List.of(a, b), List.of(c))
                        );

        CriticalElementsResult originalCriticalElements =
                new CriticalElementsResult(
                        List.of(b),
                        List.of(e1, e2)
                );

        CriticalElementsResult affectedCriticalElements =
                new CriticalElementsResult(
                        List.of(),
                        List.of()
                );

        return new FailureSimulationResult(
                "A",
                "C",
                Optional.of(originalRoute),
                affectedRoute,
                originalComponents,
                affectedComponents,
                originalCriticalElements,
                affectedCriticalElements,
                unavailableNodes,
                unavailableEdges
        );
    }

    private static void createResultWithInvalidSource() {
        new FailureSimulationResult(
            null,
            "C",
            Optional.empty(),
            Optional.empty(),
            new ConnectedComponentsResult(List.of(
                    List.of(new Node("A", "Node A"))
            )),
            new ConnectedComponentsResult(List.of(
                    List.of(new Node("A", "Node A"))
            )),
            new CriticalElementsResult(List.of(), List.of()),
            new CriticalElementsResult(List.of(), List.of()),
            Set.of(),
            Set.of()
        );
    }
}
