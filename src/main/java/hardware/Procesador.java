/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;


import modelos.ProcessControlBlock;
import java.util.concurrent.Semaphore;

/**
 * Representa la Unidad Central de Procesamiento (CPU).
 * Maneja la ejecución del proceso actual con exclusión mutua estricta.
 */
public class Procesador {

    private ProcessControlBlock procesoActual;
    private final java.util.concurrent.Semaphore cpuMutex;

    /**
     * Constructor que inicializa el semáforo para garantizar exclusión mutua.
     */
    public Procesador() {
        this.procesoActual = null;
        // Semáforo instanciado en 1 (comportamiento de Mutex binario)
        this.cpuMutex = new Semaphore(1);
    }

    /**
     * Asigna un nuevo proceso a la CPU.
     * Protegido por Semáforo.
     * @param pcb
     */
    public void asignarProceso(ProcessControlBlock pcb) {
        try {
            cpuMutex.acquire();
            this.procesoActual = pcb;
            if (this.procesoActual != null) {
                // Mapeo estricto del estado utilizando el Enum interno del PCB
                this.procesoActual.setEstado(ProcessControlBlock.EstadoProceso.EJECUCION);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción durante la asignación de proceso en CPU.");
        } finally {
            cpuMutex.release();
        }
    }

    /**
     * Desaloja el proceso actual de la CPU, dejándola inactiva.
     * Protegido por Semáforo.
     * @return 
     */
    public ProcessControlBlock desalojarProceso() {
        ProcessControlBlock pcbDesalojado = null;
        try {
            cpuMutex.acquire();
            pcbDesalojado = this.procesoActual;
            this.procesoActual = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción durante el desalojo de proceso en CPU.");
        } finally {
            cpuMutex.release();
        }
        return pcbDesalojado;
    }

    /**
     * Avanza el estado interno del proceso actual.
     * Es invocado por el RelojSistema. También se protege con Mutex 
     * para evitar inconsistencias si el despachador interrumpe en este microsegundo.
     */
    public void ejecutarCiclo() {
        try {
            cpuMutex.acquire();
            if (this.procesoActual != null) {
                this.procesoActual.avanzarCicloReloj();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cpuMutex.release();
        }
    }

    /**
     * Retorna la referencia del proceso actual de forma segura para la GUI u otros hilos observadores.
     * Protegido por Semáforo.
     * @return 
     */
    public ProcessControlBlock getProcesoActualSeguro() {
        ProcessControlBlock snapshot = null;
        try {
            cpuMutex.acquire();
            snapshot = this.procesoActual;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cpuMutex.release();
        }
        return snapshot;
    }
    
    /**
     * Retorna el semáforo de la CPU para interrupciones críticas de hardware.
     * @return 
     */
    public java.util.concurrent.Semaphore getMutex() {
        return this.cpuMutex;
    }
}