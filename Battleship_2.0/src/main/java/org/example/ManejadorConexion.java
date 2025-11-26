package org.example;
import java.io.*;
import java.net.*;

public class ManejadorConexion {
    private static final int PUERTO = 12345;
    private Socket socket;
    private ServerSocket serverSocket;
    private PrintWriter salida;
    private BufferedReader entrada;

    // Almacena si el objeto actúa como Servidor
    private boolean esServidor;

    // Métodos de Inicialización de Conexión

    public boolean esperarConexion(IConsola ui) {
        this.esServidor = true;
        try {
            ui.mostrarMensaje("\nIniciando servidor en puerto " + PUERTO + "...");
            serverSocket = new ServerSocket(PUERTO);
            ui.mostrarMensaje("Esperando conexión de otro jugador...");

            socket = serverSocket.accept();
            ui.mostrarMensaje("¡Jugador conectado desde: " + socket.getInetAddress() + "!");

            configurarFlujos();
            return true;
        } catch (IOException e) {
            ui.mostrarMensaje("Error al esperar conexión: " + e.getMessage());
            return false;
        }
    }

    public boolean conectarAPartida(String ip, IConsola ui) {
        this.esServidor = false;
        try {
            ui.mostrarMensaje("Conectando a " + ip + ":" + PUERTO + "...");
            socket = new Socket(ip, PUERTO);
            ui.mostrarMensaje("¡Conectado exitosamente!");

            configurarFlujos();
            return true;
        } catch (IOException e) {
            ui.mostrarMensaje("Error al conectar: " + e.getMessage());
            return false;
        }
    }

    private void configurarFlujos() throws IOException {
        salida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Métodos de Comunicación

    // Maneja el intercambio de nombres al inicio de la partida.

    public String intercambiarNombres(String nombreLocal) throws IOException {
        if (esServidor) {
            // Servidor: Espera nombre, luego envía el suyo
            String nombreOponente = entrada.readLine();
            salida.println(nombreLocal);
            return nombreOponente;
        } else {
            // Cliente: Envía nombre, luego espera el del oponente
            salida.println(nombreLocal);
            String nombreOponente = entrada.readLine();
            return nombreOponente;
        }
    }


    // Envía un mensaje a través del stream de salida.
    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    public String recibirMensaje() throws IOException {
        return entrada.readLine();
    }

    public boolean esServidor() {
        return esServidor;
    }


    // Cierra todos los recursos de red.

    public void cerrarConexion(IConsola ui) {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            ui.mostrarMensaje("Conexión cerrada.");
        } catch (IOException e) {
            ui.mostrarMensaje("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
