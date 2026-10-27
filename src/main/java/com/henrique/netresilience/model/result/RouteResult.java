package com.henrique.netresilience.model.result;

import java.util.List;
import java.util.Objects;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Node;

public class RouteResult {
    
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final double totalWeight;

    public RouteResult(List<Node> nodes, List<Edge> edges) {

        Objects.requireNonNull(nodes, "Nodes cannot be null");
        Objects.requireNonNull(edges, "Edges cannot be null");

        if(nodes.isEmpty()){
            throw new IllegalArgumentException("A route must contain at least one node");
        }
        if(edges.size() != nodes.size() - 1){
            throw new IllegalArgumentException("A route must have exactly one edge between each pair of consecutive nodes");
        }

        this.nodes = nodes;
        this.edges = edges;
        
        double calculatedWeight = 0.0;

        for(int i = 0; i < this.edges.size(); i++){
            Node current = this.nodes.get(i);
            Node next = this.nodes.get(i + 1);
            Edge edge = this.edges.get(i);

            Node otherNode = edge.getOtherNode(current);

            if(!otherNode.getId().equals(next.getId())){
                throw new IllegalArgumentException("An edge does not connect consecutive nodes in the route");
            }

            double weight = edge.getWeight();

            if(!Double.isFinite(weight) || weight < 0){
                throw new IllegalArgumentException("Edge weight must be a non-negative finite number");
            }

            calculatedWeight += weight;

            if(!Double.isFinite(calculatedWeight)){
                throw new IllegalArgumentException("Total route weight must be finite");
            }
        }

        this.totalWeight = calculatedWeight;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public Node getSource(){
        return nodes.get(0);
    }

    public Node getTarget(){
        return nodes.get(nodes.size() - 1);
    }
}
