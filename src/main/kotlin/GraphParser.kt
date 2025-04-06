package com.example

/**
 * Parses an edge list from a string input.
 * Each line should be in the format "A -> B".
 */
fun parseGraph(input: String): Graph {
    val graph = Graph()
    val regex = Regex("""\s*([\w\d]+)\s*->\s*([\w\d]+)\s*""")

    input.lineSequence().forEach { line ->
        if (line.isNotBlank()) {
            val matchResult = regex.find(line)
            if (matchResult != null) {
                val (source, target) = matchResult.destructured

                // Add source vertex if not present.
                if (!graph.vertices.containsKey(source)) {
                    graph.vertices[source] = Vertex(source)
                }
                // Add target vertex if not present.
                if (!graph.vertices.containsKey(target)) {
                    graph.vertices[target] = Vertex(target)
                }

                // Add the edge.
                graph.edges.add(Edge(source, target))
            } else {
                println("Warning: Could not parse line: \"$line\"")
            }
        }
    }
    return graph
}
