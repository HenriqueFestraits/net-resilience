package com.henrique.netresilience.service;

import java.util.Objects;
import java.util.Optional;

import com.henrique.netresilience.algorithm.shortestpath.Dijkstra;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.CustomerImpactResult;
import com.henrique.netresilience.model.result.RouteResult;

/**
 * Measures customer impact by comparing the shortest-path latency from
 * the selected Internet gateway to each customer access point before
 * and after a failure scenario.
 *
 * For the current FTTH data model, nodes with a provided dependent_devices
 * value greater than zero represent customer aggregation points (CTOs).
 */
public final class CustomerImpactAnalysisService {

    private final Dijkstra dijkstra = new Dijkstra();

    public CustomerImpactResult analyze(
            Graph plannedGraph,
            Graph operationalGraph,
            String gatewayNodeId
    ) {
        Objects.requireNonNull(plannedGraph, "Planned graph cannot be null.");
        Objects.requireNonNull(operationalGraph, "Operational graph cannot be null.");
        if (gatewayNodeId == null || gatewayNodeId.isBlank()) {
            throw new IllegalArgumentException("Gateway node ID cannot be blank.");
        }
        plannedGraph.getNode(gatewayNodeId);

        int totalCustomers = 0;
        int customersWithoutInternet = 0;
        int customersWithIncreasedLatency = 0;
        int connectedCustomers = 0;
        int affectedAccessPoints = 0;
        int disconnectedAccessPoints = 0;
        double connectedLatencyWeightedSum = 0.0;
        double additionalLatencyWeightedSum = 0.0;

        boolean gatewayOperational = operationalGraph.findNode(gatewayNodeId).isPresent();

        for (Node accessPoint : plannedGraph.getNodes()) {
            if (!accessPoint.hasDependentDeviceCount()
                    || accessPoint.getDependentDevices() <= 0) {
                continue;
            }

            int customers = accessPoint.getDependentDevices();
            totalCustomers += customers;

            Optional<RouteResult> originalRoute =
                    dijkstra.findShortestPath(plannedGraph, gatewayNodeId, accessPoint.getId());

            Optional<RouteResult> operationalRoute = gatewayOperational
                    && operationalGraph.findNode(accessPoint.getId()).isPresent()
                    ? dijkstra.findShortestPath(operationalGraph, gatewayNodeId, accessPoint.getId())
                    : Optional.empty();

            if (operationalRoute.isEmpty()) {
                customersWithoutInternet += customers;
                disconnectedAccessPoints++;
                if (originalRoute.isPresent()) {
                    affectedAccessPoints++;
                }
                continue;
            }

            double operationalLatency = operationalRoute.get().getTotalWeight();
            connectedCustomers += customers;
            connectedLatencyWeightedSum += operationalLatency * customers;

            if (originalRoute.isPresent()) {
                double originalLatency = originalRoute.get().getTotalWeight();
                if (operationalLatency > originalLatency) {
                    customersWithIncreasedLatency += customers;
                    additionalLatencyWeightedSum +=
                            (operationalLatency - originalLatency) * customers;
                    affectedAccessPoints++;
                }
            }
        }

        double averageConnectedLatency = connectedCustomers == 0
                ? 0.0
                : connectedLatencyWeightedSum / connectedCustomers;

        double averageAdditionalLatency = customersWithIncreasedLatency == 0
                ? 0.0
                : additionalLatencyWeightedSum / customersWithIncreasedLatency;

        return new CustomerImpactResult(
                totalCustomers,
                customersWithoutInternet,
                customersWithIncreasedLatency,
                connectedCustomers,
                averageConnectedLatency,
                averageAdditionalLatency,
                affectedAccessPoints,
                disconnectedAccessPoints
        );
    }
}
