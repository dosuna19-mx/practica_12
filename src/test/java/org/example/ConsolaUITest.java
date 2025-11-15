package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ConsolaUITest {

    // Redirección de I/O y Captura de Salida
    private final InputStream standardIn = System.in;
    private final PrintStream standardOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;
    private ConsolaUI ui;

    @BeforeEach
    void setUp() {
        // Redirige System.out para capturar la salida de la consola
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        // Restaurar System.in y System.out a sus valores originales
        System.setIn(standardIn);
        System.setOut(standardOut);

        // Cerrar el Scanner de ConsolaUI
        if (ui != null) {
            ui.close();
        }
    }

    private void setInput(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
        System.setIn(inputStream);
        // UI debe crearse después de cambiar System.in
        ui = new ConsolaUI();
    }

    @Test
    void solicitarNombre() {
        String nombreEsperado = "Jugador de Prueba";
        setInput(nombreEsperado + "\n");

        String nombreObtenido = ui.solicitarNombre();

        assertEquals(nombreEsperado, nombreObtenido, "El nombre retornado debe coincidir con la entrada simulada.");
    }

    @Test
    void obtenerOpcionModo() {
        // Entrada inválida ("3") seguida de una válida ("1")
        String inputData = "3\n1\n";
        String opcionEsperada = "1";
        setInput(inputData);

        String opcionObtenida = ui.obtenerOpcionModo();

        assertEquals(opcionEsperada, opcionObtenida, "Debe retornar la opción válida '1'.");
        assertTrue(outputStreamCaptor.toString().contains("Opción inválida."), "Debe mostrar mensaje de error para la entrada '3'.");
    }

    @Test
    void solicitarIP() {
        String ipEsperada = "127.0.0.1";
        setInput(ipEsperada + "\n");

        String ipObtenida = ui.solicitarIP();

        assertEquals(ipEsperada, ipObtenida, "La IP retornada debe coincidir con la entrada simulada.");
    }

    @Test
    void obtenerDisparoJugador() {
        // Validación (Formato/Rango/Excepción) seguidos de una entrada válida.
        String inputData =
                "5" + "\n" +         // 1. Formato inválido (solo 5)
                        "10,1" + "\n" +      // 2. Rango inválido (10)
                        "A,B" + "\n" +       // 3. NumberFormatException
                        "7,3" + "\n";        // 4. Válido

        int[] esperado = {7, 3};
        setInput(inputData);

        int[] resultado = ui.obtenerDisparoJugador(false);

        assertArrayEquals(esperado, resultado, "Debe retornar la última entrada válida: [7, 3].");
        String output = outputStreamCaptor.toString();
        // Error de ConsolaUI se muestran
        assertTrue(output.contains("Formato inválido. Usa: fila,columna"), "Debe manejar el error de formato.");
        assertTrue(output.contains("Coordenadas fuera de rango. Usa números del 0 al 9."), "Debe manejar el error de rango.");
        assertTrue(output.contains("Por favor ingresa números válidos."), "Debe manejar el error de tipo de dato.");
    }

    // Opcional: Agregar una prueba para 'mostrarMensaje' para asegurar que la captura de salida funciona
    @Test
    void mostrarMensaje_muestraCorrectamente() {
        ui = new ConsolaUI();
        String mensaje = "Verificación de Salida";
        ui.mostrarMensaje(mensaje);

        assertTrue(outputStreamCaptor.toString().trim().contains(mensaje));
    }
}