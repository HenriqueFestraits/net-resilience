package com.henrique.netresilience.service;

import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.result.RedundancyAnalysisResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedundancyAnalysisServiceTest {

    private final RedundancyAnalysisService service = new RedundancyAnalysisService();

    @Test
    void shouldReduceBridgeCountWhenAddingAlternativeLinkToLineNetwork() {
        Graph graph = new Graph();
        graph.addNode("A", "Central");
        graph.addNode("B", "Distribution point");
        graph.addNode("C", "Neighborhood");
        graph.addEdge("E1", "A", "B", 2);
        graph.addEdge("E2", "B", "C", 3);

        RedundancyAnalysisResult result =
                service.analyze(graph, "H1", "A", "C", 4);

        assertEquals(2, result.getBridgeCountBefore());
        assertEquals(0, result.getBridgeCountAfter());
        assertEquals(1, result.getArticulationPointCountBefore());
        assertEquals(0, result.getArticulationPointCountAfter());
        assertEquals(1, result.getComponentCountBefore());
        assertEquals(1, result.getComponentCountAfter());
        assertEquals(2, graph.edgeCount(), "Original graph must remain unchanged.");
        assertTrue(graph.findEdge("H1").isEmpty());
    }

    @Test
    void shouldConnectPreviouslyDisconnectedComponents() {
        Graph graph = new Graph();
        graph.addNode("A", "Central A");
        graph.addNode("B", "Central B");

        RedundancyAnalysisResult result =
                service.analyze(graph, "H1", "A", "B", 1.5);

        assertEquals(2, result.getComponentCountBefore());
        assertEquals(1, result.getComponentCountAfter());
        assertEquals(0, graph.edgeCount());
    }

    @Test
    void shouldRejectUnknownNodesAndInvalidConnections() {
        Graph graph = new Graph();
        graph.addNode("A", "A");
        graph.addNode("B", "B");

        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(graph, "H1", "A", "X", 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(graph, "H1", "A", "A", 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(graph, "H1", "A", "B", -1));
    }

    @Test
    void shouldAllowParallelLinkAsPhysicalRedundancyCandidate() {
        Graph graph = new Graph();
        graph.addNode("A", "A");
        graph.addNode("B", "B");
        graph.addEdge("E1", "A", "B", 5);

        RedundancyAnalysisResult result =
                service.analyze(graph, "H1", "A", "B", 6);

        assertEquals(1, result.getBridgeCountBefore());
        assertEquals(0, result.getBridgeCountAfter());
        assertEquals(1, graph.edgeCount());
    }
}
