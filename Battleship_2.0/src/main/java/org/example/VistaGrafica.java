package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VistaGrafica implements IConsola {

    private JFrame frame;
    private JLabel labelMensaje;
    private JPanel panelTableroPropio;
    private JPanel panelTableroEnemigo;

    // Variables para la sincronización del disparo (wait/notify)
    private final Object lockDisparo = new Object();
    private int[] coordenadasDisparo = null;

    public VistaGrafica() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Configuración de la Ventana Principal
        frame = new JFrame("Battleship P2P - Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        // Panel Superior para Mensajes
        labelMensaje = new JLabel("Bienvenido a Battleship", SwingConstants.CENTER);
        labelMensaje.setFont(new Font("Arial", Font.BOLD, 16));
        labelMensaje.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(labelMensaje, BorderLayout.NORTH);

        // Panel Central para los Tableros (Usamos GridLayout de 1 fila, 2 columnas)
        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 20, 0));

        // Inicializamos los paneles de los tableros
        panelTableroPropio = new JPanel(new GridLayout(10, 10)); // 10x10 celdas
        panelTableroPropio.setBorder(BorderFactory.createTitledBorder("TU TABLERO"));

        panelTableroEnemigo = new JPanel(new GridLayout(10, 10)); // 10x10 celdas
        panelTableroEnemigo.setBorder(BorderFactory.createTitledBorder("TABLERO ENEMIGO (Click para disparar)"));

        panelCentral.add(panelTableroPropio);
        panelCentral.add(panelTableroEnemigo);

        frame.add(panelCentral, BorderLayout.CENTER);

        // Hacemos visible la ventana
        frame.setVisible(true);
    }

    // ==========================================
    // IMPLEMENTACIÓN DE IConsola
    // ==========================================

    @Override
    public void mostrarMensaje(String mensaje) {
        // Actualizamos el JLabel en el hilo de despacho de eventos
        SwingUtilities.invokeLater(() -> labelMensaje.setText(mensaje));
    }

    @Override
    public void mostrarTablero(char[][] tablero, String titulo) {
        SwingUtilities.invokeLater(() -> {
            JPanel panelDestino = titulo.contains("TU TABLERO") ? panelTableroPropio : panelTableroEnemigo;
            actualizarGrid(panelDestino, tablero, titulo.contains("ENEMIGO"));
        });
    }

    @Override
    public String solicitarNombre() {
        return JOptionPane.showInputDialog(frame, "Ingresa tu nombre:", "Configuración", JOptionPane.QUESTION_MESSAGE);
    }

    @Override
    public String solicitarIP() {
        return JOptionPane.showInputDialog(frame, "Ingresa la IP del oponente:", "127.0.0.1");
    }

    @Override
    public String obtenerOpcionModo() {
        String[] opciones = {"Servidor (Esperar)", "Cliente (Conectar)"};
        int seleccion = JOptionPane.showOptionDialog(frame, "Selecciona tu rol:", "Modo de Juego",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);
        return (seleccion == 0) ? "1" : "2";
    }

    @Override
    public int[] obtenerDisparoJugador(boolean yaDisparado) {
        mostrarMensaje("¡TU TURNO! Selecciona una casilla en el tablero enemigo.");

        synchronized (lockDisparo) {
            coordenadasDisparo = null; // Reiniciar disparo anterior

            // Esperar hasta que el usuario haga clic en un botón (notify)
            while (coordenadasDisparo == null) {
                try {
                    lockDisparo.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return coordenadasDisparo;
        }
    }

    // MÉTODOS AUXILIARES GRÁFICOS

    private void actualizarGrid(JPanel panel, char[][] matriz, boolean esEnemigo) {
        // Limpiar el panel para redibujarlo
        panel.removeAll();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Obtenemos el caracter lógico (ej: 'P', 'X', '~')
                char celda = matriz[i][j];

                // Convertir ese caracter en una IMAGEN
                ImageIcon icono = cargarImagen(celda, esEnemigo);

                if (esEnemigo) {
                    // TABLERO ENEMIGO (Botones interactivos)
                    JButton boton = new JButton();
                    boton.setIcon(icono);

                    // Ajustes visuales para que se vea bien
                    boton.setContentAreaFilled(false); // Quitar fondo estándar del botón
                    boton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

                    final int fila = i;
                    final int col = j;

                    // Al hacer clic, enviamos la coordenada
                    boton.addActionListener(e -> notificarDisparo(fila, col));

                    panel.add(boton);

                } else {
                    // TABLERO PROPIO (Solo visualización)
                    JLabel etiqueta = new JLabel();
                    etiqueta.setIcon(icono);
                    etiqueta.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    panel.add(etiqueta);
                }
            }
        }

        panel.revalidate();
        panel.repaint();
    }

    private void notificarDisparo(int fila, int col) {
        synchronized (lockDisparo) {
            coordenadasDisparo = new int[]{fila, col};
            // Desbloquea obtenerDisparoJugador
            lockDisparo.notifyAll();
        }
    }

    // Carga de imágenes desde resources
    private ImageIcon cargarImagen(char c) {
        String rutaImagen;
        switch (c) {
            case 'P': rutaImagen = "/img/porta_tr.png"; break;
            case 'A': rutaImagen = "/img/acora_tr.png"; break;
            case 'C': rutaImagen = "/img/cruce_tr.png"; break;
            case 'S': rutaImagen = "/img/subm_tr.png"; break;
            case 'D': rutaImagen = "/img/dest_tr.png"; break;
            case 'X': rutaImagen = "/img/equis.png"; break;
            case 'O': rutaImagen = "/img/equis.png"; break;
            case '~':
            case ' ':
            default:  rutaImagen = "/img/agua.png"; break;
        }

        URL url = getClass().getResource(rutaImagen);
        if (url != null) {
            // Escalar la imagen para que quepa en el botón (aprox 40x40)
            ImageIcon original = new ImageIcon(url);
            Image img = original.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            // Si no encuentra la imagen, retorna null (el botón se verá vacío o con texto si se lo pones)
            // System.err.println("Imagen no encontrada: " + rutaImagen);
            return null;
        }
    }

    private ImageIcon cargarImagen(char c, boolean esEnemigo) {
        String rutaImagen;

        // Si es el enemigo, ocultamos los barcos
        if (esEnemigo && (c == 'P' || c == 'A' || c == 'C' || c == 'S' || c == 'D')) {
            c = ' '; // Lo tratamos como agua
        }

        switch (c) {
            //  BARCOS
            case 'P': rutaImagen = "/img/porta_tr.png"; break; // Portaaviones
            case 'A': rutaImagen = "/img/acora_tr.png"; break; // Acorazado
            case 'C': rutaImagen = "/img/cruce_tr.png"; break; // Crucero
            case 'S': rutaImagen = "/img/subm_tr.png";  break; // Submarino
            case 'D': rutaImagen = "/img/dest_tr.png";  break; // Destructor

            // ESTADOS DEL JUEGO
            case 'X': rutaImagen = "/img/equis.png";    break; // Impacto (Tu imagen equis.png)
            case 'O': rutaImagen = "/img/equis.png";    break; // Fallo (Si no tienes, usa Tablero.png o crea una gris)

            // AGUA / FONDO
            case '~':
            case ' ':
            case '?':
            default:  rutaImagen = "/img/agua.png";  break; // Tu fondo de agua
        }

        // Cargar y escalar la imagen
        java.net.URL url = getClass().getResource(rutaImagen);
        if (url != null) {
            ImageIcon original = new ImageIcon(url);
            // Escalamos la imagen a 40x40 píxeles (ajusta según el tamaño de tu ventana)
            Image img = original.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {

            return null;
        }
    }
}