package org.example;

public interface InterfazUsuario {
    // Muestra un mensaje simple en la interfaz.
    void mostrarMensaje(String mensaje);

    // Muestra el tablero al usuario.
    void mostrarTablero(char[][] tablero, String titulo);

    // Solicita al usuario el modo de juego (Servidor/Cliente).
    String obtenerOpcionModo();

    // Solicita al usuario su nombre.
    String solicitarNombre();

    // Solicita al usuario la IP para conectarse.
    String solicitarIP();

    // Solicita y valida las coordenadas de disparo del usuario.
    int[] obtenerDisparoJugador(boolean yaDisparado); // El parámetro 'yaDisparado' se actualizará en el controlador
}
