
package com.henrique.netresilience.algorithm.connectivity;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.CriticalElementsResult;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TarjanAnalysisTest {

    private final TarjanAnalysis analysis = new TarjanAnalysis();

    @Test
    void shouldFindBridgesAndArticulationPointsInLineGraph() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 1.0);
        graph.addEdge("E2", "B", "C", 1.0);

        CriticalElementsResult result = analysis.analyze(graph);

        assertEquals(
                Set.of("B"),
                nodeIds(result)
        );

        assertEquals(
                Set.of("E1", "E2"),
                edgeIds(result)
        );
    }

    @Test
    void shouldFindNoCriticalElementsInTriangle() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 1.0);
        graph.addEdge("E2", "B", "C", 1.0);
        graph.addEdge("E3", "C", "A", 1.0);

        CriticalElementsResult result = analysis.analyze(graph);

        assertTrue(result.getArticulationPoints().isEmpty());
        assertTrue(result.getBridges().isEmpty());
    }

    @Test
    void shouldHandleParallelEdgesCorrectly() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 2.0);
        graph.addEdge("E2", "A", "B", 3.0);
        graph.addEdge("E3", "B", "C", 4.0);

        CriticalElementsResult result = analysis.analyze(graph);

        assertEquals(
                Set.of("B"),
                nodeIds(result)
        );

        assertEquals(
                Set.of("E3"),
                edgeIds(result)
        );
    }

    @Test
    void shouldIdentifyRootWithMultipleDfsChildren() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 1.0);
        graph.addEdge("E2", "A", "C", 1.0);

        CriticalElementsResult result = analysis.analyze(graph);

        assertEquals(
                Set.of("A"),
                nodeIds(result)
        );

        assertEquals(
                Set.of("E1", "E2"),
                edgeIds(result)
        );
    }

    @Test
    void shouldAnalyzeDisconnectedComponents() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");
        graph.addNode("D", "Node D");
        graph.addNode("E", "Isolated node");

        graph.addEdge("E1", "A", "B", 1.0);
        graph.addEdge("E2", "C", "D", 1.0);

        CriticalElementsResult result = analysis.analyze(graph);

        assertTrue(result.getArticulationPoints().isEmpty());

        assertEquals(
                Set.of("E1", "E2"),
                edgeIds(result)
        );
    }

    private Set<String> nodeIds(CriticalElementsResult result) {
        return result.getArticulationPoints()
                .stream()
                .map(Node::getId)
                .collect(Collectors.toSet());
    }

    private Set<String> edgeIds(CriticalElementsResult result) {
        return result.getBridges()
                .stream()
                .map(Edge::getId)
                .collect(Collectors.toSet());
    }
}
