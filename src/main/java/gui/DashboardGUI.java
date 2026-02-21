package gui;

import hardware.Procesador;
import hardware.RelojSistema;
import interrupciones.InyectorEventos;
import modelos.ProcessControlBlock;
import motor.GestorMemoria;
import planificacion.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Interfaz Gráfica Principal (Dashboard & Mission Control).
 */
public class DashboardGUI {

    // Atributos Ticket 5.1
    private JFrame ventanaPrincipal;
    private JLabel lblRelojGlobal;
    private JLabel lblProcesoCPU;
    private JLabel lblIndicadorModo;
    private JTable tablaListos, tablaBloqueados, tablaListosSuspendidos, tablaBloqueadosSuspendidos;
    private DefaultTableModel modeloListos, modeloBloqueados, modeloListosSuspendidos, modeloBloqueadosSuspendidos;

    // Atributos Ticket 5.2
    private JButton btnGenerarMasivo;
    private JButton btnEmergencia;
    private JSpinner spinDuracionCiclo;
    private JComboBox<String> comboPoliticas;
    private JTextArea txtLogEventos;
    
    //Nuevo atributo para la telemetría
    private PanelTelemetria panelTelemetria;

    private final GestorMemoria gestorMemoria;
    private final Procesador cpu;
    private final RelojSistema reloj;

    public DashboardGUI(GestorMemoria memoria, Procesador cpu, RelojSistema reloj) {
        this.gestorMemoria = memoria;
        this.cpu = cpu;
        this.reloj = reloj;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        ventanaPrincipal = new JFrame("UNIMET-Sat RTOS Simulator - Mission Control");
        ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventanaPrincipal.setSize(1200, 800);
        ventanaPrincipal.setLayout(new BorderLayout(10, 10));

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        lblRelojGlobal = new JLabel("MISSION CLOCK: Cycle 0");
        lblRelojGlobal.setFont(new Font("Monospaced", Font.BOLD, 24));
        lblIndicadorModo = new JLabel("Modo: PROGRAMA USUARIO");
        lblIndicadorModo.setForeground(Color.GREEN);
        lblIndicadorModo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelTop.add(lblRelojGlobal);
        panelTop.add(lblIndicadorModo);
        ventanaPrincipal.add(panelTop, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(2, 2, 10, 10));
        String[] columnas = {"ID", "Nombre", "Estado", "PC", "MAR", "Prioridad", "Deadline"};        
        modeloListos = new DefaultTableModel(columnas, 0);
        modeloBloqueados = new DefaultTableModel(columnas, 0);
        modeloListosSuspendidos = new DefaultTableModel(columnas, 0);
        modeloBloqueadosSuspendidos = new DefaultTableModel(columnas, 0);

        tablaListos = new JTable(modeloListos);
        tablaBloqueados = new JTable(modeloBloqueados);
        tablaListosSuspendidos = new JTable(modeloListosSuspendidos);
        tablaBloqueadosSuspendidos = new JTable(modeloBloqueadosSuspendidos);

        panelCentro.add(crearPanelTabla("Ready Queue (RAM)", tablaListos));
        panelCentro.add(crearPanelTabla("Blocked Queue (RAM)", tablaBloqueados));
        panelCentro.add(crearPanelTabla("Ready-Suspended (Disk)", tablaListosSuspendidos));
        panelCentro.add(crearPanelTabla("Blocked-Suspended (Disk)", tablaBloqueadosSuspendidos));
        ventanaPrincipal.add(panelCentro, BorderLayout.CENTER);

        JPanel panelCPU = new JPanel(new BorderLayout());
        panelCPU.setBorder(BorderFactory.createTitledBorder("RUNNING PROCESS (CPU)"));
        lblProcesoCPU = new JLabel("IDLE", SwingConstants.CENTER);
        lblProcesoCPU.setFont(new Font("Monospaced", Font.BOLD, 20));
        panelCPU.add(lblProcesoCPU, BorderLayout.CENTER);
        panelCPU.setPreferredSize(new Dimension(300, 0));
        ventanaPrincipal.add(panelCPU, BorderLayout.EAST);
        // Inicializar el panel de telemetría y colocarlo a la izquierda (WEST)
        panelTelemetria = new PanelTelemetria();
        panelTelemetria.setPreferredSize(new Dimension(300, 0)); // Darle un ancho fijo
        ventanaPrincipal.add(panelTelemetria, BorderLayout.WEST);
       

        JPanel panelBottom = new JPanel(new BorderLayout());
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnGenerarMasivo = new JButton("Generar 20 Procesos Aleatorios");
        btnEmergencia = new JButton("EMERGENCIA (MICRO-METEORITO)");
        btnEmergencia.setBackground(Color.RED);
        btnEmergencia.setForeground(Color.WHITE);
        
        spinDuracionCiclo = new JSpinner(new SpinnerNumberModel(1000, 10, 5000, 100));
        JButton btnAplicarVelocidad = new JButton("Aplicar Vel. (ms)");
        btnAplicarVelocidad.addActionListener(e -> {
            String valor = spinDuracionCiclo.getValue().toString();
            if (validarEntradaCiclo(valor)) {
                reloj.setDuracionCicloMs(Integer.parseInt(valor));
                agregarLog("Velocidad del reloj actualizada a " + valor + " ms.");
            }
        });

        String[] politicas = {"FCFS", "Round Robin", "SRT", "Prioridad Estática", "EDF"};
        comboPoliticas = new JComboBox<>(politicas);

        panelControles.add(btnGenerarMasivo);
        panelControles.add(btnEmergencia);
        panelControles.add(new JLabel("Velocidad:"));
        panelControles.add(spinDuracionCiclo);
        panelControles.add(btnAplicarVelocidad);
        panelControles.add(new JLabel("Política:"));
        panelControles.add(comboPoliticas);

        txtLogEventos = new JTextArea(6, 50);
        txtLogEventos.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLogEventos);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log de Sistema"));

        panelBottom.add(panelControles, BorderLayout.NORTH);
        panelBottom.add(scrollLog, BorderLayout.CENTER);
        ventanaPrincipal.add(panelBottom, BorderLayout.SOUTH);
        ventanaPrincipal.setVisible(true);
    }

    private JPanel crearPanelTabla(String titulo, JTable tabla) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    public void actualizarTablas(ProcessControlBlock[] snapshotListos, ProcessControlBlock[] snapshotBloqueados, ProcessControlBlock[] snapshotListosSusp, ProcessControlBlock[] snapshotBloqSusp) {
        SwingUtilities.invokeLater(() -> {
            llenarModelo(modeloListos, snapshotListos);
            llenarModelo(modeloBloqueados, snapshotBloqueados);
            llenarModelo(modeloListosSuspendidos, snapshotListosSusp);
            llenarModelo(modeloBloqueadosSuspendidos, snapshotBloqSusp);
            lblRelojGlobal.setText("MISSION CLOCK: Cycle " + reloj.getCicloGlobal());
        });
    }

    private void llenarModelo(DefaultTableModel modelo, ProcessControlBlock[] datos) {
        modelo.setRowCount(0);
        if (datos != null) {
            for (ProcessControlBlock pcb : datos) {
                if (pcb != null) {
                    // --- NUEVO BLOQUE VISUAL PARA DEADLINES ---
                    int tr = pcb.getTiempoRestanteDeadline();
                    String deadlineVisual = (tr < 0) ? "¡VENCIDO! (" + tr + ")" : tr + " ciclos";
                    // ------------------------------------------

                    modelo.addRow(new Object[]{
                        pcb.getId(), 
                        pcb.getNombre(), 
                        pcb.getEstado(),
                        pcb.getPc() + "/" + pcb.getCantidadInstrucciones(),
                        pcb.getMar(), 
                        pcb.getPrioridad(), 
                        deadlineVisual // <--- Usamos nuestra nueva variable aquí
                    });
                }
            }
        }
    }

    public void actualizarCPU(ProcessControlBlock enCpu, boolean ejecutandoOS) {
        SwingUtilities.invokeLater(() -> {
            if (enCpu != null) {
                // --- Lógica Visual de Deadline ---
                int tr = enCpu.getTiempoRestanteDeadline();
                // Si es menor a cero, lo ponemos en rojo y dice VENCIDO
                String deadlineVisual = (tr < 0) ? "<font color='red'><b>¡VENCIDO! (" + tr + ")</b></font>" : tr + " ciclos";
                
                // Actualizamos la etiqueta de la CPU agregando también el MAR
                lblProcesoCPU.setText("<html><center>" + 
                        enCpu.getNombre() + "<br>" +
                        "[ID: " + enCpu.getId() + "]<br>" +
                        "PC: " + enCpu.getPc() + "<br>" +
                        "MAR: " + enCpu.getMar() + "<br>" +
                        "Deadline: " + deadlineVisual + 
                        "</center></html>");
            } else {
                lblProcesoCPU.setText("IDLE");
            }
            
            // Indicador de Modo (Kernel vs Usuario)
            if (ejecutandoOS) {
                lblIndicadorModo.setText("Modo: SISTEMA OPERATIVO (Kernel)");
                lblIndicadorModo.setForeground(Color.RED);
            } else {
                lblIndicadorModo.setText("Modo: PROGRAMA USUARIO");
                lblIndicadorModo.setForeground(Color.GREEN);
            }
        });
    }

    public void configurarListeners(InyectorEventos inyector, Planificador planificador) {
        btnGenerarMasivo.addActionListener(e -> {
            agregarLog("Generando 20 procesos de estrés aleatorios...");
            
            new Thread(() -> {
                for (int i = 0; i < 20; i++) {
                    String id = "P" + ((int) (Math.random() * 9000) + 1000);
                    int inst = (int) (Math.random() * 50) + 10;
                    int prio = (int) (Math.random() * 5) + 1;
                    int dl = (int) (Math.random() * 200) + 50;
                    
                    ProcessControlBlock pcb = new ProcessControlBlock(
                        id, "Task_RND_" + i, inst, prio, dl, false, 0, 0
                    );
                    inyector.inyectarProcesoAperiodico(pcb);
                }
            }, "Hilo-Inyector-Masivo").start();
        });;

        btnEmergencia.addActionListener(e -> {
            agregarLog("¡ALERTA! Interrupción detectada: Micro-Meteorito.");
            
            // Creamos un hilo mensajero para no bloquear la interfaz (EDT)
            new Thread(() -> {
                inyector.dispararInterrupcionHardware("Micro-Meteorito", 25);
            }, "Hilo-Interrupcion-Hardware").start();
        });

        comboPoliticas.addActionListener(e -> {
            String seleccion = (String) comboPoliticas.getSelectedItem();
            PoliticaPlanificacion nuevaPolitica = null;
            switch (seleccion) {
                case "FCFS": nuevaPolitica = new PoliticaFCFS(); break;
                case "Round Robin": nuevaPolitica = new PoliticaRR(10); break;
                case "SRT": nuevaPolitica = new PoliticaSRT(); break;
                case "Prioridad Estática": nuevaPolitica = new PoliticaPrioridadEstatica(); break;
                case "EDF": nuevaPolitica = new PoliticaEDF(); break;
            }
            if (nuevaPolitica != null && planificador != null) {
                planificador.setPolitica(nuevaPolitica);
                agregarLog("Política cambiada a: " + nuevaPolitica.getNombrePolitica());
            }
        });
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtLogEventos.append("> " + mensaje + "\n");
            txtLogEventos.setCaretPosition(txtLogEventos.getDocument().getLength());
        });
    }

    private boolean validarEntradaCiclo(String input) {
        try {
            int valor = Integer.parseInt(input);
            if (valor <= 0) {
                JOptionPane.showMessageDialog(ventanaPrincipal, "Debe ser > 0 ms.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(ventanaPrincipal, "Solo números enteros.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public PanelTelemetria getPanelTelemetria() {
        return panelTelemetria;
    }
    
    public hardware.RelojSistema getReloj() {
        return reloj;
    }
    
    // Atributo estático temporal para recibir datos del backend
    private static PanelTelemetria telemetriaGlobal;

    // Agrega este método al final de DashboardGUI
    public static void registrarProcesoTerminadoGlobal(ProcessControlBlock pcb) {
        if (telemetriaGlobal != null) {
            telemetriaGlobal.registrarProcesoTerminado(pcb);
        }
    }
}