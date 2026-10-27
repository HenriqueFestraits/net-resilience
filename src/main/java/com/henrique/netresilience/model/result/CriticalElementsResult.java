package com.henrique.netresilience.model.result;

import java.util.List;
import java.util.Objects;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Node;

public final class CriticalElementsResult {

    private final List<Node> articulationPoints;
    private final List<Edge> bridges;

    public CriticalElementsResult(
        List<Node> articulationPoints,
        List<Edge> bridges
    ){
        Objects.requireNonNull(articulationPoints, "Articulation points cannot be null");
        Objects.requireNonNull(bridges, "Bridges cannot be null");

        this.articulationPoints = List.copyOf(articulationPoints);
        this.bridges = List.copyOf(bridges);
    }

    public List<Node> getArticulationPoints() {
        return articulationPoints;
    }

    public List<Edge> getBridges() {
        return bridges;
    }
}
