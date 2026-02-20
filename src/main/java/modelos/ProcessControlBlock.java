/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

/**
 * Representa el Bloque de Control de Proceso (PCB) de una tarea en el RTOS.
 * Mantiene el contexto de ejecución, restricciones de tiempo (deadline) y
 * estado del proceso sin depender de ninguna estructura externa o colección de Java.
 */
public class ProcessControlBlock {

    /**
     * Enum interno que define los 7 estados permitidos para el ciclo de vida del proceso.
     */
    public enum EstadoProceso {
        NUEVO,
        LISTO,
        EJECUCION,
        BLOQUEADO,
        TERMINADO,
        LISTO_SUSPENDIDO,
        BLOQUEADO_SUSPENDIDO
    }

    private String id;
    private String nombre;
    private String estado;
    private int cantidadInstrucciones;
    private int prioridad;
    private int deadline;
    private int tiempoRestanteDeadline;
    private int pc;
    private int mar;
    private boolean esTipoIO;
    private int ciclosParaExcepcionIO;
    private int ciclosParaSatisfacerIO;

    /**
     * Constructor principal del PCB. Inicializa los registros y el contador del deadline.
     * * @param id Identificador único.
     * @param id
     * @param nombre Nombre descriptivo de la tarea.
     * @param cantidadInstrucciones Total de ciclos/instrucciones a ejecutar.
     * @param prioridad Prioridad estática de planificación.
     * @param deadline Tiempo límite crítico.
     * @param esTipoIO Bandera para tareas de Entrada/Salida.
     * @param ciclosParaExcepcionIO Ciclos antes de solicitar E/S.
     * @param ciclosParaSatisfacerIO Ciclos que tarda la operación de E/S.
     */
    public ProcessControlBlock(String id, String nombre, int cantidadInstrucciones, int prioridad, int deadline, boolean esTipoIO, int ciclosParaExcepcionIO, int ciclosParaSatisfacerIO) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadInstrucciones = cantidadInstrucciones;
        this.prioridad = prioridad;
        this.deadline = deadline;
        this.esTipoIO = esTipoIO;
        this.ciclosParaExcepcionIO = ciclosParaExcepcionIO;
        this.ciclosParaSatisfacerIO = ciclosParaSatisfacerIO;

        // Inicializaciones de registros y variables de control por defecto
        this.pc = 0;
        this.mar = 0;
        this.tiempoRestanteDeadline = deadline;
        this.estado = EstadoProceso.NUEVO.name();
    }

    /**
     * Simula el avance de un ciclo de reloj de la CPU en el proceso actual.
     * Incrementa los registros e intenta cumplir el deadline (cuenta regresiva).
     */
    public void avanzarCicloReloj() {
        this.pc++;
        this.mar++;
        this.tiempoRestanteDeadline--;
    }

    // ==========================================
    // GETTERS Y SETTERS ESTÁNDAR
    // ==========================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    /**
     * Asigna el estado basándose en la enumeración interna para asegurar consistencia.
     * @param estadoProceso Instancia del enum EstadoProceso
     */
    public void setEstado(EstadoProceso estadoProceso) {
        this.estado = estadoProceso.name();
    }
    
    /**
     * Sobrecarga del setter para aceptar un String, validando el mapeo.
     * @param estado String del estado a asignar.
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantidadInstrucciones() {
        return cantidadInstrucciones;
    }

    public void setCantidadInstrucciones(int cantidadInstrucciones) {
        this.cantidadInstrucciones = cantidadInstrucciones;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getTiempoRestanteDeadline() {
        return tiempoRestanteDeadline;
    }

    public void setTiempoRestanteDeadline(int tiempoRestanteDeadline) {
        this.tiempoRestanteDeadline = tiempoRestanteDeadline;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getMar() {
        return mar;
    }

    public void setMar(int mar) {
        this.mar = mar;
    }

    public boolean isEsTipoIO() {
        return esTipoIO;
    }

    public void setEsTipoIO(boolean esTipoIO) {
        this.esTipoIO = esTipoIO;
    }

    public int getCiclosParaExcepcionIO() {
        return ciclosParaExcepcionIO;
    }

    public void setCiclosParaExcepcionIO(int ciclosParaExcepcionIO) {
        this.ciclosParaExcepcionIO = ciclosParaExcepcionIO;
    }

    public int getCiclosParaSatisfacerIO() {
        return ciclosParaSatisfacerIO;
    }

    public void setCiclosParaSatisfacerIO(int ciclosParaSatisfacerIO) {
        this.ciclosParaSatisfacerIO = ciclosParaSatisfacerIO;
    }
}