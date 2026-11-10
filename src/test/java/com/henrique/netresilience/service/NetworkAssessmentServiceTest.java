package com.henrique.netresilience.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.henrique.netresilience.model.graph.Graph;

class NetworkAssessmentServiceTest {

    private final NetworkAssessmentService service = new NetworkAssessmentService();

    @Test
    void reportIncludesTopologyCriticalityAndRedundancyRecommendation() {
        Graph graph = new Graph();
        graph.addNode("A", "Central", 20);
        graph.addNode("B", "Distribuidor", 180);
        graph.addNode("C", "Caixa", 75);
        graph.addEdge("AB", "A", "B", 1.0);
        graph.addEdge("BC", "B", "C", 1.0);

        String report = service.generateReport(graph);

        assertTrue(report.contains("Equipamentos: 3"));
        assertTrue(report.contains("Pontes: 2"));
        assertTrue(report.contains("B — Distribuidor"));
        assertTrue(report.contains("180"));
        assertTrue(report.contains("A <-> C"));
        assertTrue(report.contains("pontos de articulação: 1 -> 0"));
    }

    @Test
    void reportCanBeGeneratedForGraphWithoutCriticalElements() {
        Graph graph = new Graph();
        graph.addNode("A", "A", 0);
        graph.addNode("B", "B", 0);
        graph.addNode("C", "C", 0);
        graph.addEdge("AB", "A", "B", 1.0);
        graph.addEdge("BC", "B", "C", 1.0);
        graph.addEdge("CA", "C", "A", 1.0);

        String report = assertDoesNotThrow(() -> service.generateReport(graph));
        assertTrue(report.contains("Nenhuma ponte ou ponto de articulação"));
    }
}
