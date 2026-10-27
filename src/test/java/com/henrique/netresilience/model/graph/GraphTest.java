
package com.henrique.netresilience.model.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void shouldAddNodesAndRetrieveThem() {
        Graph graph = new Graph();

        Node a = graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");

        assertEquals(2, graph.nodeCount());
        assertSame(a, graph.getNode("A"));
        assertEquals("Node B", graph.getNode("B").getLabel());
    }

    @Test
    void shouldRejectDuplicateNodeId() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addNode("A", "Another Node")
        );

        assertEquals(1, graph.nodeCount());
        assertEquals("Node A", graph.getNode("A").getLabel());
    }

    @Test
    void shouldAddEdgeAndRegisterItForBothNodes() {
        Graph graph = new Graph();

        Node a = graph.addNode("A", "Node A");
        Node b = graph.addNode("B", "Node B");

        Edge edge = graph.addEdge("E1", "A", "B", 5.0);

        assertEquals(1, graph.edgeCount());
        assertEquals(5.0, edge.getWeight(), 0.0001);

        assertEquals(1, graph.getIncidentEdges("A").size());
        assertEquals(1, graph.getIncidentEdges("B").size());

        assertSame(edge, graph.getIncidentEdges("A").get(0));
        assertSame(edge, graph.getIncidentEdges("B").get(0));

        assertSame(edge, graph.findEdge("E1").orElseThrow());

        assertSame(b, edge.getOtherNode(a));
        assertSame(a, edge.getOtherNode(b));
    }

    @Test
    void shouldRejectDuplicateEdgeId() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 5.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge("E1", "A", "C", 3.0)
        );

        assertEquals(1, graph.edgeCount());
        assertEquals(1, graph.getIncidentEdges("A").size());
        assertEquals(0, graph.getIncidentEdges("C").size());
    }

    @Test
    void shouldRejectEdgeWhenSourceDoesNotExist() {
        Graph graph = new Graph();

        graph.addNode("B", "Node B");

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge("E1", "A", "B", 5.0)
        );

        assertEquals(0, graph.edgeCount());
        assertTrue(graph.getIncidentEdges("B").isEmpty());
    }

    @Test
    void shouldRejectEdgeWhenTargetDoesNotExist() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge("E1", "A", "B", 5.0)
        );

        assertEquals(0, graph.edgeCount());
        assertTrue(graph.getIncidentEdges("A").isEmpty());
    }

    @Test
    void shouldRejectSelfLoop() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge("E1", "A", "A", 5.0)
        );

        assertEquals(0, graph.edgeCount());
        assertTrue(graph.getIncidentEdges("A").isEmpty());
    }

    @Test
    void shouldReturnUniqueNeighborsWhenParallelEdgesExist() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");

        graph.addEdge("E1", "A", "B", 5.0);
        graph.addEdge("E2", "A", "B", 3.0);

        assertEquals(2, graph.edgeCount());
        assertEquals(2, graph.getIncidentEdges("A").size());

        assertEquals(1, graph.getNeighbors("A").size());
        assertEquals("B", graph.getNeighbors("A").get(0).getId());
    }

    @Test
    void shouldReturnUnmodifiableCollections() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addEdge("E1", "A", "B", 5.0);

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getNodes().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getEdges().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getIncidentEdges("A").clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getNeighbors("A").clear()
        );
    }
}
