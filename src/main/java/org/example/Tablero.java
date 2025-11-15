package org.example;
import java.util.*;
public class Tablero {

        private static final int TAMANIO_TABLERO = 10;
        private char[][] grid; // Renombrado de tableroPropio a grid para ser genérico
        private Map<String, Integer> barcos;
        private Map<String, Integer> impactosPorBarco;

        // Constructor diseñado para ser usado para el tablero PROPIO
        public Tablero() {
            this.grid = new char[TAMANIO_TABLERO][TAMANIO_TABLERO];
            this.barcos = new HashMap<>();
            this.barcos.put("PORTAAVIONES", 5);
            this.barcos.put("ACORAZADO", 4);
            this.barcos.put("CRUCERO", 3);
            this.barcos.put("SUBMARINO", 3);
            this.barcos.put("DESTRUCTOR", 2);

            this.impactosPorBarco = new HashMap<>();
            for (String barco : barcos.keySet()) {
                this.impactosPorBarco.put(barco, 0);
            }

            inicializar();
        }

        // Constructor auxiliar para el tablero ENEMIGO (solo para registrar disparos)
        public Tablero(boolean esTableroEnemigo) {
            this.grid = new char[TAMANIO_TABLERO][TAMANIO_TABLERO];
            this.barcos = Collections.emptyMap(); // No necesita la lista de barcos
            this.impactosPorBarco = Collections.emptyMap(); // No necesita conteo de impactos
            inicializar(esTableroEnemigo);
        }

        private void inicializar() {
            inicializar(false);
        }

        private void inicializar(boolean esEnemigo) {
            char simboloInicial = esEnemigo ? '?' : '~';
            for (int i = 0; i < TAMANIO_TABLERO; i++) {
                for (int j = 0; j < TAMANIO_TABLERO; j++) {
                    grid[i][j] = simboloInicial;
                }
            }
        }

        // Métodos de Colocación de Barcos

    public void colocarBarcosAutomaticamente() {
        Random random = new Random();

        for (Map.Entry<String, Integer> entrada : barcos.entrySet()) {
            String nombreBarco = entrada.getKey();
            int tamanio = entrada.getValue();
            boolean colocado = false;

            while (!colocado) {
                boolean horizontal = random.nextBoolean(); // Genera el valor
                int fila = random.nextInt(TAMANIO_TABLERO);
                int columna = random.nextInt(TAMANIO_TABLERO);

                if (puedeColocarBarco(fila, columna, tamanio, horizontal)) {
                    colocarBarco(fila, columna, tamanio, horizontal, nombreBarco.charAt(0));
                    colocado = true;
                }
            }
        }
    }

    private boolean puedeColocarBarco(int fila, int columna, int tamanio, boolean horizontal) {

        if (horizontal) {
            if (columna + tamanio > TAMANIO_TABLERO) return false;
            for (int i = columna; i < columna + tamanio; i++) {
                if (grid[fila][i] != '~') return false;
            }
        } else {
            if (fila + tamanio > TAMANIO_TABLERO) return false;
            for (int i = fila; i < fila + tamanio; i++) {
                if (grid[i][columna] != '~') return false;
            }
        }
        return true;
    }

    private void colocarBarco(int fila, int columna, int tamanio, boolean horizontal, char simbolo) {

        if (horizontal) {
            for (int i = columna; i < columna + tamanio; i++) {
                grid[fila][i] = simbolo;
            }
        } else {
            for (int i = fila; i < fila + tamanio; i++) {
                grid[i][columna] = simbolo;
            }
        }
    }

        // Metodos de Recepción de Disparo (Centrales para la lógica)


        public boolean recibirDisparo(int fila, int columna) {


            if (grid[fila][columna] == 'X' || grid[fila][columna] == 'O') {
                // En un juego P2P, esto no debería pasar si la validación del turno es correcta,
                // pero mantenemos la lógica de seguridad.
                return false;
            }

            if (grid[fila][columna] != '~') {

                char caracterBarco = grid[fila][columna];
                String tipoBarco = obtenerTipoBarcoDesdeCaracter(caracterBarco);

                if (impactosPorBarco.containsKey(tipoBarco)) {
                    impactosPorBarco.put(tipoBarco, impactosPorBarco.get(tipoBarco) + 1);
                }

                grid[fila][columna] = 'X'; // Marcar como impactado
                return true;
            } else {
                grid[fila][columna] = 'O'; // Marcar como agua impactada
                return false;
            }
        }

        public String obtenerTipoBarcoEn(int fila, int columna) {
            char c = grid[fila][columna];
            return obtenerTipoBarcoDesdeCaracter(c);
        }

        private String obtenerTipoBarcoDesdeCaracter(char c) {
            switch (c) {
                case 'P': return "PORTAAVIONES";
                case 'A': return "ACORAZADO";
                case 'C': return "CRUCERO";
                case 'S': return "SUBMARINO";
                case 'D': return "DESTRUCTOR";
                case 'X':

                    return "DESCONOCIDO";
                default: return "DESCONOCIDO";
            }
        }

        public boolean estaBarcoHundido(String tipoBarco) {
            if (!impactosPorBarco.containsKey(tipoBarco) || !barcos.containsKey(tipoBarco)) {
                return false;
            }

            int impactos = impactosPorBarco.get(tipoBarco);
            int tamanio = barcos.get(tipoBarco);
            return impactos >= tamanio;
        }

        public boolean todosBarcosHundidos() {
            for (String barco : barcos.keySet()) {
                if (!estaBarcoHundido(barco)) {
                    return false;
                }
            }
            return true;
        }

        // Métodos de Interfaz (Para el Controlador)

        public char[][] getGrid() {
            return grid;
        }

        // Métodos para actualizar el Tablero Enemigo
        public void registrarImpactoEnemigo(int fila, int columna) {
            if (grid[fila][columna] == '?') {
                grid[fila][columna] = 'X';
            }
        }

        public void registrarFalloEnemigo(int fila, int columna) {
            if (grid[fila][columna] == '?') {
                grid[fila][columna] = 'O';
            }
        }
    }
