package com.compumaib;

import com.compumaib.modelo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Clase de prueba para la Unidad 3.
 * Demuestra el modelo ORM ampliado y las 3 consultas JPQL requeridas
 * (filtros + ordenamiento + paginación).
 */
public class PruebaJPQL {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("compumaibPU");
        EntityManager em = emf.createEntityManager();

        try {
            // 1. Insertar datos de ejemplo
            cargarDatosPrueba(em);

            // 2. Ejecutar las 3 consultas JPQL de la tarea
            consulta1_OrdenesPorClienteYEstado(em);
            consulta2_OrdenesPorTecnicoYCosto(em);
            consulta3_RepuestosMasUsados(em);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

    // =====================================================================
    // DATOS DE PRUEBA
    // =====================================================================
    static void cargarDatosPrueba(EntityManager em) {
        System.out.println("=== CARGANDO DATOS DE PRUEBA ===\n");

        em.getTransaction().begin();

        // --- Clientes ---
        Cliente c1 = new Cliente("1712345678", "Ana Pérez", "0991112233", "ana@mail.com", "Av. 6 de Diciembre");
        Cliente c2 = new Cliente("0987654321", "Luis Mendoza", "0987654321", "luis@mail.com", "Calle Amazonas");
        em.persist(c1);
        em.persist(c2);

        // --- Técnicos ---
        Tecnico t1 = new Tecnico("1700112233", "Carlos Ruiz", "Hardware / Laptops", "0998877665");
        Tecnico t2 = new Tecnico("1711223344", "María López", "Software / Redes", "0987766554");
        em.persist(t1);
        em.persist(t2);

        // --- Equipos ---
        Equipo e1 = new Equipo("Asus", "ROG G15", "SN-ASUS-001", "AMD Ryzen 7", "16GB", "SSD 500GB", "Laptop", c1);
        Equipo e2 = new Equipo("HP", "Pavilion 15", "SN-HP-002", "Intel Core i7", "16GB", "SSD 512GB", "Laptop", c2);
        Equipo e3 = new Equipo("Lenovo", "IdeaPad 3", "SN-LEN-003", "Intel Core i5", "8GB", "SSD 256GB", "Laptop", c1);
        em.persist(e1);
        em.persist(e2);
        em.persist(e3);

        // --- Repuestos ---
        Repuesto r1 = new Repuesto("RAM-16", "Memoria RAM DDR4 16GB", "Kingston", 45.00, 20);
        Repuesto r2 = new Repuesto("SSD-500", "Disco SSD 500GB", "Samsung", 55.00, 15);
        Repuesto r3 = new Repuesto("BAT-HP", "Batería HP Pavilion", "HP Original", 38.00, 8);
        Repuesto r4 = new Repuesto("PASTA", "Pasta térmica 5g", "Arctic", 8.50, 50);
        em.persist(r1);
        em.persist(r2);
        em.persist(r3);
        em.persist(r4);

        // --- Órdenes ---
        OrdenServicio o1 = new OrdenServicio(c1, e1, t1, "Ampliar RAM y cambiar disco", "EN_REPARACION", 25.00);
        o1.agregarRepuesto(r1, 1, 45.00);
        o1.agregarRepuesto(r2, 1, 55.00);
        em.persist(o1);

        OrdenServicio o2 = new OrdenServicio(c2, e2, t1, "La batería no carga", "RECIBIDA", 15.00);
        o2.agregarRepuesto(r3, 1, 38.00);
        em.persist(o2);

        OrdenServicio o3 = new OrdenServicio(c1, e3, t2, "Sobrecalentamiento", "FINALIZADA", 20.00);
        o3.agregarRepuesto(r4, 1, 8.50);
        o3.setFechaEntrega(LocalDate.now().minusDays(2));
        em.persist(o3);

        OrdenServicio o4 = new OrdenServicio(c1, e1, t1, "Limpieza general + pasta térmica", "FINALIZADA", 30.00);
        o4.agregarRepuesto(r4, 1, 8.50);
        o4.setFechaEntrega(LocalDate.now().minusDays(5));
        em.persist(o4);

        OrdenServicio o5 = new OrdenServicio(c2, e2, t2, "Reinstalación de Windows + drivers", "EN_REPARACION", 40.00);
        em.persist(o5);

        em.getTransaction().commit();
        System.out.println("Datos de prueba guardados correctamente.\n");
    }

    // =====================================================================
    // CONSULTA 1 – Órdenes de un cliente filtradas por estado + PAGINACIÓN
    // =====================================================================
    static void consulta1_OrdenesPorClienteYEstado(EntityManager em) {
        System.out.println("========================================================");
        System.out.println("CONSULTA 1: Órdenes de un cliente por estado (paginada)");
        System.out.println("========================================================");

        String jpql = """
            SELECT o FROM OrdenServicio o
            JOIN o.cliente c
            WHERE c.cedula = :cedula
              AND o.estado IN :estados
            ORDER BY o.fechaIngreso DESC
            """;

        List<OrdenServicio> resultados = em.createQuery(jpql, OrdenServicio.class)
                .setParameter("cedula", "1712345678")
                .setParameter("estados", Arrays.asList("EN_REPARACION", "FINALIZADA"))
                .setFirstResult(0)      // página 0
                .setMaxResults(5)       // tamaño de página
                .getResultList();

        if (resultados.isEmpty()) {
            System.out.println("  (sin resultados)");
        } else {
            for (OrdenServicio o : resultados) {
                System.out.printf("  ID=%d | %s | %s %s | Estado=%s | Costo=$%.2f%n",
                        o.getId(),
                        o.getCliente().getNombres(),
                        o.getEquipo().getMarca(), o.getEquipo().getModelo(),
                        o.getEstado(),
                        o.getCostoManoObra());
            }
        }
        System.out.println();
    }

    // =====================================================================
    // CONSULTA 2 – Órdenes de un técnico con costo > umbral + ORDENAMIENTO
    // =====================================================================
    static void consulta2_OrdenesPorTecnicoYCosto(EntityManager em) {
        System.out.println("========================================================");
        System.out.println("CONSULTA 2: Órdenes de un técnico con costo > $20");
        System.out.println("========================================================");

        // Primero obtenemos el id del técnico Carlos Ruiz
        Tecnico tecnico = em.createQuery(
                "SELECT t FROM Tecnico t WHERE t.cedula = :ced", Tecnico.class)
                .setParameter("ced", "1700112233")
                .getSingleResult();

        String jpql = """
            SELECT o FROM OrdenServicio o
            JOIN o.tecnico t
            WHERE t.id = :idTecnico
              AND o.costoManoObra > :minCosto
            ORDER BY o.costoManoObra DESC
            """;

        List<OrdenServicio> resultados = em.createQuery(jpql, OrdenServicio.class)
                .setParameter("idTecnico", tecnico.getId())
                .setParameter("minCosto", 20.0)
                .getResultList();

        if (resultados.isEmpty()) {
            System.out.println("  (sin resultados)");
        } else {
            for (OrdenServicio o : resultados) {
                System.out.printf("  ID=%d | Técnico=%s | Cliente=%s | Costo=$%.2f | Estado=%s%n",
                        o.getId(),
                        o.getTecnico().getNombres(),
                        o.getCliente().getNombres(),
                        o.getCostoManoObra(),
                        o.getEstado());
            }
        }
        System.out.println();
    }

    // =====================================================================
    // CONSULTA 3 – Ranking de repuestos más usados + PAGINACIÓN
    // =====================================================================
    static void consulta3_RepuestosMasUsados(EntityManager em) {
        System.out.println("========================================================");
        System.out.println("CONSULTA 3: Ranking de repuestos más usados (paginado)");
        System.out.println("========================================================");

        String jpql = """
            SELECT r.nombre, SUM(d.cantidad)
            FROM DetalleOrden d
            JOIN d.repuesto r
            JOIN d.orden o
            WHERE o.fechaIngreso BETWEEN :desde AND :hasta
            GROUP BY r.id, r.nombre
            ORDER BY SUM(d.cantidad) DESC
            """;

        List<Object[]> resultados = em.createQuery(jpql, Object[].class)
                .setParameter("desde", LocalDate.of(2024, 1, 1))
                .setParameter("hasta", LocalDate.of(2026, 12, 31))
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();

        if (resultados.isEmpty()) {
            System.out.println("  (sin resultados)");
        } else {
            int pos = 1;
            for (Object[] fila : resultados) {
                System.out.printf("  #%d  %-30s  →  %d unidades%n",
                        pos++, fila[0], ((Number) fila[1]).intValue());
            }
        }
        System.out.println();
    }
}
