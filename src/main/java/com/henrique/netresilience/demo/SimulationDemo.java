
package com.henrique.netresilience.demo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.henrique.netresilience.io.GraphCsvLoader;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;

import com.henrique.netresilience.model.result.ConnectedComponentsResult;
import com.henrique.netresilience.model.result.CriticalElementsResult;
import com.henrique.netresilience.model.result.FailureSimulationResult;
import com.henrique.netresilience.model.result.RouteResult;

import com.henrique.netresilience.model.scenario.FailureScenario;

import com.henrique.netresilience.service.FailureSimulationService;

public final class SimulationDemo {

        private static final FailureSimulationService service =
                new FailureSimulationService();

        public static void main(String[] args) throws IOException {

                // Without arguments, execute the built-in demonstration.
                if (args.length == 0) {
                        runBuiltInDemo();
                        return;
                }

                // With six arguments, load a graph from CSV files.
                if (args.length < 6) {
                        printUsage();
                        return;
                }

                runCsvDemo(args);
        }

        private static void runBuiltInDemo() {

                Graph graph = createExampleGraph();

                printHeader("NET RESILIENCE - DEMONSTRACAO");

                printGraph(graph);

                // Scenario 1: Failure of an edge with an alternative route.
                FailureScenario scenario1 = new FailureScenario();
                scenario1.addUnavailableEdge("E1");

                runScenario(
                        graph,
                        "A",
                        "F",
                        "CENARIO 1 - FALHA DA CONEXAO E1",
                        scenario1
                );

                // Scenario 2: Failure of a bridge.
                FailureScenario scenario2 = new FailureScenario();
                scenario2.addUnavailableEdge("E5");

                runScenario(
                        graph,
                        "A",
                        "F",
                        "CENARIO 2 - FALHA DA CONEXAO E5",
                        scenario2
                );

                printOriginalGraphIntegrity(graph);
        }

        private static void runCsvDemo(String[] args) throws IOException {

                Path nodesFile = Path.of(args[0]);
                Path edgesFile = Path.of(args[1]);

                String sourceId = args[2];
                String targetId = args[3];

                // Parse all failure options.
                FailureScenario scenario;

                try {
                        scenario = parseFailureOptions(args, 4);
                } catch (IllegalArgumentException exception) {
                        System.err.println(
                                "Erro nos argumentos: " + exception.getMessage()
                        );

                        printUsage();
                        return;
                }

                // Load graph from CSV files.
                GraphCsvLoader loader = new GraphCsvLoader();
                Graph graph = loader.load(nodesFile, edgesFile);

                printHeader("NET RESILIENCE - CSV IMPORT");

                System.out.println("Arquivo de nos: " + nodesFile);
                System.out.println("Arquivo de conexoes: " + edgesFile);

                printGraph(graph);

                String scenarioTitle = scenario.isEmpty()
                        ? "ANALISE DA REDE - SEM FALHAS"
                        : "SIMULACAO COM MULTIPLAS FALHAS";

                runScenario(
                        graph,
                        sourceId,
                        targetId,
                        scenarioTitle,
                        scenario
                );

                printOriginalGraphIntegrity(graph);
        }

        static FailureScenario parseFailureOptions(
                String[] args,
                int startIndex
        ) {

                if (args == null) {
                        throw new IllegalArgumentException(
                                "Arguments cannot be null."
                        );
                }

                if (startIndex < 0 || startIndex > args.length) {
                        throw new IllegalArgumentException(
                                "Invalid failure options starting position."
                        );
                }

                int remainingArguments = args.length - startIndex;

                // Every option must have a corresponding ID.
                if (remainingArguments % 2 != 0) {
                        throw new IllegalArgumentException(
                                "Every failure option must be followed by an ID."
                                );
                }

                FailureScenario scenario = new FailureScenario();

                for (int i = startIndex; i < args.length; i += 2) {

                      String option = args[i];
                        String id = args[i + 1];

                        if (option == null || option.isBlank()) {
                                throw new IllegalArgumentException(
                                        "Failure option cannot be blank."
                                );
                        }

                        if (id == null || id.isBlank()) {
                                throw new IllegalArgumentException(
                                        "Failure ID cannot be blank."
                                );
                        }

                        if (id.startsWith("--")) {
                                throw new IllegalArgumentException(
                                        "Missing ID after option: " + option
                                );
                        }

                        switch (option.toLowerCase(Locale.ROOT)) {

                                case "--node":
                                        scenario.addUnavailableNode(id);
                                        break;

                                case "--edge":
                                        scenario.addUnavailableEdge(id);
                                        break;

                                default:
                                        throw new IllegalArgumentException(
                                                "Unknown failure option: " + option +
                                                ". Use --node or --edge."
                                        );
                        }
                }

                return scenario;
        }

        private static void runScenario(
            Graph graph,
            String sourceId,
            String targetId,
            String title,
            FailureScenario scenario
        ) {

                FailureSimulationResult result = service.simulate(
                        graph,
                        sourceId,
                        targetId,
                        scenario
                );

                printHeader(title);

                System.out.println("Origem: " + sourceId);
                System.out.println("Destino: " + targetId);

                System.out.println(
                        "Nos indisponiveis: " +
                        result.getUnavailableNodeIds()
                );

                System.out.println(
                        "Conexoes indisponiveis: " +
                        result.getUnavailableEdgeIds()
                );

                // Routes
                System.out.println("\n--- ROTAS ---");

                printRoute("Rota original:", result.getOriginalRoute());

                printRoute("Rota apos as falhas:", result.getAffectedRoute());

                if (result.getRouteCostDifference().isPresent()) {

                        System.out.printf(
                                "Variacao do custo: %+.2f%n",
                                result.getRouteCostDifference().getAsDouble()
                        );

                } else if (result.isRouteLost()) {

                        System.out.println(
                                "A rota entre os pontos selecionados deixou de existir."
                        );
                }

                // Connected components
                System.out.println("\n--- COMPONENTES CONEXOS ---");

                System.out.println(
                        "Antes: " +
                        result.getOriginalComponents().getComponentCount()
                );

                printComponents(result.getOriginalComponents());

                System.out.println(
                        "\nDepois: " +
                        result.getAffectedComponents().getComponentCount()
                );

                printComponents(result.getAffectedComponents());

                // Critical elements
                System.out.println("\n--- ELEMENTOS CRITICOS ---");

                System.out.println("Antes das falhas:");

                printCriticalElements(
                        result.getOriginalCriticalElements()
                );

                System.out.println("\nDepois das falhas:");

                printCriticalElements(
                        result.getAffectedCriticalElements()
                );
        }

        private static Graph createExampleGraph() {

                Graph graph = new Graph();

                graph.addNode("A", "Central de distribuição A");
                graph.addNode("B", "Caixa de distribuição B");
                graph.addNode("C", "Caixa de distribuição C");
                graph.addNode("D", "Ponto de distribuição D");
                graph.addNode("E", "Caixa de distribuição E");
                graph.addNode("F", "Ponto de atendimento F");

                graph.addEdge("E1", "A", "B", 2.0);
                graph.addEdge("E2", "B", "D", 2.0);
                graph.addEdge("E3", "A", "C", 4.0);
                graph.addEdge("E4", "C", "D", 4.0);
                graph.addEdge("E5", "D", "E", 1.0);
                graph.addEdge("E6", "E", "F", 1.0);

                return graph;
        }

        private static void printGraph(Graph graph) {

                System.out.println("\nREDE ORIGINAL");

                System.out.println("Nos: " + graph.nodeCount());
                System.out.println("Conexoes: " + graph.edgeCount());

                System.out.println("\nConexoes cadastradas:");

                for (Edge edge : graph.getEdges()) {

                        System.out.printf(
                                "  %s: %s <-> %s | Peso: %.2f%n",
                                edge.getId(),
                                edge.getSource().getId(),
                                edge.getTarget().getId(),
                                edge.getWeight()
                        );
                }
        }

        private static void printRoute(
            String title,
            Optional<RouteResult> optionalRoute
        ) {

                System.out.println("\n" + title);

                if (optionalRoute.isEmpty()) {
                        System.out.println("  Nenhuma rota disponivel.");
                        return;
                }

                RouteResult route = optionalRoute.get();

                String path = route.getNodes()
                        .stream()
                        .map(Node::getId)
                        .collect(Collectors.joining(" -> "));

                System.out.println("  Caminho: " + path);

                System.out.printf(
                        "  Custo total: %.2f%n",
                        route.getTotalWeight()
                );
        }

        private static void printComponents(
            ConnectedComponentsResult result
        ) {

                int index = 1;

                for (var component : result.getComponents()) {

                        String nodes = component.stream()
                                .map(Node::getId)
                                .collect(Collectors.joining(", "));

                        System.out.println(
                                "  Componente " + index + ": [" + nodes + "]"
                        );

                        index++;
                }
        }

        private static void printCriticalElements(
            CriticalElementsResult result
        ){

                String articulationPoints =
                        result.getArticulationPoints()
                        .stream()
                        .map(Node::getId)
                        .collect(Collectors.joining(", "));

                String bridges = result.getBridges()
                        .stream()
                        .map(edge ->
                                edge.getId() + " (" +
                                edge.getSource().getId() + " - " +
                                edge.getTarget().getId() + ")"
                        )
                        .collect(Collectors.joining(", "));

                System.out.println(
                        "  Pontos de articulacao: " +
                        (articulationPoints.isEmpty()
                                ? "Nenhum"
                                : articulationPoints)
                );

                System.out.println(
                        "  Pontes: " +
                        (bridges.isEmpty() ? "Nenhuma" : bridges)
                );
        }

        private static void printOriginalGraphIntegrity(Graph graph) {

                System.out.println("\n==========================================");
                System.out.println("VERIFICACAO DA REDE ORIGINAL");
                System.out.println("Nos: " + graph.nodeCount());
                System.out.println("Conexoes: " + graph.edgeCount());
                System.out.println("==========================================");
        }

        private static void printHeader(String title) {

                System.out.println("\n==========================================");
                System.out.println("       " + title);
                System.out.println("==========================================");
        }

        private static void printUsage() {

                System.out.println("\nUso:");

                System.out.println(
                        "java -cp target/classes " +
                        "com.henrique.netresilience.demo.SimulationDemo " +
                        "<nodes.csv> <edges.csv> " +
                        "<sourceId> <targetId> " +
                        "[--node <id> | --edge <id>]..."
                );

                System.out.println("\nExemplos:");

                System.out.println(
                        "Sem falhas:"
                );

                System.out.println(
                        "data/nodes.csv data/edges.csv A F"
                );

                System.out.println(
                        "\nUma falha:"
                );

                System.out.println(
                        "data/nodes.csv data/edges.csv A F --edge E1"
                );

                System.out.println(
                        "\nMultiplas falhas:"
                );

                System.out.println(
                        "data/nodes.csv data/edges.csv A F " +
                        "--edge E1 --node D --edge E5"
                );

                System.out.println(
                        "\nPara executar a demonstracao interna, " +
                        "execute sem argumentos."
                );
        }
}
