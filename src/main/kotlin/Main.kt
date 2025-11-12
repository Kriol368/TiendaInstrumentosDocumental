package org.example

import java.util.Scanner
import com.mongodb.client.MongoClients
import org.bson.json.JsonWriterSettings


const val NOM_SRV = "mongodb://root:Taller2014@localhost:27017"
const val NOM_BD = "tiendainstrumentos"
const val NOM_COLECCION = "instrumento"

val scanner = Scanner(System.`in`)

fun main() {
    mostrarInstrumentos()
}

fun mostrarInstrumentos() {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    val cursor = coleccion.find().iterator()
    cursor.use {
        println("=".repeat(80))
        println("%-4s %-20s %-15s %-6s %-10s".format(
            "ID", "NOMBRE", "FABRICANTE", "AÑO", "PRECIO"
        ))
        println("=".repeat(80))

        while (it.hasNext()) {
            val doc = it.next()
            println("%-4s %-20s %-15s %-6s %-10s".format(
                doc.get("id_instrumento").toString(),
                doc.getString("nombre_instrumento"),
                doc.getString("fabricante"),
                doc.get("ano_fabricacion").toString(),
                "${doc.get("precio")}€"
            ))
        }
        println("=".repeat(80))
    }
    cliente.close()
}