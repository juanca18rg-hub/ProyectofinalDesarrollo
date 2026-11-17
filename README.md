# Cards4Play API

Base Maven para desarrollar el backend del proyecto final en Java 17, siguiendo la estructura sencilla del taller de Tienda (`src/model`, `src/service`, etc.).

## Estructura inicial

- `src/model`: entidades, enumeraciones y reglas del dominio.
- `src/service`: lógica de aplicación que iremos implementando.
- `src/repository`: persistencia JSON en archivos.
- `src/api`: rutas REST con Spark.
- `src/util`: utilidades compartidas.
- `data`: archivos JSON persistentes; no se deben subir datos sensibles.

## Dependencias

Maven descarga Spark Java 2.9.4 y Gson 2.10.1. Java 17 es la versión de compilación configurada. En VS Code, abre esta carpeta como proyecto Java/Maven y carga el proyecto Maven cuando aparezca la notificación.

## Nota de diseño

El archivo del proyecto final pide persistencia JSON. El taller de Tienda sirve como guía para Maven, Spark, Gson y Docker, pero su persistencia de texto separado por comas no se debe copiar para Cards4Play.

La API, los repositorios JSON, la autenticación HTTP y Docker se implementarán después de comprender y completar este modelo.
