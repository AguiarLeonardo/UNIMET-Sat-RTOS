/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;

/**
 * Política de Planificación Earliest Deadline First (EDF).
 * Selecciona el proceso cuyo tiempo restante para su deadline esté más cercano a cero.
 */
public class PoliticaEDF extends PoliticaPlanificacion {

    @Override
    public ProcessControlBlock seleccionarSiguienteProceso(ThreadSafeQueue colaListos) {
        ProcessControlBlock[] snapshot = colaListos.obtenerSnapshot();
        
        if (snapshot == null || snapshot.length == 0) {
            return null;
        }

        int menorTiempoRestante = Integer.MAX_VALUE;
        String idSeleccionado = null;

        for (ProcessControlBlock pcbActual : snapshot) {
            if (pcbActual != null && pcbActual.getTiempoRestanteDeadline() < menorTiempoRestante) {
                menorTiempoRestante = pcbActual.getTiempoRestanteDeadline();
                idSeleccionado = pcbActual.getId();
            }
        }

        if (idSeleccionado != null) {
            return colaListos.extraerPorId(idSeleccionado);
        }

        return null;
    }

    @Override
    public boolean requiereDesalojo(ProcessControlBlock procesoEnCpu, ThreadSafeQueue colaListos) {
        if (procesoEnCpu == null) {
            return false; 
        }

        ProcessControlBlock[] snapshot = colaListos.obtenerSnapshot();
        
        if (snapshot == null || snapshot.length == 0) {
            return false;
        }

        for (ProcessControlBlock pcbEnCola : snapshot) {
            // Si hay un proceso cuyo deadline está más apremiante que el proceso actual en CPU
            if (pcbEnCola != null && pcbEnCola.getTiempoRestanteDeadline() < procesoEnCpu.getTiempoRestanteDeadline()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getNombrePolitica() {
        return "Earliest Deadline First (EDF)";
    }
}