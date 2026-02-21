/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import modelos.ProcessControlBlock;
import java.util.concurrent.Semaphore;

/**
 * Cola concurrente Thread-Safe. Garantiza la exclusión mutua en las operaciones
 * sobre la estructura de datos subyacente mediante el uso de Semáforos.
 */
public class ThreadSafeQueue {
    
    private final CustomLinkedList listaInterna;
    private final Semaphore mutex;

    public ThreadSafeQueue() {
        this.listaInterna = new CustomLinkedList();
        this.mutex = new Semaphore(1); // Semáforo binario para exclusión mutua estricta
    }

    public void encolar(ProcessControlBlock pcb) {
        try {
            mutex.acquire();
            listaInterna.insertarAlFinal(pcb);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            System.err.println("Hilo interrumpido al intentar encolar: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    public void encolarOrdenado(ProcessControlBlock pcb, boolean priorizarPorDeadline) {
        try {
            mutex.acquire();
            listaInterna.insertarPorPrioridad(pcb, priorizarPorDeadline);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Hilo interrumpido al intentar encolar ordenado: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    public ProcessControlBlock desencolar() {
        ProcessControlBlock extraido = null;
        try {
            mutex.acquire();
            extraido = listaInterna.eliminarAlInicio();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Hilo interrumpido al intentar desencolar: " + e.getMessage());
        } finally {
            mutex.release();
        }
        return extraido;
    }

    public ProcessControlBlock extraerPorId(String id) {
        ProcessControlBlock extraido = null;
        try {
            mutex.acquire();
            extraido = listaInterna.eliminarPorId(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Hilo interrumpido al intentar extraer por ID: " + e.getMessage());
        } finally {
            mutex.release();
        }
        return extraido;
    }

    /**
     * Retorna un arreglo estándar de Java con la copia de los elementos actuales.
     * Vital para iterar en la GUI sin lanzar ConcurrentModificationException.
     * @return 
     */
    public ProcessControlBlock[] obtenerSnapshot() {
        ProcessControlBlock[] snapshot = new ProcessControlBlock[0];
        try {
            mutex.acquire();
            snapshot = listaInterna.aArreglo();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Hilo interrumpido al intentar obtener snapshot: " + e.getMessage());
        } finally {
            mutex.release();
        }
        return snapshot;
    }

    public int obtenerTamanoSeguro() {
        int tamano = 0;
        try {
            mutex.acquire();
            tamano = listaInterna.getTamano();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Hilo interrumpido al intentar obtener tamaño: " + e.getMessage());
        } finally {
            mutex.release();
        }
        return tamano;
    }
}