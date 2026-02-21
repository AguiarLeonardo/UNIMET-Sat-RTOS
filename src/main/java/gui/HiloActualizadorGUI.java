/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import hardware.Procesador;
import motor.GestorMemoria;
import modelos.ProcessControlBlock;

/**
 * Hilo Observador (Puente). 
 * Captura el estado del backend a intervalos regulares y lo empuja a la interfaz
 * garantizando cero condiciones de carrera.
 */
public class HiloActualizadorGUI extends Thread {

    private final GestorMemoria gestorMemoria;
    private final Procesador cpu;
    private final DashboardGUI guiPrincipal;
    private final int tasaRefrescoMs;

    /**
     * Constructor exacto.
     * @param memoria Gestor de colas de estados.
     * @param cpu Referencia al procesador.
     * @param gui Instancia de la interfaz gráfica principal.
     */
    public HiloActualizadorGUI(GestorMemoria memoria, Procesador cpu, DashboardGUI gui) {
        this.gestorMemoria = memoria;
        this.cpu = cpu;
        this.guiPrincipal = gui;
        this.tasaRefrescoMs = 100; // Refresco predeterminado de 100 milisegundos
        this.setDaemon(true); // Se detendrá automáticamente cuando se cierre la ventana
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 1. Extraer los Snapshots estáticos de lectura (Arreglos puros [])
                ProcessControlBlock[] snapshotListos = gestorMemoria.getCola("LISTOS").obtenerSnapshot();
                ProcessControlBlock[] snapshotBloqueados = gestorMemoria.getCola("BLOQUEADOS").obtenerSnapshot();
                ProcessControlBlock[] snapshotListosSusp = gestorMemoria.getCola("LISTOS_SUSPENDIDOS").obtenerSnapshot();
                ProcessControlBlock[] snapshotBloqSusp = gestorMemoria.getCola("BLOQUEADOS_SUSPENDIDOS").obtenerSnapshot();

                // 2. Extraer de manera segura el proceso actualmente en CPU
                ProcessControlBlock enCpu = cpu.getProcesoActualSeguro();

                // 3. Empujar los datos de las colas hacia la GUI
                guiPrincipal.actualizarTablas(
                        snapshotListos, 
                        snapshotBloqueados, 
                        snapshotListosSusp, 
                        snapshotBloqSusp
                );

                // 4. Actualizar la vista de la CPU (Inferencia básica del modo kernel/usuario)
                boolean ejecutandoOS = false;
                if (enCpu != null && enCpu.getPrioridad() <= 1) {
                    ejecutandoOS = true; // Asumimos que prioridad 1 es Tarea del Sistema
                }
                guiPrincipal.actualizarCPU(enCpu, ejecutandoOS);
                
                // 5. ALIMENTAR LA GRÁFICA DE TELEMETRÍA (NUEVO)
                PanelTelemetria telemetria = guiPrincipal.getPanelTelemetria();
                if (telemetria != null) {
                    int cicloActual = guiPrincipal.getReloj().getCicloGlobal();
                    // Si hay un proceso en CPU, el uso es 100%. Si está IDLE, es 0%.
                    double usoCPU = (enCpu != null) ? 100.0 : 0.0; 
                    telemetria.agregarPuntoUsoCPU(cicloActual, usoCPU);
                }

                // 6. Esperar hasta el próximo ciclo de refresco
                Thread.sleep(tasaRefrescoMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Hilo Puente detenido de forma segura.");
            } catch (Exception e) {
                System.err.println("Excepción ignorada en Hilo Actualizador (previniendo cuelgue de GUI): " + e.getMessage());
            }
        }
    }
}