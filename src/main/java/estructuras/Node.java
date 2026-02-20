/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import modelos.ProcessControlBlock;

/**
 * Representa un nodo simétrico para la lista enlazada personalizada.
 * Almacena el PCB y la referencia al siguiente nodo en la memoria.
 */
public class Node {
    
    private ProcessControlBlock pcb;
    private Node siguiente;

    public Node(ProcessControlBlock pcb) {
        this.pcb = pcb;
        this.siguiente = null;
    }

    public ProcessControlBlock getPcb() {
        return pcb;
    }

    public void setPcb(ProcessControlBlock pcb) {
        this.pcb = pcb;
    }

    public Node getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Node siguiente) {
        this.siguiente = siguiente;
    }
}