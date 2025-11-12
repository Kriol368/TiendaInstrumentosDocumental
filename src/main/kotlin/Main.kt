package org.example

import java.util.Scanner
import com.mongodb.client.MongoClients



const val NOM_SRV = "mongodb://root:Taller2014@localhost:27017"
const val NOM_BD = "tiendainstrumentos"
const val NOM_COLECCION = "instrumento"

val scanner = Scanner(System.`in`)

fun main() {
    mostrarPlantas()
}

fun mostrarPlantas() {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    val cursor = coleccion.find().iterator()
    cursor.use {
        while (it.hasNext()) {
            val doc = it.next()
            println(doc.toJson())
        }
    }

    cliente.close()
}