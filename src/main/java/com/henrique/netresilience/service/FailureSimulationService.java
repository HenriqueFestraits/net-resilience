package com.henrique.netresilience.service;

import java.util.Objects;
import java.util.Optional;

import com.henrique.netresilience.algorithm.connectivity.ConnectedComponents;
import com.henrique.netresilience.algorithm.connectivity.TarjanAnalysis;
import com.henrique.netresilience.algorithm.shortestpath.Dijkstra;
import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.ConnectedComponentsResult;
import com.henrique.netresilience.model.result.CriticalElementsResult;
import com.henrique.netresilience.model.result.FailureSimulationResult;
import com.henrique.netresilience.model.result.NetworkResilienceMetrics;
import com.henrique.netresilience.model.result.RouteResult;
import com.henrique.netresilience.model.scenario.FailureScenario;

public final class FailureSimulationService {

    private final Dijkstra dijkstra = new Dijkstra();

    private final TarjanAnalysis tarjanAnalysis = new TarjanAnalysis();

    private final ConnectedComponents connectedComponents = new ConnectedComponents();

    public FailureSimulationResult simulate(
        Graph graph,
        String sourceNodeId,
        String targetNodeId,
        FailureScenario scenario
    ){
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(scenario, "Scenario cannot be null");

        graph.getNode(sourceNodeId);
        graph.getNode(targetNodeId);

        validateScenario(graph, scenario);

        //1. Analyze the original graph
        Optional<RouteResult> originalRoute = dijkstra.findShortestPath(graph, sourceNodeId, targetNodeId);
        
        ConnectedComponentsResult originalComponents = connectedComponents.analyze(graph);

        CriticalElementsResult originalCriticalElements = tarjanAnalysis.analyze(graph);

        //2. Build a temporary graph without unavailable elements
        Graph affectedGraph = buildAffectedGraph(graph, scenario);

        //3. Analyze the affected graph
        Optional<RouteResult> affectedRoute = Optional.empty();

        boolean sourceAvailable = affectedGraph.findNode(sourceNodeId).isPresent();

        boolean targetAvailable = affectedGraph.findNode(targetNodeId).isPresent();

        //Dijkstra can only run if both endpoints are available
        if(sourceAvailable && targetAvailable){
            affectedRoute = dijkstra.findShortestPath(affectedGraph, sourceNodeId, targetNodeId);
        }

        ConnectedComponentsResult affectedComponents = connectedComponents.analyze(affectedGraph);

        CriticalElementsResult affectedCriticalElements = tarjanAnalysis.analyze(affectedGraph);

        int largestAffectedComponentsSize =
            affectedComponents.getComponents()
                .stream()
                .mapToInt(component -> component.size())
                .max()
                .orElse(0);

        NetworkResilienceMetrics resilienceMetrics = new NetworkResilienceMetrics(
            graph.nodeCount(),
            affectedGraph.nodeCount(),
            graph.edgeCount(),
            affectedGraph.edgeCount(),
            originalComponents.getComponentCount(),
            affectedComponents.getComponentCount(),
            largestAffectedComponentsSize
        );

        //4. Consolidade all results
        return new FailureSimulationResult(
            sourceNodeId,
            targetNodeId,
            originalRoute,
            affectedRoute,
            originalComponents,
            affectedComponents,
            originalCriticalElements,
            affectedCriticalElements,
            scenario.getUnavailableNodeIds(),
            scenario.getUnavailableEdgeIds(),
            resilienceMetrics
        );
    }

    private Graph buildAffectedGraph(Graph originalGraph, FailureScenario scenario){

        Graph affectedGraph = new Graph();

        //Add nodes first
        for(Node node : originalGraph.getNodes()){
            if(!scenario.isNodeUnavailable(node.getId())){
                affectedGraph.addNode(node.getId(), node.getLabel());
            }
        }

        //Add edges whose endpoints are available
        for(Edge edge : originalGraph.getEdges()){
            if(scenario.canUseEdge(edge)){
                affectedGraph.addEdge(
                    edge.getId(),
                    edge.getSource().getId(),
                    edge.getTarget().getId(),
                    edge.getWeight()
                );
            }
        }
        return affectedGraph;
    }

    private void validateScenario(Graph graph, FailureScenario scenario){
        
        for(String nodeId : scenario.getUnavailableNodeIds()){
            if(graph.findNode(nodeId).isEmpty()){
                throw new IllegalArgumentException("Scenario references an unknown node: " + nodeId);
            }
        }

        for(String edgeId : scenario.getUnavailableEdgeIds()){
            if(graph.findEdge(edgeId).isEmpty()){
                throw new IllegalArgumentException("Scenario references an unknown edge: " + edgeId);
            }
        }
    }
    
}
