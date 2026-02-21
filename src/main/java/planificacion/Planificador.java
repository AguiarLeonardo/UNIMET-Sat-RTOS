/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import hardware.Procesador;
import motor.GestorMemoria;
import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;
import java.util.concurrent.Semaphore;

/**
 * Motor del Planificador (Scheduler Engine).
 * Corre en su propio hilo y orquesta el Cambio de Contexto (Context Switch)
 * evaluando dinámicamente la política actual.
 */
public class Planificador implements Runnable {

    private PoliticaPlanificacion politicaActual;
    private final GestorMemoria gestorMemoria;
    private final Procesador cpu;
    private volatile boolean planificadorActivo;
    private final Semaphore planificadorMutex;

    public Planificador(GestorMemoria gestor, Procesador cpu, PoliticaPlanificacion politicaInicial) {
        this.gestorMemoria = gestor;
        this.cpu = cpu;
        this.politicaActual = politicaInicial;
        this.planificadorActivo = false;
        this.planificadorMutex = new Semaphore(1);
    }

    public void setPolitica(PoliticaPlanificacion nuevaPolitica) {
        try {
            planificadorMutex.acquire();
            this.politicaActual = nuevaPolitica;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción al cambiar la política de planificación.");
        } finally {
            planificadorMutex.release();
        }
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        this.planificadorActivo = true;
        ThreadSafeQueue colaListos = gestorMemoria.getCola("LISTOS");

        while (planificadorActivo) {
            try {
                // Pequeña pausa para no saturar el núcleo del host físico en el bucle infinito
                Thread.sleep(10); 

                planificadorMutex.acquire();
                
                ProcessControlBlock enCpu = cpu.getProcesoActualSeguro();
                boolean debeDesalojar = false;

                if (enCpu == null) {
                    debeDesalojar = true; // CPU libre
                } else {
                    debeDesalojar = politicaActual.requiereDesalojo(enCpu, colaListos);
                }

                if (debeDesalojar) {
                    ProcessControlBlock siguiente = politicaActual.seleccionarSiguienteProceso(colaListos);
                    
                    // Realizar el Cambio de Contexto si hay algo que hacer
                    if (siguiente != null || enCpu != null) {
                        
                        if (enCpu != null) {
                            ProcessControlBlock desalojado = cpu.desalojarProceso();
                            // Si no ha terminado, regresa a Listos (Preemption)
                            if (desalojado.getPc() < desalojado.getCantidadInstrucciones()) {
                                desalojado.setEstado(ProcessControlBlock.EstadoProceso.LISTO);
                                colaListos.encolar(desalojado);
                            } else {
                                // 1. Marcamos su estado oficial
                                desalojado.setEstado(ProcessControlBlock.EstadoProceso.TERMINADO);
                                // 2. Liberamos la RAM para que entren los suspendidos (Swap In)
                                gestorMemoria.liberarMemoria();
                                // 3. Le avisamos a la interfaz que sume un éxito
                                gui.DashboardGUI.registrarProcesoTerminadoGlobal(desalojado);
                            }
                            // Nota: Si terminó, la gestión final se hace en otra etapa (ej. liberar memoria)
                        }

                        if (siguiente != null) {
                            cpu.asignarProceso(siguiente);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.planificadorActivo = false;
            } finally {
                planificadorMutex.release();
            }
        }
    }

    public void detenerPlanificador() {
        this.planificadorActivo = false;
    }
}