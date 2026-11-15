============================================================
  COMPUMAIB - Unidad 3  |  Modelo ORM + Consultas JPQL + GUI
  Universidad Politécnica Salesiana
============================================================

CÓMO ABRIR EN NETBEANS
----------------------
1. File → Open Project
2. Selecciona la carpeta COMPUMAIB_U3
3. NetBeans detectará que es un proyecto Maven

CONFIGURAR LA BASE DE DATOS
---------------------------
Edita:  src/main/resources/META-INF/persistence.xml

Cambia usuario/contraseña de PostgreSQL si es necesario.
Crea la base si no existe:
  CREATE DATABASE bd_compumaib;

Las tablas se crean/actualizan solas (hbm2ddl.auto = update).

CLASES PRINCIPALES
------------------
  Main.java          →  Interfaz gráfica (Swing)  ← ejecutar esta
  PruebaJPQL.java    →  Prueba de las 3 consultas JPQL por consola

ENTIDADES (6)
-------------
  Cliente, Equipo, Tecnico, OrdenServicio, Repuesto, DetalleOrden

RELACIONES
----------
  Cliente     1 ─── N  OrdenServicio
  Equipo      1 ─── N  OrdenServicio
  Tecnico     1 ─── N  OrdenServicio
  Cliente     1 ─── N  Equipo
  OrdenServicio  N ─── N  Repuesto   (vía DetalleOrden)

CÓMO EJECUTAR
-------------
  Opción A (GUI):     clic derecho en Main.java → Run File
  Opción B (consola): clic derecho en PruebaJPQL.java → Run File

  El botón "Ejecutar consultas JPQL" de la GUI muestra las 3 consultas
  en un cuadro de diálogo.

RECOMENDACIÓN
-------------
1. Ejecuta primero PruebaJPQL (carga datos de ejemplo + muestra consultas).
2. Luego ejecuta Main (interfaz gráfica con esos datos).
============================================================
