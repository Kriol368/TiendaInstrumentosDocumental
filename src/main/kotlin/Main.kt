import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
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
lateinit var coleccionCategorias: MongoCollection<Document>
lateinit var coleccionProveedores: MongoCollection<Document>

const val NOM_BD = "tiendainstrumentos"
const val NOM_COLECCION_INSTRUMENTOS = "instrumento"
const val NOM_COLECCION_CATEGORIAS = "categoria"
const val NOM_COLECCION_PROVEEDORES = "proveedor"

val scanner = Scanner(System.`in`)

fun conectarBD() {
    servidor = MongoServer(MemoryBackend())
    val address = servidor.bind()
    uri = "mongodb://${address.hostName}:${address.port}"

    cliente = MongoClients.create(uri)
    val bd = cliente.getDatabase(NOM_BD)
    coleccionInstrumentos = bd.getCollection(NOM_COLECCION_INSTRUMENTOS)
    coleccionCategorias = bd.getCollection(NOM_COLECCION_CATEGORIAS)
    coleccionProveedores = bd.getCollection(NOM_COLECCION_PROVEEDORES)

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
    importarBD("src/main/resources/categorias.json", coleccionCategorias)
    importarBD("src/main/resources/proveedores.json", coleccionProveedores)

    menu()

    exportarBD(coleccionInstrumentos, "src/main/resources/instrumentos.json")
    exportarBD(coleccionCategorias, "src/main/resources/categorias.json")
    exportarBD(coleccionProveedores, "src/main/resources/proveedores.json")
    desconectarBD()
}

fun menu() {
    var opcion: Int
    do {
        println("=".repeat(50))
        println("          GESTIÓN DE TIENDA DE INSTRUMENTOS")
        println("=".repeat(50))
        println("1. Gestión de Instrumentos")
        println("2. Gestión de Categorías")
        println("3. Gestión de Proveedores")
        println("4. Consultas especiales")
        println("5. Instrumentos con categoría")
        println("6. Instrumentos con proveedor")
        println("7. Instrumentos con proveedor y categoria")
        println("8. Salir")
        println("=".repeat(50))

        opcion = try {
            scanner.nextLine().toInt()
        } catch (_: NumberFormatException) {
            -1
        }

        when (opcion) {
            1 -> menuInstrumentos()
            2 -> menuCategorias()
            3 -> menuProveedores()
            4 -> variasOperaciones()
            5 -> instrumentosConCategoria()
            6 -> instrumentosConProveedor()
            7 -> instrumentosConProveedorYCategoria()
            8 -> println("¡Hasta pronto!")
            else -> println("Opción no válida. Intente nuevamente.")
        }
    } while (opcion != 8)
}

fun menuInstrumentos() {
    var opcion: Int
    do {
        println("=".repeat(50))
        println("          GESTIÓN DE INSTRUMENTOS")
        println("=".repeat(50))
        println("1. Listar todos los instrumentos")
        println("2. Insertar nuevo instrumento")
        println("3. Actualizar precio de instrumento")
        println("4. Eliminar instrumento por ID")
        println("5. Volver al menú principal")
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
            5 -> println("Volviendo al menú principal...")
            else -> println("Opción no válida. Intente nuevamente.")
        }
    } while (opcion != 5)
}

fun listarInstrumentos() {
    val cursor = coleccionInstrumentos.find().iterator()
    cursor.use {
        println("=".repeat(120))
        println(
            "%-4s %-20s %-15s %-6s %-10s %-15s %-15s".format(
                "ID", "NOMBRE", "FABRICANTE", "AÑO", "PRECIO", "ID CATEGORÍA", "ID PROVEEDOR"
            )
        )
        println("=".repeat(120))

        while (it.hasNext()) {
            val doc = it.next()
            val idCategoria = doc.getInteger("id_categoria") ?: "N/A"
            val idProveedor = doc.getInteger("id_proveedor") ?: "N/A"
            println(
                "%-4s %-20s %-15s %-6s %-10s %-15s %-15s".format(
                    doc["id_instrumento"].toString(),
                    doc.getString("nombre_instrumento"),
                    doc.getString("fabricante"),
                    doc["ano_fabricacion"].toString(),
                    "${doc["precio"]}€",
                    idCategoria.toString(),
                    idProveedor.toString()
                )
            )
        }
        println("=".repeat(120))
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

    var idCategoria: Int?
    println("ID de categoría: ")
    val entradaCategoria = scanner.nextLine()
    idCategoria = if (entradaCategoria.isBlank()) null else entradaCategoria.toIntOrNull()

    var idProveedor: Int?
    println("ID de proveedor: ")
    val entradaProveedor = scanner.nextLine()
    idProveedor = if (entradaProveedor.isBlank()) null else entradaProveedor.toIntOrNull()

    val doc = Document("id_instrumento", idInstrumento).append("nombre_instrumento", nombreInstrumento)
        .append("fabricante", fabricante).append("ano_fabricacion", anoFabricacion).append("precio", precio)

    if (idCategoria != null) {
        doc.append("id_categoria", idCategoria)
    }

    if (idProveedor != null) {
        doc.append("id_proveedor", idProveedor)
    }

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
        println("Instrumento encontrado: ${instrumento.getString("nombre_instrumento")} (precio: ${instrumento["precio"]}€)")

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
            Filters.eq("id_instrumento", idInstrumento), Updates.set("precio", precio)
        )

        if (result.modifiedCount > 0) println("Precio actualizado correctamente.")
        else println("No se modificó ningún documento.")
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

fun menuCategorias() {
    var opcion: Int
    do {
        println("=".repeat(50))
        println("          GESTIÓN DE CATEGORÍAS")
        println("=".repeat(50))
        println("1. Listar todas las categorías")
        println("2. Insertar nueva categoría")
        println("3. Actualizar descripción de categoría")
        println("4. Eliminar categoría por ID")
        println("5. Volver al menú principal")
        println("=".repeat(50))

        opcion = try {
            scanner.nextLine().toInt()
        } catch (_: NumberFormatException) {
            -1
        }

        when (opcion) {
            1 -> listarCategorias()
            2 -> insertarCategoria()
            3 -> actualizarDescripcionCategoria()
            4 -> eliminarCategoria()
            5 -> println("Volviendo al menú principal...")
            else -> println("Opción no válida. Intente nuevamente.")
        }
    } while (opcion != 5)
}

fun listarCategorias() {
    val cursor = coleccionCategorias.find().iterator()
    cursor.use {
        println("=".repeat(80))
        println(
            "%-4s %-20s %-40s".format(
                "ID", "NOMBRE", "DESCRIPCIÓN"
            )
        )
        println("=".repeat(80))

        while (it.hasNext()) {
            val doc = it.next()
            println(
                "%-4s %-20s %-40s".format(
                    doc["id_categoria"].toString(), doc.getString("nombre"), doc.getString("descripcion")
                )
            )
        }
        println("=".repeat(80))
    }
}

fun insertarCategoria() {
    var idCategoria: Int? = null
    while (idCategoria == null) {
        print("ID de la categoría: ")
        val entrada = scanner.nextLine()
        idCategoria = entrada.toIntOrNull()
        if (idCategoria == null) {
            println("El ID debe ser un número !!!")
        }
    }

    print("Nombre de la categoría: ")
    val nombre = scanner.nextLine()

    print("Descripción: ")
    val descripcion = scanner.nextLine()

    val doc = Document("id_categoria", idCategoria).append("nombre", nombre).append("descripcion", descripcion)

    coleccionCategorias.insertOne(doc)
    println("Categoría insertada con ID: $idCategoria")
}

fun actualizarDescripcionCategoria() {
    var idCategoria: Int? = null
    while (idCategoria == null) {
        print("ID de la categoría a actualizar: ")
        val entrada = scanner.nextLine()
        idCategoria = entrada.toIntOrNull()
        if (idCategoria == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val categoria = coleccionCategorias.find(Filters.eq("id_categoria", idCategoria)).firstOrNull()
    if (categoria == null) {
        println("No se encontró ninguna categoría con id_categoria = \"$idCategoria\".")
    } else {
        println("Categoría encontrada: ${categoria.getString("nombre")} (descripción: ${categoria.getString("descripcion")})")

        print("Nueva descripción: ")
        val nuevaDescripcion = scanner.nextLine()

        val result = coleccionCategorias.updateOne(
            Filters.eq("id_categoria", idCategoria), Updates.set("descripcion", nuevaDescripcion)
        )

        if (result.modifiedCount > 0) println("Descripción actualizada correctamente.")
        else println("No se modificó ningún documento.")
    }
}

fun eliminarCategoria() {
    var idCategoria: Int? = null
    while (idCategoria == null) {
        print("ID de la categoría a eliminar: ")
        val entrada = scanner.nextLine()
        idCategoria = entrada.toIntOrNull()
        if (idCategoria == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val result = coleccionCategorias.deleteOne(Filters.eq("id_categoria", idCategoria))
    if (result.deletedCount > 0) println("Categoría eliminada correctamente.")
    else println("No se encontró ninguna categoría con ese ID.")
}

fun menuProveedores() {
    var opcion: Int
    do {
        println("=".repeat(50))
        println("          GESTIÓN DE PROVEEDORES")
        println("=".repeat(50))
        println("1. Listar todos los proveedores")
        println("2. Insertar nuevo proveedor")
        println("3. Actualizar email de proveedor")
        println("4. Eliminar proveedor por ID")
        println("5. Volver al menú principal")
        println("=".repeat(50))

        opcion = try {
            scanner.nextLine().toInt()
        } catch (_: NumberFormatException) {
            -1
        }

        when (opcion) {
            1 -> listarProveedores()
            2 -> insertarProveedor()
            3 -> actualizarEmailProveedor()
            4 -> eliminarProveedor()
            5 -> println("Volviendo al menú principal...")
            else -> println("Opción no válida. Intente nuevamente.")
        }
    } while (opcion != 5)
}

fun listarProveedores() {
    val cursor = coleccionProveedores.find().iterator()
    cursor.use {
        println("=".repeat(120))
        println(
            "%-4s %-30s %-15s %-30s %-40s".format(
                "ID", "NOMBRE", "TELÉFONO", "EMAIL", "DIRECCIÓN"
            )
        )
        println("=".repeat(120))

        while (it.hasNext()) {
            val doc = it.next()
            println(
                "%-4s %-30s %-15s %-30s %-40s".format(
                    doc["id_proveedor"].toString(),
                    doc.getString("nombre"),
                    doc.getString("telefono"),
                    doc.getString("email"),
                    doc.getString("direccion")
                )
            )
        }
        println("=".repeat(120))
    }
}

fun insertarProveedor() {
    var idProveedor: Int? = null
    while (idProveedor == null) {
        print("ID del proveedor: ")
        val entrada = scanner.nextLine()
        idProveedor = entrada.toIntOrNull()
        if (idProveedor == null) {
            println("El ID debe ser un número !!!")
        }
    }

    print("Nombre del proveedor: ")
    val nombre = scanner.nextLine()

    print("Teléfono: ")
    val telefono = scanner.nextLine()

    print("Email: ")
    val email = scanner.nextLine()

    print("Dirección: ")
    val direccion = scanner.nextLine()

    val doc = Document("id_proveedor", idProveedor).append("nombre", nombre).append("telefono", telefono)
        .append("email", email).append("direccion", direccion)

    coleccionProveedores.insertOne(doc)
    println("Proveedor insertado con ID: $idProveedor")
}

fun actualizarEmailProveedor() {
    var idProveedor: Int? = null
    while (idProveedor == null) {
        print("ID del proveedor a actualizar: ")
        val entrada = scanner.nextLine()
        idProveedor = entrada.toIntOrNull()
        if (idProveedor == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val proveedor = coleccionProveedores.find(Filters.eq("id_proveedor", idProveedor)).firstOrNull()
    if (proveedor == null) {
        println("No se encontró ningún proveedor con id_proveedor = \"$idProveedor\".")
    } else {
        println("Proveedor encontrado: ${proveedor.getString("nombre")} (email: ${proveedor.getString("email")})")

        print("Nuevo email: ")
        val nuevoEmail = scanner.nextLine()

        val result = coleccionProveedores.updateOne(
            Filters.eq("id_proveedor", idProveedor), Updates.set("email", nuevoEmail)
        )

        if (result.modifiedCount > 0) println("Email actualizado correctamente.")
        else println("No se modificó ningún documento.")
    }
}

fun eliminarProveedor() {
    var idProveedor: Int? = null
    while (idProveedor == null) {
        print("ID del proveedor a eliminar: ")
        val entrada = scanner.nextLine()
        idProveedor = entrada.toIntOrNull()
        if (idProveedor == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val result = coleccionProveedores.deleteOne(Filters.eq("id_proveedor", idProveedor))
    if (result.deletedCount > 0) println("Proveedor eliminado correctamente.")
    else println("No se encontró ningún proveedor con ese ID.")
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
                doc["id_instrumento"].toString(),
                doc.getString("nombre_instrumento"),
                doc.getString("fabricante"),
                doc["ano_fabricacion"].toString(),
                "${doc["precio"]}€"
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
                    doc["id_instrumento"].toString(), doc.getString("nombre_instrumento"), doc.getString("fabricante")
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

fun instrumentosConCategoria() {
    println("\n" + "=".repeat(120))
    println("INSTRUMENTOS CON CATEGORÍA")
    println("=".repeat(120))
    println("%-4s %-20s %-15s %-10s %-20s %-25s".format(
        "ID", "INSTRUMENTO", "FABRICANTE", "PRECIO", "CATEGORÍA", "DESCRIPCIÓN"
    ))
    println("=".repeat(120))

    val pipeline = listOf(
        Document(
            $$"$lookup", Document()
            .append("from", "categoria")
            .append("localField", "id_categoria")
            .append("foreignField", "id_categoria")
            .append("as", "categoria_info")
        ),
        Document(
            $$"$unwind", Document()
            .append("path", $$"$categoria_info")
            .append("preserveNullAndEmptyArrays", true)
        ),
        Document(
            $$"$project", Document()
            .append("id_instrumento", 1)
            .append("nombre_instrumento", 1)
            .append("fabricante", 1)
            .append("precio", 1)
            .append("categoria_nombre", $$"$categoria_info.nombre")
            .append("categoria_descripcion", $$"$categoria_info.descripcion")
        )
    )

    try {
        val cursor = coleccionInstrumentos.aggregate(pipeline).iterator()
        cursor.use {
            while (it.hasNext()) {
                val doc = it.next()
                val categoriaNombre = doc.getString("categoria_nombre") ?: "Sin categoría"
                val categoriaDesc = doc.getString("categoria_descripcion") ?: "N/A"

                println(
                    "%-4s %-20s %-15s %-10s %-20s %-25s".format(
                        doc["id_instrumento"].toString(),
                        doc.getString("nombre_instrumento"),
                        doc.getString("fabricante"),
                        "${doc["precio"]}€",
                        categoriaNombre,
                        categoriaDesc
                    )
                )
            }
        }
    } catch (e: Exception) {
        println("Error en la consulta: ${e.message}")
    }

    println("=".repeat(120))
}

fun instrumentosConProveedor() {
    println("\n" + "=".repeat(150))
    println("INSTRUMENTOS CON PROVEEDOR")
    println("=".repeat(150))
    println("%-4s %-20s %-15s %-10s %-30s %-15s %-25s".format(
        "ID", "INSTRUMENTO", "FABRICANTE", "PRECIO", "PROVEEDOR", "TELÉFONO", "EMAIL"
    ))
    println("=".repeat(150))

    val pipeline = listOf(
        Document(
            $$"$lookup", Document()
            .append("from", "proveedor")
            .append("localField", "id_proveedor")
            .append("foreignField", "id_proveedor")
            .append("as", "proveedor_info")
        ),
        Document(
            $$"$unwind", Document()
            .append("path", $$"$proveedor_info")
            .append("preserveNullAndEmptyArrays", true)
        ),
        Document(
            $$"$project", Document()
            .append("id_instrumento", 1)
            .append("nombre_instrumento", 1)
            .append("fabricante", 1)
            .append("precio", 1)
            .append("proveedor_nombre", $$"$proveedor_info.nombre")
            .append("proveedor_telefono", $$"$proveedor_info.telefono")
            .append("proveedor_email", $$"$proveedor_info.email")
        )
    )

    try {
        val cursor = coleccionInstrumentos.aggregate(pipeline).iterator()
        cursor.use {
            while (it.hasNext()) {
                val doc = it.next()
                val proveedorNombre = doc.getString("proveedor_nombre") ?: "Sin proveedor"
                val proveedorTelefono = doc.getString("proveedor_telefono") ?: "N/A"
                val proveedorEmail = doc.getString("proveedor_email") ?: "N/A"

                println(
                    "%-4s %-20s %-15s %-10s %-30s %-15s %-25s".format(
                        doc["id_instrumento"].toString(),
                        doc.getString("nombre_instrumento"),
                        doc.getString("fabricante"),
                        "${doc["precio"]}€",
                        proveedorNombre,
                        proveedorTelefono,
                        proveedorEmail
                    )
                )
            }
        }
    } catch (e: Exception) {
        println("Error en la consulta: ${e.message}")
    }

    println("=".repeat(150))
}

fun instrumentosConProveedorYCategoria() {
    println("\n" + "=".repeat(180))
    println("INSTRUMENTOS CON PROVEEDOR Y CATEGORÍA")
    println("=".repeat(180))
    println("%-4s %-20s %-15s %-10s %-20s %-30s %-20s %-20s".format(
        "ID", "INSTRUMENTO", "FABRICANTE", "PRECIO", "CATEGORÍA", "PROVEEDOR", "TELÉFONO", "EMAIL"
    ))
    println("=".repeat(180))

    val pipeline = listOf(
        Document(
            $$"$lookup", Document()
            .append("from", "categoria")
            .append("localField", "id_categoria")
            .append("foreignField", "id_categoria")
            .append("as", "categoria_info")
        ),
        Document(
            $$"$unwind", Document()
            .append("path", $$"$categoria_info")
            .append("preserveNullAndEmptyArrays", true)
        ),
        Document(
            $$"$lookup", Document()
            .append("from", "proveedor")
            .append("localField", "id_proveedor")
            .append("foreignField", "id_proveedor")
            .append("as", "proveedor_info")
        ),
        Document(
            $$"$unwind", Document()
            .append("path", $$"$proveedor_info")
            .append("preserveNullAndEmptyArrays", true)
        ),
        Document(
            $$"$project", Document()
            .append("id_instrumento", 1)
            .append("nombre_instrumento", 1)
            .append("fabricante", 1)
            .append("precio", 1)
            .append("categoria_nombre", $$"$categoria_info.nombre")
            .append("proveedor_nombre", $$"$proveedor_info.nombre")
            .append("proveedor_telefono", $$"$proveedor_info.telefono")
            .append("proveedor_email", $$"$proveedor_info.email")
        )
    )

    try {
        val cursor = coleccionInstrumentos.aggregate(pipeline).iterator()
        cursor.use {
            while (it.hasNext()) {
                val doc = it.next()
                val categoriaNombre = doc.getString("categoria_nombre") ?: "Sin categoría"
                val proveedorNombre = doc.getString("proveedor_nombre") ?: "Sin proveedor"
                val proveedorTelefono = doc.getString("proveedor_telefono") ?: "N/A"
                val proveedorEmail = doc.getString("proveedor_email") ?: "N/A"

                println(
                    "%-4s %-20s %-15s %-10s %-20s %-30s %-20s %-20s".format(
                        doc["id_instrumento"].toString(),
                        doc.getString("nombre_instrumento"),
                        doc.getString("fabricante"),
                        "${doc["precio"]}€",
                        categoriaNombre,
                        proveedorNombre,
                        proveedorTelefono,
                        proveedorEmail
                    )
                )
            }
        }
    } catch (e: Exception) {
        println("Error en la consulta: ${e.message}")
    }

    println("=".repeat(180))
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

    val nombreColeccion = when (coleccion) {
        coleccionInstrumentos -> "instrumentos"
        coleccionCategorias -> "categorías"
        coleccionProveedores -> "proveedores"
        else -> "datos"
    }
    println("Exportación de $nombreColeccion completada")
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
        val nombreColeccion = when (coleccion) {
            coleccionInstrumentos -> "instrumentos"
            coleccionCategorias -> "categorías"
            coleccionProveedores -> "proveedores"
            else -> "datos"
        }
        println("Importación de $nombreColeccion completada: ${documentos.size} documentos.")
    } catch (e: Exception) {
        println("Error importando documentos: ${e.message}")
    }
}