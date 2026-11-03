
package com.henrique.netresilience.algorithm.connectivity;

import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.ConnectedComponentsResult;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConnectedComponentsTest {

    private final ConnectedComponents algorithm =
            new ConnectedComponents();

    @Test
    void shouldFindSingleComponentInConnectedGraph() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 2.0);
        graph.addEdge("E2", "B", "C", 3.0);

        ConnectedComponentsResult result = algorithm.analyze(graph);

        assertEquals(1, result.getComponentCount());
        assertTrue(result.isConnected());

        assertEquals(
                Set.of("A", "B", "C"),
                nodeIds(result.getComponents().get(0))
        );
    }

    @Test
    void shouldIdentifyMultipleDisconnectedComponents() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");
        graph.addNode("D", "Node D");
        graph.addNode("E", "Isolated node");

        graph.addEdge("E1", "A", "B", 1.0);
        graph.addEdge("E2", "C", "D", 1.0);

        ConnectedComponentsResult result = algorithm.analyze(graph);

        assertEquals(3, result.getComponentCount());
        assertFalse(result.isConnected());

        Set<Set<String>> actual = result.getComponents()
                .stream()
                .map(ConnectedComponentsTest::nodeIds)
                .collect(Collectors.toSet());

        Set<Set<String>> expected = Set.of(
                Set.of("A", "B"),
                Set.of("C", "D"),
                Set.of("E")
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNoComponentsForEmptyGraph() {
        Graph graph = new Graph();

        ConnectedComponentsResult result = algorithm.analyze(graph);

        assertEquals(0, result.getComponentCount());
        assertTrue(result.getComponents().isEmpty());
        assertFalse(result.isConnected());
    }

    @Test
    void shouldTreatSingleNodeAsConnected() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");

        ConnectedComponentsResult result = algorithm.analyze(graph);

        assertEquals(1, result.getComponentCount());
        assertTrue(result.isConnected());
        assertEquals(
                Set.of("A"),
                nodeIds(result.getComponents().get(0))
        );
    }

    @Test
    void shouldHandleParallelEdgesWithoutDuplicatingNodes() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 5.0);
        graph.addEdge("E2", "A", "B", 3.0);
        graph.addEdge("E3", "B", "C", 2.0);

        ConnectedComponentsResult result = algorithm.analyze(graph);

        assertEquals(1, result.getComponentCount());
        assertTrue(result.isConnected());

        assertEquals(
                Set.of("A", "B", "C"),
                nodeIds(result.getComponents().get(0))
        );
    }

    private static Set<String> nodeIds(Iterable<Node> nodes) {
        java.util.Set<String> ids = new java.util.HashSet<>();

        for (Node node : nodes) {
            ids.add(node.getId());
        }

        return ids;
    }
}
