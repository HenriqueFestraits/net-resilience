package com.henrique.netresilience.model.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Graph {
    
    private final Map<String, Node> nodesById = new LinkedHashMap<>();
    private final Map<String, Edge> edgesById = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new LinkedHashMap<>();


    // Add a node to the graph
    public Node addNode(String id, String Label){
        Node node = new Node(id, Label);

        if(nodesById.containsKey(id)){
            throw new IllegalArgumentException("Node with id " + id + " already exists");
        }

        nodesById.put(node.getId(), node);
        adjacencyList.put(node.getId(), new ArrayList<>());

        return node;
    }

    // Add an edge to the graph
    public Edge addEdge(
        String edgeId,
        String sourceId,
        String targetId,
        double weight
    ){
        if(edgesById.containsKey(edgeId)){
            throw new IllegalArgumentException("Edge with id " + edgeId + " already exists");
        }
        Node source = getNode(sourceId);
        Node target = getNode(targetId);

        if(source.getId().equals(target.getId())){
            throw new IllegalArgumentException("Source and target nodes cannot be the same");
        }

        Edge edge = new Edge(edgeId, source, target, weight);

        edgesById.put(edge.getId(), edge);
        adjacencyList.get(source.getId()).add(edge);
        adjacencyList.get(target.getId()).add(edge);

        return edge;
    }

    // Get a node by its id  
    public Node getNode(String id){

        Node node = nodesById.get(id);

        if(node == null){
            throw new IllegalArgumentException("Node with id " + id + " does not exist");
        }

        return node;
    }

    //Search for a node by its id and return it if it exists
    public Optional<Node> findNode(String id){
        return Optional.ofNullable(nodesById.get(id));
    }

    //Search for an edge by its id and return it if it exists
    public Optional<Edge> findEdge(String id){
        return Optional.ofNullable(edgesById.get(id));
    }

    //Return a list of all nodes in the graph
    public List<Node> getNodes(){
        return List.copyOf(nodesById.values());
    }

    //Return a list of all edges in the graph
    public List<Edge> getEdges(){
        return List.copyOf(edgesById.values());
    }

    //Return a list of all edges incident to a given node
    public List<Edge> getIncidentEdges(String nodeId){
        if(!nodesById.containsKey(nodeId)){
            throw new IllegalArgumentException("Node with id " + nodeId + " does not exist");
        }

        return List.copyOf(adjacencyList.get(nodeId));
    }

    //Return a list of all neighbors of a given node
    public List<Node> getNeighbors(String nodeId){
        if(!nodesById.containsKey(nodeId)){
            throw new IllegalArgumentException("Node with id " + nodeId + " does not exist");
        }

        Map<String, Node> neighbors = new LinkedHashMap<>();

        for(Edge edge : adjacencyList.get(nodeId)){
            Node neighbor = edge.getOtherNode(nodesById.get(nodeId));
            neighbors.putIfAbsent(neighbor.getId(), neighbor);
        }

        return List.copyOf(neighbors.values());
    }

    public int nodeCount(){
        return nodesById.size();
    }

    public int edgeCount(){
        return edgesById.size();
    }
}
