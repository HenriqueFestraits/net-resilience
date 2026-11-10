package com.henrique.netresilience.ui.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;
import com.henrique.netresilience.model.graph.Node;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.input.MouseEvent;

import javafx.scene.layout.Region;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class GraphCanvas extends Region {

    private static final double NODE_RADIUS = 23.0;
    private static final double EDGE_CLICK_TOLERANCE = 10.0;
    private static final double POSITION_PADDING = NODE_RADIUS + 45.0;

    private static final Color BACKGROUND_COLOR =
            Color.web("#0f172a");

    private static final Color EDGE_COLOR =
            Color.web("#64748b");

    private static final Color NODE_COLOR =
            Color.web("#2563eb");

    private static final Color NODE_BORDER_COLOR =
            Color.web("#93c5fd");

    private static final Color FAILED_COLOR =
            Color.web("#dc2626");

    private static final Color ORIGINAL_ROUTE_COLOR =
            Color.web("#38bdf8");

    private static final Color AFFECTED_ROUTE_COLOR =
            Color.web("#22c55e");

    private static final Color TEXT_COLOR =
            Color.web("#f8fafc");

    private static final Color WEIGHT_COLOR =
            Color.web("#cbd5e1");

    private final Canvas canvas = new Canvas();

    private Graph graph;

    private Map<String, Point2D> nodePositions = Map.of();

    private Set<String> unavailableNodeIds = Set.of();
    private Set<String> unavailableEdgeIds = Set.of();

    private Set<String> originalRouteEdgeIds = Set.of();
    private Set<String> affectedRouteEdgeIds = Set.of();

    private String hypotheticalSourceId;
    private String hypotheticalTargetId;

    private Consumer<Node> onNodeClicked = node -> {};
    private Consumer<Edge> onEdgeClicked = edge -> {};

    public GraphCanvas() {

        getChildren().add(canvas);

        setMinSize(400, 350);
        setPrefSize(800, 600);

        widthProperty().addListener(
                (observable, oldValue, newValue) -> redraw()
        );

        heightProperty().addListener(
                (observable, oldValue, newValue) -> redraw()
        );

        canvas.setOnMouseClicked(this::handleCanvasClick);
    }

    public void setGraph(Graph graph) {

        this.graph = graph;
        clearHypotheticalConnection();

        redraw();
    }

    public void setOnNodeClicked(Consumer<Node> handler) {

        this.onNodeClicked = handler == null
                ? node -> {}
                : handler;
    }

    public void setOnEdgeClicked(Consumer<Edge> handler) {

        this.onEdgeClicked = handler == null
                ? edge -> {}
                : handler;
    }

    public void setUnavailableElements(
            Set<String> nodeIds,
            Set<String> edgeIds
    ) {

        this.unavailableNodeIds = Set.copyOf(nodeIds);
        this.unavailableEdgeIds = Set.copyOf(edgeIds);

        redraw();
    }

    public void setHypotheticalConnection(String sourceId, String targetId) {
        this.hypotheticalSourceId = sourceId;
        this.hypotheticalTargetId = targetId;
        redraw();
    }

    public void clearHypotheticalConnection() {
        this.hypotheticalSourceId = null;
        this.hypotheticalTargetId = null;
        redraw();
    }

    public void setHighlightedRoutes(
            Set<String> originalRouteIds,
            Set<String> affectedRouteIds
    ) {

        this.originalRouteEdgeIds = Set.copyOf(originalRouteIds);
        this.affectedRouteEdgeIds = Set.copyOf(affectedRouteIds);

        redraw();
    }

    @Override
    protected void layoutChildren() {

        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());

        redraw();
    }

    private void redraw() {

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, width, height);

        if (graph == null || graph.nodeCount() == 0) {

            nodePositions = Map.of();

            drawEmptyMessage(gc, width, height);

            return;
        }

        List<Node> nodes = graph.getNodes();

        nodePositions = calculatePositions(
                nodes,
                width,
                height
        );

        drawEdges(gc);
        drawHypotheticalConnection(gc);
        drawNodes(gc, nodes);
    }

    private Map<String, Point2D> calculatePositions(
            List<Node> nodes,
            double width,
            double height
    ) {

        boolean allNodesHaveCoordinates = nodes.stream()
                .allMatch(Node::hasPosition);

        if (allNodesHaveCoordinates) {
            return calculateCoordinatePositions(nodes, width, height);
        }

        // Compatibility fallback for graphs created without visual metadata.
        return calculateCircularPositions(nodes, width, height);
    }

    /**
     * Maps user-defined graph coordinates into the available canvas area.
     *
     * The mapping preserves the relative proportions between X and Y.
     * Positive X points right and positive Y points down, matching JavaFX.
     */
    private Map<String, Point2D> calculateCoordinatePositions(
            List<Node> nodes,
            double width,
            double height
    ) {

        Map<String, Point2D> positions = new HashMap<>();

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Node node : nodes) {

            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());

            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        double availableWidth = Math.max(
                1.0,
                width - 2.0 * POSITION_PADDING
        );

        double availableHeight = Math.max(
                1.0,
                height - 2.0 * POSITION_PADDING
        );

        double scaleX = rangeX > 0.0
                ? availableWidth / rangeX
                : Double.POSITIVE_INFINITY;

        double scaleY = rangeY > 0.0
                ? availableHeight / rangeY
                : Double.POSITIVE_INFINITY;

        double scale = Math.min(scaleX, scaleY);

        // A graph with a single unique coordinate needs no scaling.
        if (!Double.isFinite(scale)) {
            scale = 1.0;
        }

        double centerDataX = (minX + maxX) / 2.0;
        double centerDataY = (minY + maxY) / 2.0;

        double centerCanvasX = width / 2.0;
        double centerCanvasY = height / 2.0;

        for (Node node : nodes) {

            double screenX = centerCanvasX
                    + (node.getX() - centerDataX) * scale;

            double screenY = centerCanvasY
                    + (node.getY() - centerDataY) * scale;

            positions.put(
                    node.getId(),
                    new Point2D(screenX, screenY)
            );
        }

        return positions;
    }

    private Map<String, Point2D> calculateCircularPositions(
            List<Node> nodes,
            double width,
            double height
    ) {

        Map<String, Point2D> positions = new HashMap<>();

        int nodeCount = nodes.size();

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        double radius = Math.max(
                0,
                Math.min(width, height) * 0.35
        );

        for (int i = 0; i < nodeCount; i++) {

            double angle =
                    -Math.PI / 2.0 +
                    (2.0 * Math.PI * i / nodeCount);

            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            positions.put(
                    nodes.get(i).getId(),
                    new Point2D(x, y)
            );
        }

        return positions;
    }

    private void drawEdges(GraphicsContext gc) {

        gc.setLineWidth(2.5);

        gc.setFill(WEIGHT_COLOR);
        gc.setFont(Font.font("System", 11));

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (Edge edge : graph.getEdges()) {

            Point2D source =
                    nodePositions.get(edge.getSource().getId());

            Point2D target =
                    nodePositions.get(edge.getTarget().getId());

            if (source == null || target == null) {
                continue;
            }

            double dx = target.getX() - source.getX();
            double dy = target.getY() - source.getY();

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance == 0) {
                continue;
            }

            double offsetX = dx / distance * NODE_RADIUS;
            double offsetY = dy / distance * NODE_RADIUS;

            double startX = source.getX() + offsetX;
            double startY = source.getY() + offsetY;

            double endX = target.getX() - offsetX;
            double endY = target.getY() - offsetY;

            boolean unavailable =
                    unavailableEdgeIds.contains(edge.getId())
                    || unavailableNodeIds.contains(
                            edge.getSource().getId()
                    )
                    || unavailableNodeIds.contains(
                            edge.getTarget().getId()
                    );

            Color edgeColor = EDGE_COLOR;

            if (unavailable) {

                edgeColor = FAILED_COLOR;

            } else if (affectedRouteEdgeIds.contains(edge.getId())) {

                edgeColor = AFFECTED_ROUTE_COLOR;

            } else if (originalRouteEdgeIds.contains(edge.getId())) {

                edgeColor = ORIGINAL_ROUTE_COLOR;
            }

            gc.setStroke(edgeColor);

            gc.strokeLine(
                    startX,
                    startY,
                    endX,
                    endY
            );

            double middleX =
                    (source.getX() + target.getX()) / 2.0;

            double middleY =
                    (source.getY() + target.getY()) / 2.0;

            String label = edge.getId() +
                    " (" + String.format("%.2f", edge.getWeight()) + ")";

            // Background behind edge label.
            gc.setFill(BACKGROUND_COLOR);

            gc.fillRoundRect(
                    middleX - 37,
                    middleY - 23,
                    74,
                    18,
                    5,
                    5
            );

            gc.setFill(edgeColor);

            gc.fillText(
                    label,
                    middleX,
                    middleY - 14
            );
        }
    }

    private void drawHypotheticalConnection(GraphicsContext gc) {

        if (hypotheticalSourceId == null || hypotheticalTargetId == null) {
            return;
        }

        Point2D source = nodePositions.get(hypotheticalSourceId);
        Point2D target = nodePositions.get(hypotheticalTargetId);
        if (source == null || target == null) {
            return;
        }

        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double distance = Math.hypot(dx, dy);
        if (distance == 0.0) {
            return;
        }

        double offsetX = dx / distance * NODE_RADIUS;
        double offsetY = dy / distance * NODE_RADIUS;

        gc.setStroke(Color.web("#f59e0b"));
        gc.setLineWidth(3.0);
        gc.setLineDashes(9.0, 6.0);
        gc.strokeLine(
                source.getX() + offsetX,
                source.getY() + offsetY,
                target.getX() - offsetX,
                target.getY() - offsetY
        );
        gc.setLineDashes();

        double middleX = (source.getX() + target.getX()) / 2.0;
        double middleY = (source.getY() + target.getY()) / 2.0;
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRoundRect(middleX - 42, middleY - 34, 84, 18, 5, 5);
        gc.setFill(Color.web("#fbbf24"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("ENLACE HIPOTÉTICO", middleX, middleY - 25);
    }

    private void drawNodes(
            GraphicsContext gc,
            List<Node> nodes
    ) {

        for (Node node : nodes) {

            Point2D position =
                    nodePositions.get(node.getId());

            if (position == null) {
                continue;
            }

            double x = position.getX();
            double y = position.getY();

            boolean unavailable =
                    unavailableNodeIds.contains(node.getId());

            // Node circle.
            gc.setFill(
                    unavailable ? FAILED_COLOR : NODE_COLOR
            );

            gc.fillOval(
                    x - NODE_RADIUS,
                    y - NODE_RADIUS,
                    NODE_RADIUS * 2,
                    NODE_RADIUS * 2
            );

            gc.setStroke(NODE_BORDER_COLOR);
            gc.setLineWidth(2);

            gc.strokeOval(
                    x - NODE_RADIUS,
                    y - NODE_RADIUS,
                    NODE_RADIUS * 2,
                    NODE_RADIUS * 2
            );

            // Node ID.
            gc.setFill(TEXT_COLOR);

            gc.setFont(
                    Font.font("System", FontWeight.BOLD, 14)
            );

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            gc.fillText(node.getId(), x, y);

            // Node label.
            gc.setFont(Font.font("System", 11));
            gc.setFill(WEIGHT_COLOR);

            String label = node.getLabel() == null
                    ? ""
                    : node.getLabel();

            gc.fillText(
                    label,
                    x,
                    y + NODE_RADIUS + 14,
                    130
            );
        }
    }

    private void handleCanvasClick(MouseEvent event) {

        if (graph == null) {
            return;
        }

        Point2D click = new Point2D(
                event.getX(),
                event.getY()
        );

        // Nodes have priority over edges.
        for (Node node : graph.getNodes()) {

            Point2D position =
                    nodePositions.get(node.getId());

            if (position == null) {
                continue;
            }

            if (click.distance(position) <= NODE_RADIUS + 5) {

                onNodeClicked.accept(node);

                return;
            }
        }

        // Find the closest edge to the click.
        Edge closestEdge = null;

        double smallestDistance = EDGE_CLICK_TOLERANCE;

        for (Edge edge : graph.getEdges()) {

            Point2D source =
                    nodePositions.get(edge.getSource().getId());

            Point2D target =
                    nodePositions.get(edge.getTarget().getId());

            if (source == null || target == null) {
                continue;
            }

            double distance = distanceToSegment(
                    click,
                    source,
                    target
            );

            if (distance < smallestDistance) {

                smallestDistance = distance;
                closestEdge = edge;
            }
        }

        if (closestEdge != null) {
            onEdgeClicked.accept(closestEdge);
        }
    }

    private double distanceToSegment(
            Point2D point,
            Point2D start,
            Point2D end
    ) {

        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();

        double lengthSquared = dx * dx + dy * dy;

        if (lengthSquared == 0) {
            return point.distance(start);
        }

        double projection =
                ((point.getX() - start.getX()) * dx
                + (point.getY() - start.getY()) * dy)
                / lengthSquared;

        projection = Math.max(
                0,
                Math.min(1, projection)
        );

        double closestX = start.getX() + projection * dx;
        double closestY = start.getY() + projection * dy;

        return point.distance(closestX, closestY);
    }

    private void drawEmptyMessage(
            GraphicsContext gc,
            double width,
            double height
    ) {

        gc.setFill(Color.web("#94a3b8"));

        gc.setFont(Font.font("System", 16));

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.fillText(
                "Importe os arquivos CSV para visualizar a rede.",
                width / 2.0,
                height / 2.0
        );
    }
}
