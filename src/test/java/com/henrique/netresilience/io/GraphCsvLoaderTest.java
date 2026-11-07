
package com.henrique.netresilience.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.henrique.netresilience.model.graph.Graph;

class GraphCsvLoaderTest {

    @TempDir
    Path tempDir;

    private final GraphCsvLoader loader = new GraphCsvLoader();

    
@Test
void shouldLoadGraphFromCsvFiles() throws IOException {

    String nodesCsv = """
            id,label
            A,"Estação, Central"
            B,"Estação ""Norte"" Centro"
            C,Estação Sul
            """;

    String edgesCsv = """
            id,source,target,weight
            E1,A,B,2.5
            E2,B,C,3.0
            """;

    Graph graph = loadGraph(nodesCsv, edgesCsv);

    assertEquals(3, graph.nodeCount());
    assertEquals(2, graph.edgeCount());

    assertEquals(
            "Estação, Central",
            graph.getNode("A").getLabel()
    );

    assertEquals(
            "Estação \"Norte\" Centro",
            graph.getNode("B").getLabel()
    );

    assertEquals(
            2.5,
            graph.findEdge("E1").orElseThrow().getWeight(),
            0.0001
    );
}


    @Test
    void shouldAllowParallelEdges() throws IOException {

        String nodesCsv = """
                id,label
                A,Station A
                B,Station B
                """;

        String edgesCsv = """
                id,source,target,weight
                E1,A,B,2.0
                E2,A,B,3.0
                """;

        Graph graph = loadGraph(nodesCsv, edgesCsv);

        assertEquals(2, graph.edgeCount());
        assertEquals(2, graph.getIncidentEdges("A").size());
    }

    @Test
    void shouldRejectDuplicateNodeIds() throws IOException {

        String nodesCsv = """
                id,label
                A,Station A
                A,Another Station A
                """;

        String edgesCsv = """
                id,source,target,weight
                E1,A,A,2.0
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loadGraph(nodesCsv, edgesCsv)
        );

        assertTrue(exception.getMessage().contains("nodes.csv"));
        assertTrue(exception.getMessage().contains("line 3"));
    }

    
@Test
void shouldRejectInvalidEdgeWeight() throws IOException {

    String nodesCsv = """
            id,label
            A,Station A
            B,Station B
            """;

    String edgesCsv = """
            id,source,target,weight
            E1,A,B,NaN
            """;

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> loadGraph(nodesCsv, edgesCsv)
    );

    // The error must originate from the edges CSV file.
    assertTrue(
            exception.getMessage().contains("edges.csv"),
            exception.getMessage()
    );

    // The invalid record is on line 2.
    assertTrue(
            exception.getMessage().contains("line 2"),
            exception.getMessage()
    );
}


    @Test
    void shouldRejectNegativeEdgeWeight() throws IOException {

        String nodesCsv = """
                id,label
                A,Station A
                B,Station B
                """;

        String edgesCsv = """
                id,source,target,weight
                E1,A,B,-2.0
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> loadGraph(nodesCsv, edgesCsv)
        );
    }

    @Test
    void shouldRejectEdgesWithUnknownNodes() throws IOException {

        String nodesCsv = """
                id,label
                A,Station A
                """;

        String edgesCsv = """
                id,source,target,weight
                E1,A,UNKNOWN,2.0
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loadGraph(nodesCsv, edgesCsv)
        );

        assertTrue(exception.getMessage().contains("edges.csv"));
        assertTrue(exception.getMessage().contains("line 2"));
    }

    @Test
    void shouldRejectDuplicateEdgeIds() throws IOException {

        String nodesCsv = """
                id,label
                A,Station A
                B,Station B
                C,Station C
                """;

        String edgesCsv = """
                id,source,target,weight
                E1,A,B,2.0
                E1,B,C,3.0
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> loadGraph(nodesCsv, edgesCsv)
        );
    }

    @Test
    void shouldRejectMalformedCsvQuotes() throws IOException {

        String nodesCsv = """
                id,label
                A,"Station A
                """;

        String edgesCsv = """
                id,source,target,weight
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> loadGraph(nodesCsv, edgesCsv)
        );
    }

    @Test
    void shouldRejectUnexpectedHeader() throws IOException {

        String nodesCsv = """
                identifier,label
                A,Station A
                """;

        String edgesCsv = """
                id,source,target,weight
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> loadGraph(nodesCsv, edgesCsv)
        );
    }

    private Graph loadGraph(
            String nodesCsv,
            String edgesCsv
    ) throws IOException {

        Path nodesFile = tempDir.resolve("nodes.csv");
        Path edgesFile = tempDir.resolve("edges.csv");

        Files.writeString(
                nodesFile,
                nodesCsv,
                StandardCharsets.UTF_8
        );

        Files.writeString(
                edgesFile,
                edgesCsv,
                StandardCharsets.UTF_8
        );

        return loader.load(nodesFile, edgesFile);
    }
}
