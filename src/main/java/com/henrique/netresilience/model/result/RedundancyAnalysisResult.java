package com.henrique.netresilience.model.result;

import java.util.Objects;

public final class RedundancyAnalysisResult {

    private final String edgeId;
    private final String sourceNodeId;
    private final String targetNodeId;
    private final double weight;
    private final CriticalElementsResult originalCriticalElements;
    private final CriticalElementsResult proposedCriticalElements;
    private final ConnectedComponentsResult originalComponents;
    private final ConnectedComponentsResult proposedComponents;

    public RedundancyAnalysisResult(
            String edgeId,
            String sourceNodeId,
            String targetNodeId,
            double weight,
            CriticalElementsResult originalCriticalElements,
            CriticalElementsResult proposedCriticalElements,
            ConnectedComponentsResult originalComponents,
            ConnectedComponentsResult proposedComponents) {
        this.edgeId = requireText(edgeId, "Edge ID");
        this.sourceNodeId = requireText(sourceNodeId, "Source node ID");
        this.targetNodeId = requireText(targetNodeId, "Target node ID");
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be finite and non-negative.");
        }
        if (sourceNodeId.equals(targetNodeId)) {
            throw new IllegalArgumentException("Source and target nodes must be different.");
        }
        this.weight = weight;
        this.originalCriticalElements = Objects.requireNonNull(originalCriticalElements);
        this.proposedCriticalElements = Objects.requireNonNull(proposedCriticalElements);
        this.originalComponents = Objects.requireNonNull(originalComponents);
        this.proposedComponents = Objects.requireNonNull(proposedComponents);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank.");
        }
        return value;
    }

    public String getEdgeId() { return edgeId; }
    public String getSourceNodeId() { return sourceNodeId; }
    public String getTargetNodeId() { return targetNodeId; }
    public double getWeight() { return weight; }
    public CriticalElementsResult getOriginalCriticalElements() { return originalCriticalElements; }
    public CriticalElementsResult getProposedCriticalElements() { return proposedCriticalElements; }
    public ConnectedComponentsResult getOriginalComponents() { return originalComponents; }
    public ConnectedComponentsResult getProposedComponents() { return proposedComponents; }

    public int getBridgeCountBefore() { return originalCriticalElements.getBridges().size(); }
    public int getBridgeCountAfter() { return proposedCriticalElements.getBridges().size(); }
    public int getArticulationPointCountBefore() { return originalCriticalElements.getArticulationPoints().size(); }
    public int getArticulationPointCountAfter() { return proposedCriticalElements.getArticulationPoints().size(); }
    public int getComponentCountBefore() { return originalComponents.getComponentCount(); }
    public int getComponentCountAfter() { return proposedComponents.getComponentCount(); }
    public int getBridgeCountReduction() { return getBridgeCountBefore() - getBridgeCountAfter(); }
    public int getArticulationPointCountReduction() { return getArticulationPointCountBefore() - getArticulationPointCountAfter(); }
}
