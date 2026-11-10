package com.henrique.netresilience.model.result;

/**
 * Summarizes the customer impact of a network failure scenario.
 *
 * Customer counts are taken from nodes with an explicitly provided
 * dependent-device count greater than zero (for the FTTH model, CTOs).
 */
public final class CustomerImpactResult {

    private final int totalCustomers;
    private final int customersWithoutInternet;
    private final int customersWithIncreasedLatency;
    private final int connectedCustomers;
    private final double averageConnectedLatencyMs;
    private final double averageAdditionalLatencyMs;
    private final int affectedAccessPoints;
    private final int disconnectedAccessPoints;

    public CustomerImpactResult(
            int totalCustomers,
            int customersWithoutInternet,
            int customersWithIncreasedLatency,
            int connectedCustomers,
            double averageConnectedLatencyMs,
            double averageAdditionalLatencyMs,
            int affectedAccessPoints,
            int disconnectedAccessPoints
    ) {
        if (totalCustomers < 0 || customersWithoutInternet < 0
                || customersWithIncreasedLatency < 0 || connectedCustomers < 0
                || affectedAccessPoints < 0 || disconnectedAccessPoints < 0) {
            throw new IllegalArgumentException("Customer impact counts cannot be negative.");
        }
        if (customersWithoutInternet + connectedCustomers != totalCustomers) {
            throw new IllegalArgumentException("Connected and disconnected customers must sum to total customers.");
        }
        if (customersWithIncreasedLatency > connectedCustomers) {
            throw new IllegalArgumentException("Customers with increased latency cannot exceed connected customers.");
        }
        if (!Double.isFinite(averageConnectedLatencyMs) || averageConnectedLatencyMs < 0
                || !Double.isFinite(averageAdditionalLatencyMs) || averageAdditionalLatencyMs < 0) {
            throw new IllegalArgumentException("Latency averages must be finite and non-negative.");
        }

        this.totalCustomers = totalCustomers;
        this.customersWithoutInternet = customersWithoutInternet;
        this.customersWithIncreasedLatency = customersWithIncreasedLatency;
        this.connectedCustomers = connectedCustomers;
        this.averageConnectedLatencyMs = averageConnectedLatencyMs;
        this.averageAdditionalLatencyMs = averageAdditionalLatencyMs;
        this.affectedAccessPoints = affectedAccessPoints;
        this.disconnectedAccessPoints = disconnectedAccessPoints;
    }

    public int getTotalCustomers() { return totalCustomers; }
    public int getCustomersWithoutInternet() { return customersWithoutInternet; }
    public int getCustomersWithIncreasedLatency() { return customersWithIncreasedLatency; }
    public int getConnectedCustomers() { return connectedCustomers; }
    public double getAverageConnectedLatencyMs() { return averageConnectedLatencyMs; }
    public double getAverageAdditionalLatencyMs() { return averageAdditionalLatencyMs; }
    public int getAffectedAccessPoints() { return affectedAccessPoints; }
    public int getDisconnectedAccessPoints() { return disconnectedAccessPoints; }
}
