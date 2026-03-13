/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;

import motor.GestorMemoria;
import estructuras.ThreadSafeQueue;
import modelos.ProcessControlBlock;

public class RelojSistema implements Runnable {

    // --- Atributos Originales ---
    private int cicloGlobal;
    private volatile int duracionCicloMs;
    private volatile boolean simulacionActiva;
    
    // --- Dependencias del Sistema ---
    private Procesador cpu;
    private GestorMemoria gestorMemoria;
    private gui.DashboardGUI dashboard;

    public RelojSistema(int duracionInicialMs) {
        this.cicloGlobal = 0;
        this.duracionCicloMs = duracionInicialMs;
        this.simulacionActiva = false;
    }

    // --- Setters para Inyección de Dependencias ---
    public void setProcesador(Procesador cpu) {
        this.cpu = cpu;
    }

    public void setGestorMemoria(GestorMemoria gestor) {
        this.gestorMemoria = gestor;
    }

    public void setDashboard(gui.DashboardGUI dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void run() {
        this.simulacionActiva = true;
        
        while (simulacionActiva) {
            try {
                // Suspender el hilo según la velocidad de la simulación
                Thread.sleep(duracionCicloMs);
                cicloGlobal++;
                
                // 1. Ejecución de la CPU y registro de estado
                if (cpu != null) {
                    cpu.ejecutarCiclo();
                    cpu.registrarEstadoCiclo(); // Registra el uso para la gráfica
                }
                
                // 2. Envejecimiento de Deadlines en Memoria
                actualizarTiemposDelSistema();

                // 3. Enviar punto de Telemetría a la Gráfica
                if (cpu != null) {
                    gui.DashboardGUI.agregarPuntoGraficaGlobal(cicloGlobal, cpu.getUsoCPUPromedio());
                }

                // 4. FORZAR REPINTADO DE LA INTERFAZ
                if (dashboard != null && gestorMemoria != null) {
                    // Repintar las tablas sacando snapshots frescos de la memoria
                    dashboard.actualizarTablas(
                        gestorMemoria.getCola("LISTOS") != null ? gestorMemoria.getCola("LISTOS").obtenerSnapshot() : null,
                        gestorMemoria.getCola("BLOQUEADOS") != null ? gestorMemoria.getCola("BLOQUEADOS").obtenerSnapshot() : null,
                        gestorMemoria.getCola("SUSPENDIDOS") != null ? gestorMemoria.getCola("SUSPENDIDOS").obtenerSnapshot() : null,
                        gestorMemoria.getCola("BLOQUEADOS_SUSPENDIDOS") != null ? gestorMemoria.getCola("BLOQUEADOS_SUSPENDIDOS").obtenerSnapshot() : null
                    );
                    
                    // Repintar el panel de la CPU (false indica Modo Usuario por defecto)
                    dashboard.actualizarCPU(cpu.getProcesoActualSeguro(), false);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.simulacionActiva = false;
                System.err.println("El Reloj del Sistema fue interrumpido críticamente.");
            }
        }
    }

    /**
     * Envejece los deadlines de todos los procesos que están esperando en colas.
     */
    private void actualizarTiemposDelSistema() {
        if (gestorMemoria == null) return;

        // Actualizar Listos (RAM)
        ThreadSafeQueue colaListos = gestorMemoria.getCola("LISTOS");
        if (colaListos != null) {
            for (ProcessControlBlock pcb : colaListos.obtenerSnapshot()) {
                if (pcb != null) pcb.reducirDeadline(); 
            }
        }

        // Actualizar Suspendidos (Disco)
        ThreadSafeQueue colaSusp = gestorMemoria.getCola("SUSPENDIDOS");
        if (colaSusp != null) {
            for (ProcessControlBlock pcb : colaSusp.obtenerSnapshot()) {
                if (pcb != null) pcb.reducirDeadline();
            }
        }
    }

    public void setDuracionCicloMs(int duracion) {
        this.duracionCicloMs = duracion;
    }

    public int getCicloGlobal() {
        return cicloGlobal;
    }

    public void detenerReloj() {
        this.simulacionActiva = false;
    }
}