/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simuladorrtos;

import hardware.Procesador;
import hardware.RelojSistema;
import interrupciones.InyectorEventos;
import motor.GestorMemoria;
import planificacion.Planificador;
import planificacion.PoliticaFCFS; 
import gui.DashboardGUI;
import gui.HiloActualizadorGUI;
import modelos.ProcessControlBlock;

import javax.swing.SwingUtilities;

/**
 * Clase principal que inicializa e interconecta todos los módulos del RTOS.
 * Representa el "Boot Sequence" (Secuencia de arranque) del simulador.
 */
public class main {

    public static void main(String[] args) {
        System.out.println("Iniciando Secuencia de Boot del RTOS UNIMET-Sat...");

        // ---------------------------------------------------------
        // 1. INICIALIZACIÓN DEL HARDWARE
        // ---------------------------------------------------------
        Procesador cpu = new Procesador();
        
        // El spinner de la GUI arranca en 1000ms, inicializamos el reloj igual
        RelojSistema reloj = new RelojSistema(1000); 
        reloj.setProcesador(cpu); // ¡Conexión vital descubierta en tu código!

        // ---------------------------------------------------------
        // 2. INICIALIZACIÓN DEL SISTEMA OPERATIVO (MOTOR)
        // ---------------------------------------------------------
        GestorMemoria gestorMemoria = new GestorMemoria(5); // RAM limitada a 5 procesos
        
        Planificador planificador = new Planificador(gestorMemoria, cpu, new PoliticaFCFS());
        
        InyectorEventos inyector = new InyectorEventos(cpu, gestorMemoria);

        // ---------------------------------------------------------
        // 3. INICIALIZACIÓN DE LA INTERFAZ GRÁFICA (GUI)
        // ---------------------------------------------------------
        SwingUtilities.invokeLater(() -> {
            DashboardGUI ventana = new DashboardGUI(gestorMemoria, cpu, reloj);
            ventana.configurarListeners(inyector, planificador);
            ventana.agregarLog("Boot completo. Sistema Operativo listo y a la espera.");
             
            // ---------------------------------------------------------
            // 3.5 GENERACIÓN INICIAL AUTOMÁTICA (REQUISITO PDF)
            // ---------------------------------------------------------
            ventana.agregarLog("Generando conjunto inicial de 7 procesos automáticos...");
            for (int i = 1; i <= 7; i++) {
                String id = "P" + ((int) (Math.random() * 9000) + 1000);
                int inst = (int) (Math.random() * 50) + 10;
                int prio = (int) (Math.random() * 5) + 1;
                int dl = (int) (Math.random() * 200) + 50;
                
                ProcessControlBlock pcb = new ProcessControlBlock(
                    id, "Init_Task_" + i, inst, prio, dl, false, 0, 0
                );
                inyector.inyectarProcesoAperiodico(pcb);
            }

            // ---------------------------------------------------------
            // 4. ENCENDIDO DE LOS HILOS DEL SISTEMA (START)
            // ---------------------------------------------------------
            HiloActualizadorGUI hiloPuente = new HiloActualizadorGUI(gestorMemoria, cpu, ventana);
            hiloPuente.start();
            
            // Como implementan Runnable, los envolvemos en Threads nativos
            new Thread(planificador, "Hilo-Planificador").start();
            new Thread(reloj, "Hilo-RelojSistema").start();
            
            System.out.println("Sistema encendido y operando.");
        });
    }
}