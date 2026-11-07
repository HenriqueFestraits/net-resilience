package com.henrique.netresilience.model.graph;

public class Node {

    private final String id;
    private final String label;

    private final double x;
    private final double y;
    private final boolean hasPosition;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;

        this.x = 0.0;
        this.y = 0.0;
        this.hasPosition = false;
    }

    public Node(String id, String label, double x, double y) {

        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Node coordinates must be finite numbers"
            );
        }

        this.id = id;
        this.label = label;

        this.x = x;
        this.y = y;
        this.hasPosition = true;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Indicates whether this node has an explicit visual position.
     *
     * Coordinates are used only by the graphical interface. They do not
     * affect graph algorithms or edge weights.
     */
    public boolean hasPosition() {
        return hasPosition;
    }

    /**
     * Returns the horizontal visual coordinate.
     *
     * @throws IllegalStateException when this node has no explicit position
     */
    public double getX() {

        if (!hasPosition) {
            throw new IllegalStateException(
                    "Node " + id + " does not have a visual position"
            );
        }

        return x;
    }

    /**
     * Returns the vertical visual coordinate.
     *
     * @throws IllegalStateException when this node has no explicit position
     */
    public double getY() {

        if (!hasPosition) {
            throw new IllegalStateException(
                    "Node " + id + " does not have a visual position"
            );
        }

        return y;
    }
}
