package com.henrique.netresilience.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.henrique.netresilience.model.graph.Graph;

public final class GraphCsvLoader {

    private static final List<String> NODE_HEADER = List.of("id", "label");
    private static final List<String> EDGE_HEADER = List.of("id", "source", "target", "weight");

    public Graph load(Path nodesFile, Path edgesFile) throws IOException {
        
        Objects.requireNonNull(nodesFile, "Nodes file cannot be null");
        Objects.requireNonNull(edgesFile, "Edges file cannot be null");

        Graph graph = new Graph();

        loadNodes(graph, nodesFile);
        loadEdges(graph, edgesFile);

        return graph;
    }

    private void loadNodes(Graph graph, Path file) throws IOException{
        try(BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)){

            String line;
            int lineNumber = 0;
            boolean headerRead = false;

            while((line = reader.readLine()) != null){
                lineNumber++;

                //Remove UTF-8 BOM if present
                if(lineNumber == 1 && line.startsWith("\uFEFF")){
                    line = line.substring(1);
                }
                if(line.isBlank()){
                    continue;
                }

                List<String> fields;

                try{
                    fields = parseCsvLine(line);
                }catch (IllegalArgumentException e){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        e.getMessage(),
                        e
                    );
                }

                if(!headerRead){
                    validateHeader(fields, NODE_HEADER, file, lineNumber);

                    headerRead = true;
                    continue;
                }

                if(fields.size() != 2){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        "Expected 2 fields: id, label"
                    );
                }

                String id = requireValue(fields.get(0).trim(), "Node ID", file, lineNumber);
                String label = requireValue(fields.get(1).trim(), "Node Label", file, lineNumber);

                try{
                    graph.addNode(id, label);
                }catch(IllegalArgumentException e){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        e.getMessage(),
                        e
                    );
                }
            }

            if(!headerRead){
                throw invalidCsv(
                    file,
                    1,
                    "Missing header. Expected: id, label"
                );
            } 
        }
    }

    private void loadEdges(Graph graph, Path file) throws IOException{
        try(BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)){
            
            String line;
            int lineNumber = 0;
            boolean headerRead = false;

            while((line = reader.readLine()) != null){

                lineNumber++;

                if(lineNumber == 1 && line.startsWith("\uFEFF")){
                    line = line.substring(1);
                }

                if(line.isBlank()){
                    continue;
                }

                List<String> fields;

                try{
                    fields = parseCsvLine(line);
                }catch(IllegalArgumentException e){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        e.getMessage(),
                        e
                    );
                }

                if(!headerRead){
                    validateHeader(fields, EDGE_HEADER, file, lineNumber);

                    headerRead = true;
                    continue;
                }

                if(fields.size() != 4){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        "Expected 4 fields: id, source, target, weight"
                    );
                }

                String id = requireValue(fields.get(0).trim(), "Edge ID", file, lineNumber);
                String source = requireValue(fields.get(1).trim(), "Edge Source", file, lineNumber);
                String target = requireValue(fields.get(2).trim(), "Edge Target", file, lineNumber);
                String weightValue = requireValue(fields.get(3).trim(), "Edge Weight", file, lineNumber);
                
                double weight;

                try{
                    weight = Double.parseDouble(weightValue);
                }catch(NumberFormatException e){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        "Invalid weight value: " + weightValue,
                        e
                    );
                }

                if(!Double.isFinite(weight) || weight < 0){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        "Weight must be a finite number and non-negative"
                    );
                }

                try{
                    graph.addEdge(id, source, target, weight);
                }catch(IllegalArgumentException e){
                    throw invalidCsv(
                        file,
                        lineNumber,
                        e.getMessage(),
                        e
                    );
                }
            }

            if(!headerRead){
                throw invalidCsv(
                    file,
                    1,
                    "Missing header. Expected: id, source, target, weight"
                );
            }
        }
    }

    private void validateHeader(
        List<String> actual,
        List<String> expected,
        Path file,
        int lineNumber
    ){
        if(!actual.equals(expected)){
            throw invalidCsv(
                file,
                lineNumber,
                "Invalid header. Expected: " + String.join(", ", expected)
            );
        }
    }

    private String requireValue(String value, String fieldName, Path file, int lineNumber){
        if(value.isBlank()){
            throw invalidCsv(
                file,
                lineNumber,
                fieldName + " cannot be empty"
            );
        }
        return value;
    }

    private List<String> parseCsvLine(String line){
        List<String> fields = new ArrayList<>();

        int index = 0;

        while(true){
            while(index < line.length() && isCsvWhitespace(line.charAt(index))){
                index++;
            }

            String value;

            if(index < line.length() && line.charAt(index) == '"'){

                index ++;

                StringBuilder quotedValue = new StringBuilder();
                boolean closed = false;

                while(index < line.length()){
                    char current = line.charAt(index++);

                    if(current == '"'){
                        if(index < line.length() && line.charAt(index) == '"'){
                            quotedValue.append('"');
                            index++;
                        }else{
                            closed = true;
                            break;
                        }
                    }else{
                        quotedValue.append(current);
                    }
                }

                if(!closed){
                    throw new IllegalArgumentException("Unclosed quoted field");
                }

                while(index < line.length() && isCsvWhitespace(line.charAt(index))){
                    index++;
                }

                if(index < line.length() && line.charAt(index) != ','){
                    throw new IllegalArgumentException("Unexpected character after quoted field");
                }
                value = quotedValue.toString();

            }else{
                int start = index;

                while(index < line.length() && line.charAt(index) != ','){
                    
                    if(line.charAt(index) == '"'){
                        throw new IllegalArgumentException("Unexpected quote in unquoted field");
                    }
                    index++;
                }

                value = line.substring(start, index).trim();
            }

            fields.add(value);

            if(index >= line.length()){
                break;
            }

            index++;

            if(index == line.length()){
                fields.add("");
                break;
            }
        }
        
        return fields;
    }

    private boolean isCsvWhitespace(char c){
        return c == ' ' || c == '\t';
    }

    private IllegalArgumentException invalidCsv(Path file, int lineNumber, String message){
        return new IllegalArgumentException(
            "Invalid CSV in file " + file + " at line " + lineNumber + ": " + message
        );
    }

    private IllegalArgumentException invalidCsv(Path file, int lineNumber, String message, Throwable cause){
        return new IllegalArgumentException(
            "Invalid CSV in file " + file + " at line " + lineNumber + ": " + message,
            cause
        );
    }
    
}
