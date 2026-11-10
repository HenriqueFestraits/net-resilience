
package com.henrique.netresilience.ui;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import com.henrique.netresilience.io.GraphCsvLoader;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;

import com.henrique.netresilience.model.result.ConnectedComponentsResult;
import com.henrique.netresilience.model.result.CriticalElementsResult;
import com.henrique.netresilience.model.result.FailureSimulationResult;
import com.henrique.netresilience.model.result.NetworkResilienceMetrics;
import com.henrique.netresilience.model.result.RouteResult;
import com.henrique.netresilience.model.result.RedundancyAnalysisResult;

import com.henrique.netresilience.model.scenario.FailureScenario;

import com.henrique.netresilience.service.FailureSimulationService;
import com.henrique.netresilience.service.RedundancyAnalysisService;
import com.henrique.netresilience.service.NetworkAssessmentService;

import com.henrique.netresilience.ui.component.GraphCanvas;

import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class NetResilienceApplication extends Application {

    private final GraphCsvLoader graphCsvLoader =
            new GraphCsvLoader();

    private final FailureSimulationService simulationService =
            new FailureSimulationService();

    private final RedundancyAnalysisService redundancyService =
            new RedundancyAnalysisService();

    private final NetworkAssessmentService assessmentService =
            new NetworkAssessmentService();

    private final GraphCanvas graphCanvas =
            new GraphCanvas();

    private final Set<String> unavailableNodeIds =
            new LinkedHashSet<>();

    private final Set<String> unavailableEdgeIds =
            new LinkedHashSet<>();

    private final ComboBox<String> sourceSelector =
            new ComboBox<>();

    private final ComboBox<String> targetSelector =
            new ComboBox<>();

    private final Label nodesFileLabel =
            new Label("Nenhum arquivo selecionado");

    private final Label edgesFileLabel =
            new Label("Nenhum arquivo selecionado");

    private final Label statusLabel =
            new Label("Aguardando importação da rede");

    private final Label graphInfoLabel =
            new Label("Nenhuma rede carregada");

    private final Label failureSelectionLabel =
            new Label("Nenhuma falha selecionada");

    private final TextArea resultArea =
            new TextArea();

    private final Button simulateButton =
            createActionButton("Executar simulação");

    private final Button clearFailuresButton =
            createSecondaryButton("Limpar falhas");

    private final Button analyzeRedundancyButton =
            createSecondaryButton("Testar conexão hipotética");

    private final Button assessNetworkButton =
            createActionButton("Avaliar rede e gerar relatório");

    private Graph graph;

    private Path nodesFile;
    private Path edgesFile;

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {

        primaryStage = stage;

        // Configure graph interaction.
        graphCanvas.setOnNodeClicked(this::toggleNodeFailure);
        graphCanvas.setOnEdgeClicked(this::toggleEdgeFailure);

        BorderPane root = new BorderPane();

        root.setStyle("-fx-background-color: #0b1120;");

        root.setTop(createHeader());

        root.setLeft(createSidebar());

        StackPane graphContainer = new StackPane(graphCanvas);

        graphContainer.setPadding(new Insets(20));

        graphContainer.setStyle(
                "-fx-background-color: #0b1120;"
        );

        root.setCenter(graphContainer);

        Scene scene = new Scene(root, 1350, 850);

        stage.setTitle("NetResilience");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);

        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {

        Label title = new Label("NetResilience");

        title.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #f8fafc;"
        );

        Label subtitle = new Label(
                "Análise de resiliência de redes de telecomunicações"
        );

        subtitle.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #94a3b8;"
        );

        VBox texts = new VBox(4, title, subtitle);

        HBox header = new HBox(texts);

        header.setPadding(
                new Insets(20, 28, 20, 28)
        );

        header.setAlignment(Pos.CENTER_LEFT);

        header.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-border-color: #1e293b;" +
                "-fx-border-width: 0 0 1 0;"
        );

        return header;
    }

    private ScrollPane createSidebar() {

        Label importTitle = createTitle("Importar rede");

        Button loadNodesButton =
                createActionButton("Selecionar CSV de nós");

        Button loadEdgesButton =
                createActionButton("Selecionar CSV de conexões");

        loadNodesButton.setOnAction(
                event -> selectNodesFile()
        );

        loadEdgesButton.setOnAction(
                event -> selectEdgesFile()
        );

        nodesFileLabel.setWrapText(true);
        edgesFileLabel.setWrapText(true);

        styleSecondaryText(nodesFileLabel);
        styleSecondaryText(edgesFileLabel);

        VBox importSection = new VBox(
                9,
                createSectionLabel("Arquivo de nós"),
                loadNodesButton,
                nodesFileLabel,
                createSectionLabel("Arquivo de conexões"),
                loadEdgesButton,
                edgesFileLabel
        );

        Label graphTitle = createTitle("Rede");

        graphInfoLabel.setWrapText(true);
        styleSecondaryText(graphInfoLabel);

        VBox graphSection = new VBox(
                8,
                graphInfoLabel
        );

        Label routeTitle = createTitle("Análise de rota");

        sourceSelector.setPromptText("Ponto de origem");
        targetSelector.setPromptText("Ponto de destino");

        sourceSelector.setMaxWidth(Double.MAX_VALUE);
        targetSelector.setMaxWidth(Double.MAX_VALUE);

        VBox routeSection = new VBox(
                9,
                createSectionLabel("Origem"),
                sourceSelector,
                createSectionLabel("Destino"),
                targetSelector
        );

        Label failureTitle = createTitle("Simulação de falhas");

        Label instructions = new Label(
                "Clique em um equipamento ou enlace no grafo " +
                "para alternar sua disponibilidade."
        );

        instructions.setWrapText(true);
        styleSecondaryText(instructions);

        failureSelectionLabel.setWrapText(true);
        failureSelectionLabel.setStyle(
                "-fx-text-fill: #fca5a5;" +
                "-fx-font-size: 12px;"
        );

        clearFailuresButton.setOnAction(
                event -> clearFailures()
        );

        simulateButton.setOnAction(
                event -> runSimulation()
        );

        analyzeRedundancyButton.setOnAction(
                event -> runRedundancyAnalysis()
        );

        simulateButton.setDisable(true);
        clearFailuresButton.setDisable(true);
        analyzeRedundancyButton.setDisable(true);

        VBox failureSection = new VBox(
                10,
                instructions,
                failureSelectionLabel,
                clearFailuresButton,
                simulateButton,
                analyzeRedundancyButton
        );

        assessNetworkButton.setOnAction(event -> runNetworkAssessment());
        assessNetworkButton.setDisable(true);

        VBox assessmentSection = new VBox(
                8,
                createTitle("Diagnóstico da rede"),
                new Label("Gere um relatório consolidado com vulnerabilidades e sugestões de redundância."),
                assessNetworkButton
        );

        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(14);

        resultArea.setText(
                "Os resultados da simulação aparecerão aqui."
        );

        resultArea.setStyle(
                "-fx-control-inner-background: #0f172a;" +
                "-fx-text-fill: #e2e8f0;" +
                "-fx-highlight-fill: #2563eb;" +
                "-fx-highlight-text-fill: white;" +
                "-fx-font-family: monospace;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 7px;"
        );

        Label statusTitle = createTitle("Status");

        statusLabel.setWrapText(true);
        styleSecondaryText(statusLabel);

        VBox statusSection = new VBox(
                8,
                statusLabel
        );

        VBox sidebar = new VBox(
                18,
                importTitle,
                importSection,
                new Separator(),
                graphTitle,
                graphSection,
                assessmentSection,
                new Separator(),
                routeTitle,
                routeSection,
                new Separator(),
                failureTitle,
                failureSection,
                new Separator(),
                statusTitle,
                statusSection
        );

        sidebar.setPadding(new Insets(22));
        sidebar.setPrefWidth(350);

        sidebar.setStyle(
                "-fx-background-color: #111827;"
        );

        ScrollPane scrollPane = new ScrollPane(sidebar);

        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(370);

        scrollPane.setStyle(
                "-fx-background: #111827;" +
                "-fx-background-color: #111827;"
        );

        return scrollPane;
    }

    private Button createActionButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 11 14 11 14;" +
                "-fx-background-radius: 7;"
        );

        return button;
    }

    private Button createSecondaryButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.setStyle(
                "-fx-background-color: #334155;" +
                "-fx-text-fill: #f8fafc;" +
                "-fx-padding: 10 14 10 14;" +
                "-fx-background-radius: 7;"
        );

        return button;
    }

    private Label createTitle(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #f8fafc;"
        );

        return label;
    }

    private Label createSectionLabel(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-text-fill: #e2e8f0;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    private void styleSecondaryText(Label label) {

        label.setStyle(
                "-fx-text-fill: #94a3b8;" +
                "-fx-font-size: 11px;"
        );
    }

    private void selectNodesFile() {

        File selectedFile = chooseCsvFile(
                "Selecionar arquivo de nós"
        );

        if (selectedFile == null) {
            return;
        }

        nodesFile = selectedFile.toPath();

        nodesFileLabel.setText(selectedFile.getName());

        loadGraphIfReady();
    }

    private void selectEdgesFile() {

        File selectedFile = chooseCsvFile(
                "Selecionar arquivo de conexões"
        );

        if (selectedFile == null) {
            return;
        }

        edgesFile = selectedFile.toPath();

        edgesFileLabel.setText(selectedFile.getName());

        loadGraphIfReady();
    }

    private File chooseCsvFile(String title) {

        FileChooser fileChooser = new FileChooser();

        File initialDirectory = Path.of("data")
            .toAbsolutePath()
            .normalize()
            .toFile();

        if(initialDirectory.exists() && initialDirectory.isDirectory()){
            fileChooser.setInitialDirectory(initialDirectory);
        }

        fileChooser.setTitle(title);

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Arquivos CSV",
                        "*.csv"
                )
        );

        return fileChooser.showOpenDialog(primaryStage);
    }

    private void loadGraphIfReady() {

        if (nodesFile == null || edgesFile == null) {

            statusLabel.setText(
                    "Selecione os dois arquivos CSV."
            );

            return;
        }

        try {

            Graph loadedGraph = graphCsvLoader.load(
                    nodesFile,
                    edgesFile
            );

            graph = loadedGraph;

            clearSelectedFailures();

            graphCanvas.setGraph(graph);

            updateGraphInformation();

            updateNodeSelectors();

            simulateButton.setDisable(false);
            clearFailuresButton.setDisable(false);
            analyzeRedundancyButton.setDisable(false);
            assessNetworkButton.setDisable(false);

            statusLabel.setText(
                    "Rede carregada com sucesso. " +
                    "Selecione os elementos para simular falhas."
            );

        } catch (IOException | IllegalArgumentException exception) {

            graph = null;

            graphCanvas.setGraph(null);

            simulateButton.setDisable(true);
            clearFailuresButton.setDisable(true);
            analyzeRedundancyButton.setDisable(true);
            assessNetworkButton.setDisable(true);

            graphInfoLabel.setText("Nenhuma rede carregada.");

            showError(
                    "Erro ao importar a rede",
                    exception.getMessage()
            );
        }
    }

    private void updateGraphInformation() {

        graphInfoLabel.setText(
                "Nós: " + graph.nodeCount() +
                "\nConexões: " + graph.edgeCount()
        );
    }

    private void updateNodeSelectors() {

        List<String> nodeIds = graph.getNodes()
                .stream()
                .map(Node::getId)
                .toList();

        sourceSelector.getItems().setAll(nodeIds);
        targetSelector.getItems().setAll(nodeIds);

        sourceSelector.getSelectionModel().selectFirst();
        targetSelector.getSelectionModel().selectLast();
    }

    private void toggleNodeFailure(Node node) {

        String nodeId = node.getId();

        if (!unavailableNodeIds.add(nodeId)) {
            unavailableNodeIds.remove(nodeId);
        }

        invalidateSimulation();
    }

    private void toggleEdgeFailure(Edge edge) {

        String edgeId = edge.getId();

        if (!unavailableEdgeIds.add(edgeId)) {
            unavailableEdgeIds.remove(edgeId);
        }

        invalidateSimulation();
    }

    private void clearFailures() {

        clearSelectedFailures();

        statusLabel.setText(
                "Todas as falhas selecionadas foram removidas."
        );
    }

    private void clearSelectedFailures() {

        unavailableNodeIds.clear();
        unavailableEdgeIds.clear();

        graphCanvas.setUnavailableElements(
                unavailableNodeIds,
                unavailableEdgeIds
        );

        graphCanvas.setHighlightedRoutes(
                Set.of(),
                Set.of()
        );

        graphCanvas.clearHypotheticalConnection();

        failureSelectionLabel.setText(
                "Nenhuma falha selecionada"
        );

        resultArea.setText(
                "Selecione os elementos e execute a simulação."
        );
    }

    private void invalidateSimulation() {

        graphCanvas.setUnavailableElements(
                unavailableNodeIds,
                unavailableEdgeIds
        );

        graphCanvas.setHighlightedRoutes(
                Set.of(),
                Set.of()
        );
        graphCanvas.clearHypotheticalConnection();

        updateFailureSelection();

        resultArea.setText(
                "A seleção de falhas foi alterada. " +
                "Execute uma nova simulação."
        );

        statusLabel.setText(
                "Cenário de falhas atualizado."
        );
    }

    private void updateFailureSelection() {

        String unavailableNodes =
                unavailableNodeIds.isEmpty()
                        ? "Nenhum"
                        : String.join(", ", unavailableNodeIds);

        String unavailableEdges =
                unavailableEdgeIds.isEmpty()
                        ? "Nenhum"
                        : String.join(", ", unavailableEdgeIds);

        failureSelectionLabel.setText(
                "Equipamentos: " + unavailableNodes +
                "\nEnlaces: " + unavailableEdges
        );
    }

    private void runSimulation() {

        if (graph == null) {
            showError(
                    "Simulação indisponível",
                    "Importe uma rede antes de executar a simulação."
            );

            return;
        }

        String sourceId = sourceSelector.getValue();
        String targetId = targetSelector.getValue();

        if (sourceId == null || targetId == null) {

            showError(
                    "Rota inválida",
                    "Selecione a origem e o destino."
            );

            return;
        }

        try {

            FailureScenario scenario = new FailureScenario();

            unavailableNodeIds.forEach(
                    scenario::addUnavailableNode
            );

            unavailableEdgeIds.forEach(
                    scenario::addUnavailableEdge
            );

            FailureSimulationResult result =
                    simulationService.simulate(
                            graph,
                            sourceId,
                            targetId,
                            scenario
                    );

            graphCanvas.setHighlightedRoutes(
                    extractRouteEdgeIds(result.getOriginalRoute()),
                    extractRouteEdgeIds(result.getAffectedRoute())
            );

            displaySimulationResult(result);

            statusLabel.setText(
                    "Simulação concluída com sucesso."
            );

        } catch (IllegalArgumentException exception) {

            showError(
                    "Erro na simulação",
                    exception.getMessage()
            );
        }
    }

    private void runNetworkAssessment() {
        if (graph == null) {
            showError("Avaliação indisponível", "Importe uma rede antes de gerar o relatório.");
            return;
        }
        try {
            String report = assessmentService.generateReport(graph);
            showReportDialog("Relatório de avaliação da rede", report);
            statusLabel.setText("Relatório de avaliação gerado.");
        } catch (IllegalArgumentException exception) {
            showError("Falha na avaliação", exception.getMessage());
        }
    }

    private void showReportDialog(String title, String content) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.initOwner(primaryStage);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        TextArea reportArea = new TextArea(content);
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        reportArea.setPrefColumnCount(85);
        reportArea.setPrefRowCount(32);
        reportArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        dialog.getDialogPane().setContent(reportArea);
        dialog.getDialogPane().setPrefSize(850, 700);
        dialog.showAndWait();
    }

    private void runRedundancyAnalysis() {
        if (graph == null) {
            showError("Análise indisponível", "Importe uma infraestrutura antes de continuar.");
            return;
        }
        String sourceId = sourceSelector.getValue();
        String targetId = targetSelector.getValue();
        if (sourceId == null || targetId == null || sourceId.equals(targetId)) {
            showError("Pontos inválidos", "Selecione dois pontos diferentes para avaliar a nova conexão.");
            return;
        }

        TextInputDialog idDialog = new TextInputDialog("REDUNDANT_LINK_01");
        idDialog.setTitle("Planejamento de redundância");
        idDialog.setHeaderText("Identificação do enlace hipotético");
        idDialog.setContentText("ID da nova conexão:");
        Optional<String> edgeId = idDialog.showAndWait();
        if (edgeId.isEmpty()) return;

        TextInputDialog weightDialog = new TextInputDialog("1.0");
        weightDialog.setTitle("Planejamento de redundância");
        weightDialog.setHeaderText("Peso do enlace hipotético");
        weightDialog.setContentText("Peso/custo abstrato da conexão:");
        Optional<String> weightText = weightDialog.showAndWait();
        if (weightText.isEmpty()) return;

        try {
            double weight = Double.parseDouble(weightText.get().trim());
            RedundancyAnalysisResult result = redundancyService.analyze(
                    graph, edgeId.get().trim(), sourceId, targetId, weight);
            graphCanvas.setHypotheticalConnection(sourceId, targetId);
            StringBuilder output = new StringBuilder();
            output.append("ANÁLISE DE CONEXÃO REDUNDANTE HIPOTÉTICA\n");
            output.append("========================================\n");
            output.append("Nova conexão: ").append(sourceId).append(" <-> ")
                    .append(targetId).append(" (").append(edgeId.get().trim()).append(")\n");
            output.append("Peso informado: ").append(weight).append("\n\n");
            output.append("INDICADORES ESTRUTURAIS\n");
            output.append("Pontes: ").append(result.getBridgeCountBefore())
                    .append(" -> ").append(result.getBridgeCountAfter()).append("\n");
            output.append("Pontos de articulação: ")
                    .append(result.getArticulationPointCountBefore())
                    .append(" -> ").append(result.getArticulationPointCountAfter()).append("\n");
            output.append("Componentes conexos: ").append(result.getComponentCountBefore())
                    .append(" -> ").append(result.getComponentCountAfter()).append("\n\n");
            output.append("Redução de pontes: ").append(result.getBridgeCountReduction()).append("\n");
            output.append("Redução de pontos de articulação: ")
                    .append(result.getArticulationPointCountReduction()).append("\n\n");
            output.append("A infraestrutura original não foi alterada.\n");
            output.append("A viabilidade física (capacidade, portas, orçamento óptico e rota de fibra) precisa ser validada separadamente.");
            showReportDialog("Análise de conexão hipotética", output.toString());
            statusLabel.setText("Análise concluída. A conexão avaliada é hipotética.");
        } catch (NumberFormatException exception) {
            showError("Peso inválido", "Informe um número válido para o peso da conexão.");
        } catch (IllegalArgumentException exception) {
            showError("Não foi possível avaliar a conexão", exception.getMessage());
        }
    }

    private Set<String> extractRouteEdgeIds(
            Optional<RouteResult> optionalRoute
    ) {

        if (optionalRoute.isEmpty()) {
            return Set.of();
        }

        return optionalRoute.get()
                .getEdges()
                .stream()
                .map(Edge::getId)
                .collect(Collectors.toSet());
    }

    private void displaySimulationResult(
            FailureSimulationResult result
    ) {

        StringBuilder output = new StringBuilder();

        output.append("SIMULAÇÃO CONCLUÍDA\n");
        output.append("==============================\n");

        output.append("Origem: ")
                .append(result.getSourceNodeId())
                .append("\n");

        output.append("Destino: ")
                .append(result.getTargetNodeId())
                .append("\n\n");

        // Routes
        output.append("ROTAS\n");
        output.append("------------------------------\n");

        output.append("Original:\n")
                .append(describeRoute(result.getOriginalRoute()))
                .append("\n\n");

        output.append("Após as falhas:\n")
                .append(describeRoute(result.getAffectedRoute()))
                .append("\n\n");

        appendOptionalDifference(output, result);

        // Network metrics
        NetworkResilienceMetrics metrics =
                result.getResilienceMetrics();

        output.append("\nINDICADORES DE RESILIÊNCIA\n");
        output.append("------------------------------\n");

        output.append("Nós disponíveis: ")
                .append(metrics.getAvailableNodeCount())
                .append("/")
                .append(metrics.getOriginalNodeCount())
                .append("\n");

        output.append("Disponibilidade de nós: ")
                .append(formatPercentage(
                        metrics.getNodeAvailabilityPercentage()
                ))
                .append("\n");

        output.append("Enlaces disponíveis: ")
                .append(metrics.getAvailableEdgeCount())
                .append("/")
                .append(metrics.getOriginalEdgeCount())
                .append("\n");

        output.append("Disponibilidade de enlaces: ")
                .append(formatPercentage(
                        metrics.getEdgeAvailabilityPercentage()
                ))
                .append("\n");

        output.append("Maior componente: ")
                .append(metrics.getLargestAffectedComponentSize())
                .append(" nós\n");

        output.append("Cobertura do maior componente: ")
                .append(formatPercentage(
                        metrics.getLargestComponentCoveragePercentage()
                ))
                .append("\n");

        output.append("Componentes antes: ")
                .append(metrics.getOriginalComponentCount())
                .append("\n");

        output.append("Componentes depois: ")
                .append(metrics.getAffectedComponentCount())
                .append("\n");

        output.append("Variação de componentes: ")
                .append(String.format(
                        "%+d",
                        metrics.getComponentCountChange()
                ))
                .append("\n");

        // Critical elements
        output.append("\nELEMENTOS CRÍTICOS\n");
        output.append("------------------------------\n");

        output.append("Pontos de articulação antes: ")
                .append(describeArticulationPoints(
                        result.getOriginalCriticalElements()
                ))
                .append("\n");

        output.append("Pontes antes: ")
                .append(describeBridges(
                        result.getOriginalCriticalElements()
                ))
                .append("\n");

        output.append("Pontos de articulação depois: ")
                .append(describeArticulationPoints(
                        result.getAffectedCriticalElements()
                ))
                .append("\n");

        output.append("Pontes depois: ")
                .append(describeBridges(
                        result.getAffectedCriticalElements()
                ))
                .append("\n");

        // Components
        output.append("\nCOMPONENTES APÓS AS FALHAS\n");
        output.append("------------------------------\n");

        output.append(
                describeComponents(result.getAffectedComponents())
        );

        showReportDialog("Resultado da simulação de falhas", output.toString());
    }

    private String describeRoute(
            Optional<RouteResult> optionalRoute
    ) {

        if (optionalRoute.isEmpty()) {
            return "Nenhuma rota disponível.";
        }

        RouteResult route = optionalRoute.get();

        String path = route.getNodes()
                .stream()
                .map(Node::getId)
                .collect(Collectors.joining(" -> "));

        return path +
                "\nCusto: " +
                formatNumber(route.getTotalWeight());
    }

    private void appendOptionalDifference(
            StringBuilder output,
            FailureSimulationResult result
    ) {

        output.append("Variação do custo: ");

        OptionalDouble difference =
                result.getRouteCostDifference();

        if (difference.isPresent()) {

            output.append(
                    String.format(
                            Locale.ROOT,
                            "%+.2f",
                            difference.getAsDouble()
                    )
            );

        } else {

            output.append("N/A");
        }

        output.append("\n");

        output.append("Variação percentual: ");

        output.append(
                formatPercentage(
                        result.getRouteCostChangePercentage()
                )
        );

        output.append("\n");
    }

    private String formatPercentage(OptionalDouble percentage) {

        if (percentage.isEmpty()) {
            return "N/A";
        }

        return String.format(
                Locale.ROOT,
                "%.2f%%",
                percentage.getAsDouble()
        );
    }

    private String formatNumber(double value) {

        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        );
    }

    private String describeArticulationPoints(
            CriticalElementsResult result
    ) {

        String ids = result.getArticulationPoints()
                .stream()
                .map(Node::getId)
                .collect(Collectors.joining(", "));

        return ids.isEmpty() ? "Nenhum" : ids;
    }

    private String describeBridges(
            CriticalElementsResult result
    ) {

        String ids = result.getBridges()
                .stream()
                .map(Edge::getId)
                .collect(Collectors.joining(", "));

        return ids.isEmpty() ? "Nenhuma" : ids;
    }

    private String describeComponents(
            ConnectedComponentsResult result
    ) {

        StringBuilder output = new StringBuilder();

        int index = 1;

        for (List<Node> component : result.getComponents()) {

            String nodeIds = component.stream()
                    .map(Node::getId)
                    .collect(Collectors.joining(", "));

            output.append("Componente ")
                    .append(index++)
                    .append(": [")
                    .append(nodeIds)
                    .append("]\n");
        }

        if (result.getComponentCount() == 0) {
            output.append("Nenhum componente disponível.\n");
        }

        return output.toString();
    }

    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.initOwner(primaryStage);

        alert.setTitle(title);
        alert.setHeaderText("Não foi possível concluir a operação.");
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
