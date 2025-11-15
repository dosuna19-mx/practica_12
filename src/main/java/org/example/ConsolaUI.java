package org.example;
import java.util.Scanner;

public class ConsolaUI implements InterfazUsuario{
    private Scanner scanner;
    private static final int TAMANIO_TABLERO = 10;

    public ConsolaUI() {
        this.scanner = new Scanner(System.in);
    }

    // Implementación de Métodos de la Interfaz

    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public void mostrarTablero(char[][] tablero, String titulo) {
        mostrarMensaje("\n=== " + titulo + " ===");

        System.out.print("  ");
        for (int i = 0; i < TAMANIO_TABLERO; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        for (int i = 0; i < TAMANIO_TABLERO; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < TAMANIO_TABLERO; j++) {
                System.out.print(tablero[i][j] + " ");
            }
            System.out.println();
        }
        mostrarMensaje("\nLeyenda: ~=Agua, ?=Desconocido, X=Impacto, O=Fallo, Letras=Barcos");
    }

    @Override
    public String solicitarNombre() {
        mostrarMensaje("Ingresa tu nombre: ");
        return scanner.nextLine();
    }

    @Override
    public String obtenerOpcionModo() {
        while (true) {
            mostrarMensaje("\nSelecciona modo:");
            mostrarMensaje("1. Crear partida (Esperar conexión)");
            mostrarMensaje("2. Unirse a partida (Conectar a otro jugador)");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();

            if ("1".equals(opcion) || "2".equals(opcion)) {
                return opcion;
            } else {
                mostrarMensaje("Opción inválida. Intenta nuevamente.");
            }
        }
    }

    @Override
    public String solicitarIP() {
        System.out.print("\nIngresa la IP del otro jugador: ");
        return scanner.nextLine();
    }

    @Override
    public int[] obtenerDisparoJugador(boolean yaDisparado) {
        while (true) {
            try {
                System.out.print("Ingresa coordenadas para disparar (fila,columna 0-9): ");
                String entrada = scanner.nextLine();
                String[] coordenadas = entrada.split(",");

                if (coordenadas.length != 2) {
                    mostrarMensaje("Formato inválido. Usa: fila,columna");
                    continue;
                }

                int fila = Integer.parseInt(coordenadas[0].trim());
                int columna = Integer.parseInt(coordenadas[1].trim());

                if (fila >= 0 && fila < 10 && columna >= 0 && columna < 10) {
                    // La validación de 'yaDisparado' se hace en el controlador,
                    // pero mantenemos el loop hasta obtener coordenadas válidas.
                    return new int[] { fila, columna };
                } else {
                    mostrarMensaje("Coordenadas fuera de rango. Usa números del 0 al 9.");
                }
            } catch (NumberFormatException e) {
                mostrarMensaje("Por favor ingresa números válidos.");
            }
        }
    }

    // Metodo para cerrar el Scanner
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
