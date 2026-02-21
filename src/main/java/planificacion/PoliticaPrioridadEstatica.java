/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;

/**
 * Política de Planificación de Prioridad Estática (Preemptiva).
 * Selecciona el proceso con la mayor prioridad numérica (valor más cercano a 1).
 */
public class PoliticaPrioridadEstatica extends PoliticaPlanificacion {

    @Override
    public ProcessControlBlock seleccionarSiguienteProceso(ThreadSafeQueue colaListos) {
        ProcessControlBlock[] snapshot = colaListos.obtenerSnapshot();
        
        if (snapshot == null || snapshot.length == 0) {
            return null;
        }

        // Asumimos que un valor numérico MENOR significa MAYOR prioridad (ej. 1 es lo más crítico)
        int mejorPrioridad = Integer.MAX_VALUE;
        String idSeleccionado = null;

        for (ProcessControlBlock pcbActual : snapshot) {
            if (pcbActual != null && pcbActual.getPrioridad() < mejorPrioridad) {
                mejorPrioridad = pcbActual.getPrioridad();
                idSeleccionado = pcbActual.getId();
            }
        }

        if (idSeleccionado != null) {
            // Extraemos de la cola real usando el semáforo interno de ThreadSafeQueue
            return colaListos.extraerPorId(idSeleccionado);
        }

        return null;
    }

    @Override
    public boolean requiereDesalojo(ProcessControlBlock procesoEnCpu, ThreadSafeQueue colaListos) {
        if (procesoEnCpu == null) {
            return false; // Si la CPU está vacía, no hay a quién desalojar
        }

        ProcessControlBlock[] snapshot = colaListos.obtenerSnapshot();
        
        if (snapshot == null || snapshot.length == 0) {
            return false;
        }

        for (ProcessControlBlock pcbEnCola : snapshot) {
            // Si hay un proceso en la cola con mayor urgencia (menor número) que el de la CPU
            if (pcbEnCola != null && pcbEnCola.getPrioridad() < procesoEnCpu.getPrioridad()) {
                return true; 
            }
        }

        return false;
    }

    @Override
    public String getNombrePolitica() {
        return "Prioridad Estática (Preemptiva)";
    }
}