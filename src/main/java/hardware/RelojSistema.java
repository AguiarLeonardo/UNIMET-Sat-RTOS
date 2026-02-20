/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;

/**
 * Representa el Marcapasos o "Mission Clock" del RTOS.
 * Corre en su propio hilo y dicta el avance del tiempo global.
 */
public class RelojSistema implements Runnable {

    private int cicloGlobal;
    private volatile int duracionCicloMs;
    private volatile boolean simulacionActiva;
    
    // Referencia al procesador para notificarle el pulso de reloj
    private Procesador cpu;

    /**
     * Constructor exacto solicitado.
     * @param duracionInicialMs Duración en ms de cada pulso de reloj.
     */
    public RelojSistema(int duracionInicialMs) {
        this.cicloGlobal = 0;
        this.duracionCicloMs = duracionInicialMs;
        this.simulacionActiva = false;
    }

    /**
     * Método adicional para inyectar la dependencia de la CPU sin alterar
     * el constructor obligatorio del ticket.
     * @param cpu
     */
    public void setProcesador(Procesador cpu) {
        this.cpu = cpu;
    }

    @Override
    public void run() {
        this.simulacionActiva = true;
        
        while (simulacionActiva) {
            try {
                // Suspender el hilo según la velocidad de la simulación
                Thread.sleep(duracionCicloMs);
                
                // Avanzar el tiempo global
                cicloGlobal++;
                
                // Notificar a la CPU para que ejecute una instrucción
                if (cpu != null) {
                    cpu.ejecutarCiclo();
                }
                
            } catch (InterruptedException e) {
                // Manejo adecuado de la interrupción: restaurar la bandera de interrupción y detener
                Thread.currentThread().interrupt();
                this.simulacionActiva = false;
                System.err.println("El Reloj del Sistema fue interrumpido críticamente.");
            }
        }
    }

    public void setDuracionCicloMs(int duracion) {
        this.duracionCicloMs = duracion;
    }

    public int getCicloGlobal() {
        return cicloGlobal;
    }

    public void detenerReloj() {
        this.simulacionActiva = false;
    }
}