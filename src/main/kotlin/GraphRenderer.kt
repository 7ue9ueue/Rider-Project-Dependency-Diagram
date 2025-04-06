package com.example

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.body
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import javax.imageio.ImageIO

/**
 * Converts the given graph into a PlantUML formatted string.
 * Only includes edges where both vertices are enabled.
 */
fun graphToPlantUML(graph: Graph): String {
    val sb = StringBuilder()
    sb.appendLine("@startuml")

    // Enforce a left-to-right direction for the diagram
    sb.appendLine("left to right direction")

    // Make the diagram larger
    // scale <factor> tells PlantUML to enlarge the drawing.
    sb.appendLine("scale 1.5")

    // Increase DPI for higher resolution
    sb.appendLine("skinparam dpi 200")

    // Use orthogonal lines and adjust spacing
    sb.appendLine("skinparam linetype ortho")
    sb.appendLine("skinparam ranksep 30")
    sb.appendLine("skinparam nodesep 20")

    // Customize font (optional)
    sb.appendLine("skinparam defaultFontName Courier")
    sb.appendLine("skinparam defaultFontSize 14")

    // Generate enabled vertices
    for ((id, vertex) in graph.vertices) {
        if (vertex.enabled == true) {
            sb.appendLine("object $id")
        }
    }
    // Generate edges only for enabled vertices
    for (edge in graph.edges) {
        val sourceVertex = graph.vertices[edge.source]
        val targetVertex = graph.vertices[edge.target]
        if (sourceVertex?.enabled == true && targetVertex?.enabled == true) {
            sb.appendLine("${edge.source} --> ${edge.target}")
        }
    }

    sb.appendLine("@enduml")
    return sb.toString()
}



/**
 * Encodes the PlantUML text.
 * It compresses the text with the Deflate algorithm and then encodes it using a custom Base64 scheme.
 */
fun encodePlantUML(text: String): String {
    val bytes = text.toByteArray(Charsets.UTF_8)
    val deflater = Deflater(9, true)
    deflater.setInput(bytes)
    deflater.finish()
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    while (!deflater.finished()) {
        val count = deflater.deflate(buffer)
        output.write(buffer, 0, count)
    }
    deflater.end()
    val compressed = output.toByteArray()
    return encode64(compressed)
}

/**
 * Encodes a byte array into a string using PlantUML's custom Base64 encoding.
 */
fun encode64(data: ByteArray): String {
    val base64 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
    val stringBuffer = StringBuilder()
    var i = 0
    while (i < data.size) {
        if (i + 2 < data.size) {
            val b1 = data[i].toInt() and 0xFF
            val b2 = data[i + 1].toInt() and 0xFF
            val b3 = data[i + 2].toInt() and 0xFF
            stringBuffer.append(base64[b1 shr 2])
            stringBuffer.append(base64[((b1 and 0x3) shl 4) or (b2 shr 4)])
            stringBuffer.append(base64[((b2 and 0xF) shl 2) or (b3 shr 6)])
            stringBuffer.append(base64[b3 and 0x3F])
            i += 3
        } else if (i + 1 < data.size) {
            val b1 = data[i].toInt() and 0xFF
            val b2 = data[i + 1].toInt() and 0xFF
            stringBuffer.append(base64[b1 shr 2])
            stringBuffer.append(base64[((b1 and 0x3) shl 4) or (b2 shr 4)])
            stringBuffer.append(base64[(b2 and 0xF) shl 2])
            i += 2
        } else {
            val b1 = data[i].toInt() and 0xFF
            stringBuffer.append(base64[b1 shr 2])
            stringBuffer.append(base64[(b1 and 0x3) shl 4])
            i += 1
        }
    }
    return stringBuffer.toString()
}

/**
 * Renders the diagram by sending the PlantUML description to a remote PlantUML server.
 * It returns an ImageBitmap generated from the PNG response.
 */
suspend fun renderDiagram(graph: Graph): ImageBitmap {
    // Convert the graph to a PlantUML description.
    val plantUMLText = graphToPlantUML(graph)
    println("Generated PlantUML:\n$plantUMLText")

    // Encode the PlantUML text.
    val encoded = encodePlantUML(plantUMLText)
    val url = "http://www.plantuml.com/plantuml/png/$encoded"

    // Create an HTTP client with the CIO engine.
    val client = HttpClient(CIO)
    try {
        // Fetch the PNG image as a byte array.
        val response = client.get(url)
        val imageBytes: ByteArray = response.body()
        // Convert the byte array into a BufferedImage.
        val inputStream = ByteArrayInputStream(imageBytes)
        val bufferedImage: BufferedImage = ImageIO.read(inputStream)
            ?: error("Failed to decode image from PlantUML server")
        // Convert the BufferedImage to Compose's ImageBitmap.
        return bufferedImage.toComposeImageBitmap()
    } finally {
        client.close()
    }
}
