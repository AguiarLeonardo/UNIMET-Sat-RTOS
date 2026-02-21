/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interrupciones;

import hardware.Procesador;
import modelos.ProcessControlBlock;

/**
 * Hilo asíncrono que modela eventos externos de hardware críticos.
 * Tiene prioridad máxima a nivel de sistema operativo para expropiar la CPU.
 */
public class HiloInterrupcionEmergencia extends Thread {

    private final String nombreEvento;
    private int duracionCiclos;
    private final Procesador cpu;
    private ProcessControlBlock procesoInterrumpido;

    /**
     * Constructor exacto de la interrupción de emergencia.
     * @param nombreEvento Descripción de la interrupción.
     * @param duracionCiclos Ciclos que tomará atender la emergencia.
     * @param cpu Referencia directa a la unidad de procesamiento.
     */
    public HiloInterrupcionEmergencia(String nombreEvento, int duracionCiclos, Procesador cpu) {
        this.nombreEvento = nombreEvento;
        this.duracionCiclos = duracionCiclos;
        this.cpu = cpu;
        this.procesoInterrumpido = null;
        
        // Restricción crítica: Prioridad máxima a nivel del Thread de Java
        this.setPriority(Thread.MAX_PRIORITY);
    }

    /**
     * Secuestra la CPU y guarda el estado del proceso actual.
     */
    private void guardarContexto() {
        this.procesoInterrumpido = cpu.desalojarProceso();
    }

    /**
     * Devuelve el proceso interrumpido a la CPU (si había alguno).
     */
    private void restaurarContexto() {
        if (this.procesoInterrumpido != null) {
            cpu.asignarProceso(this.procesoInterrumpido);
        }
    }

    @Override
    public void run() {
        try {
            // Adquirir control absoluto, bloqueando al planificador y al reloj
            cpu.getMutex().acquire();
            
            System.out.println(">>> [EMERGENCIA DE HARDWARE] " + nombreEvento + " detectado. Expropiando CPU...");
            guardarContexto();
            
            // Simulación del servicio de la interrupción
            while (duracionCiclos > 0) {
                // Pausa simulada para dar feedback visual a la futura GUI (100ms por ciclo)
                Thread.sleep(100); 
                duracionCiclos--;
            }
            
            restaurarContexto();
            System.out.println("<<< [SISTEMA ESTABLE] Interrupción resuelta. Contexto restaurado.");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error fatal: La rutina de servicio de interrupción fue abortada.");
        } finally {
            // Es vital liberar el semáforo o la CPU quedará bloqueada para siempre (Deadlock)
            cpu.getMutex().release();
        }
    }
}