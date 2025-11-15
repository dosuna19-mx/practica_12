package battleship;

public class ProtocoloBattleship {
    // Comandos del protocolo
    public static final String INICIAR_JUEGO = "INICIAR";
    public static final String LISTO = "LISTO";
    public static final String COLOCAR_BARCOS = "COLOCAR_BARCOS";
    public static final String DISPARAR = "DISPARAR";
    public static final String IMPACTO = "IMPACTO";
    public static final String FALLO = "FALLO";
    public static final String HUNDIDO = "HUNDIDO";
    public static final String JUEGO_TERMINADO = "JUEGO_TERMINADO";
    public static final String ERROR = "ERROR";
    public static final String SALIR = "SALIR";
    public static final String CONECTADO = "CONECTADO";

    // Separadores
    public static final String SEPARADOR_CAMPOS = "|";
    public static final String SEPARADOR_COORD = ",";

    /**
     * Construye un mensaje para disparar
     */
    public static String construirMensajeDisparo(int x, int y) {
        return DISPARAR + SEPARADOR_CAMPOS + x + SEPARADOR_COORD + y;
    }

    /**
     * Construye un mensaje de resultado
     */
    public static String construirMensajeResultado(String resultado, int x, int y, String tipoBarco) {
        return resultado + SEPARADOR_CAMPOS + x + SEPARADOR_COORD + y +
                (tipoBarco != null ? SEPARADOR_CAMPOS + tipoBarco : "");
    }

    /**
     * Parsea un mensaje recibido
     */
    public static Mensaje parsearMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("Mensaje nulo o vacío");
        }

        String[] partes = mensaje.split("\\" + SEPARADOR_CAMPOS);
        String comando = partes[0];

        try {
            switch (comando) {
                case DISPARAR:
                    String[] coordenadas = partes[1].split(SEPARADOR_COORD);
                    int x = Integer.parseInt(coordenadas[0]);
                    int y = Integer.parseInt(coordenadas[1]);
                    return new Mensaje(comando, x, y);

                case IMPACTO:
                case FALLO:
                case HUNDIDO:
                    coordenadas = partes[1].split(SEPARADOR_COORD);
                    x = Integer.parseInt(coordenadas[0]);
                    y = Integer.parseInt(coordenadas[1]);
                    String tipoBarco = partes.length > 2 ? partes[2] : null;
                    return new Mensaje(comando, x, y, tipoBarco);

                default:
                    return new Mensaje(comando);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Mensaje con formato inválido: " + mensaje, e);
        }
    }

    public static class Mensaje {
        public final String comando;
        public final int x;
        public final int y;
        public final String tipoBarco;

        public Mensaje(String comando) {
            this(comando, -1, -1, null);
        }

        public Mensaje(String comando, int x, int y) {
            this(comando, x, y, null);
        }

        public Mensaje(String comando, int x, int y, String tipoBarco) {
            this.comando = comando;
            this.x = x;
            this.y = y;
            this.tipoBarco = tipoBarco;
        }
    }
}
