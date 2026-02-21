/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interrupciones;

import hardware.Procesador;
import modelos.ProcessControlBlock;
import motor.GestorMemoria;

/**
 * Clase de utilidad (Helper) para disparar eventos asíncronos o 
 * inyectar tareas aperiódicas en tiempo de ejecución.
 */
public class InyectorEventos {

    private final Procesador cpu;
    private final GestorMemoria gestorMemoria;

    /**
     * Constructor exacto.
     * @param cpu Referencia a la CPU actual.
     * @param memoria Referencia al gestor de memoria/colas.
     */
    public InyectorEventos(Procesador cpu, GestorMemoria memoria) {
        this.cpu = cpu;
        this.gestorMemoria = memoria;
    }

    /**
     * Desata un evento crítico de hardware en su propio hilo de máxima prioridad.
     * @param nombre Descripción del evento.
     * @param ciclos Duración del evento en la CPU.
     */
    public void dispararInterrupcionHardware(String nombre, int ciclos) {
        HiloInterrupcionEmergencia hiloEmergencia = new HiloInterrupcionEmergencia(nombre, ciclos, this.cpu);
        hiloEmergencia.start();
    }

    /**
     * Inyecta de manera repentina un nuevo proceso al sistema.
     * Al entrar en la memoria/colas de listos, el planificador reevaluará las prioridades.
     * @param nuevoPcb El PCB del proceso aperiódico.
     */
    public void inyectarProcesoAperiodico(ProcessControlBlock nuevoPcb) {
        if (nuevoPcb != null && this.gestorMemoria != null) {
            System.out.println("Inyectando proceso aperiódico repentino: " + nuevoPcb.getNombre());
            
            // Asume que tu compañero creó este método en GestorMemoria para encolar el proceso.
            // Si lo llamó distinto (ej. agregarProceso), ajústalo aquí.
            this.gestorMemoria.admitirNuevoProceso(nuevoPcb);
        }
    }
}