package com.henrique.netresilience.model.graph;

public class Node{

    private final String id;
    private final String label;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
    }


    public String getId() {
        return id;
    }
    public String getLabel() {
        return label;
    }

}