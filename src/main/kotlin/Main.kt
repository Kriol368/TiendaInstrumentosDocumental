package org.example

import com.mongodb.client.MongoClients
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import org.bson.Document
import java.util.*

const val NOM_SRV = "mongodb://root:Taller2014@localhost:27017"
const val NOM_BD = "tiendainstrumentos"
const val NOM_COLECCION = "instrumento"

val scanner = Scanner(System.`in`)

fun main() {
    menu()
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
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    val cursor = coleccion.find().iterator()
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
                    doc.get("id_instrumento").toString(),
                    doc.getString("nombre_instrumento"),
                    doc.getString("fabricante"),
                    doc.get("ano_fabricacion").toString(),
                    "${doc.get("precio")}€"
                )
            )
        }
        println("=".repeat(80))
    }
    cliente.close()
}

fun insertarInstrumento() {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

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

    coleccion.insertOne(doc)
    println("Instrumento insertado con ID: $idInstrumento")

    cliente.close()
}

fun actualizarPrecio() {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    var idInstrumento: Int? = null
    while (idInstrumento == null) {
        print("ID del instrumento a actualizar: ")
        val entrada = scanner.nextLine()
        idInstrumento = entrada.toIntOrNull()
        if (idInstrumento == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val instrumento = coleccion.find(Filters.eq("id_instrumento", idInstrumento)).firstOrNull()
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

        val result = coleccion.updateOne(
            Filters.eq("id_instrumento", idInstrumento), Document($$"$set", Document("precio", precio))
        )

        if (result.modifiedCount > 0) println("Precio actualizado correctamente (${result.modifiedCount} documento modificado).")
        else println("No se modificó ningún documento (el precio quizá ya era el mismo).")
    }

    cliente.close()
}

fun eliminarInstrumento() {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    var idInstrumento: Int? = null
    while (idInstrumento == null) {
        print("ID del instrumento a eliminar: ")
        val entrada = scanner.nextLine()
        idInstrumento = entrada.toIntOrNull()
        if (idInstrumento == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val result = coleccion.deleteOne(Filters.eq("id_instrumento", idInstrumento))
    if (result.deletedCount > 0) println("Instrumento eliminado correctamente.")
    else println("No se encontró ningún instrumento con ese ID.")

    cliente.close()
}

fun variasOperaciones() {
    val client = MongoClients.create(NOM_SRV)
    val col = client.getDatabase(NOM_BD).getCollection(NOM_COLECCION)

    println("\n" + "=".repeat(60))
    println("INSTRUMENTOS CON PRECIO MAYOR A 300€")
    println("=".repeat(60))
    println("%-4s %-20s %-15s %-6s %-10s".format("ID", "NOMBRE", "FABRICANTE", "AÑO", "PRECIO"))
    println("-".repeat(60))

    col.find(Filters.gt("precio", 300)).forEach { doc ->
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

    col.find().projection(Projections.include("nombre_instrumento")).forEach { doc ->
        println("%-25s".format(doc.getString("nombre_instrumento")))
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

    val aggCursor = col.aggregate(pipeline).iterator()
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

    client.close()
}