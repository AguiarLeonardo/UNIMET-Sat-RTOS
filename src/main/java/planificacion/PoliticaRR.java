/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;

public class PoliticaRR extends PoliticaPlanificacion {

    private final int quantumInicial;
    private int ciclosEjecutadosProcesoActual;
    private ProcessControlBlock ultimoProcesoEvaluado;

    public PoliticaRR(int quantum) {
        this.quantumInicial = quantum;
        this.ciclosEjecutadosProcesoActual = 0;
        this.ultimoProcesoEvaluado = null;
    }

    @Override
    public ProcessControlBlock seleccionarSiguienteProceso(ThreadSafeQueue colaListos) {
        // Al asignar un nuevo proceso, el quantum se reinicia
        this.ciclosEjecutadosProcesoActual = 0;
        return colaListos.desencolar();
    }

    @Override
    public boolean requiereDesalojo(ProcessControlBlock procesoEnCpu, ThreadSafeQueue colaListos) {
        if (procesoEnCpu == null) return false;

        // Validar si el proceso en CPU cambió externamente para reiniciar el contador
        if (this.ultimoProcesoEvaluado != procesoEnCpu) {
            this.ultimoProcesoEvaluado = procesoEnCpu;
            this.ciclosEjecutadosProcesoActual = 0;
        }

        this.ciclosEjecutadosProcesoActual++;

        return this.ciclosEjecutadosProcesoActual >= this.quantumInicial;
    }

    @Override
    public String getNombrePolitica() {
        return "Round Robin (Q=" + quantumInicial + ")";
    }
}