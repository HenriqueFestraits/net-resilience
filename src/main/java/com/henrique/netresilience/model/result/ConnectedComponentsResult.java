package com.henrique.netresilience.model.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.henrique.netresilience.model.graph.Node;

public final class ConnectedComponentsResult {
    
    private final List<List<Node>> components;

    public ConnectedComponentsResult(List<List<Node>> components) {

        Objects.requireNonNull(components, "Components cannot be null");

        List<List<Node>> copies = new ArrayList<>();

        for(List<Node> component : components){
            if(component == null || component.isEmpty()){
                throw new IllegalArgumentException("A component cannot be null or empty");
            }

            copies.add(List.copyOf(component));
        }
        this.components = List.copyOf(copies);
    }

    public List<List<Node>> getComponents() {
        return components;
    }
    
    public int getComponentCount() {
        return components.size();
    }

    public boolean isConnected() {
        return components.size() == 1;
    }
}
