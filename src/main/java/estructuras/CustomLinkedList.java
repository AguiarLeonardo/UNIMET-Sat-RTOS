/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import modelos.ProcessControlBlock;

/**
 * Lista enlazada simple construida desde cero. 
 * Maneja la inserción estándar y la inserción ordenada (para planificadores estáticos y EDF).
 */
public class CustomLinkedList {
    
    private Node cabeza;
    private Node cola;
    private int tamano;

    public CustomLinkedList() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    public void insertarAlFinal(ProcessControlBlock pcb) {
        Node nuevo = new Node(pcb);
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            cola = nuevo;
        }
        tamano++;
    }

    /**
     * Inserta un PCB manteniendo el orden. 
     * Asume que menor valor numérico = mayor prioridad (ej. deadline más cercano o prioridad más alta).
     * @param pcb
     * @param priorizarPorDeadline
     */
    public void insertarPorPrioridad(ProcessControlBlock pcb, boolean priorizarPorDeadline) {
        Node nuevo = new Node(pcb);
        
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
            tamano++;
            return;
        }

        // Evaluar si va al inicio (cabeza)
        boolean vaAlInicio = priorizarPorDeadline 
            ? (pcb.getTiempoRestanteDeadline() < cabeza.getPcb().getTiempoRestanteDeadline())
            : (pcb.getPrioridad() < cabeza.getPcb().getPrioridad());

        if (vaAlInicio) {
            nuevo.setSiguiente(cabeza);
            cabeza = nuevo;
            tamano++;
            return;
        }

        // Recorrer para encontrar la posición correcta
        Node actual = cabeza;
        while (actual.getSiguiente() != null) {
            ProcessControlBlock sigPcb = actual.getSiguiente().getPcb();
            boolean insertarAqui = priorizarPorDeadline
                ? (pcb.getTiempoRestanteDeadline() < sigPcb.getTiempoRestanteDeadline())
                : (pcb.getPrioridad() < sigPcb.getPrioridad());

            if (insertarAqui) {
                break;
            }
            actual = actual.getSiguiente();
        }

        nuevo.setSiguiente(actual.getSiguiente());
        actual.setSiguiente(nuevo);
        
        // Si se insertó al final, actualizar la cola
        if (nuevo.getSiguiente() == null) {
            cola = nuevo;
        }
        tamano++;
    }

    public ProcessControlBlock eliminarAlInicio() {
        if (estaVacia()) return null;
        
        ProcessControlBlock pcb = cabeza.getPcb();
        cabeza = cabeza.getSiguiente();
        tamano--;
        
        if (estaVacia()) {
            cola = null;
        }
        return pcb;
    }

    public ProcessControlBlock eliminarPorId(String id) {
        if (estaVacia()) return null;

        // Si es la cabeza
        if (cabeza.getPcb().getId().equals(id)) {
            return eliminarAlInicio();
        }

        Node actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getPcb().getId().equals(id)) {
                ProcessControlBlock pcbExtraido = actual.getSiguiente().getPcb();
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                
                // Si eliminamos el último, actualizamos la cola
                if (actual.getSiguiente() == null) {
                    cola = actual;
                }
                tamano--;
                return pcbExtraido;
            }
            actual = actual.getSiguiente();
        }
        return null; // No se encontró
    }

    public ProcessControlBlock obtenerPorId(String id) {
        Node actual = cabeza;
        while (actual != null) {
            if (actual.getPcb().getId().equals(id)) {
                return actual.getPcb();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int getTamano() {
        return tamano;
    }

    public ProcessControlBlock[] aArreglo() {
        ProcessControlBlock[] arreglo = new ProcessControlBlock[tamano];
        Node actual = cabeza;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.getPcb();
            actual = actual.getSiguiente();
        }
        return arreglo;
    }
}
