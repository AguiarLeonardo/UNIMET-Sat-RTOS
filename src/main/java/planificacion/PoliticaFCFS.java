/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;

public class PoliticaFCFS extends PoliticaPlanificacion {

    @Override
    public ProcessControlBlock seleccionarSiguienteProceso(ThreadSafeQueue colaListos) {
        // En FCFS, el proceso más antiguo es el primero de la cola (desencolar estándar)
        return colaListos.desencolar();
    }

    @Override
    public boolean requiereDesalojo(ProcessControlBlock procesoEnCpu, ThreadSafeQueue colaListos) {
        // FCFS es una política No Expropiativa (Non-Preemptive)
        // Solo saldrá si termina o se bloquea (evaluado externamente)
        return false;
    }

    @Override
    public String getNombrePolitica() {
        return "FCFS";
    }
}