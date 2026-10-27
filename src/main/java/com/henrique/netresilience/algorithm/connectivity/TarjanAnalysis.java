package com.henrique.netresilience.algorithm.connectivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.CriticalElementsResult;

public final class TarjanAnalysis{

    public CriticalElementsResult analyze(Graph graph){
        
        Objects.requireNonNull(graph, "Graph cannot be null");

        Map<String, Integer> discoveryTimes = new HashMap<>();
        Map<String, Integer> lowValues = new HashMap<>();

        Set<String> articulationNodeIds = new HashSet<>();
        List<Edge> bridges = new ArrayList<>();

        int[] time = {0};

        for(Node node : graph.getNodes()){
            if(!discoveryTimes.containsKey(node.getId())){
                dfs(
                    graph,
                    node,
                    null,
                    discoveryTimes,
                    lowValues,
                    articulationNodeIds,
                    bridges,
                    time
                );
            }
        }

        List<Node> articulationPoints = new ArrayList<>();

        for(Node node : graph.getNodes()){
            if(articulationNodeIds.contains(node.getId())){
                articulationPoints.add(node);
            }
        }

        return new CriticalElementsResult(articulationPoints, bridges);
    }

    private void dfs(
        Graph graph,
        Node current,
        Edge parentEdge,
        Map<String, Integer> discoveryTimes,
        Map<String, Integer> lowValues,
        Set<String> articulationNodeIds,
        List<Edge> bridges,
        int[] time
    ){
        String currentId = current.getId();

        int descoveryTime = time[0]++;

        discoveryTimes.put(currentId, descoveryTime);
        lowValues.put(currentId, descoveryTime);

        int children = 0;

        for(Edge edge : graph.getIncidentEdges(currentId)){
            //Skip only the exact edge used to reach this node.
            //This is important when parallel edges exist.
            if(edge == parentEdge){
                continue;
            }

            Node neighbor = edge.getOtherNode(current);
            String neighborId = neighbor.getId();

            if(!discoveryTimes.containsKey(neighborId)){
                children++;
                dfs(
                    graph,
                    neighbor,
                    edge,
                    discoveryTimes,
                    lowValues,
                    articulationNodeIds,
                    bridges,
                    time
                );

                int neighborLow = lowValues.get(neighborId);

                lowValues.put(
                    currentId,
                    Math.min(lowValues.get(currentId), neighborLow)
                );

                //A non-root node is an articulation point if a child cannot reach a strict ancestor through a back edge.
                if(parentEdge != null && neighborLow >= discoveryTimes.get(currentId)){
                    articulationNodeIds.add(currentId);
                }

                //The tree edge is a bridge if the child's subtree cannot reach the current node or any ancestors
                if(neighborLow > discoveryTimes.get(currentId)){
                    bridges.add(edge);
                }
            } else {
                //A visited neighbor provides a back edge.
                lowValues.put(
                    currentId,
                    Math.min(lowValues.get(currentId), discoveryTimes.get(neighborId))
                );
            }
        }

        //A DFS root is an articulation point if it has more than one child in the DFS tree.
        if(parentEdge == null && children > 1){
            articulationNodeIds.add(currentId);
        }
    }

}