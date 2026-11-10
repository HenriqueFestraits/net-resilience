package com.henrique.netresilience.service;

import java.util.Objects;

import com.henrique.netresilience.algorithm.connectivity.ConnectedComponents;
import com.henrique.netresilience.algorithm.connectivity.TarjanAnalysis;
import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.ConnectedComponentsResult;
import com.henrique.netresilience.model.result.CriticalElementsResult;
import com.henrique.netresilience.model.result.RedundancyAnalysisResult;

/**
 * Evaluates the structural effect of adding a hypothetical network link.
 * The supplied graph is never modified.
 */
public final class RedundancyAnalysisService {

    private final TarjanAnalysis tarjan = new TarjanAnalysis();
    private final ConnectedComponents connectedComponents = new ConnectedComponents();

    public RedundancyAnalysisResult analyze(
            Graph originalGraph,
            String edgeId,
            String sourceNodeId,
            String targetNodeId,
            double weight) {
        Objects.requireNonNull(originalGraph, "Graph cannot be null");
        requireText(edgeId, "Edge ID");
        requireText(sourceNodeId, "Source node ID");
        requireText(targetNodeId, "Target node ID");

        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be finite and non-negative.");
        }
        if (sourceNodeId.equals(targetNodeId)) {
            throw new IllegalArgumentException("Source and target nodes must be different.");
        }
        if (originalGraph.findEdge(edgeId).isPresent()) {
            throw new IllegalArgumentException("An edge with ID " + edgeId + " already exists.");
        }

        Node source = originalGraph.findNode(sourceNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown source node: " + sourceNodeId));
        Node target = originalGraph.findNode(targetNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown target node: " + targetNodeId));

        CriticalElementsResult originalCritical = tarjan.analyze(originalGraph);
        ConnectedComponentsResult originalComponents = connectedComponents.analyze(originalGraph);

        Graph proposedGraph = copyGraph(originalGraph);
        proposedGraph.addEdge(edgeId, source.getId(), target.getId(), weight);

        CriticalElementsResult proposedCritical = tarjan.analyze(proposedGraph);
        ConnectedComponentsResult proposedComponents = connectedComponents.analyze(proposedGraph);

        return new RedundancyAnalysisResult(
                edgeId, sourceNodeId, targetNodeId, weight,
                originalCritical, proposedCritical,
                originalComponents, proposedComponents);
    }

    private Graph copyGraph(Graph source) {
        Graph copy = new Graph();
        for (Node node : source.getNodes()) {
            if (node.hasDependentDeviceCount()) {
                if (node.hasPosition()) {
                    copy.addNode(node.getId(), node.getLabel(),
                            node.getDependentDevices(), node.getX(), node.getY());
                } else {
                    copy.addNode(node.getId(), node.getLabel(), node.getDependentDevices());
                }
            } else if (node.hasPosition()) {
                copy.addNode(node.getId(), node.getLabel(), node.getX(), node.getY());
            } else {
                copy.addNode(node.getId(), node.getLabel());
            }
        }
        for (Edge edge : source.getEdges()) {
            copy.addEdge(edge.getId(), edge.getSource().getId(),
                    edge.getTarget().getId(), edge.getWeight());
        }
        return copy;
    }

    private void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank.");
        }
    }
}
