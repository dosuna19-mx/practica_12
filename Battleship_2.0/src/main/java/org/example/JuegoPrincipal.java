
package org.example;

import javax.swing.JOptionPane;

public class JuegoPrincipal {

    public static void main(String[] args) {
        // Crea el objeto Interfaz para elegir el tipo de muestra
        IConsola interfazSeleccionada = seleccionarModoUI();

        // Si hay un valor distinto de Nulo, inicia el juego
        if (interfazSeleccionada != null) {
            iniciarJuego(interfazSeleccionada);
        } else {
            System.out.println("No se seleccionó ninguna opción.");
        }
    }

    // Metodo para seleccionar una opcion (Entre Terminal y Ventana)
    private static IConsola seleccionarModoUI() {

        String[] opciones = {"Modo Gráfico (Ventana)", "Modo Consola (Terminal)"};

        // Muestra un mensaje gráfico
        int seleccion = JOptionPane.showOptionDialog(
                null,
                "Selecciona el modo de interfaz para BATTLESHIP:",
                "Selector de Interfaz",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (seleccion == 0) { // Modo Gráfico
            return new VistaGrafica();
        } else if (seleccion == 1) { // Modo Consola
            return new VistaTerminal();
        } else {
            return null; // Cancelar
        }
    }

    // Metodo para mandar la interfaz al GestorPartida (Dependiendo si es Terminal o Consola)
    private static void iniciarJuego(IConsola ui) {
        try {
            // Crea el objeto que controla el flujo de partida y lo inicia
            // (Se le manda al constructor el tipo de interfaz seleccionado)
            GestorPartida gestor = new GestorPartida(ui);
            gestor.iniciar();
        } catch (Exception e) {
            // Mensaje en caso de error
            ui.mostrarMensaje("Error fatal al iniciar el juego: " + e.getMessage());
            e.printStackTrace();
        }
    }
}