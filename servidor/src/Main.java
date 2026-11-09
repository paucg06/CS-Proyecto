// ============================================================
// Main (Servidor): Inicia el servidor HTTP en host local
// y gestiona las peticiones REST conectando con ServerDb (SQLite).
// ============================================================

// Librerias para el servidor HTTP de Java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

// Entrada / Salida y Red
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

// Colecciones y concurrencia
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class Main
{
    // Configuracion de red
    public static final String HOST = "localhost";
    public static final int PUERTO = 8080;

    public static void main(String[] args)
    {
        try
        {
            System.out.println("CS-PROYECTO: SERVIDOR LOCAL");

            // Inicializar la base de datos SQLite y sus tablas
            ServerDb.init();

            // Crear servidor HTTP en localhost:8080
            HttpServer servidor = HttpServer.create(new InetSocketAddress(HOST, PUERTO), 0);

            // Registrar rutas (endpoints)
            servidor.createContext("/api/usuario", new UsuarioHandler());
            servidor.createContext("/api/usuario/clave-publica", new ClavePublicaHandler());
            servidor.createContext("/api/archivo", new ArchivoHandler());
            servidor.createContext("/api/archivos", new ListarArchivosHandler());
            servidor.createContext("/api/archivo/compartir", new CompartirArchivoHandler());

            // Asignar pool de hilos para peticiones concurrentes
            servidor.setExecutor(Executors.newCachedThreadPool());

            // Iniciar servidor
            servidor.start();

            System.out.println("[OK] Servidor iniciado correctamente en http://" + HOST + ":" + PUERTO);
            System.out.println("[INFO] Esperando conexiones de clientes...");
        }
        catch (IOException e)
        {
            System.err.println("[ERROR] No se pudo arrancar el servidor: " + e.getMessage());
        }
    }


    // HANDLERS (Controladores de peticiones HTTP)
    // USUARIOS: Registro y Login / Obtencion de datos
    static class UsuarioHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            String metodo = exchange.getRequestMethod();

            // GET /api/usuario?username=... (Login / recuperar claves y salt)
            if ("GET".equalsIgnoreCase(metodo))
            {
                Map<String, String> params = ParsearQuery(exchange.getRequestURI().getQuery());
                String username = params.get("username");

                if (username == null || username.isBlank())
                {
                    EnviarRespuesta(exchange, 400, "Falta el parametro username");
                    return;
                }

                String[] datos = ServerDb.obtenerUsuario(username);

                if (datos != null)
                {
                    EnviarRespuesta(exchange, 200, String.join(";", datos));
                }
                else
                {
                    EnviarRespuesta(exchange, 404, "Usuario no encontrado");
                }
            }

            // POST /api/usuario (Registro de nuevo usuario)
            else if ("POST".equalsIgnoreCase(metodo))
            {
                String body = LeerCuerpo(exchange);
                String[] dataline = body.split(";", -1);

                if (dataline.length >= 5 && ServerDb.guardarUsuario(dataline))
                {
                    EnviarRespuesta(exchange, 201, "OK");
                }
                else
                {
                    EnviarRespuesta(exchange, 400, "Error: datos invalidos o usuario ya existente");
                }
            }
            else
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
            }
        }
    }

    // CLAVE PUBLICA: Obtener clave publica de otro usuario (Fase II)
    static class ClavePublicaHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                Map<String, String> params = ParsearQuery(exchange.getRequestURI().getQuery());
                String username = params.get("username");

                if (username == null || username.isBlank())
                {
                    EnviarRespuesta(exchange, 400, "Falta el parametro username");
                    return;
                }

                String clavePublica = ServerDb.obtenerClavePublica(username);
                if (clavePublica != null)
                {
                    EnviarRespuesta(exchange, 200, clavePublica);
                }
                else
                {
                    EnviarRespuesta(exchange, 404, "Clave publica no encontrada");
                }
            }
            else
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
            }
        }
    }

    // ARCHIVOS: Guardar y Descargar archivo cifrado
    static class ArchivoHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            String metodo = exchange.getRequestMethod();

            // GET /api/archivo?nombre=...&usuario=...
            if ("GET".equalsIgnoreCase(metodo))
            {
                Map<String, String> params = ParsearQuery(exchange.getRequestURI().getQuery());
                String nombre = params.get("nombre");
                String usuario = params.get("usuario");

                if (nombre == null || nombre.isBlank())
                {
                    EnviarRespuesta(exchange, 400, "Falta el parametro nombre");
                    return;
                }

                String[] datos = ServerDb.obtenerArchivo(nombre, usuario);
                if (datos != null)
                {
                    EnviarRespuesta(exchange, 200, String.join(";", datos));
                }
                else
                {
                    EnviarRespuesta(exchange, 404, "Archivo no encontrado o acceso denegado");
                }
            }
            // POST /api/archivo (Subir archivo cifrado con su clave AES protegida)
            else if ("POST".equalsIgnoreCase(metodo))
            {
                String body = LeerCuerpo(exchange);
                String[] dataline = body.split(";", -1);

                if (dataline.length >= 5 && ServerDb.guardarArchivo(dataline))
                {
                    EnviarRespuesta(exchange, 201, "OK");
                }
                else
                {
                    EnviarRespuesta(exchange, 400, "Error: datos invalidos o archivo ya existente");
                }
            }
            else
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
            }
        }
    }


    // LISTAR ARCHIVOS: Obtener nombres de archivos de un usuario
    static class ListarArchivosHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                Map<String, String> params = ParsearQuery(exchange.getRequestURI().getQuery());
                String usuario = params.get("usuario");

                if (usuario == null || usuario.isBlank())
                {
                    EnviarRespuesta(exchange, 400, "Falta el parametro usuario");
                    return;
                }

                String[] archivos = ServerDb.obtenerArchivosUsuario(usuario);
                EnviarRespuesta(exchange, 200, String.join(";", archivos));
            }
            else
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
            }
        }
    }

    // COMPARTIR ARCHIVO: Compartir archivo con otro usuario (Fase II)
    static class CompartirArchivoHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                String body = LeerCuerpo(exchange);
                String[] partes = body.split(";", -1);

                if (partes.length < 3)
                {
                    EnviarRespuesta(exchange, 400, "Formato invalido: requiere nombreArchivo;usuarioDestino;claveAesCifrada");
                    return;
                }

                boolean ok = ServerDb.compartirArchivo(partes[0], partes[1], partes[2]);
                if (ok)
                {
                    EnviarRespuesta(exchange, 200, "OK");
                }
                else
                {
                    EnviarRespuesta(exchange, 400, "No se pudo compartir el archivo");
                }
            }
            else
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
            }
        }
    }

    // METODOS AUXILIARES (HTTP y Red)
    private static String LeerCuerpo(HttpExchange exchange) throws IOException
    {
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)))
        {
            StringBuilder sb = new StringBuilder();
            String linea;

            while ((linea = lector.readLine()) != null)
            {
                sb.append(linea);
            }
            
            return sb.toString();
        }
    }

    private static Map<String, String> ParsearQuery(String query)
    {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank())
        {
            return params;
        }

        for (String par : query.split("&"))
        {
            String[] kv = par.split("=", 2);
            if (kv.length == 2)
            {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }

        return params;
    }

    private static void EnviarRespuesta(HttpExchange exchange, int codigo, String respuesta) throws IOException
    {
        byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(bytes);
        }

        System.out.println("[" + codigo + "] " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
    }
}
