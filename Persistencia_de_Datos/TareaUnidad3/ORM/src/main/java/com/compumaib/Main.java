package com.compumaib;

import com.compumaib.modelo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Interfaz gráfica de COMPUMAIB – Órdenes de servicio técnico.
 * Adaptada al modelo ORM de la Unidad 3 (Cliente, Equipo, Técnico, etc.).
 */
public class Main extends JFrame {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("compumaibPU");

    // ----- Campos del formulario -----
    private final JTextField txtClienteCedula = new JTextField();
    private final JTextField txtClienteNombre = new JTextField();
    private final JTextField txtEquipoMarca = new JTextField();
    private final JTextField txtEquipoModelo = new JTextField();
    private final JTextField txtProcesador = new JTextField();
    private final JTextField txtRam = new JTextField();
    private final JTextField txtAlmacenamiento = new JTextField();
    private final JTextField txtFalla = new JTextField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(
            new String[]{"RECIBIDA", "EN_REPARACION", "FINALIZADA", "ENTREGADA"});
    private final JTextField txtCosto = new JTextField();
    private final JComboBox<TecnicoItem> cmbTecnico = new JComboBox<>();

    // ----- Botones -----
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnInsertar = new JButton("Insertar");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnConsultas = new JButton("Ejecutar consultas JPQL");

    // ----- Tabla -----
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Cliente", "Equipo", "Técnico", "Falla", "Estado", "Fecha", "Costo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private Integer idSeleccionado = null;

    public Main() {
        super("COMPUMAIB - Órdenes de servicio técnico (Unidad 3)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Formulario
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Datos de la orden"));

        form.add(new JLabel("Cédula cliente:"));
        form.add(txtClienteCedula);
        form.add(new JLabel("Nombre cliente:"));
        form.add(txtClienteNombre);

        form.add(new JLabel("Marca equipo:"));
        form.add(txtEquipoMarca);
        form.add(new JLabel("Modelo equipo:"));
        form.add(txtEquipoModelo);

        form.add(new JLabel("Procesador:"));
        form.add(txtProcesador);
        form.add(new JLabel("RAM:"));
        form.add(txtRam);

        form.add(new JLabel("Almacenamiento:"));
        form.add(txtAlmacenamiento);
        form.add(new JLabel("Falla / servicio:"));
        form.add(txtFalla);

        form.add(new JLabel("Estado:"));
        form.add(cmbEstado);
        form.add(new JLabel("Costo mano obra ($):"));
        form.add(txtCosto);

        form.add(new JLabel("Técnico:"));
        form.add(cmbTecnico);

        add(form, BorderLayout.NORTH);

        // Tabla
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel();
        botones.add(btnNuevo);
        botones.add(btnInsertar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnConsultas);
        add(botones, BorderLayout.SOUTH);

        // Eventos
        btnNuevo.addActionListener(e -> nuevo());
        btnInsertar.addActionListener(e -> insertar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnConsultas.addActionListener(e -> ejecutarConsultasJPQL());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaEnFormulario();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                emf.close();
            }
        });

        cargarTecnicos();
        cargarTabla();
        nuevo();
    }

    // ---------- Utilidades ----------
    private void transaccion(Consumer<EntityManager> accion) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                accion.accept(em);
                em.getTransaction().commit();
            } catch (RuntimeException ex) {
                em.getTransaction().rollback();
                throw ex;
            }
        }
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }

    private void error(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void modoEdicion(boolean editando) {
        btnInsertar.setEnabled(!editando);
        btnActualizar.setEnabled(editando);
        btnEliminar.setEnabled(editando);
    }

    private Double validarCosto() {
        if (txtClienteCedula.getText().isBlank() || txtClienteNombre.getText().isBlank()
                || txtEquipoMarca.getText().isBlank() || txtEquipoModelo.getText().isBlank()) {
            error("Cédula, nombre del cliente, marca y modelo del equipo son obligatorios.");
            return null;
        }
        try {
            String texto = txtCosto.getText().isBlank() ? "0" : txtCosto.getText().trim();
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            error("El costo debe ser un número (ejemplo: 75.50).");
            return null;
        }
    }

    // ---------- Carga de datos ----------
    private void cargarTecnicos() {
        cmbTecnico.removeAllItems();
        cmbTecnico.addItem(new TecnicoItem(null, "(Sin asignar)"));
        try (EntityManager em = emf.createEntityManager()) {
            List<Tecnico> lista = em.createQuery(
                    "SELECT t FROM Tecnico t WHERE t.activo = true ORDER BY t.nombres", Tecnico.class)
                    .getResultList();
            for (Tecnico t : lista) {
                cmbTecnico.addItem(new TecnicoItem(t.getId(), t.getNombres() + " (" + t.getEspecialidad() + ")"));
            }
        }
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        try (EntityManager em = emf.createEntityManager()) {
            List<OrdenServicio> ordenes = em.createQuery(
                    "SELECT o FROM OrdenServicio o "
                    + "LEFT JOIN FETCH o.cliente "
                    + "LEFT JOIN FETCH o.equipo "
                    + "LEFT JOIN FETCH o.tecnico "
                    + "ORDER BY o.id", OrdenServicio.class)
                    .getResultList();
            for (OrdenServicio o : ordenes) {
                String tecnico = o.getTecnico() != null ? o.getTecnico().getNombres() : "-";
                modelo.addRow(new Object[]{
                    o.getId(),
                    o.getCliente().getNombres(),
                    o.getEquipo().getMarca() + " " + o.getEquipo().getModelo(),
                    tecnico,
                    o.getFallaReportada(),
                    o.getEstado(),
                    o.getFechaIngreso(),
                    o.getCostoManoObra()
                });
            }
        }
    }

    private void cargarFilaEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (Integer) modelo.getValueAt(fila, 0);

        try (EntityManager em = emf.createEntityManager()) {
            OrdenServicio o = em.find(OrdenServicio.class, idSeleccionado);
            if (o == null) {
                return;
            }
            txtClienteCedula.setText(o.getCliente().getCedula());
            txtClienteNombre.setText(o.getCliente().getNombres());
            txtEquipoMarca.setText(o.getEquipo().getMarca());
            txtEquipoModelo.setText(o.getEquipo().getModelo());
            txtProcesador.setText(texto(o.getEquipo().getProcesador()));
            txtRam.setText(texto(o.getEquipo().getRam()));
            txtAlmacenamiento.setText(texto(o.getEquipo().getAlmacenamiento()));
            txtFalla.setText(texto(o.getFallaReportada()));
            cmbEstado.setSelectedItem(o.getEstado());
            txtCosto.setText(o.getCostoManoObra() != null ? o.getCostoManoObra().toString() : "");

            // Seleccionar técnico en el combo
            if (o.getTecnico() != null) {
                for (int i = 0; i < cmbTecnico.getItemCount(); i++) {
                    TecnicoItem item = cmbTecnico.getItemAt(i);
                    if (item.id != null && item.id.equals(o.getTecnico().getId())) {
                        cmbTecnico.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                cmbTecnico.setSelectedIndex(0);
            }
        }
        modoEdicion(true);
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    // ---------- Operaciones CRUD ----------
    private void nuevo() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtClienteCedula.setText("");
        txtClienteNombre.setText("");
        txtEquipoMarca.setText("");
        txtEquipoModelo.setText("");
        txtProcesador.setText("");
        txtRam.setText("");
        txtAlmacenamiento.setText("");
        txtFalla.setText("");
        cmbEstado.setSelectedIndex(0);
        txtCosto.setText("");
        cmbTecnico.setSelectedIndex(0);
        modoEdicion(false);
        txtClienteCedula.requestFocus();
    }

    private void insertar() {
        Double costo = validarCosto();
        if (costo == null) {
            return;
        }
        try {
            transaccion(em -> {
                // Buscar o crear Cliente
                Cliente cliente = buscarOCrearCliente(em,
                        txtClienteCedula.getText().trim(),
                        txtClienteNombre.getText().trim());

                // Crear Equipo
                Equipo equipo = new Equipo(
                        txtEquipoMarca.getText().trim(),
                        txtEquipoModelo.getText().trim(),
                        null,
                        txtProcesador.getText().trim(),
                        txtRam.getText().trim(),
                        txtAlmacenamiento.getText().trim(),
                        "Laptop",
                        cliente);
                em.persist(equipo);

                // Técnico (opcional)
                Tecnico tecnico = null;
                TecnicoItem item = (TecnicoItem) cmbTecnico.getSelectedItem();
                if (item != null && item.id != null) {
                    tecnico = em.find(Tecnico.class, item.id);
                }

                OrdenServicio orden = new OrdenServicio(
                        cliente, equipo, tecnico,
                        txtFalla.getText().trim(),
                        (String) cmbEstado.getSelectedItem(),
                        costo);
                em.persist(orden);
            });
            cargarTabla();
            nuevo();
            mensaje("¡Orden guardada exitosamente!");
        } catch (RuntimeException e) {
            error("No se pudo guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizar() {
        if (idSeleccionado == null) {
            error("Seleccione una orden de la tabla.");
            return;
        }
        Double costo = validarCosto();
        if (costo == null) {
            return;
        }
        try {
            transaccion(em -> {
                OrdenServicio o = em.find(OrdenServicio.class, idSeleccionado);
                if (o == null) {
                    throw new RuntimeException("Orden no encontrada");
                }

                // Actualizar cliente
                o.getCliente().setCedula(txtClienteCedula.getText().trim());
                o.getCliente().setNombres(txtClienteNombre.getText().trim());

                // Actualizar equipo
                o.getEquipo().setMarca(txtEquipoMarca.getText().trim());
                o.getEquipo().setModelo(txtEquipoModelo.getText().trim());
                o.getEquipo().setProcesador(txtProcesador.getText().trim());
                o.getEquipo().setRam(txtRam.getText().trim());
                o.getEquipo().setAlmacenamiento(txtAlmacenamiento.getText().trim());

                o.setFallaReportada(txtFalla.getText().trim());
                o.setEstado((String) cmbEstado.getSelectedItem());
                o.setCostoManoObra(costo);

                TecnicoItem item = (TecnicoItem) cmbTecnico.getSelectedItem();
                if (item != null && item.id != null) {
                    o.setTecnico(em.find(Tecnico.class, item.id));
                } else {
                    o.setTecnico(null);
                }
            });
            cargarTabla();
            nuevo();
            mensaje("¡Orden actualizada!");
        } catch (RuntimeException e) {
            error("No se pudo actualizar: " + e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            error("Seleccione una orden de la tabla.");
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la orden #" + idSeleccionado + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            transaccion(em -> {
                OrdenServicio o = em.find(OrdenServicio.class, idSeleccionado);
                if (o != null) {
                    em.remove(o);
                }
            });
            cargarTabla();
            nuevo();
            mensaje("Orden eliminada.");
        } catch (RuntimeException e) {
            error("No se pudo eliminar: " + e.getMessage());
        }
    }

    /** Busca cliente por cédula; si no existe lo crea. */
    private Cliente buscarOCrearCliente(EntityManager em, String cedula, String nombres) {
        List<Cliente> lista = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.cedula = :ced", Cliente.class)
                .setParameter("ced", cedula)
                .getResultList();
        if (!lista.isEmpty()) {
            Cliente c = lista.get(0);
            c.setNombres(nombres); // actualiza nombre por si cambió
            return c;
        }
        Cliente nuevo = new Cliente(cedula, nombres, null, null, null);
        em.persist(nuevo);
        return nuevo;
    }

    // ---------- Consultas JPQL desde la GUI ----------
    private void ejecutarConsultasJPQL() {
        try (EntityManager em = emf.createEntityManager()) {
            StringBuilder sb = new StringBuilder();

            // Consulta 1
            sb.append("=== CONSULTA 1: Órdenes EN_REPARACION o FINALIZADA (paginadas) ===\n");
            List<OrdenServicio> c1 = em.createQuery(
                    "SELECT o FROM OrdenServicio o JOIN o.cliente c "
                    + "WHERE o.estado IN :estados ORDER BY o.fechaIngreso DESC",
                    OrdenServicio.class)
                    .setParameter("estados", List.of("EN_REPARACION", "FINALIZADA"))
                    .setFirstResult(0)
                    .setMaxResults(5)
                    .getResultList();
            for (OrdenServicio o : c1) {
                sb.append(String.format("  #%d | %s | %s %s | %s | $%.2f%n",
                        o.getId(), o.getCliente().getNombres(),
                        o.getEquipo().getMarca(), o.getEquipo().getModelo(),
                        o.getEstado(), o.getCostoManoObra()));
            }

            // Consulta 2
            sb.append("\n=== CONSULTA 2: Órdenes con costo > $20 ordenadas por costo ===\n");
            List<OrdenServicio> c2 = em.createQuery(
                    "SELECT o FROM OrdenServicio o WHERE o.costoManoObra > :min "
                    + "ORDER BY o.costoManoObra DESC",
                    OrdenServicio.class)
                    .setParameter("min", 20.0)
                    .getResultList();
            for (OrdenServicio o : c2) {
                String tec = o.getTecnico() != null ? o.getTecnico().getNombres() : "-";
                sb.append(String.format("  #%d | Técnico: %s | $%.2f | %s%n",
                        o.getId(), tec, o.getCostoManoObra(), o.getEstado()));
            }

            // Consulta 3
            sb.append("\n=== CONSULTA 3: Ranking de repuestos más usados ===\n");
            List<Object[]> c3 = em.createQuery(
                    "SELECT r.nombre, SUM(d.cantidad) FROM DetalleOrden d "
                    + "JOIN d.repuesto r GROUP BY r.id, r.nombre ORDER BY SUM(d.cantidad) DESC",
                    Object[].class)
                    .setMaxResults(10)
                    .getResultList();
            if (c3.isEmpty()) {
                sb.append("  (sin repuestos registrados aún – ejecuta PruebaJPQL para cargar datos de ejemplo)\n");
            } else {
                int pos = 1;
                for (Object[] fila : c3) {
                    sb.append(String.format("  #%d  %-25s → %s unidades%n",
                            pos++, fila[0], fila[1]));
                }
            }

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Resultados JPQL", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            error("Error en consultas: " + e.getMessage());
        }
    }

    // ---------- Item del combo de técnicos ----------
    private static class TecnicoItem {
        final Integer id;
        final String etiqueta;

        TecnicoItem(Integer id, String etiqueta) {
            this.id = id;
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        // Asegura que existan técnicos de ejemplo si la BD está vacía
        asegurarDatosBase();

        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    /** Crea técnicos de ejemplo si no hay ninguno (para que el combo no esté vacío). */
    private static void asegurarDatosBase() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("compumaibPU");
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery("SELECT COUNT(t) FROM Tecnico t", Long.class).getSingleResult();
            if (count == 0) {
                em.getTransaction().begin();
                em.persist(new Tecnico("1700112233", "Carlos Ruiz", "Hardware / Laptops", "0998877665"));
                em.persist(new Tecnico("1711223344", "María López", "Software / Redes", "0987766554"));
                em.getTransaction().commit();
                System.out.println("Técnicos de ejemplo creados.");
            }
        } finally {
            emf.close();
        }
    }
}
