package org.example;

public interface IConsola {
    // Muestra un mensaje en la interfaz.
    public void mostrarMensaje(String mensaje);

    // Muestra el tablero al usuario.
    public void mostrarTablero(char[][] tablero, String titulo);

    // Solicita al usuario el modo de juego (Servidor/Cliente).
    public String obtenerOpcionModo();

    // Solicita al usuario su nombre.
    public String solicitarNombre();

    // Solicita al usuario la IP para conectarse.
    public String solicitarIP();

    // Solicita y valida las coordenadas de disparo del usuario.
    public int[] obtenerDisparoJugador(boolean yaDisparado); // El parámetro 'yaDisparado' se actualizará en el controlador
}


