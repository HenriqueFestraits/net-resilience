package com.henrique.netresilience.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.RouteResult;

public final class Dijkstra{

    public Optional<RouteResult> findShortestPath(
        Graph graph,
        String sourceId,
        String targetId
    ){
        Objects.requireNonNull(graph, "Graph cannot be null");

        Node source = graph.getNode(sourceId);
        Node target = graph.getNode(targetId);

        Map<String, Double> distances = new HashMap<>();

        Map<String, String> previousNodeById = new HashMap<>();
        Map<String, Edge> previousEdgeById = new HashMap<>();

        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(
            Comparator.comparingDouble(QueueEntry::distance)
        );

        distances.put(source.getId(), 0.0);
        queue.offer(new QueueEntry(source, 0.0));

        while(!queue.isEmpty()){
            QueueEntry entry = queue.poll();

            Node currentNode = entry.node();
            String currentId = currentNode.getId();

            double bestKnownDistance = distances.getOrDefault(currentId, Double.POSITIVE_INFINITY);

            if(Double.compare(entry.distance(), bestKnownDistance) != 0){
                continue;
            }

            if(currentId.equals(target.getId())){
                break;
            }

            for(Edge edge : graph.getIncidentEdges(currentId)){
                double weight = edge.getWeight();

                if(!Double.isFinite(weight) || weight < 0){
                    throw new IllegalArgumentException("Edge weight must be a non-negative finite number");
                }

                Node neighbor = edge.getOtherNode(currentNode);
                String neighborId = neighbor.getId();

                double newDistance = entry.distance() + weight;

                if(!Double.isFinite(newDistance)){
                    throw new IllegalArgumentException("Total distance must be finite");
                }

                double currentBest = distances.getOrDefault(neighborId, Double.POSITIVE_INFINITY);

                if(newDistance < currentBest){
                    distances.put(neighborId, newDistance);
                    previousNodeById.put(neighborId, currentId);
                    previousEdgeById.put(neighborId, edge);
                    queue.offer(new QueueEntry(neighbor, newDistance));
                }
            }
        }

        if(!distances.containsKey(target.getId())){
            return Optional.empty();
        }

        return Optional.of(
            reconstructRoute(
                graph,
                source.getId(),
                target.getId(),
                previousNodeById,
                previousEdgeById
            )
        );
    }

    private RouteResult reconstructRoute(
        Graph graph,
        String sourceId,
        String targetId,
        Map<String, String> previousNodeById,
        Map<String, Edge> previousEdgeById
    ){
        List<Node> reversedNodes = new ArrayList<>();
        List<Edge> reversedEdges = new ArrayList<>();

        String currentId = targetId;
        reversedNodes.add(graph.getNode(currentId));

        while(!currentId.equals(sourceId)){
            String previousId = previousNodeById.get(currentId);
            Edge previousEdge = previousEdgeById.get(currentId);

            if(previousId == null || previousEdge == null){
                throw new IllegalStateException("Inconsistent state while reconstructing the route");
            }

            reversedEdges.add(previousEdge);

            currentId = previousId;
            reversedNodes.add(graph.getNode(currentId));
        }

        Collections.reverse(reversedNodes);
        Collections.reverse(reversedEdges);

        return new RouteResult(reversedNodes, reversedEdges);
    }

    private record QueueEntry(Node node, double distance) {}

}
