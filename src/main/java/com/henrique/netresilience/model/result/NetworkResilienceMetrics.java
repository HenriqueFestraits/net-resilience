
package com.henrique.netresilience.model.result;

import java.util.OptionalDouble;

public final class NetworkResilienceMetrics {

    private final int originalNodeCount;
    private final int availableNodeCount;

    private final int originalEdgeCount;
    private final int availableEdgeCount;

    private final int originalComponentCount;
    private final int affectedComponentCount;

    private final int largestAffectedComponentSize;

    public NetworkResilienceMetrics(
            int originalNodeCount,
            int availableNodeCount,
            int originalEdgeCount,
            int availableEdgeCount,
            int originalComponentCount,
            int affectedComponentCount,
            int largestAffectedComponentSize
    ) {

        validateNonNegative(originalNodeCount, "Original node count");
        validateNonNegative(availableNodeCount, "Available node count");
        validateNonNegative(originalEdgeCount, "Original edge count");
        validateNonNegative(availableEdgeCount, "Available edge count");
        validateNonNegative(originalComponentCount, "Original component count");
        validateNonNegative(affectedComponentCount, "Affected component count");
        validateNonNegative(
                largestAffectedComponentSize,
                "Largest component size"
        );

        if (availableNodeCount > originalNodeCount) {
            throw new IllegalArgumentException(
                    "Available nodes cannot exceed original nodes."
            );
        }

        if (availableEdgeCount > originalEdgeCount) {
            throw new IllegalArgumentException(
                    "Available edges cannot exceed original edges."
            );
        }

        if (originalComponentCount > originalNodeCount
                || affectedComponentCount > availableNodeCount) {
            throw new IllegalArgumentException(
                    "Component count cannot exceed node count."
            );
        }

        if (largestAffectedComponentSize > availableNodeCount) {
            throw new IllegalArgumentException(
                    "Largest component cannot exceed available nodes."
            );
        }

        // An empty original graph must have zero components.
        if ((originalNodeCount == 0) != (originalComponentCount == 0)) {
            throw new IllegalArgumentException(
                    "Original component count is inconsistent with node count."
            );
        }

        // An empty affected graph must have zero components
        // and a largest component size of zero.
        if (availableNodeCount == 0) {

            if (affectedComponentCount != 0
                    || largestAffectedComponentSize != 0) {

                throw new IllegalArgumentException(
                        "Affected component information is inconsistent."
                );
            }

        } else {

            if (affectedComponentCount == 0
                    || largestAffectedComponentSize == 0) {

                throw new IllegalArgumentException(
                        "A non-empty graph must have at least one component."
                );
            }
        }

        this.originalNodeCount = originalNodeCount;
        this.availableNodeCount = availableNodeCount;

        this.originalEdgeCount = originalEdgeCount;
        this.availableEdgeCount = availableEdgeCount;

        this.originalComponentCount = originalComponentCount;
        this.affectedComponentCount = affectedComponentCount;

        this.largestAffectedComponentSize = largestAffectedComponentSize;
    }

    public int getOriginalNodeCount() {
        return originalNodeCount;
    }

    public int getAvailableNodeCount() {
        return availableNodeCount;
    }

    public int getUnavailableNodeCount() {
        return originalNodeCount - availableNodeCount;
    }

    public int getOriginalEdgeCount() {
        return originalEdgeCount;
    }

    public int getAvailableEdgeCount() {
        return availableEdgeCount;
    }

    public int getUnavailableEdgeCount() {
        return originalEdgeCount - availableEdgeCount;
    }

    public int getOriginalComponentCount() {
        return originalComponentCount;
    }

    public int getAffectedComponentCount() {
        return affectedComponentCount;
    }

    public int getComponentCountChange() {
        return affectedComponentCount - originalComponentCount;
    }

    public int getLargestAffectedComponentSize() {
        return largestAffectedComponentSize;
    }

    public OptionalDouble getNodeAvailabilityPercentage() {
        return calculatePercentage(
                availableNodeCount,
                originalNodeCount
        );
    }

    public OptionalDouble getEdgeAvailabilityPercentage() {
        return calculatePercentage(
                availableEdgeCount,
                originalEdgeCount
        );
    }

    public OptionalDouble getLargestComponentCoveragePercentage() {
        return calculatePercentage(
                largestAffectedComponentSize,
                originalNodeCount
        );
    }

    private OptionalDouble calculatePercentage(int value, int total) {

        if (total == 0) {
            return OptionalDouble.empty();
        }

        // Convert to double before division to preserve decimal places.
        return OptionalDouble.of(
                ((double) value / total) * 100.0
        );
    }

    private void validateNonNegative(int value, String name) {

        if (value < 0) {
            throw new IllegalArgumentException(
                    name + " cannot be negative."
            );
        }
    }
}
