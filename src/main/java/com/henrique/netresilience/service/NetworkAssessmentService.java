package com.henrique.netresilience.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.henrique.netresilience.algorithm.connectivity.ConnectedComponents;
import com.henrique.netresilience.algorithm.connectivity.TarjanAnalysis;
import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;
import com.henrique.netresilience.model.result.ConnectedComponentsResult;
import com.henrique.netresilience.model.result.CriticalElementsResult;

/**
 * Produces a consolidated topological assessment and ranks hypothetical
 * links by the structural improvement they provide.
 *
 * Recommendations are mathematical candidates only. Physical feasibility,
 * capacity, optical budget, access technology and construction cost are
 * outside this graph model.
 */
public final class NetworkAssessmentService {

    private static final int MAX_RECOMMENDATIONS = 5;
    private static final double HYPOTHETICAL_WEIGHT = 1.0;

    private final TarjanAnalysis tarjan = new TarjanAnalysis();
    private final ConnectedComponents connectedComponents = new ConnectedComponents();

    public String generateReport(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        CriticalElementsResult critical = tarjan.analyze(graph);
        ConnectedComponentsResult components = connectedComponents.analyze(graph);

        StringBuilder report = new StringBuilder();
        int totalDependentDevices = graph.getNodes().stream()
                .mapToInt(Node::getDependentDevices)
                .sum();

        report.append("NETRESILIENCE — RELATÓRIO DE AVALIAÇÃO DA REDE\n");
        report.append("================================================\n\n");
        report.append("1. VISÃO GERAL\n");
        report.append("Equipamentos: ").append(graph.nodeCount()).append("\n");
        report.append("Enlaces: ").append(graph.edgeCount()).append("\n");
        report.append("Dispositivos dependentes informados: ")
                .append(totalDependentDevices).append("\n");
        report.append("Componentes conexos: ")
                .append(components.getComponentCount()).append("\n\n");

        report.append("2. VULNERABILIDADES TOPOLÓGICAS\n");
        report.append("Pontes: ").append(critical.getBridges().size()).append("\n");
        for (Edge bridge : critical.getBridges()) {
            report.append("  - ").append(bridge.getId()).append(" (")
                    .append(bridge.getSource().getId()).append(" <-> ")
                    .append(bridge.getTarget().getId()).append(")\n");
        }
        report.append("Pontos de articulação: ")
                .append(critical.getArticulationPoints().size()).append("\n");
        for (Node node : critical.getArticulationPoints()) {
            report.append("  - ").append(node.getId()).append(" — ")
                    .append(node.getLabel()).append("; dispositivos dependentes: ")
                    .append(node.getDependentDevices()).append("\n");
        }
        if (critical.getBridges().isEmpty()
                && critical.getArticulationPoints().isEmpty()) {
            report.append("Nenhuma ponte ou ponto de articulação identificado.\n");
        }

        report.append("\n3. COMPONENTES CONEXOS\n");
        int componentIndex = 1;
        for (List<Node> component : components.getComponents()) {
            report.append("Componente ").append(componentIndex++)
                    .append(": ").append(component.size()).append(" equipamento(s) — ");
            report.append(component.stream().map(Node::getId)
                    .sorted().reduce((a, b) -> a + ", " + b).orElse(""));
            report.append("\n");
        }

        report.append("\n4. EQUIPAMENTOS COM MAIOR EXPOSIÇÃO\n");
        List<Node> rankedNodes = new ArrayList<>(graph.getNodes());
        Set<String> articulationIds = new HashSet<>();
        critical.getArticulationPoints().forEach(n -> articulationIds.add(n.getId()));
        rankedNodes.sort(Comparator
                .comparing((Node n) -> articulationIds.contains(n.getId())).reversed()
                .thenComparing(Node::getDependentDevices, Comparator.reverseOrder()));

        int listed = 0;
        for (Node node : rankedNodes) {
            if (node.getDependentDevices() == 0
                    && !articulationIds.contains(node.getId())) {
                continue;
            }
            report.append("  - ").append(node.getId()).append(" — ")
                    .append(node.getLabel()).append(": ")
                    .append(node.getDependentDevices()).append(" dispositivo(s)");
            if (articulationIds.contains(node.getId())) {
                report.append("; ponto de articulação");
            }
            report.append("\n");
            if (++listed == 10) break;
        }
        if (listed == 0) {
            report.append("Não há dados de dispositivos dependentes ou pontos de articulação.\n");
        }

        report.append("\n5. SUGESTÕES DE REDUNDÂNCIA\n");
        List<Candidate> candidates = findCandidates(graph, critical);
        if (candidates.isEmpty()) {
            report.append("Não foi encontrada conexão candidata que reduza a quantidade de pontes ou pontos de articulação.\n");
        } else {
            int rank = 1;
            for (Candidate candidate : candidates) {
                report.append(rank++).append(") ").append(candidate.source.getId())
                        .append(" <-> ").append(candidate.target.getId())
                        .append(" [prioridade ").append(candidate.priority).append("]\n");
                report.append("   Pontes: ").append(candidate.beforeBridges)
                        .append(" -> ").append(candidate.afterBridges)
                        .append("; pontos de articulação: ")
                        .append(candidate.beforeArticulations).append(" -> ")
                        .append(candidate.afterArticulations).append("\n");
                report.append("   Dispositivos dependentes nos extremos: ")
                        .append(candidate.source.getDependentDevices() + candidate.target.getDependentDevices())
                        .append("\n");
            }
        }

        report.append("\n6. CONCLUSÃO E LIMITAÇÕES\n");
        if (!critical.getArticulationPoints().isEmpty()
                || !critical.getBridges().isEmpty()) {
            report.append("A topologia apresenta elementos cuja remoção pode fragmentar a rede. ")
                    .append("Priorize a investigação dos pontos de articulação com maior quantidade ")
                    .append("de dispositivos dependentes e dos enlaces identificados como pontes.\n");
        } else {
            report.append("Os indicadores de pontes e pontos de articulação não apontaram ")
                    .append("dependências topológicas críticas nesta topologia.\n");
        }
        report.append("As quantidades de dispositivos são informadas pelo usuário e representam ")
                .append("dispositivos dependentes de cada nó; não são contagens inferidas pelo grafo.\n");
        report.append("As conexões sugeridas são hipóteses avaliadas apenas por conectividade. ")
                .append("Não comprovam viabilidade física, capacidade, disponibilidade de portas, ")
                .append("orçamento óptico, rota de fibra ou custo de implantação.\n");
        report.append("O relatório não estima automaticamente o número exato de clientes afetados ")
                .append("por uma falha; isso exige uma hierarquia de atendimento e dados operacionais confiáveis.\n");

        return report.toString();
    }

    private List<Candidate> findCandidates(
            Graph graph,
            CriticalElementsResult baseline
    ) {
        List<Node> nodes = graph.getNodes();
        List<Candidate> candidates = new ArrayList<>();
        int sequence = 1;

        for (int i = 0; i < nodes.size(); i++) {
            Node source = nodes.get(i);
            Set<String> neighbors = new HashSet<>();
            graph.getNeighbors(source.getId()).forEach(n -> neighbors.add(n.getId()));

            for (int j = i + 1; j < nodes.size(); j++) {
                Node target = nodes.get(j);
                if (neighbors.contains(target.getId())) continue;

                Graph proposed = copyGraph(graph);
                proposed.addEdge("ASSESSMENT_CANDIDATE_" + sequence++,
                        source.getId(), target.getId(), HYPOTHETICAL_WEIGHT);

                CriticalElementsResult after = tarjan.analyze(proposed);
                int bridgeReduction = baseline.getBridges().size() - after.getBridges().size();
                int articulationReduction = baseline.getArticulationPoints().size()
                        - after.getArticulationPoints().size();

                int priorityScore = articulationReduction * 200 + bridgeReduction * 100;
                if (priorityScore > 0) {
                    candidates.add(new Candidate(source, target,
                            baseline.getBridges().size(), after.getBridges().size(),
                            baseline.getArticulationPoints().size(),
                            after.getArticulationPoints().size(), priorityScore));
                }
            }
        }

        candidates.sort(Comparator.comparingInt((Candidate c) -> c.priority).reversed()
                .thenComparingInt(c -> -(c.source.getDependentDevices()
                        + c.target.getDependentDevices())));
        return candidates.stream().limit(MAX_RECOMMENDATIONS).toList();
    }

    private Graph copyGraph(Graph original) {
        Graph copy = new Graph();
        for (Node node : original.getNodes()) {
            if (node.hasPosition()) {
                copy.addNode(node.getId(), node.getLabel(),
                        node.getDependentDevices(), node.getX(), node.getY());
            } else {
                copy.addNode(node.getId(), node.getLabel(), node.getDependentDevices());
            }
        }
        for (Edge edge : original.getEdges()) {
            copy.addEdge(edge.getId(), edge.getSource().getId(),
                    edge.getTarget().getId(), edge.getWeight());
        }
        return copy;
    }

    private static final class Candidate {
        private final Node source;
        private final Node target;
        private final int beforeBridges;
        private final int afterBridges;
        private final int beforeArticulations;
        private final int afterArticulations;
        private final int priority;

        private Candidate(Node source, Node target, int beforeBridges,
                          int afterBridges, int beforeArticulations,
                          int afterArticulations, int priority) {
            this.source = source;
            this.target = target;
            this.beforeBridges = beforeBridges;
            this.afterBridges = afterBridges;
            this.beforeArticulations = beforeArticulations;
            this.afterArticulations = afterArticulations;
            this.priority = priority;
        }
    }
}
