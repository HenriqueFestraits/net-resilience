
package com.henrique.netresilience.model.result;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NetworkResilienceMetricsTest {

    @Test
    void shouldCalculateAvailabilityAndCoveragePercentages() {

        NetworkResilienceMetrics metrics =
                new NetworkResilienceMetrics(
                        10, 8,
                        20, 15,
                        2, 4,
                        5
                );

        assertEquals(
                80.0,
                metrics.getNodeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                75.0,
                metrics.getEdgeAvailabilityPercentage().orElseThrow(),
                0.0001
        );

        assertEquals(
                50.0,
                metrics.getLargestComponentCoveragePercentage().orElseThrow(),
                0.0001
        );

        assertEquals(2, metrics.getUnavailableNodeCount());
        assertEquals(5, metrics.getUnavailableEdgeCount());

        assertEquals(2, metrics.getComponentCountChange());
    }

    @Test
void shouldHandleNetworksWithoutEdges() {

    NetworkResilienceMetrics metrics =
            new NetworkResilienceMetrics(
                    3, 3,
                    0, 0,
                    3, 3,
                    1
            );

    assertTrue(
            metrics.getEdgeAvailabilityPercentage().isEmpty()
    );

    assertEquals(
            100.0,
            metrics.getNodeAvailabilityPercentage().orElseThrow(),
            0.0001
    );

    assertEquals(
            100.0 / 3.0,
            metrics.getLargestComponentCoveragePercentage().orElseThrow(),
            0.0001
    );

    assertEquals(3, metrics.getAffectedComponentCount());
    assertEquals(1, metrics.getLargestAffectedComponentSize());
}

    @Test
    void shouldHandleEmptyNetworks() {

        NetworkResilienceMetrics metrics =
                new NetworkResilienceMetrics(
                        0, 0,
                        0, 0,
                        0, 0,
                        0
                );

        assertTrue(metrics.getNodeAvailabilityPercentage().isEmpty());
        assertTrue(metrics.getEdgeAvailabilityPercentage().isEmpty());

        assertTrue(
                metrics.getLargestComponentCoveragePercentage().isEmpty()
        );
    }

    @Test
    void shouldRejectAvailabilityGreaterThanOriginalCounts() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkResilienceMetrics(
                        3, 4,
                        5, 5,
                        1, 1,
                        4
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkResilienceMetrics(
                        3, 3,
                        5, 6,
                        1, 1,
                        3
                )
        );
    }

    @Test
    void shouldRejectLargestComponentGreaterThanAvailableNodes() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkResilienceMetrics(
                        5, 3,
                        5, 3,
                        1, 2,
                        4
                )
        );
    }
}
