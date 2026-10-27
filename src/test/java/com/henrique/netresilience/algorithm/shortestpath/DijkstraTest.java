
package com.henrique.netresilience.algorithm.shortestpath;

import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.result.RouteResult;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraTest {

    @Test
    void shouldFindShortestPath() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 5.0);
        graph.addEdge("E2", "B", "C", 3.0);
        graph.addEdge("E3", "A", "C", 10.0);

        Dijkstra dijkstra = new Dijkstra();

        RouteResult result = dijkstra.findShortestPath(
                graph, "A", "C"
        ).orElseThrow();

        assertEquals(
                List.of("A", "B", "C"),
                result.getNodes().stream()
                        .map(node -> node.getId())
                        .toList()
        );

        assertEquals(
                List.of("E1", "E2"),
                result.getEdges().stream()
                        .map(edge -> edge.getId())
                        .toList()
        );

        assertEquals(8.0, result.getTotalWeight(), 0.0001);
    }

    @Test
    void shouldReturnEmptyWhenNoRouteExists() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 5.0);

        Dijkstra dijkstra = new Dijkstra();

        Optional<RouteResult> result = dijkstra.findShortestPath(
                graph, "A", "C"
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnZeroCostWhenSourceEqualsTarget() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");

        Dijkstra dijkstra = new Dijkstra();

        RouteResult result = dijkstra.findShortestPath(
                graph, "A", "A"
        ).orElseThrow();

        assertEquals(List.of("A"), result.getNodes().stream()
                .map(node -> node.getId())
                .toList());

        assertTrue(result.getEdges().isEmpty());
        assertEquals(0.0, result.getTotalWeight(), 0.0001);
    }

    @Test
    void shouldChooseCheapestParallelEdge() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        graph.addEdge("E1", "A", "B", 10.0);
        graph.addEdge("E2", "A", "B", 3.0);
        graph.addEdge("E3", "B", "C", 2.0);

        Dijkstra dijkstra = new Dijkstra();

        RouteResult result = dijkstra.findShortestPath(
                graph, "A", "C"
        ).orElseThrow();

        assertEquals(5.0, result.getTotalWeight(), 0.0001);

        assertEquals(
                List.of("E2", "E3"),
                result.getEdges().stream()
                        .map(edge -> edge.getId())
                        .toList()
        );
    }
}
