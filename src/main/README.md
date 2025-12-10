# Tienda de Instrumentos

## Descripción general

Aplicación Kotlin para la gestión de una tienda de instrumentos musicales que utiliza MongoDB en memoria. El sistema permite gestionar tres colecciones principales: instrumentos, categorías y proveedores, con operaciones CRUD completas y consultas combinadas entre las colecciones.

## Requisitos

- **JDK 11 o superior**
- **Gradle** o capacidad para ejecutar proyectos Kotlin
- **Dependencias** (incluidas en build.gradle):
    - MongoDB Java Driver
    - MongoDB Embedded para base de datos en memoria
    - json.org para manejo de JSON

## Base de datos

El sistema utiliza MongoDB en memoria con tres colecciones:

### 1. **instrumentos**
Campos: `id_instrumento`, `nombre_instrumento`, `fabricante`, `ano_fabricacion`, `precio`, `id_categoria`, `id_proveedor`

### 2. **categorias**
Campos: `id_categoria`, `nombre`, `descripcion`

### 3. **proveedores**
Campos: `id_proveedor`, `nombre`, `telefono`, `email`, `direccion`

**Relaciones:**
- Un instrumento puede tener una categoría (relación con `id_categoria`)
- Un instrumento puede tener un proveedor (relación con `id_proveedor`)

## Cómo ejecutar

1. Clonar o descargar el proyecto
2. Asegurarse de tener las dependencias en `build.gradle`
3. Ejecutar la clase principal que contiene la función `main()`
4. Los archivos JSON deben estar en `src/main/resources/`:
    - `instrumentos.json`
    - `categorias.json`
    - `proveedores.json`

## Opciones del programa y ejemplos de uso

### Menú Principal
```
==================================================
          GESTIÓN DE TIENDA DE INSTRUMENTOS
==================================================
1. Gestión de Instrumentos
2. Gestión de Categorías
3. Gestión de Proveedores
4. Consultas especiales
5. Instrumentos con categoría
6. Instrumentos con proveedor
7. Consulta completa (3 colecciones)
8. Salir
==================================================
```

### Ejemplo 1: Gestión de Instrumentos
**Seleccionar opción 1 → luego opción 1 (Listar):**
```
==================================================
          GESTIÓN DE INSTRUMENTOS
==================================================
1. Listar todos los instrumentos
2. Insertar nuevo instrumento
3. Actualizar precio de instrumento
4. Eliminar instrumento por ID
5. Volver al menú principal
==================================================

Opción: 1

ID   NOMBRE                FABRICANTE      AÑO    PRECIO      ID CATEGORÍA  ID PROVEEDOR
========================================================================================
1    Guitarra Eléctrica    Fender          2021   44.33€      1             1
2    Batería Acústica      Pearl           2018   2350€       3             2
```

### Ejemplo 2: Consultas Combinadas
**Seleccionar opción 7 (Consulta completa):**
```
INSTRUMENTOS CON CATEGORÍA Y PROVEEDOR (3 COLECCIONES)
======================================================================================
ID   INSTRUMENTO          FABRICANTE      PRECIO    CATEGORÍA        PROVEEDOR
1    Guitarra Eléctrica   Fender          44.33€    Cuerda           Music Suppliers S.A.
2    Batería Acústica     Pearl           2350€     Percusión        Instrumentos Profesionales SL
```

### Ejemplo 3: Insertar Nuevo Proveedor
**Seleccionar opción 3 → luego opción 2:**
```
==================================================
          GESTIÓN DE PROVEEDORES
==================================================
1. Listar todos los proveedores
2. Insertar nuevo proveedor
3. Actualizar email de proveedor
4. Eliminar proveedor por ID
5. Volver al menú principal
==================================================

Opción: 2

ID del proveedor: 4
Nombre del proveedor: Sonido Premium
Teléfono: 666555444
Email: contacto@sonidopremium.com
Dirección: Calle Melodía 99, Sevilla
Proveedor insertado con ID: 4
```

## Notas importantes

1. **Importación/Exportación**:
    - Al iniciar, importa datos desde los archivos JSON en `src/main/resources/`
    - Al salir, exporta los datos actualizados a los mismos archivos JSON
2. **Relaciones opcionales**: Los instrumentos pueden no tener categoría ni proveedor asignados
3. **IDs únicos**: Cada colección requiere IDs únicos para sus elementos