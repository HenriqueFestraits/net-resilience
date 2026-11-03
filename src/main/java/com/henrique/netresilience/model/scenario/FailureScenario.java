package com.henrique.netresilience.model.scenario;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.henrique.netresilience.model.graph.Edge;

public final class FailureScenario {
    
    private final Set<String> unavailableNodeIds = new LinkedHashSet<>();

    private final Set<String> unavailableEdgeIds = new LinkedHashSet<>();

    //Register unavailable node 
    public void addUnavailableNode(String nodeId) {
        unavailableNodeIds.add(validateId(nodeId, "Node"));
    }

    //Register unavailable edge 
    public void addUnavailableEdge(String edgeId) {
        unavailableEdgeIds.add(validateId(edgeId, "Edge"));
    }

    //Restore a previously unavailable node
    public void restoreNode(String nodeId) {
        unavailableNodeIds.remove(validateId(nodeId, "Node"));
    }

    //Restore a previously unavailable edge
    public void restoreEdge(String edgeId) {
        unavailableEdgeIds.remove(validateId(edgeId, "Edge"));
    }

    //Check whether a node is unavailable
    public boolean isNodeUnavailable(String nodeId) {
        return unavailableNodeIds.contains(validateId(nodeId, "Node"));
    }

    // Check whether an edge ID is directly registered as unavailable.
    public boolean isEdgeIdUnavailable(String edgeId) {
        return unavailableEdgeIds.contains(
            validateId(edgeId, "Edge")
        );
    }

    // Check whether an edge is unavailable in the current scenario.
    public boolean isEdgeUnavailable(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");

        return isEdgeIdUnavailable(edge.getId())
            || isNodeUnavailable(edge.getSource().getId())
            || isNodeUnavailable(edge.getTarget().getId());
    }

    // Check whether an edge can be used.
    public boolean canUseEdge(Edge edge) {
        return !isEdgeUnavailable(edge);
    }
    
    public Set<String> getUnavailableNodeIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(unavailableNodeIds));
    }

    public Set<String> getUnavailableEdgeIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(unavailableEdgeIds));
    }

    public boolean isEmpty() {
        return unavailableNodeIds.isEmpty() && unavailableEdgeIds.isEmpty();
    }

    public void clear(){
        unavailableNodeIds.clear();
        unavailableEdgeIds.clear();
    }

    private String validateId(String id, String type) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(type + " ID cannot be blank");
        }
        return id;
    }
}
