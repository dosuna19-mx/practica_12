package battleship;

import java.io.*;
import java.net.*;
import java.util.*;

public class BattleshipP2P {
    private static final int PUERTO = 12345;
    private Socket socket;
    private ServerSocket serverSocket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private JuegoBattleship juego;
    private boolean esServidor;
    private String nombreJugador;
    private Scanner scanner;

    public BattleshipP2P() {
        this.scanner = new Scanner(System.in);
        this.juego = new JuegoBattleship();
    }

    public void iniciar() {
        System.out.println("=== BATTLESHIP P2P ===");
        System.out.print("Ingresa tu nombre: ");
        this.nombreJugador = scanner.nextLine();

        elegirModo();
    }

    private void elegirModo() {
        while (true) {
            System.out.println("\nSelecciona modo:");
            System.out.println("1. Crear partida (Esperar conexión)");
            System.out.println("2. Unirse a partida (Conectar a otro jugador)");
            System.out.print("Opción: ");

            String opcion = scanner.nextLine();

            if ("1".equals(opcion)) {
                esServidor = true;
                esperarConexion();
                break;
            } else if ("2".equals(opcion)) {
                esServidor = false;
                conectarAPartida();
                break;
            } else {
                System.out.println("Opción inválida. Intenta nuevamente.");
            }
        }
    }

    private void esperarConexion() {
        try {
            System.out.println("\nIniciando servidor en puerto " + PUERTO + "...");
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("Esperando conexión de otro jugador...");

            socket = serverSocket.accept();
            System.out.println("¡Jugador conectado desde: " + socket.getInetAddress() + "!");

            configurarFlujos();
            intercambiarNombres();
            iniciarJuego();

        } catch (IOException e) {
            System.err.println("Error al esperar conexión: " + e.getMessage());
        }
    }

    private void conectarAPartida() {
        try {
            System.out.print("\nIngresa la IP del otro jugador: ");
            String ip = scanner.nextLine();

            System.out.println("Conectando a " + ip + ":" + PUERTO + "...");
            socket = new Socket(ip, PUERTO);
            System.out.println("¡Conectado exitosamente!");

            configurarFlujos();
            intercambiarNombres();
            iniciarJuego();

        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            System.out.println("¿Deseas intentar nuevamente? (s/n)");
            String respuesta = scanner.nextLine();
            if (respuesta.equalsIgnoreCase("s")) {
                conectarAPartida();
            }
        }
    }

    private void configurarFlujos() throws IOException {
        salida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void intercambiarNombres() throws IOException {
        if (esServidor) {
            // Esperar nombre del cliente
            String nombreOponente = entrada.readLine();
            salida.println(nombreJugador);
            System.out.println("Jugando contra: " + nombreOponente);
        } else {
            // Enviar nombre primero
            salida.println(nombreJugador);
            String nombreOponente = entrada.readLine();
            System.out.println("Jugando contra: " + nombreOponente);
        }
    }

    private void iniciarJuego() throws IOException {
        System.out.println("\n=== INICIANDO JUEGO ===");

        // Colocar barcos automáticamente
        juego.colocarBarcosAutomaticamente();
        System.out.println("Tus barcos han sido colocados automáticamente.");
        juego.mostrarTableroPropio();

        boolean juegoActivo = true;
        boolean miTurno = esServidor; // El servidor comienza

        try {
            salida.println(ProtocoloBattleship.LISTO);
            String respuesta = entrada.readLine();

            if (respuesta == null) {
                System.out.println("El oponente se desconectó durante la inicialización.");
                return;
            }

            if (ProtocoloBattleship.LISTO.equals(respuesta)) {
                System.out.println("¡Ambos jugadores listos! El juego comienza.");

                if (miTurno) {
                    System.out.println("\n¡Tú comienzas!");
                } else {
                    System.out.println("\nEl oponente comienza...");
                }

                while (juegoActivo) {
                    if (miTurno) {
                        juegoActivo = turnoLocal();
                        if (juegoActivo) {
                            miTurno = false;
                        }
                    } else {
                        juegoActivo = turnoRemoto();
                        if (juegoActivo) {
                            miTurno = true;
                        }
                    }

                    // Pequeña pausa para estabilizar la comunicación
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private boolean turnoLocal() throws IOException {
        System.out.println("\n=== TU TURNO ===");
        juego.mostrarTableroEnemigo();
        juego.mostrarTableroPropio();

        int[] disparo = obtenerDisparoJugador();
        salida.println(ProtocoloBattleship.construirMensajeDisparo(disparo[0], disparo[1]));

        String respuesta = entrada.readLine();

        // VERIFICACIÓN DE NULL AÑADIDA
        if (respuesta == null) {
            System.out.println("El oponente se desconectó o hubo un error en la comunicación.");
            return false;
        }

        try {
            ProtocoloBattleship.Mensaje mensaje = ProtocoloBattleship.parsearMensaje(respuesta);

            switch (mensaje.comando) {
                case ProtocoloBattleship.IMPACTO:
                    System.out.println("¡IMPACTO en (" + mensaje.x + "," + mensaje.y + ")!");
                    juego.registrarImpacto(mensaje.x, mensaje.y);
                    return true;

                case ProtocoloBattleship.FALLO:
                    System.out.println("FALLO en (" + mensaje.x + "," + mensaje.y + ")");
                    juego.registrarFallo(mensaje.x, mensaje.y);
                    return true;

                case ProtocoloBattleship.HUNDIDO:
                    System.out.println("¡HUNDIDO! " + mensaje.tipoBarco + " en (" + mensaje.x + "," + mensaje.y + ")");
                    juego.registrarImpacto(mensaje.x, mensaje.y);
                    return true;

                case ProtocoloBattleship.JUEGO_TERMINADO:
                    System.out.println("¡FELICIDADES! ¡HAS GANADO!");
                    return false;

                default:
                    System.out.println("Respuesta inesperada: " + respuesta);
                    return true;
            }
        } catch (Exception e) {
            System.out.println("Error procesando respuesta: " + e.getMessage());
            System.out.println("Respuesta recibida: " + respuesta);
            return false;
        }
    }

   private boolean turnoRemoto() throws IOException {
    System.out.println("\n=== TURNO DEL OPONENTE ===");
    System.out.println("Esperando disparo del oponente...");
    
    String mensajeEntrante = entrada.readLine();
    
    if (mensajeEntrante == null) {
        System.out.println("El oponente se desconectó.");
        return false;
    }
    
    try {
        ProtocoloBattleship.Mensaje mensaje = ProtocoloBattleship.parsearMensaje(mensajeEntrante);
        
        if (ProtocoloBattleship.DISPARAR.equals(mensaje.comando)) {
            boolean impacto = juego.recibirDisparo(mensaje.x, mensaje.y);
            
            if (impacto) {
                String tipoBarco = juego.obtenerTipoBarcoEn(mensaje.x, mensaje.y);
                
                // VERIFICACIÓN MEJORADA
                if (tipoBarco.equals("DESCONOCIDO")) {
                    // No podemos determinar el tipo de barco, solo decimos IMPACTO
                    salida.println(ProtocoloBattleship.construirMensajeResultado(
                        ProtocoloBattleship.IMPACTO, mensaje.x, mensaje.y, null));
                    System.out.println("El oponente impactó en (" + mensaje.x + "," + mensaje.y + ")");
                } else if (juego.estaBarcoHundido(tipoBarco)) {
                    salida.println(ProtocoloBattleship.construirMensajeResultado(
                        ProtocoloBattleship.HUNDIDO, mensaje.x, mensaje.y, tipoBarco));
                    
                    if (juego.todosBarcosHundidos()) {
                        salida.println(ProtocoloBattleship.JUEGO_TERMINADO);
                        System.out.println("El oponente hundió tu " + tipoBarco);
                        System.out.println("¡HAS PERDIDO!");
                        return false;
                    } else {
                        System.out.println("El oponente hundió tu " + tipoBarco + " en (" + mensaje.x + "," + mensaje.y + ")");
                    }
                } else {
                    salida.println(ProtocoloBattleship.construirMensajeResultado(
                        ProtocoloBattleship.IMPACTO, mensaje.x, mensaje.y, null));
                    System.out.println("El oponente impactó en (" + mensaje.x + "," + mensaje.y + ")");
                }
            } else {
                salida.println(ProtocoloBattleship.construirMensajeResultado(
                    ProtocoloBattleship.FALLO, mensaje.x, mensaje.y, null));
                System.out.println("El oponente falló en (" + mensaje.x + "," + mensaje.y + ")");
            }
        }
        
        juego.mostrarTableroPropio();
        return true;
        
    } catch (Exception e) {
        System.out.println("Error procesando mensaje del oponente: " + e.getMessage());
        e.printStackTrace(); // Esto te dará más detalles del error
        return false;
    }
}
    private int[] obtenerDisparoJugador() {
        while (true) {
            try {
                System.out.print("Ingresa coordenadas para disparar (fila,columna 0-9): ");
                String entrada = scanner.nextLine();
                String[] coordenadas = entrada.split(",");

                if (coordenadas.length != 2) {
                    System.out.println("Formato inválido. Usa: fila,columna");
                    continue;
                }

                int fila = Integer.parseInt(coordenadas[0].trim());
                int columna = Integer.parseInt(coordenadas[1].trim());

                if (fila >= 0 && fila < 10 && columna >= 0 && columna < 10) {
                    if (!juego.yaDisparado(fila, columna)) {
                        return new int[] { fila, columna };
                    } else {
                        System.out.println("Ya disparaste en esa posición.");
                    }
                } else {
                    System.out.println("Coordenadas fuera de rango. Usa números del 0 al 9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingresa números válidos.");
            }
        }
    }

    private void cerrarConexion() {
        try {
            if (entrada != null)
                entrada.close();
            if (salida != null)
                salida.close();
            if (socket != null)
                socket.close();
            if (serverSocket != null)
                serverSocket.close();
            scanner.close();
            System.out.println("Conexión cerrada.");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        BattleshipP2P juego = new BattleshipP2P();
        juego.iniciar();
    }
}
