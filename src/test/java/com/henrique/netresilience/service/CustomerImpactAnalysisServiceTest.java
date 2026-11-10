package com.henrique.netresilience.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.result.CustomerImpactResult;

class CustomerImpactAnalysisServiceTest {

    private final CustomerImpactAnalysisService service =
            new CustomerImpactAnalysisService();

    @Test
    void calculatesDisconnectedCustomersAndWeightedLatencyIncrease() {
        Graph planned = new Graph();
        planned.addNode("GW", "Internet Gateway");
        planned.addNode("A", "Primary route");
        planned.addNode("B", "Alternative route");
        planned.addNode("CTO-1", "CTO 1", 10);
        planned.addNode("CTO-2", "CTO 2", 20);
        planned.addNode("CTO-3", "CTO 3", 5);

        planned.addEdge("E1", "GW", "A", 1.0);
        planned.addEdge("E2", "A", "CTO-1", 1.0);
        planned.addEdge("E3", "GW", "B", 1.0);
        planned.addEdge("E4", "B", "CTO-1", 3.0);
        planned.addEdge("E5", "GW", "CTO-2", 5.0);
        planned.addEdge("E6", "GW", "CTO-3", 3.0);

        // Remove the primary path to CTO-1 and the only path to CTO-3.
        Graph withTwoFailures = new Graph();
        withTwoFailures.addNode("GW", "Internet Gateway");
        withTwoFailures.addNode("A", "Primary route");
        withTwoFailures.addNode("B", "Alternative route");
        withTwoFailures.addNode("CTO-1", "CTO 1", 10);
        withTwoFailures.addNode("CTO-2", "CTO 2", 20);
        withTwoFailures.addNode("CTO-3", "CTO 3", 5);
        withTwoFailures.addEdge("E1", "GW", "A", 1.0);
        withTwoFailures.addEdge("E3", "GW", "B", 1.0);
        withTwoFailures.addEdge("E4", "B", "CTO-1", 3.0);
        withTwoFailures.addEdge("E5", "GW", "CTO-2", 5.0);

        CustomerImpactResult result =
                service.analyze(planned, withTwoFailures, "GW");

        assertEquals(35, result.getTotalCustomers());
        assertEquals(5, result.getCustomersWithoutInternet());
        assertEquals(30, result.getConnectedCustomers());
        assertEquals(10, result.getCustomersWithIncreasedLatency());
        assertEquals(1, result.getDisconnectedAccessPoints());
        assertEquals(2, result.getAffectedAccessPoints());
        assertEquals(14.0 / 3.0, result.getAverageConnectedLatencyMs(), 0.0001);
        assertEquals(2.0, result.getAverageAdditionalLatencyMs(), 0.0001);
    }

    @Test
    void reportIncludesCustomerImpactSectionAndGateway() {
        Graph graph = new Graph();
        graph.addNode("GW", "Gateway");
        graph.addNode("CTO", "Customer terminal", 12);
        graph.addEdge("LINK", "GW", "CTO", 4.5);

        String report = new NetworkAssessmentService().generateReport(
                graph, graph, Set.of(), Set.of(), Set.of(), "GW");

        assertTrue(report.contains("C) IMPACTO SOBRE OS CLIENTES"));
        assertTrue(report.contains("Gateway de referência: GW"));
        assertTrue(report.contains("Clientes considerados: 12"));
        assertTrue(report.contains("Latência média ponderada: 4.50 ms"));
    }
}
