package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManejadorMensajesTest {

    // Comandos y Separadores de la clase ManejadorMensajes
    private static final String DISPARAR = ManejadorMensajes.DISPARAR;
    private static final String IMPACTO = ManejadorMensajes.IMPACTO;
    private static final String FALLO = ManejadorMensajes.FALLO;
    private static final String SEPARADOR_CAMPOS = ManejadorMensajes.SEPARADOR_CAMPOS;
    private static final String SEPARADOR_COORD = ManejadorMensajes.SEPARADOR_COORD;

    // =================================================================
    // TESTS DE CONSTRUCCIÓN (De parámetros a String)
    // =================================================================

    @Test
    void construirMensajeDisparo() {
        int x = 5;
        int y = 9;
        String esperado = DISPARAR + SEPARADOR_CAMPOS + x + SEPARADOR_COORD + y;

        String resultado = ManejadorMensajes.construirMensajeDisparo(x, y);

        assertEquals(esperado, resultado, "El mensaje de DISPARAR debe tener el formato: DISPARAR|x,y");
    }

    @Test
    void construirMensajeResultado_Impacto() {
        int x = 2;
        int y = 7;
        String tipoBarco = null;
        String esperado = IMPACTO + SEPARADOR_CAMPOS + x + SEPARADOR_COORD + y;

        String resultado = ManejadorMensajes.construirMensajeResultado(IMPACTO, x, y, tipoBarco);

        assertEquals(esperado, resultado, "El mensaje de IMPACTO no debe incluir el tipoBarco si es null.");
    }

    @Test
    void construirMensajeResultado_Hundido() {
        int x = 8;
        int y = 1;
        String tipoBarco = "ACORAZADO";
        String esperado = ManejadorMensajes.HUNDIDO + SEPARADOR_CAMPOS + x + SEPARADOR_COORD + y + SEPARADOR_CAMPOS + tipoBarco;

        String resultado = ManejadorMensajes.construirMensajeResultado(ManejadorMensajes.HUNDIDO, x, y, tipoBarco);

        assertEquals(esperado, resultado, "El mensaje de HUNDIDO debe incluir el tipo de barco.");
    }

    @Test
    void construirMensajeResultado_Fallo() {
        int x = 0;
        int y = 0;
        String tipoBarco = null;
        String esperado = FALLO + SEPARADOR_CAMPOS + x + SEPARADOR_COORD + y;

        String resultado = ManejadorMensajes.construirMensajeResultado(FALLO, x, y, tipoBarco);

        assertEquals(esperado, resultado, "El mensaje de FALLO no debe incluir tipoBarco.");
    }

    // =================================================================
    // TESTS DE PARSEO (De String a objeto Mensaje)
    // =================================================================

    @Test
    void parsearMensaje_Disparo() {
        String mensaje = DISPARAR + SEPARADOR_CAMPOS + "4" + SEPARADOR_COORD + "3";

        ManejadorMensajes.Mensaje msg = ManejadorMensajes.parsearMensaje(mensaje);

        assertEquals(DISPARAR, msg.comando, "El comando debe ser DISPARAR.");
        assertEquals(4, msg.x, "La coordenada X debe ser 4.");
        assertEquals(3, msg.y, "La coordenada Y debe ser 3.");
        assertNull(msg.tipoBarco, "El tipoBarco debe ser null para DISPARAR.");
    }

    @Test
    void parsearMensaje_Impacto() {
        String mensaje = IMPACTO + SEPARADOR_CAMPOS + "1" + SEPARADOR_COORD + "2";

        ManejadorMensajes.Mensaje msg = ManejadorMensajes.parsearMensaje(mensaje);

        assertEquals(IMPACTO, msg.comando, "El comando debe ser IMPACTO.");
        assertEquals(1, msg.x, "La coordenada X debe ser 1.");
        assertEquals(2, msg.y, "La coordenada Y debe ser 2.");
        assertNull(msg.tipoBarco, "El tipoBarco debe ser null para IMPACTO simple.");
    }

    @Test
    void parsearMensaje_Hundido() {
        String mensaje = ManejadorMensajes.HUNDIDO + SEPARADOR_CAMPOS + "9" + SEPARADOR_COORD + "5" + SEPARADOR_CAMPOS + "SUBMARINO";

        ManejadorMensajes.Mensaje msg = ManejadorMensajes.parsearMensaje(mensaje);

        assertEquals(ManejadorMensajes.HUNDIDO, msg.comando, "El comando debe ser HUNDIDO.");
        assertEquals(9, msg.x, "La coordenada X debe ser 9.");
        assertEquals(5, msg.y, "La coordenada Y debe ser 5.");
        assertEquals("SUBMARINO", msg.tipoBarco, "El tipoBarco debe ser SUBMARINO.");
    }

    @Test
    void parsearMensaje_ComandoSimple() {
        String mensaje = ManejadorMensajes.LISTO;

        ManejadorMensajes.Mensaje msg = ManejadorMensajes.parsearMensaje(mensaje);

        assertEquals(ManejadorMensajes.LISTO, msg.comando, "El comando simple debe parsearse correctamente.");
        assertEquals(-1, msg.x, "La coordenada X debe ser -1 para comandos sin coordenadas.");
        assertEquals(-1, msg.y, "La coordenada Y debe ser -1 para comandos sin coordenadas.");
        assertNull(msg.tipoBarco, "El tipoBarco debe ser null para comandos simples.");
    }

    // =================================================================
    // TESTS DE ERRORES Y EXCEPCIONES
    // =================================================================

    @Test
    void parsearMensaje_FallaFormatoInvalido() {
        String mensajeConError = "DISPARAR|5-5"; // Separador incorrecto

        // Se espera que el método lance una IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            ManejadorMensajes.parsearMensaje(mensajeConError);
        }, "Debe lanzar IllegalArgumentException para un formato de coordenadas incorrecto.");
    }

    @Test
    void parsearMensaje_FallaTipoDeDato() {
        String mensajeConError = "DISPARAR|A,5"; // Coordenada no numérica

        // Se espera que el método lance una IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            ManejadorMensajes.parsearMensaje(mensajeConError);
        }, "Debe lanzar IllegalArgumentException para coordenadas no numéricas.");
    }
}