package com.example

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun GraphVisualizerScreen() {
    // State for the graph input text.
    var graphInput by remember { mutableStateOf("A -> B\nB -> C\nC -> A\nD -> E") }

    // Parse the graph from input.
    val parsedGraph = remember(graphInput) { parseGraph(graphInput) }

    // Create a mutable state map to hold each vertex's enabled state.
    // This state is independent of the parsed graph, so changes persist even if the graph is re-parsed.
    val vertexStates = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize vertexStates for new vertices if they don't exist.
    parsedGraph.vertices.keys.forEach { id ->
        if (id !in vertexStates) {
            vertexStates[id] = parsedGraph.vertices[id]?.enabled ?: true
        }
    }

    // Create a derived state snapshot so that changes to vertexStates trigger recomposition.
    val vertexStateSnapshot by remember { derivedStateOf { vertexStates.toMap() } }

    // Update each vertex in the parsed graph with the current state.
    parsedGraph.vertices.forEach { (id, vertex) ->
        vertex.enabled = vertexStateSnapshot[id] ?: true
    }

    // State for the generated diagram image.
    var diagramImage by remember { mutableStateOf<ImageBitmap?>(null) }

    // Launch a coroutine to generate the diagram whenever the parsed graph or vertex states change.
    LaunchedEffect(parsedGraph, vertexStateSnapshot) {
        diagramImage = renderDiagram(parsedGraph)
    }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Left panel: Graph input and vertex list.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text("Graph Input", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = graphInput,
                onValueChange = { graphInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Vertices", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(parsedGraph.vertices.values.toList()) { vertex ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Button(
                            onClick = {
                                // Toggle the vertex's state in our state map.
                                vertexStates[vertex.id] = !(vertexStates[vertex.id] ?: true)
                            },
                            colors = ButtonDefaults.buttonColors(
                                // Change background color based on the vertex's current state.
                                backgroundColor = if (vertexStates[vertex.id] == true) {
                                    MaterialTheme.colors.primary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colors.error.copy(alpha = 0.2f)
                                }
                            )
                        ) {
                            Text(
                                text = "${vertex.id} " + if (vertexStates[vertex.id] == true) "(Enabled)" else "(Disabled)"
                            )
                        }
                    }
                }
            }
        }
        // Right panel: Diagram display area.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (diagramImage != null) {
                Image(bitmap = diagramImage!!, contentDescription = "Graph Diagram")
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
