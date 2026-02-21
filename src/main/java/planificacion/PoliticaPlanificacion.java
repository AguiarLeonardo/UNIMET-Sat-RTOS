/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacion;

import modelos.ProcessControlBlock;
import estructuras.ThreadSafeQueue;

/**
 * Clase base para el Patrón Estrategia.
 * Define el contrato que toda política de planificación debe implementar.
 */
public abstract class PoliticaPlanificacion {
    
    public abstract ProcessControlBlock seleccionarSiguienteProceso(ThreadSafeQueue colaListos);
    
    public abstract boolean requiereDesalojo(ProcessControlBlock procesoEnCpu, ThreadSafeQueue colaListos);
    
    public abstract String getNombrePolitica();
}
