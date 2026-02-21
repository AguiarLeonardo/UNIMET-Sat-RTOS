/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package motor;

import estructuras.ThreadSafeQueue;
import modelos.ProcessControlBlock;
import java.util.concurrent.Semaphore;

/**
 * Gestor de Memoria (Dispatcher & Swap).
 * Administra las colas de estado del RTOS y aplica la regla de saturación
 * enviando procesos a las colas "Suspendidas" si la RAM alcanza su límite.
 * Operaciones protegidas contra concurrencia asíncrona mediante un Mutex estricto.
 */
public class GestorMemoria {

    // Colas Thread-Safe desarrolladas en el Ticket 1.4
    private final ThreadSafeQueue colaListos;
    private final ThreadSafeQueue colaBloqueados;
    private final ThreadSafeQueue colaListosSuspendidos;
    private final ThreadSafeQueue colaBloqueadosSuspendidos;

    // Control de límite de memoria principal (RAM)
    private final int limiteMemoriaPrincipal;
    private int procesosEnMemoriaActual;
    private final Semaphore gestorMutex;

    /**
     * Constructor principal.
     * @param limiteMemoriaPrincipal Cantidad máxima de procesos permitidos en RAM.
     */
    public GestorMemoria(int limiteMemoriaPrincipal) {
        this.colaListos = new ThreadSafeQueue();
        this.colaBloqueados = new ThreadSafeQueue();
        this.colaListosSuspendidos = new ThreadSafeQueue();
        this.colaBloqueadosSuspendidos = new ThreadSafeQueue();
        
        this.limiteMemoriaPrincipal = limiteMemoriaPrincipal;
        this.procesosEnMemoriaActual = 0;
        this.gestorMutex = new Semaphore(1); // Exclusión mutua estricta
    }

    /**
     * Evalúa la admisión de un nuevo proceso. Si hay espacio en memoria,
     * va a Listos. Si la memoria está llena (Saturación), hace Swap Out
     * hacia Listos-Suspendidos.
     * * @param pcb Bloque de Control de Proceso entrante.
     */
    public void admitirNuevoProceso(ProcessControlBlock pcb) {
        try {
            gestorMutex.acquire(); // Sección crítica: evaluando y modificando nivel de memoria
            
            if (procesosEnMemoriaActual < limiteMemoriaPrincipal) {
                // Hay espacio en RAM: Entra directamente a Listo
                pcb.setEstado(ProcessControlBlock.EstadoProceso.LISTO);
                colaListos.encolar(pcb);
                procesosEnMemoriaActual++;
            } else {
                // Memoria Saturada (Swap Out): Se suspende por falta de RAM
                pcb.setEstado(ProcessControlBlock.EstadoProceso.LISTO_SUSPENDIDO);
                colaListosSuspendidos.encolar(pcb);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción al admitir nuevo proceso: " + e.getMessage());
        } finally {
            gestorMutex.release(); // Liberación garantizada
        }
    }

    /**
     * Recibe un proceso desde la CPU (asumimos que cede su ejecución) y
     * lo encola en Bloqueados. (Sigue estando en Memoria Principal).
     * * @param pcb Proceso a bloquear.
     */
    public void bloquearProceso(ProcessControlBlock pcb) {
        try {
            gestorMutex.acquire();
            // El proceso sigue en RAM, por lo que procesosEnMemoriaActual NO disminuye
            pcb.setEstado(ProcessControlBlock.EstadoProceso.BLOQUEADO);
            colaBloqueados.encolar(pcb);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción al bloquear proceso: " + e.getMessage());
        } finally {
            gestorMutex.release();
        }
    }

    /**
     * Disminuye la ocupación de la memoria principal (usualmente llamado cuando
     * un proceso en RAM Termina). Además, evalúa si es posible hacer "Swap In" 
     * para traer un proceso suspendido a la memoria principal.
     */
    public void liberarMemoria() {
        try {
            gestorMutex.acquire();
            
            if (procesosEnMemoriaActual > 0) {
                procesosEnMemoriaActual--;
            }

            // Lógica de Swap In: Intentar llenar el hueco recién liberado
            if (procesosEnMemoriaActual < limiteMemoriaPrincipal) {
                
                // Prioridad 1: Traer de Listo-Suspendido a Listo
                if (colaListosSuspendidos.obtenerTamanoSeguro() > 0) {
                    ProcessControlBlock pcbSuspendido = colaListosSuspendidos.desencolar();
                    if (pcbSuspendido != null) {
                        pcbSuspendido.setEstado(ProcessControlBlock.EstadoProceso.LISTO);
                        colaListos.encolar(pcbSuspendido);
                        procesosEnMemoriaActual++;
                    }
                } 
                // Prioridad 2: Traer de Bloqueado-Suspendido a Bloqueado (Si aplica en el modelo)
                else if (colaBloqueadosSuspendidos.obtenerTamanoSeguro() > 0) {
                    ProcessControlBlock pcbBloqSuspendido = colaBloqueadosSuspendidos.desencolar();
                    if (pcbBloqSuspendido != null) {
                        pcbBloqSuspendido.setEstado(ProcessControlBlock.EstadoProceso.BLOQUEADO);
                        colaBloqueados.encolar(pcbBloqSuspendido);
                        procesosEnMemoriaActual++;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción al liberar memoria: " + e.getMessage());
        } finally {
            gestorMutex.release();
        }
    }

    /**
     * Retorna la referencia a la cola solicitada. Vital para alimentar la Interfaz
     * Gráfica, la cual usará obtenerSnapshot() sobre dicha cola devuelta.
     * * @param tipoCola Cadena de texto indicando la cola requerida.
     * @param tipoCola
     * @return Instancia de ThreadSafeQueue solicitada, o null si el nombre es inválido.
     */
    public ThreadSafeQueue getCola(String tipoCola) {
        if (tipoCola == null) return null;
        
        switch (tipoCola.toUpperCase()) {
            case "LISTOS" -> {
                return colaListos;
            }
            case "BLOQUEADOS" -> {
                return colaBloqueados;
            }
            case "LISTOS_SUSPENDIDOS" -> {
                return colaListosSuspendidos;
            }
            case "BLOQUEADOS_SUSPENDIDOS" -> {
                return colaBloqueadosSuspendidos;
            }
            default -> {
                System.err.println("Error: Tipo de cola no reconocido -> " + tipoCola);
                return null;
            }
        }
    }
}