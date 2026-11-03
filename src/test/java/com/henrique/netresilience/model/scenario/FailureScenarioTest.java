package com.henrique.netresilience.model.scenario;

import com.henrique.netresilience.model.graph.Edge;
import com.henrique.netresilience.model.graph.Graph;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FailureScenarioTest {

    @Test
    void shouldStartWithNoFailures() {
        FailureScenario scenario = new FailureScenario();

        assertTrue(scenario.isEmpty());
        assertTrue(scenario.getUnavailableNodeIds().isEmpty());
        assertTrue(scenario.getUnavailableEdgeIds().isEmpty());
    }

    @Test
    void shouldRegisterUnavailableNodesAndEdges() {
        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("A");
        scenario.addUnavailableNode("B");
        scenario.addUnavailableEdge("E1");

        assertEquals(
                Set.of("A", "B"),
                scenario.getUnavailableNodeIds()
        );

        assertEquals(
                Set.of("E1"),
                scenario.getUnavailableEdgeIds()
        );

        assertFalse(scenario.isEmpty());
    }

    @Test
    void shouldNotDuplicateUnavailableElements() {
        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("A");
        scenario.addUnavailableNode("A");

        scenario.addUnavailableEdge("E1");
        scenario.addUnavailableEdge("E1");

        assertEquals(1, scenario.getUnavailableNodeIds().size());
        assertEquals(1, scenario.getUnavailableEdgeIds().size());
    }

    @Test
    void shouldConsiderIncidentEdgesUnavailableWhenNodeFails() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");
        graph.addNode("C", "Node C");

        Edge e1 = graph.addEdge("E1", "A", "B", 5.0);
        Edge e2 = graph.addEdge("E2", "B", "C", 3.0);

        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("B");

        assertTrue(scenario.isNodeUnavailable("B"));
        assertTrue(scenario.isEdgeUnavailable(e1));
        assertTrue(scenario.isEdgeUnavailable(e2));
    }

    @Test
    void shouldRestoreIncidentEdgesWhenNodeIsRestored() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");

        Edge edge = graph.addEdge("E1", "A", "B", 5.0);

        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("A");

        assertTrue(scenario.isEdgeUnavailable(edge));

        scenario.restoreNode("A");

        assertFalse(scenario.isNodeUnavailable("A"));
        assertFalse(scenario.isEdgeUnavailable(edge));
        assertTrue(scenario.isEmpty());
    }

    @Test
    void shouldKeepExplicitEdgeFailureAfterRestoringNode() {
        Graph graph = new Graph();

        graph.addNode("A", "Node A");
        graph.addNode("B", "Node B");

        Edge edge = graph.addEdge("E1", "A", "B", 5.0);

        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("A");
        scenario.addUnavailableEdge("E1");

        scenario.restoreNode("A");

        assertFalse(scenario.isNodeUnavailable("A"));
        assertTrue(scenario.isEdgeUnavailable(edge));
    }

    @Test
    void shouldClearAllRegisteredFailures() {
        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("A");
        scenario.addUnavailableEdge("E1");

        scenario.clear();

        assertTrue(scenario.isEmpty());
        assertTrue(scenario.getUnavailableNodeIds().isEmpty());
        assertTrue(scenario.getUnavailableEdgeIds().isEmpty());
    }

    @Test
    void shouldRejectNullAndBlankIds() {
        FailureScenario scenario = new FailureScenario();

        assertThrows(
                IllegalArgumentException.class,
                () -> scenario.addUnavailableNode(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> scenario.addUnavailableNode(" ")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> scenario.addUnavailableEdge("")
        );
    }

    @Test
    void shouldReturnUnmodifiableSnapshots() {
        FailureScenario scenario = new FailureScenario();

        scenario.addUnavailableNode("A");

        Set<String> nodeIds = scenario.getUnavailableNodeIds();

        assertThrows(
                UnsupportedOperationException.class,
                () -> nodeIds.add("B")
        );

        // External modifications must not affect the scenario.
        assertFalse(scenario.isNodeUnavailable("B"));
    }
}