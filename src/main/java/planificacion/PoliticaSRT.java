/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;

public class PoliticaSRT extends PoliticaPlanificacion {

    @Override
    public ProcessControlBlock seleccionarSiguienteProceso(ThreadSafeQueue colaListos) {
        if (colaListos.obtenerTamanoSeguro() == 0) {
            return null;
        }

        ProcessControlBlock[] snapshot = colaListos.obtenerSnapshot();
        if (snapshot.length == 0) return null;

        // Buscar el proceso con el menor tiempo restante
        ProcessControlBlock srtProceso = snapshot[0];
        int menorTiempoRestante = srtProceso.getCantidadInstrucciones() - srtProceso.getPc();

        for (int i = 1; i < snapshot.length; i++) {
            ProcessControlBlock actual = snapshot[i];
            int tiempoRestante = actual.getCantidadInstrucciones() - actual.getPc();
            
            if (tiempoRestante < menorTiempoRestante) {
                srtProceso = actual;
                menorTiempoRestante = tiempoRestante;
            }
        }

        // Se extrae por ID usando el método de nuestra cola personalizada
        return colaListos.extraerPorId(srtProceso.getId());
    }

    @Override
    public boolean requiereDesalojo(ProcessControlBlock procesoEnCpu, ThreadSafeQueue colaListos) {
        if (procesoEnCpu == null) return false;
        if (colaListos.obtenerTamanoSeguro() == 0) return false;

        int tiempoRestanteCpu = procesoEnCpu.getCantidadInstrucciones() - procesoEnCpu.getPc();
        ProcessControlBlock[] snapshot = colaListos.obtenerSnapshot();

        for (ProcessControlBlock snapshot1 : snapshot) {
            int tiempoRestanteEnCola = snapshot1.getCantidadInstrucciones() - snapshot1.getPc();
            if (tiempoRestanteEnCola < tiempoRestanteCpu) {
                // Hay un proceso en cola que terminaría más rápido, expropiamos a la CPU
                return true;
            }
        }

        return false;
    }

    @Override
    public String getNombrePolitica() {
        return "SRT";
    }
}