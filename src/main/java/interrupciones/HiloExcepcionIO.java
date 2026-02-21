/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interrupciones;

import modelos.ProcessControlBlock;
import motor.GestorMemoria;
import estructuras.ThreadSafeQueue;

/**
 * Hilo independiente que simula la atención de una interrupción de Entrada/Salida (E/S).
 * Descuenta el tiempo requerido por el dispositivo de forma asíncrona y, al finalizar,
 * retorna el proceso a la cola de Listos para volver a competir por la CPU.
 */
public class HiloExcepcionIO implements Runnable {

    private final ProcessControlBlock pcbBloqueado;
    private final GestorMemoria gestorMemoria;
    private int ciclosRestantesIO;

    /**
     * Constructor exacto solicitado.
     * @param pcb Proceso que generó la excepción de E/S.
     * @param gestorMemoria Referencia al gestor para mover el proceso entre colas de forma segura.
     */
    public HiloExcepcionIO(ProcessControlBlock pcb, GestorMemoria gestorMemoria) {
        this.pcbBloqueado = pcb;
        this.gestorMemoria = gestorMemoria;
        // Se inicializa el contador con el tiempo que el hardware tarda en satisfacer la solicitud
        this.ciclosRestantesIO = pcb.getCiclosParaSatisfacerIO();
    }

    @Override
    public void run() {
        try {
            // Simular el tiempo de espera del dispositivo de hardware
            while (ciclosRestantesIO > 0) {
                Thread.sleep(1000); // Simulando 1 ciclo de reloj por segundo
                ciclosRestantesIO--;
            }

            // Al terminar la E/S, solicitamos las colas al Gestor de Memoria
            ThreadSafeQueue colaBloqueados = gestorMemoria.getCola("BLOQUEADOS");
            ThreadSafeQueue colaListos = gestorMemoria.getCola("LISTOS");

            if (colaBloqueados != null && colaListos != null) {
                // Extracción segura protegida por el Mutex interno de ThreadSafeQueue
                ProcessControlBlock extraido = colaBloqueados.extraerPorId(pcbBloqueado.getId());
                
                // Confirmamos que el proceso seguía allí (prevención de condiciones de carrera)
                if (extraido != null) {
                    // Actualizamos el estado interno del PCB usando la enumeración
                    extraido.setEstado(ProcessControlBlock.EstadoProceso.LISTO);
                    
                    // Encolamos de nuevo a Listos (el proceso ya está en RAM, por lo que no altera el límite de memoria)
                    colaListos.encolar(extraido);
                }
            }
            
        } catch (InterruptedException e) {
            // Manejo adecuado en sistemas concurrentes: restaurar la bandera de interrupción
            Thread.currentThread().interrupt();
            System.err.println("El hilo de E/S para el proceso " + pcbBloqueado.getId() + " fue interrumpido abruptamente.");
        }
    }
}