/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import modelos.ProcessControlBlock;
import javax.swing.*;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Panel Estadístico de Telemetría.
 * Muestra métricas de rendimiento del RTOS y una gráfica de uso de CPU en tiempo real.
 */
public class PanelTelemetria extends JPanel {

    private final org.jfree.chart.JFreeChart graficoCPU;
    private final org.jfree.chart.ChartPanel panelGraficoCPU;
    private final org.jfree.data.xy.XYSeries serieUsoCPU;

    private final javax.swing.JLabel lblTasaExito;
    private final javax.swing.JLabel lblThroughput;
    private final javax.swing.JLabel lblTiempoEspera;

    private int totalProcesosTerminados;
    private int totalDeadlinesCumplidos;

    /**
     * Constructor exacto. Inicializa las gráficas y componentes visuales.
     */
    public PanelTelemetria() {
        this.totalProcesosTerminados = 0;
        this.totalDeadlinesCumplidos = 0;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Métricas y Telemetría del Sistema"));

        // Inicialización de la gráfica JFreeChart
        serieUsoCPU = new XYSeries("Uso CPU (%)");
        XYSeriesCollection dataset = new XYSeriesCollection(serieUsoCPU);
        graficoCPU = ChartFactory.createXYLineChart(
                "Carga del Procesador", 
                "Ciclos de Reloj", 
                "Uso (%)", 
                dataset
        );
        panelGraficoCPU = new ChartPanel(graficoCPU);
        panelGraficoCPU.setPreferredSize(new Dimension(400, 250));

        // Inicialización de las etiquetas de métricas
        JPanel panelEstadisticas = new JPanel(new GridLayout(3, 1, 5, 5));
        lblTasaExito = new JLabel("Tasa de Éxito: 0.00%");
        lblTasaExito.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblThroughput = new JLabel("Throughput: 0 proc/ciclo");
        lblTiempoEspera = new JLabel("Tiempo Prom. Espera: 0 ms");

        panelEstadisticas.add(lblTasaExito);
        panelEstadisticas.add(lblThroughput);
        panelEstadisticas.add(lblTiempoEspera);

        // Ensamblar el panel
        add(panelEstadisticas, BorderLayout.NORTH);
        add(panelGraficoCPU, BorderLayout.CENTER);
    }

    /**
     * Evalúa si un proceso terminó antes de su deadline y actualiza las métricas.
     * @param pcb Proceso que acaba de finalizar.
     */
    public void registrarProcesoTerminado(ProcessControlBlock pcb) {
        if (pcb == null) return;

        totalProcesosTerminados++;
        
        // Si el tiempo restante es >= 0, significa que cumplió o empató con el deadline
        if (pcb.getTiempoRestanteDeadline() >= 0) {
            totalDeadlinesCumplidos++;
        }

        // Recalcular y actualizar la etiqueta visual
        double tasa = ((double) totalDeadlinesCumplidos / totalProcesosTerminados) * 100.0;
        lblTasaExito.setText(String.format("Tasa de Éxito: %.2f%%", tasa));
        lblThroughput.setText("Total Terminados: " + totalProcesosTerminados);
    }

    /**
     * Añade un nuevo punto de datos a la gráfica de JFreeChart.
     * @param cicloGlobal El ciclo actual del reloj del sistema.
     * @param porcentajeUso El nivel de uso de la CPU en ese instante.
     */
    public void agregarPuntoUsoCPU(int cicloGlobal, double porcentajeUso) {
        if (serieUsoCPU != null) {
            // SwingUtilities se asegura de que la gráfica se repinte en el hilo correcto
            SwingUtilities.invokeLater(() -> {
                serieUsoCPU.add(cicloGlobal, porcentajeUso);
            });
        }
    }
}