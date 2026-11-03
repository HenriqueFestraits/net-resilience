package com.henrique.netresilience.model.result;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public final class FailureSimulationResult {

    private final String sourceNodeId;
    private final String targetNodeId;

    private final Optional<RouteResult> originalRoute;
    private final Optional<RouteResult> affectedRoute;

    private final ConnectedComponentsResult originalComponents;
    private final ConnectedComponentsResult affectedComponents;

    private final CriticalElementsResult originalCriticalElements;
    private final CriticalElementsResult affectedCriticalElements;

    private final Set<String> unavailableNodeIds;
    private final Set<String> unavailableEdgeIds;

    public FailureSimulationResult(
        String sourceNodeId,
        String targetNodeId,
        Optional<RouteResult> originalRoute,
        Optional<RouteResult> affectedRoute,
        ConnectedComponentsResult originalComponents,
        ConnectedComponentsResult affectedComponents,
        CriticalElementsResult originalCriticalElements,
        CriticalElementsResult affectedCriticalElements,
        Set<String> unavailableNodeIds,
        Set<String> unavailableEdgeIds
        ){
            this.sourceNodeId = validateId(sourceNodeId, "Source node");
            this.targetNodeId = validateId(targetNodeId, "Target node");
            this.originalRoute = Objects.requireNonNull(originalRoute, "Original route cannot be null");
            this.affectedRoute = Objects.requireNonNull(affectedRoute, "Affected route cannot be null");
            this.originalComponents = Objects.requireNonNull(originalComponents, "Original components cannot be null");
            this.affectedComponents = Objects.requireNonNull(affectedComponents, "Affected components cannot be null");
            this.originalCriticalElements = Objects.requireNonNull(originalCriticalElements, "Original critical elements cannot be null");
            this.affectedCriticalElements = Objects.requireNonNull(affectedCriticalElements, "Affected critical elements cannot be null");

            validateRouteEndpoints(this.originalRoute, "Original route");
            validateRouteEndpoints(this.affectedRoute, "Affected route");

            this.unavailableNodeIds = copyIds(unavailableNodeIds, "Node");
            this.unavailableEdgeIds = copyIds(unavailableEdgeIds, "Edge");
        }

        public String getSourceNodeId() {
            return sourceNodeId;
        }
        
        public String getTargetNodeId() {
            return targetNodeId;
        }

    public Optional<RouteResult> getOriginalRoute() {
        return originalRoute;
    }

    public Optional<RouteResult> getAffectedRoute() {
        return affectedRoute;
    }

    public ConnectedComponentsResult getOriginalComponents() {
        return originalComponents;
    }

    public ConnectedComponentsResult getAffectedComponents() {
        return affectedComponents;
    }

    public CriticalElementsResult getOriginalCriticalElements() {
        return originalCriticalElements;
    }

    public CriticalElementsResult getAffectedCriticalElements() {
        return affectedCriticalElements;
    }

    public Set<String> getUnavailableNodeIds() {
        return unavailableNodeIds;
    }

    public Set<String> getUnavailableEdgeIds() {
        return unavailableEdgeIds;
    }

    public boolean isRouteAvailableBefore() {
        return originalRoute.isPresent();
    }

    public boolean isRouteAvailableAfter() {
        return affectedRoute.isPresent();
    }

    public boolean isRouteLost(){
        return isRouteAvailableBefore() && !isRouteAvailableAfter();
    }

    public OptionalDouble getRouteCostDifference(){
        if (originalRoute.isEmpty() || affectedRoute.isEmpty()){
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(affectedRoute.get().getTotalWeight() - originalRoute.get().getTotalWeight());
    }

    private void validateRouteEndpoints(Optional<RouteResult> route, String routeName) {
        
        route.ifPresent(result -> {
            if(!result.getSource().getId().equals(sourceNodeId) || !result.getTarget().getId().equals(targetNodeId)){
                throw new IllegalArgumentException(routeName + " does not match the requested endpoints.");
            }
        });
    }

    private Set<String> copyIds(Set<String> ids, String type){
        Objects.requireNonNull(ids, type + " IDs cannot be null");

        Set<String> copy = new LinkedHashSet<>();

        for(String id : ids){
            copy.add(validateId(id, type));
        }

        return Collections.unmodifiableSet(copy);
    }

    private String validateId(String id, String type){
        if(id == null || id.isBlank()){
            throw new IllegalArgumentException(type + " ID cannot be null or blank");
        }
        return id;
    }
}
