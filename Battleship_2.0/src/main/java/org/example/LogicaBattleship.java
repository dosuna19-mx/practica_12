package org.example;
import java.util.HashSet;
import java.util.Set;

// AHORA IMPLEMENTA LA INTERFAZ IVistaTablero
public class LogicaBattleship {

    private Tablero tableroPropio;
    private Tablero tableroEnemigo;
    private Set<String> posicionesDisparadas; // Para validar la entrada del usuario

    public LogicaBattleship() {
        // El tablero propio maneja los barcos
        this.tableroPropio = new Tablero();
        // El tablero enemigo solo maneja el estado de las celdas (desconocido, fallo, impacto)
        this.tableroEnemigo = new Tablero(true);
        this.posicionesDisparadas = new HashSet<>();
    }

    // Inicialización y Estado

    public void colocarBarcos() {
        tableroPropio.colocarBarcosAutomaticamente();
    }

    public boolean todosBarcosHundidos() {
        return tableroPropio.todosBarcosHundidos();
    }

    // MÉTODOS DE LA INTERFAZ IVistaTablero

    public char[][] getTableroPropioGrid() {
        return tableroPropio.getGrid();
    }


    public char[][] getTableroEnemigoGrid() {
        return tableroEnemigo.getGrid();
    }

    // Lógica de Disparo

    public boolean procesarDisparoOponente(int fila, int columna) {

        return tableroPropio.recibirDisparo(fila, columna);
    }

    public boolean estaBarcoHundido(String tipoBarco) {
        return tableroPropio.estaBarcoHundido(tipoBarco);
    }

    public String obtenerTipoBarcoEn(int fila, int columna) {
        return tableroPropio.obtenerTipoBarcoEn(fila, columna);
    }


    // Registra el resultado de nuestro disparo en el tablero enemigo.

    public void registrarResultadoDisparoPropio(String comando, int fila, int columna) {
        if (ManejadorMensajes.IMPACTO.equals(comando) || ManejadorMensajes.HUNDIDO.equals(comando)) {
            tableroEnemigo.registrarImpactoEnemigo(fila, columna);
        } else if (ManejadorMensajes.FALLO.equals(comando)) {
            tableroEnemigo.registrarFalloEnemigo(fila, columna);
        }

        // Registrar que la posición fue disparada
        posicionesDisparadas.add(fila + "," + columna);
    }

    public boolean yaDisparado(int fila, int columna) {
        return posicionesDisparadas.contains(fila + "," + columna);
    }

}