package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JuegoBattleshipTest {

    private JuegoBattleship juego;
    private Tablero tableroPropioReal;
    private Tablero tableroEnemigoReal;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Inicializar el objeto
        juego = new JuegoBattleship();

        Field propioField = JuegoBattleship.class.getDeclaredField("tableroPropio");
        propioField.setAccessible(true);
        tableroPropioReal = (Tablero) propioField.get(juego);

        Field enemigoField = JuegoBattleship.class.getDeclaredField("tableroEnemigo");
        enemigoField.setAccessible(true);
        tableroEnemigoReal = (Tablero) enemigoField.get(juego);

        // Limpiar las posiciones disparadas para cada prueba
        Field disparadasField = JuegoBattleship.class.getDeclaredField("posicionesDisparadas");
        disparadasField.setAccessible(true);
        ((java.util.Set<?>) disparadasField.get(juego)).clear();
    }

    // Metodo auxiliar para simular un barco hundido o en cierta posición
    private void simularImpactos(String tipoBarco, int impactos) throws NoSuchFieldException, IllegalAccessException {
        Field impactosField = Tablero.class.getDeclaredField("impactosPorBarco");
        impactosField.setAccessible(true);
        java.util.Map<String, Integer> impactosPorBarco = (java.util.Map<String, Integer>) impactosField.get(tableroPropioReal);

        if (impactosPorBarco.containsKey(tipoBarco)) {
            impactosPorBarco.put(tipoBarco, impactos);
        }
    }


    @Test
    void procesarDisparoOponente() {
        boolean esImpactoFallo = tableroPropioReal.recibirDisparo(0, 0); // Llamada directa para testear el Tablero real
        assertFalse(esImpactoFallo, "Si 0,0 es agua, debe ser un fallo.");
        juego.procesarDisparoOponente(0, 0);
    }

    @Test
    void estaBarcoHundido() throws NoSuchFieldException, IllegalAccessException {
        String destructor = "DESTRUCTOR"; // Tamaño 2

        // Prueba 1: Barco No Hundido (0 impactos)
        simularImpactos(destructor, 0);
        assertFalse(juego.estaBarcoHundido(destructor), "El destructor (0/2 impactos) no debe estar hundido.");

        // Prueba 2: Barco Parcialmente Hundido (1 impacto)
        simularImpactos(destructor, 1);
        assertFalse(juego.estaBarcoHundido(destructor), "El destructor (1/2 impactos) no debe estar hundido.");

        // Prueba 3: Barco Hundido (2 impactos)
        simularImpactos(destructor, 2);
        assertTrue(juego.estaBarcoHundido(destructor), "El destructor (2/2 impactos) debe estar hundido.");

        // Prueba 4: Barco con más impactos del necesario (prueba de robustez)
        simularImpactos(destructor, 5);
        assertTrue(juego.estaBarcoHundido(destructor), "El destructor (5/2 impactos) debe estar hundido.");
    }

    @Test
    void obtenerTipoBarcoEn() {
        String tipo = juego.obtenerTipoBarcoEn(5, 5);
        assertEquals("DESCONOCIDO", tipo, "Debe retornar DESCONOCIDO si la posición no ha sido impactada o es agua.");
    }

    @Test
    void registrarResultadoDisparoPropio() {
        int fila = 8, columna = 8;

        // Prueba 1: Fallo
        juego.registrarResultadoDisparoPropio(ManejadorMensajes.FALLO, fila, columna);
        char celdaFallo = tableroEnemigoReal.getGrid()[fila][columna];
        assertEquals('O', celdaFallo, "El fallo debe registrar 'O' en el tablero enemigo.");
        assertTrue(juego.yaDisparado(fila, columna), "La posición de fallo debe estar registrada.");

        // Prueba 2: Impacto
        int filaImpacto = 1, columnaImpacto = 1;
        juego.registrarResultadoDisparoPropio(ManejadorMensajes.IMPACTO, filaImpacto, columnaImpacto);
        char celdaImpacto = tableroEnemigoReal.getGrid()[filaImpacto][columnaImpacto];
        assertEquals('X', celdaImpacto, "El impacto debe registrar 'X' en el tablero enemigo.");
        assertTrue(juego.yaDisparado(filaImpacto, columnaImpacto), "La posición de impacto debe estar registrada.");

        // Prueba 3: Hundido
        int filaHundido = 0, columnaHundido = 0;
        juego.registrarResultadoDisparoPropio(ManejadorMensajes.HUNDIDO, filaHundido, columnaHundido);
        char celdaHundido = tableroEnemigoReal.getGrid()[filaHundido][columnaHundido];
        assertEquals('X', celdaHundido, "El hundimiento debe registrar 'X' en el tablero enemigo.");
        assertTrue(juego.yaDisparado(filaHundido, columnaHundido), "La posición de hundimiento debe estar registrada.");
    }

    @Test
    void yaDisparado() {
        int fila = 2, columna = 7;

        // 1. Probar que una coordenada no disparada retorna false
        assertFalse(juego.yaDisparado(fila, columna), "Una coordenada sin disparar debe retornar false.");

        // 2. Registrar la posición (simulando un impacto)
        juego.registrarResultadoDisparoPropio(ManejadorMensajes.IMPACTO, fila, columna);

        // 3. Probar que la coordenada ahora retorna true
        assertTrue(juego.yaDisparado(fila, columna), "Una coordenada registrada debe retornar true.");
    }
}