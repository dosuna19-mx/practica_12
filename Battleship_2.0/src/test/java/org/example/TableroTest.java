package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TableroTest {

    private Tablero tableroPropio;
    private Tablero tableroEnemigo; // Para probar los métodos de registro

    private static final String TIPO_DESTRUCTOR = "DESTRUCTOR";
    private static final char AGUA = '~';
    private static final char BARCO = 'B';
    private static final char IMPACTO = 'X';
    private static final char FALLO_ENEMIGO = 'O';
    private static final char DESCONOCIDO = '?';

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        tableroPropio = new Tablero();
        tableroEnemigo = new Tablero(true); // Constructor para tablero enemigo



        Field gridField = Tablero.class.getDeclaredField("grid");
        gridField.setAccessible(true);
        char[][] grid = (char[][]) gridField.get(tableroPropio);

        // Llenar el grid con agua
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = AGUA;
            }
        }

        // Colocar un DESTRUCTOR de tamaño 2 en (0,0) y (0,1)
        grid[0][0] = BARCO;
        grid[0][1] = BARCO;

        // 3. Reiniciar los impactos
        Field impactosField = Tablero.class.getDeclaredField("impactosPorBarco");
        impactosField.setAccessible(true);
        Map<String, Integer> impactosPorBarco = (Map<String, Integer>) impactosField.get(tableroPropio);
        impactosPorBarco.put(TIPO_DESTRUCTOR, 0);
    }

    /**
     * Helper de Reflection para obtener el valor de un campo privado.
     */
    private Object getFieldValue(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    // =================================================================
    // TESTS DE RECIBIR DISPARO
    // =================================================================

    @Test
    void recibirDisparo_enAgua() throws Exception {
        // Disparar a (5, 5), que es AGUA

        boolean resultado = tableroPropio.recibirDisparo(5, 5);

        // Debe ser FALLO
        assertFalse(resultado, "Disparar al agua debe retornar false (fallo).");

        // Verificar que la celda sigue siendo AGUA (no se debe marcar 'O' en el tablero propio)
        char[][] grid = (char[][]) getFieldValue(tableroPropio, "grid");
        assertEquals(AGUA, grid[5][5], "El grid en la posición de agua no debe cambiar.");
    }

    @Test
    void recibirDisparo_ImpactoSimple() throws Exception {
        // Disparar a (0, 0), donde hay un BARCO (DESTRUCTOR)

        boolean resultado = tableroPropio.recibirDisparo(0, 0);

        // Debe ser IMPACTO
        assertTrue(resultado, "Disparar a una posición de barco debe retornar true (impacto).");

        // Verificar que la celda se marca como IMPACTO ('X')
        char[][] grid = (char[][]) getFieldValue(tableroPropio, "grid");
        assertEquals(IMPACTO, grid[0][0], "La celda impactada debe ser marcada con 'X'.");

        // Verificar que el contador de impactos sube
        Map<String, Integer> impactos = (Map<String, Integer>) getFieldValue(tableroPropio, "impactosPorBarco");
        assertEquals(1, impactos.get(TIPO_DESTRUCTOR), "El destructor debe tener 1 impacto.");
    }

    @Test
    void recibirDisparo_ImpactoRepetido() throws Exception {
        // Primer impacto (simulación de estado)
        tableroPropio.recibirDisparo(0, 0);

        // Disparo repetido a (0, 0), que ahora es 'X' (IMPACTO)
        boolean resultado = tableroPropio.recibirDisparo(0, 0);

        // Debe ser FALLO (o no impacto), ya que la posición ya fue impactada
        assertFalse(resultado, "Disparar a una posición ya impactada debe retornar false.");

        // Verificar que el contador de impactos NO sube de nuevo
        Map<String, Integer> impactos = (Map<String, Integer>) getFieldValue(tableroPropio, "impactosPorBarco");
        assertEquals(1, impactos.get(TIPO_DESTRUCTOR), "El impacto repetido no debe contar.");
    }

    // TESTS DE ESTADO DE BARCOS

    @Test
    void estaBarcoHundido_NoHundido() throws Exception {
        tableroPropio.recibirDisparo(0, 0); // 1 impacto (necesita 2)

        assertFalse(tableroPropio.estaBarcoHundido(TIPO_DESTRUCTOR), "El barco con 1/2 impactos no está hundido.");
    }

    @Test
    void estaBarcoHundido_Hundido() throws Exception {
        tableroPropio.recibirDisparo(0, 0); // 1er impacto
        tableroPropio.recibirDisparo(0, 1); // 2do impacto (hundido)

        assertTrue(tableroPropio.estaBarcoHundido(TIPO_DESTRUCTOR), "El barco con 2/2 impactos debe estar hundido.");
    }

    @Test
    void obtenerTipoBarcoEn_Impactado() throws Exception {

        tableroPropio.recibirDisparo(0, 0); // Impacto
        String tipo = tableroPropio.obtenerTipoBarcoEn(0, 0);
        assertEquals("DESCONOCIDO", tipo, "Debe retornar DESCONOCIDO después de un impacto (limitación de la implementación).");
    }

    @Test
    void obtenerTipoBarcoEn_EnAgua() {
        String tipo = tableroPropio.obtenerTipoBarcoEn(5, 5);
        assertEquals("DESCONOCIDO", tipo, "Debe retornar DESCONOCIDO si la posición es agua.");
    }

    @Test
    void todosBarcosHundidos_NoHundido() throws Exception {
        // Solo un barco (DESTRUCTOR) colocado, no impactado.
        assertFalse(tableroPropio.todosBarcosHundidos(), "Debe retornar false si el único barco no está hundido.");
    }

    @Test
    void todosBarcosHundidos_Hundido() throws Exception {
        tableroPropio.recibirDisparo(0, 0); // 1er impacto
        tableroPropio.recibirDisparo(0, 1); // 2do impacto (hundido)

        // Usar Reflection para simular que solo existe el DESTRUCTOR en el mapa barcos
        Field barcosField = Tablero.class.getDeclaredField("barcos");
        barcosField.setAccessible(true);
        Map<String, Integer> barcosSimulados = new HashMap<>();
        barcosSimulados.put(TIPO_DESTRUCTOR, 2);
        barcosField.set(tableroPropio, barcosSimulados);

        assertTrue(tableroPropio.todosBarcosHundidos(), "Debe retornar true si el único barco está hundido.");
    }

    // =================================================================
    // TESTS DE TABLERO ENEMIGO
    // =================================================================

    @Test
    void registrarImpactoEnemigo() throws Exception {
        // El grid enemigo debe estar lleno de '?' por defecto
        char[][] grid = (char[][]) getFieldValue(tableroEnemigo, "grid");

        assertEquals(DESCONOCIDO, grid[3][3], "La celda debe iniciar como DESCONOCIDO (?).");

        tableroEnemigo.registrarImpactoEnemigo(3, 3);

        // Debe registrar 'X'
        assertEquals(IMPACTO, grid[3][3], "El impacto debe registrar 'X' en el tablero enemigo.");

        // La llamada repetida no debería cambiar 'X'
        tableroEnemigo.registrarImpactoEnemigo(3, 3);
        assertEquals(IMPACTO, grid[3][3], "El impacto repetido no debe alterar 'X'.");
    }

    @Test
    void registrarFalloEnemigo() throws Exception {
        char[][] grid = (char[][]) getFieldValue(tableroEnemigo, "grid");

        tableroEnemigo.registrarFalloEnemigo(5, 5);

        // Debe registrar 'O'
        assertEquals(FALLO_ENEMIGO, grid[5][5], "El fallo debe registrar 'O' en el tablero enemigo.");
    }
}