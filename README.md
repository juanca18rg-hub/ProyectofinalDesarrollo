# Cards4Play API

## Descripción del proyecto

Cards4Play es una API REST orientada a la gestión de una tienda de cartas coleccionables, accesorios, productos sellados, boosters y torneos competitivos.

El proyecto aplica los principios de programación orientada a objetos, arquitectura por capas, integración de servicios externos y persistencia de información mediante archivos JSON.

## Tecnologías utilizadas

- Java 17
- Maven
- Spark Java 2.9.4
- Gson 2.10.1
- SLF4J
- Docker
- Railway
- Postman
- Git y GitHub

## Funcionalidades principales

- Autenticación mediante tokens.
- Gestión de administradores y clientes.
- Registro y consulta de productos.
- Conversión de precios USD a COP mediante una API externa.
- Generación, compra y apertura de boosters.
- Registro e historial de compras.
- Creación de torneos e inscripción de participantes.
- Validación de cupos e inscripciones duplicadas.
- Persistencia de información después del reinicio de la aplicación.

## Arquitectura del proyecto

- `src/model`: entidades, enumeraciones y reglas del negocio.
- `src/service`: lógica de aplicación.
- `src/repository`: almacenamiento y recuperación de información.
- `src/api`: controladores y endpoints REST.
- `src/util`: utilidades compartidas.
- `data`: archivos JSON persistentes.

## Endpoints de la API

| Método | Endpoint | Función |
|---|---|---|
| GET | /health | Verificar disponibilidad |
| POST | /auth/login | Iniciar sesión |
| POST | /clientes | Registrar cliente |
| GET | /clientes/:id | Consultar cliente |
| POST | /productos/cartas | Registrar carta |
| POST | /productos/accesorios | Registrar accesorio |
| POST | /productos/sellados | Registrar producto sellado |
| GET | /productos | Consultar productos |
| POST | /boosters | Crear booster |
| GET | /boosters/disponibles | Consultar boosters disponibles |
| POST | /boosters/:id/abrir | Abrir booster |
| POST | /compras | Registrar compra |
| GET | /clientes/:id/compras | Consultar historial |
| POST | /torneos | Crear torneo |
| GET | /torneos | Consultar torneos |
| POST | /torneos/:id/inscripciones | Inscribir participante |

## Persistencia de datos

El sistema almacena su información mediante cuatro archivos JSON:

- usuarios.json
- productos.json
- compras.json
- torneos.json

Los repositorios permiten recuperar los datos después del reinicio de la aplicación y utilizan archivos temporales para realizar escrituras mediante reemplazo atómico.

En Railway se utiliza un volumen persistente de 500 MB montado en `/app/data`.

## Seguridad

- Autenticación mediante tokens.
- Control de acceso según el rol del usuario.
- Contraseñas protegidas mediante PBKDF2 y sales aleatorias.
- Credenciales configuradas mediante variables de entorno.
- Validación de propiedad de compras y boosters.

Los tokens de autenticación se invalidan al reiniciar la aplicación.

## Ejecución local

Requisitos: Java 17 o superior y Maven.

Compilar el proyecto:

```bash
mvn clean package
mvn dependency:copy-dependencies
```

Ejecutar en Windows:

```powershell
java -cp "target/classes;target/dependency/*" api.Main
```

Ejecutar en Linux o macOS:

```bash
java -cp "target/classes:target/dependency/*" api.Main
```

URL local:

http://localhost:4567

## Variables de entorno

| Variable | Descripción |
|---|---|
| PORT | Puerto de la aplicación |
| TASA_DEFECTO | Tasa de conversión alternativa |
| CARDS4PLAY_ADMIN_PASSWORD | Contraseña inicial del administrador |

## Despliegue en producción

El proyecto se encuentra desplegado mediante Docker en Railway.

**URL pública:**

https://cards4play-api-production-ae03e318.up.railway.app

**Verificación de disponibilidad:**

https://cards4play-api-production-ae03e318.up.railway.app/health

## Pruebas realizadas

Se realizaron pruebas funcionales mediante Postman tanto en el entorno local como en producción.

Se verificaron la autenticación, el registro de usuarios, los productos, la conversión de moneda, las compras, la apertura de boosters, los torneos, las restricciones del negocio y la persistencia de información después del reinicio del servidor.

## Proyecto académico

Proyecto final de desarrollo de software.

Institución Universitaria EAM.

Cards4Play — 2026.