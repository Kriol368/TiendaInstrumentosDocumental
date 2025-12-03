import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import org.bson.Document
import org.bson.json.JsonWriterSettings
import org.json.JSONArray
import java.io.File
import java.util.*

lateinit var servidor: MongoServer
lateinit var cliente: MongoClient
lateinit var uri: String
lateinit var coleccionInstrumentos: MongoCollection<Document>

const val NOM_BD = "tiendainstrumentos"
const val NOM_COLECCION = "instrumento"

val scanner = Scanner(System.`in`)

fun conectarBD() {
    servidor = MongoServer(MemoryBackend())
    val address = servidor.bind()
    uri = "mongodb://${address.hostName}:${address.port}"

    cliente = MongoClients.create(uri)
    coleccionInstrumentos = cliente.getDatabase(NOM_BD).getCollection(NOM_COLECCION)

    println("Servidor MongoDB en memoria iniciado en $uri")
}

fun desconectarBD() {
    cliente.close()
    servidor.shutdown()
    println("Servidor MongoDB en memoria finalizado")
}

fun main() {
    conectarBD()
    importarBD("src/main/resources/instrumentos.json", coleccionInstrumentos)

    menu()

    exportarBD(coleccionInstrumentos, "src/main/resources/instrumentos.json")
    desconectarBD()
}

fun menu() {
    var opcion: Int
    do {
        println("=".repeat(50))
        println("          GESTIÓN DE INSTRUMENTOS MUSICALES")
        println("=".repeat(50))
        println("1. Listar todos los instrumentos")
        println("2. Insertar nuevo instrumento")
        println("3. Actualizar precio de instrumento")
        println("4. Eliminar instrumento por ID")
        println("5. Consultas especiales")
        println("6. Salir")
        println("=".repeat(50))

        opcion = try {
            scanner.nextLine().toInt()
        } catch (_: NumberFormatException) {
            -1
        }

        when (opcion) {
            1 -> listarInstrumentos()
            2 -> insertarInstrumento()
            3 -> actualizarPrecio()
            4 -> eliminarInstrumento()
            5 -> variasOperaciones()
            6 -> println("¡Hasta pronto!")
            else -> println("Opción no válida. Intente nuevamente.")
        }
    } while (opcion != 6)
}

fun listarInstrumentos() {
    val cursor = coleccionInstrumentos.find().iterator()
    cursor.use {
        println("=".repeat(80))
        println(
            "%-4s %-20s %-15s %-6s %-10s".format(
                "ID", "NOMBRE", "FABRICANTE", "AÑO", "PRECIO"
            )
        )
        println("=".repeat(80))

        while (it.hasNext()) {
            val doc = it.next()
            println(
                "%-4s %-20s %-15s %-6s %-10s".format(
                    doc["id_instrumento"].toString(),
                    doc.getString("nombre_instrumento"),
                    doc.getString("fabricante"),
                    doc.get("ano_fabricacion").toString(),
                    "${doc.get("precio")}€"
                )
            )
        }
        println("=".repeat(80))
    }
}

fun insertarInstrumento() {
    var idInstrumento: Int? = null
    while (idInstrumento == null) {
        print("ID del instrumento: ")
        val entrada = scanner.nextLine()
        idInstrumento = entrada.toIntOrNull()
        if (idInstrumento == null) {
            println("El ID debe ser un número !!!")
        }
    }

    print("Nombre del instrumento: ")
    val nombreInstrumento = scanner.nextLine()

    print("Fabricante: ")
    val fabricante = scanner.nextLine()

    var anoFabricacion: Int? = null
    while (anoFabricacion == null) {
        print("Año de fabricación: ")
        val entrada = scanner.nextLine()
        anoFabricacion = entrada.toIntOrNull()
        if (anoFabricacion == null) {
            println("El año debe ser un número !!!")
        }
    }

    var precio: Double? = null
    while (precio == null) {
        print("Precio: ")
        val entrada = scanner.nextLine()
        precio = entrada.toDoubleOrNull()
        if (precio == null) {
            println("El precio debe ser un número !!!")
        }
    }

    val doc = Document("id_instrumento", idInstrumento).append("nombre_instrumento", nombreInstrumento)
        .append("fabricante", fabricante).append("ano_fabricacion", anoFabricacion).append("precio", precio)

    coleccionInstrumentos.insertOne(doc)
    println("Instrumento insertado con ID: $idInstrumento")
}

fun actualizarPrecio() {
    var idInstrumento: Int? = null
    while (idInstrumento == null) {
        print("ID del instrumento a actualizar: ")
        val entrada = scanner.nextLine()
        idInstrumento = entrada.toIntOrNull()
        if (idInstrumento == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val instrumento = coleccionInstrumentos.find(Filters.eq("id_instrumento", idInstrumento)).firstOrNull()
    if (instrumento == null) {
        println("No se encontró ningún instrumento con id_instrumento = \"$idInstrumento\".")
    } else {
        println("Instrumento encontrado: ${instrumento.getString("nombre_instrumento")} (precio: ${instrumento.get("precio")}€)")

        var precio: Double? = null
        while (precio == null) {
            print("Nuevo precio: ")
            val entrada = scanner.nextLine()
            precio = entrada.toDoubleOrNull()
            if (precio == null) {
                println("El precio debe ser un número !!!")
            }
        }

        val result = coleccionInstrumentos.updateOne(
            Filters.eq("id_instrumento", idInstrumento), Document($$"$set", Document("precio", precio))
        )

        if (result.modifiedCount > 0) println("Precio actualizado correctamente (${result.modifiedCount} documento modificado).")
        else println("No se modificó ningún documento (el precio quizá ya era el mismo).")
    }
}

fun eliminarInstrumento() {
    var idInstrumento: Int? = null
    while (idInstrumento == null) {
        print("ID del instrumento a eliminar: ")
        val entrada = scanner.nextLine()
        idInstrumento = entrada.toIntOrNull()
        if (idInstrumento == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val result = coleccionInstrumentos.deleteOne(Filters.eq("id_instrumento", idInstrumento))
    if (result.deletedCount > 0) println("Instrumento eliminado correctamente.")
    else println("No se encontró ningún instrumento con ese ID.")
}

fun variasOperaciones() {
    println("\n" + "=".repeat(60))
    println("INSTRUMENTOS CON PRECIO MAYOR A 300€")
    println("=".repeat(60))
    println("%-4s %-20s %-15s %-6s %-10s".format("ID", "NOMBRE", "FABRICANTE", "AÑO", "PRECIO"))
    println("-".repeat(60))
    coleccionInstrumentos.find(Filters.gt("precio", 300)).forEach { doc ->
        println(
            "%-4s %-20s %-15s %-6s %-10s".format(
                doc.get("id_instrumento").toString(),
                doc.getString("nombre_instrumento"),
                doc.getString("fabricante"),
                doc.get("ano_fabricacion").toString(),
                "${doc.get("precio")}€"
            )
        )
    }

    println("\n" + "=".repeat(40))
    println("SOLO NOMBRES DE INSTRUMENTOS")
    println("=".repeat(40))
    println("%-25s".format("NOMBRE DEL INSTRUMENTO"))
    println("-".repeat(25))
    coleccionInstrumentos.find().projection(Projections.include("nombre_instrumento")).forEach { doc ->
        println("%-25s".format(doc.getString("nombre_instrumento")))
    }

    println("\n" + "=".repeat(50))
    println("PRIMEROS 3 INSTRUMENTOS")
    println("=".repeat(50))
    println("%-4s %-20s %-15s".format("ID", "NOMBRE", "FABRICANTE"))
    println("-".repeat(40))
    val pipelineLimit = listOf(
        Document($$"$limit", 3)
    )
    val aggCursorLimit = coleccionInstrumentos.aggregate(pipelineLimit).iterator()
    aggCursorLimit.use {
        while (it.hasNext()) {
            val doc = it.next()
            println(
                "%-4s %-20s %-15s".format(
                    doc.get("id_instrumento").toString(),
                    doc.getString("nombre_instrumento"),
                    doc.getString("fabricante")
                )
            )
        }
    }

    println("\n" + "=".repeat(50))
    println("ESTADÍSTICAS DE PRECIOS")
    println("=".repeat(50))

    val pipeline = listOf(
        Document(
            $$"$group",
            Document("_id", null).append("precioPromedio", Document($$"$avg", $$"$precio"))
                .append("precioMaximo", Document($$"$max", $$"$precio"))
                .append("precioMinimo", Document($$"$min", $$"$precio"))
                .append("totalInstrumentos", Document($$"$sum", 1))
        )
    )

    val aggCursor = coleccionInstrumentos.aggregate(pipeline).iterator()
    aggCursor.use {
        while (it.hasNext()) {
            val resultado = it.next()

            fun obtenerDoubleValor(clave: String): Double {
                return when (val valor = resultado[clave]) {
                    is Double -> valor
                    is Int -> valor.toDouble()
                    is Number -> valor.toDouble()
                    else -> 0.0
                }
            }

            val promedio = obtenerDoubleValor("precioPromedio")
            val maximo = obtenerDoubleValor("precioMaximo")
            val minimo = obtenerDoubleValor("precioMinimo")
            val total = resultado.getInteger("totalInstrumentos") ?: 0

            println("Precio promedio: ${"%.2f".format(promedio)}€")
            println("Precio más alto: ${"%.2f".format(maximo)}€")
            println("Precio más bajo: ${"%.2f".format(minimo)}€")
            println("Total instrumentos: $total")
        }
    }

    println("=".repeat(50))
}

fun exportarBD(coleccion: MongoCollection<Document>, rutaJSON: String) {
    val settings = JsonWriterSettings.builder().indent(true).build()
    val file = File(rutaJSON)

    file.printWriter().use { out ->
        out.println("[")
        val cursor = coleccion.find().iterator()
        var first = true
        while (cursor.hasNext()) {
            if (!first) out.println(",")
            val doc = cursor.next()
            out.print(doc.toJson(settings))
            first = false
        }
        out.println("]")
        cursor.close()
    }

    println("Exportación de instrumentos completada")
}

fun importarBD(rutaJSON: String, coleccion: MongoCollection<Document>) {
    println("Iniciando importación de datos desde JSON...")

    val jsonFile = File(rutaJSON)
    if (!jsonFile.exists()) {
        println("No se encontró el archivo JSON a importar")
        return
    }

    val jsonText = try {
        jsonFile.readText()
    } catch (e: Exception) {
        println("Error leyendo el archivo JSON: ${e.message}")
        return
    }

    val array = try {
        JSONArray(jsonText)
    } catch (e: Exception) {
        println("Error al parsear JSON: ${e.message}")
        return
    }

    val documentos = mutableListOf<Document>()
    for (i in 0 until array.length()) {
        val doc = Document.parse(array.getJSONObject(i).toString())
        doc.remove("_id")
        documentos.add(doc)
    }

    if (documentos.isEmpty()) {
        println("El archivo JSON está vacío")
        return
    }

    try {
        coleccion.insertMany(documentos)
        println("Importación completada: ${documentos.size} documentos.")
    } catch (e: Exception) {
        println("Error importando documentos: ${e.message}")
    }
}