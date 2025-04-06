package com.example

import kotlin.test.*

class GraphLogicTests {

    @Test
    fun testParseGraph_basicParsing() {
        val input = """
            A -> B
            B -> C
            D -> E
        """.trimIndent()

        val graph = parseGraph(input)

        assertEquals(5, graph.vertices.size)
        assertTrue(graph.vertices.containsKey("A"))
        assertTrue(graph.vertices.containsKey("E"))
        assertEquals(3, graph.edges.size)

        val expectedEdges = listOf(
            Edge("A", "B"),
            Edge("B", "C"),
            Edge("D", "E")
        )

        assertEquals(expectedEdges, graph.edges)
    }

    @Test
    fun testParseGraph_ignoresInvalidLines() {
        val input = """
            A -> B
            just text
            ->
            B -> C
        """.trimIndent()

        val graph = parseGraph(input)

        assertEquals(3, graph.vertices.size)
        assertEquals(2, graph.edges.size)
    }

    @Test
    fun testGraphToPlantUML_structureIncludesEdgesAndObjects() {
        val graph = Graph(
            vertices = mutableMapOf(
                "A" to Vertex("A", enabled = true),
                "B" to Vertex("B", enabled = true),
                "C" to Vertex("C", enabled = true)
            ),
            edges = mutableListOf(
                Edge("A", "B"),
                Edge("B", "C")
            )
        )

        val uml = graphToPlantUML(graph)

        assertTrue("A --> B" in uml)
        assertTrue("B --> C" in uml)
        assertTrue("object A" in uml)
        assertTrue("object B" in uml)
        assertTrue("object C" in uml)
    }

    @Test
    fun testGraphToPlantUML_disabledSourceSuppressesEdge() {
        val graph = Graph(
            vertices = mutableMapOf(
                "A" to Vertex("A", enabled = false),
                "B" to Vertex("B", enabled = true)
            ),
            edges = mutableListOf(
                Edge("A", "B")
            )
        )

        val uml = graphToPlantUML(graph)

        assertFalse("A --> B" in uml, "Edge should not be included when source is disabled")
        assertTrue("object B" in uml, "Target vertex B should still appear")
    }

    @Test
    fun testGraphToPlantUML_isolatedEnabledVertexIsRendered() {
        val graph = Graph(
            vertices = mutableMapOf(
                "X" to Vertex("X", enabled = true)
            )
        )

        val uml = graphToPlantUML(graph)

        assertTrue("object X" in uml, "Isolated enabled vertex should be included")
    }

    @Test
    fun testGraphToPlantUML_isolatedDisabledVertexLabeledCorrectly() {
        val graph = Graph(
            vertices = mutableMapOf(
                "Z" to Vertex("Z", enabled = false)
            )
        )

        val uml = graphToPlantUML(graph)

        assertTrue(
            "object \"Z (Disabled)\" as Z <<disabled>>" in uml,
            "Disabled vertex should still appear with label"
        )
    }

    @Test
    fun testGraphToPlantUML_noDuplicates() {
        val graph = Graph(
            vertices = mutableMapOf(
                "A" to Vertex("A", enabled = true),
                "B" to Vertex("B", enabled = true)
            ),
            edges = mutableListOf(
                Edge("A", "B")
            )
        )

        val uml = graphToPlantUML(graph)

        // Should not declare object A or B more than once
        val countA = uml.lines().count { it.contains("object A") }
        val countB = uml.lines().count { it.contains("object B") }

        assertEquals(1, countA, "object A should be declared only once")
        assertEquals(1, countB, "object B should be declared only once")
    }
}
