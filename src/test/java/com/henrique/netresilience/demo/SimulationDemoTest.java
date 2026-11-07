
package com.henrique.netresilience.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.henrique.netresilience.model.scenario.FailureScenario;

class SimulationDemoTest {

    @Test
    void shouldParseMultipleNodeAndEdgeFailures() {

        String[] args = {
                "nodes.csv",
                "edges.csv",
                "A",
                "F",
                "--edge", "E1",
                "--node", "D",
                "--edge", "E5"
        };

        FailureScenario scenario =
                SimulationDemo.parseFailureOptions(args, 4);

        assertEquals(
                Set.of("D"),
                scenario.getUnavailableNodeIds()
        );

        assertEquals(
                Set.of("E1", "E5"),
                scenario.getUnavailableEdgeIds()
        );
    }

    @Test
    void shouldAllowScenarioWithoutFailures() {

        String[] args = {
                "nodes.csv",
                "edges.csv",
                "A",
                "F"
        };

        FailureScenario scenario =
                SimulationDemo.parseFailureOptions(args, 4);

        assertTrue(scenario.isEmpty());
    }

    @Test
    void shouldRejectOptionWithoutId() {

        String[] args = {
                "nodes.csv",
                "edges.csv",
                "A",
                "F",
                "--node"
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> SimulationDemo.parseFailureOptions(args, 4)
        );
    }

    @Test
    void shouldRejectUnknownFailureOption() {

        String[] args = {
                "nodes.csv",
                "edges.csv",
                "A",
                "F",
                "--vertex",
                "D"
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> SimulationDemo.parseFailureOptions(args, 4)
        );
    }

    @Test
    void shouldRejectAnotherOptionWhereIdIsExpected() {

        String[] args = {
                "nodes.csv",
                "edges.csv",
                "A",
                "F",
                "--node",
                "--edge",
                "E1"
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> SimulationDemo.parseFailureOptions(args, 4)
        );
    }
}
