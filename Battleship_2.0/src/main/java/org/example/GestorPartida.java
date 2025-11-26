package org.example;

import java.io.IOException;
public class GestorPartida {

    // Objeto que maneja la logica del juego
    private LogicaBattleship juego;
    // Objeto que controla la conexion entre jugadores
    private ManejadorConexion conexion;
    // Se inicia la interfaz (proviene de la opcion del jugador)
    private IConsola ui;
    // Nombre del jugador
    private String nombreJugador;
    // Nombre del enemigo
    private String nombreOponente;

    // Constructor que recibe los parametros
    public GestorPartida(IConsola ui) {
        this.juego = new LogicaBattleship();
        this.conexion = new ManejadorConexion();
        this.ui = ui;
    }

    // Metodo encargado de mostrar los primeros mensajes (Solo mensajes, no dibuja)
    public void iniciar() {
        // ui es el objeto que el usuario selecciono, este puede cambiar entre la Terminal
        // y Consola
        ui.mostrarMensaje("=== BATTLESHIP P2P ===");
        this.nombreJugador = ui.solicitarNombre();

        if (elegirModo()) {
            iniciarJuego();
        }

        // CERRAR AL FINALIZAR
        conexion.cerrarConexion(ui);

        if (ui instanceof VistaTerminal) {
            ((VistaTerminal)ui).close();
        }
    }

    private boolean elegirModo() {
        String opcion;
        boolean conectado = false;

        while (!conectado) {
            opcion = ui.obtenerOpcionModo();

            if ("1".equals(opcion)) {
                // MODO DE SERVIDOR
                conectado = conexion.esperarConexion(ui);
            } else if ("2".equals(opcion)) {
                // MODO DE CLIENTE
                String ip = ui.solicitarIP();
                conectado = conexion.conectarAPartida(ip, ui);
            }

            if (!conectado) {
                ui.mostrarMensaje("Fallo en la conexión. Intenta nuevamente.");
            }
        }
        return conectado;
    }

    //
    private void iniciarJuego() {
        ui.mostrarMensaje("\n=== INICIANDO JUEGO ===");

        try {
            // INTERCAMBIAR NOMBRES
            this.nombreOponente = conexion.intercambiarNombres(nombreJugador);
            ui.mostrarMensaje("Jugando contra: " + nombreOponente);

            // COLOCACIÓN DE BARCOS
            juego.colocarBarcos();
            ui.mostrarMensaje("Tus barcos han sido colocados automáticamente.");
            ui.mostrarTablero(juego.getTableroPropioGrid(), "TU TABLERO");

            // Confirmación LISTO
            conexion.enviarMensaje(ManejadorMensajes.LISTO);
            String respuesta = conexion.recibirMensaje();

            if (respuesta == null || !ManejadorMensajes.LISTO.equals(respuesta)) {
                ui.mostrarMensaje("El oponente se desconectó o envió una respuesta inesperada durante la inicialización.");
                return;
            }

            ui.mostrarMensaje("¡Jugadores listos!");

            boolean juegoActivo = true;
            boolean miTurno = conexion.esServidor(); // COMIENZA SERVIDOR

            ui.mostrarMensaje(miTurno ? "\n¡Tú comienzas!" : "\nEl oponente comienza...");

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

                // Pausa
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException e) {
            ui.mostrarMensaje("Error de conexión durante el juego: " + e.getMessage());
        }
    }

    private boolean turnoLocal() throws IOException {
        ui.mostrarMensaje("\n=== TU TURNO ===");
        ui.mostrarTablero(juego.getTableroEnemigoGrid(), "TABLERO ENEMIGO");
        ui.mostrarTablero(juego.getTableroPropioGrid(), "TU TABLERO");

        int[] disparo = obtenerDisparoSeguro();

        // Enviar DISPARAR usando ManejadorMensajes
        conexion.enviarMensaje(ManejadorMensajes.construirMensajeDisparo(disparo[0], disparo[1]));

        // Recibir respuesta (IMPACTO, FALLO, HUNDIDO, JUEGO_TERMINADO)
        String respuesta = conexion.recibirMensaje();

        if (respuesta == null) {
            ui.mostrarMensaje("El oponente se desconectó o hubo un error en la comunicación.");
            return false;
        }

        try {
            // Parsear respuesta usando ManejadorMensajes
            ManejadorMensajes.Mensaje mensaje = ManejadorMensajes.parsearMensaje(respuesta);

            // Registrar resultado en nuestro tablero enemigo
            juego.registrarResultadoDisparoPropio(mensaje.comando, mensaje.x, mensaje.y);

            switch (mensaje.comando) {
                case ManejadorMensajes.IMPACTO:
                    ui.mostrarMensaje("¡IMPACTO en (" + mensaje.x + "," + mensaje.y + ")!");
                    return true;

                case ManejadorMensajes.FALLO:
                    ui.mostrarMensaje("FALLO en (" + mensaje.x + "," + mensaje.y + ")");
                    return true;

                case ManejadorMensajes.HUNDIDO:
                    ui.mostrarMensaje("¡HUNDIDO! " + mensaje.tipoBarco + " en (" + mensaje.x + "," + mensaje.y + ")");
                    return true;

                case ManejadorMensajes.JUEGO_TERMINADO:
                    ui.mostrarMensaje("¡FELICIDADES! ¡HAS GANADO!");
                    return false;

                default:
                    ui.mostrarMensaje("Respuesta inesperada del oponente: " + respuesta);
                    return true;
            }
        } catch (Exception e) {
            ui.mostrarMensaje("Error procesando respuesta: " + e.getMessage());
            return false;
        }
    }

    // manejar la validación de 'yaDisparado'
    private int[] obtenerDisparoSeguro() {
        int[] disparo;
        boolean valido = false;
        do {
            // ConsolaUI se encarga de obtener la entrada válida (0-9)
            disparo = ui.obtenerDisparoJugador(false);

            // La validación de la lógica (si ya disparó ahí) la hace el Controlador
            if (juego.yaDisparado(disparo[0], disparo[1])) {
                ui.mostrarMensaje("Ya disparaste en esa posición. Intenta de nuevo.");
            } else {
                valido = true;
            }
        } while (!valido);
        return disparo;
    }

    private boolean turnoRemoto() throws IOException {
        ui.mostrarMensaje("\n=== TURNO DEL OPONENTE ===");
        ui.mostrarMensaje("Esperando disparo de " + nombreOponente + "...");

        String mensajeEntrante = conexion.recibirMensaje();

        if (mensajeEntrante == null) {
            ui.mostrarMensaje("El oponente se desconectó.");
            return false;
        }

        try {
            ManejadorMensajes.Mensaje mensaje = ManejadorMensajes.parsearMensaje(mensajeEntrante);

            if (ManejadorMensajes.DISPARAR.equals(mensaje.comando)) {

                boolean impacto = juego.procesarDisparoOponente(mensaje.x, mensaje.y);
                String tipoBarco = juego.obtenerTipoBarcoEn(mensaje.x, mensaje.y);

                if (impacto) {
                    if (juego.estaBarcoHundido(tipoBarco)) {
                        // Hundido
                        conexion.enviarMensaje(ManejadorMensajes.construirMensajeResultado(
                                ManejadorMensajes.HUNDIDO, mensaje.x, mensaje.y, tipoBarco));

                        ui.mostrarMensaje("El oponente hundió tu " + tipoBarco + " en (" + mensaje.x + "," + mensaje.y + ")");

                        if (juego.todosBarcosHundidos()) {
                            // Hemos perdido
                            conexion.enviarMensaje(ManejadorMensajes.JUEGO_TERMINADO);
                            ui.mostrarMensaje("¡HAS PERDIDO! Todos tus barcos han sido hundidos.");
                            return false;
                        }
                    } else {
                        // Solo impacto
                        conexion.enviarMensaje(ManejadorMensajes.construirMensajeResultado(
                                ManejadorMensajes.IMPACTO, mensaje.x, mensaje.y, null));
                        ui.mostrarMensaje("El oponente impactó en (" + mensaje.x + "," + mensaje.y + ")");
                    }
                } else {
                    // Fallo
                    conexion.enviarMensaje(ManejadorMensajes.construirMensajeResultado(
                            ManejadorMensajes.FALLO, mensaje.x, mensaje.y, null));
                    ui.mostrarMensaje("El oponente falló en (" + mensaje.x + "," + mensaje.y + ")");
                }
            } else {
                ui.mostrarMensaje("Comando inesperado del oponente: " + mensaje.comando);
            }

            ui.mostrarTablero(juego.getTableroPropioGrid(), "TU TABLERO");
            return true;

        } catch (Exception e) {
            ui.mostrarMensaje("Error procesando mensaje del oponente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}