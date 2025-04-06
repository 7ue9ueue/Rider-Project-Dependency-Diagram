package com.example

data class Vertex(
    val id: String,
    var enabled: Boolean = true
)

data class Edge(
    val source: String,
    val target: String
)

data class Graph (
    val vertices: MutableMap<String, Vertex> = mutableMapOf(),
    val edges: MutableList<Edge> = mutableListOf()
)

//fun main() {
//    val input = """
//        A -> B
//        B -> C
//        C -> A
//        D -> E
//    """.trimIndent()
//
//    val graph = parseGraph(input)
//    println("Vertices:")
//    graph.vertices.values.forEach { println(it) }
//    println("Edges:")
//    graph.edges.forEach { println(it) }
//}
