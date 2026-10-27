package com.henrique.netresilience.model.graph;

public class Edge {
    
    private final Node source;
    private final Node target;
    private final double weight;
    private final String id;

    public Edge(String id, Node source, Node target, double weight) {
        if(source == null || target == null){
            throw new IllegalArgumentException("Source and target nodes cannot be null");
        }

        if(weight < 0|| !Double.isFinite(weight)){
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        this.id = id;
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public Node getOtherNode(Node node) {
        if(node == null){
            throw new IllegalArgumentException("Node cannot be null");
        }

        if(source.getId().equals(node.getId())){
            return target;
        } else if(target.getId().equals(node.getId())){
            return source;
        } else {
            throw new IllegalArgumentException("Node is not part of this edge");
        }
    }
}
