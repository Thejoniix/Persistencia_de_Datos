COMPUMAIB
=========

Sistema de órdenes de servicio técnico con JPA.

Requisitos
----------
- Java 25
- PostgreSQL
- NetBeans / Maven

Configuración
-------------
Editar: src/main/resources/META-INF/persistence.xml

  jdbc.url      = jdbc:postgresql://localhost:5432/bd_compumaib
  jdbc.user     = postgres
  jdbc.password = 1234

Crear la base de datos:
  CREATE DATABASE bd_compumaib;

Ejecutar
--------
Main.java       → Interfaz gráfica (CRUD)
PruebaJPQL.java → Consultas JPQL + datos de prueba

Entidades
---------
Cliente
Equipo
Tecnico
OrdenServicio
Repuesto
DetalleOrden

Relaciones
----------
Cliente 1 → N OrdenServicio
Equipo  1 → N OrdenServicio
Tecnico 1 → N OrdenServicio
OrdenServicio N ↔ N Repuesto (DetalleOrden)
