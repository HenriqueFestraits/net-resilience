package com.henrique.netresilience.algorithm.connectivity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.ConnectedComponentsResult;

public final class ConnectedComponents {
    
    public ConnectedComponentsResult analyze(Graph graph){
        Objects.requireNonNull(graph, "Graph cannot be null");

        Set<String> visited = new HashSet<>();
        List<List<Node>> components = new ArrayList<>();

        //Start a search from every node that has not been visited
        for(Node start : graph.getNodes()){
            if(visited.contains(start.getId())){
                continue;
            }

            List<Node> component = new ArrayList<>();
            Queue<Node> queue = new ArrayDeque<>();

            visited.add(start.getId());
            queue.offer(start);

            //BFS explores all nodes reachble from the starting node.
            while(!queue.isEmpty()){
                Node current = queue.poll();

                component.add(current);

                for(Edge edge : graph.getIncidentEdges(current.getId())){
                    Node neighbor = edge.getOtherNode(current);
                    
                    if(visited.add(neighbor.getId())){
                        queue.offer(neighbor);
                    }
                }
            }

            components.add(component);
        }

        return new ConnectedComponentsResult(components);
    }
}
