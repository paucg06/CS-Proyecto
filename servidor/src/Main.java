
//Librerias para el servidor HTTP de Java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

//Entrada / Salida y Red
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

//Colecciones y concurrencia
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class Main
{
    //Configuracion de red
    public static final String HOST = "localhost";
    public static final int PUERTO = 8080;

    //Limites de validacion
    private static final int MAX_CUERPO = 256 * 1024 * 1024; // 256 MB (los archivos van en Base64 dentro del cuerpo)
    private static final int MAX_NOMBRE_USUARIO = 64;
    private static final int MAX_NOMBRE_ARCHIVO = 255;

    public static void main(String[] args)
    {
        try
        {
            System.out.println("CS-PROYECTO: SERVIDOR LOCAL");

            //Comprobar la configuracion antes de aceptar conexiones
            PasswordManager.ComprobarConfiguracion();

            //Inicializar la base de datos SQLite y sus tablas
            ServerDb.init();

            //Crear servidor HTTP en localhost:8080
            HttpServer servidor = HttpServer.create(new InetSocketAddress(HOST, PUERTO), 0);

            //Registrar rutas, endpoints
            servidor.createContext("/api/usuario", new UsuarioHandler());
            servidor.createContext("/api/usuario/salt", new SaltHandler());
            servidor.createContext("/api/usuario/clave-publica", new ClavePublicaHandler());
            servidor.createContext("/api/login", new LoginHandler());
            servidor.createContext("/api/logout", new LogoutHandler());
            servidor.createContext("/api/archivo", new ArchivoHandler());
            servidor.createContext("/api/archivos", new ListarArchivosHandler());
            servidor.createContext("/api/archivo/compartir", new CompartirArchivoHandler());

            //Asignar pool de hilos para peticiones concurrentes
            servidor.setExecutor(Executors.newCachedThreadPool());

            //Iniciar servidor
            servidor.start();

            System.out.println("[OK] Servidor iniciado correctamente en http://" + HOST + ":" + PUERTO);
            System.out.println("[INFO] Esperando conexiones de clientes...");
        }
        catch (IllegalStateException e)
        {
            System.err.println("[ERROR] Configuracion incorrecta: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.err.println("[ERROR] No se pudo inicializar la base de datos: " + e.getMessage());
        }
        catch (IOException e)
        {
            System.err.println("[ERROR] No se pudo arrancar el servidor: " + e.getMessage());
        }
    }


    //HANDLERS, controladres de peticiones HTTP 
    //Clase base, comprueba la ruta exacta y convierte cualquier fallo inesperado en una respuesta 500
    static abstract class HandlerBase implements HttpHandler
    {
        private final String ruta;

        HandlerBase(String ruta)
        {
            this.ruta = ruta;
        }

        abstract void procesar(HttpExchange exchange) throws Exception;

        @Override
        public final void handle(HttpExchange exchange) throws IOException
        {
            try
            {
                // createContext funciona por prefijo: "/api/archivo/loquesea" llegaria aqui. Solo se acepta la ruta exacta.
                if (!ruta.equals(exchange.getRequestURI().getPath()))
                {
                    EnviarRespuesta(exchange, 404, "Ruta no encontrada");
                    return;
                }
                procesar(exchange);
            }
            catch (CuerpoGrandeException e)
            {
                EnviarRespuesta(exchange, 413, "Peticion demasiado grande");
            }
            catch (SQLException e)
            {
                System.err.println("[ERROR] Base de datos: " + e.getMessage());
                EnviarRespuesta(exchange, 500, "Error interno del servidor");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                EnviarRespuesta(exchange, 500, "Error interno del servidor");
            }
            finally
            {
                exchange.close();
            }
        }
    }

    //Registro de usuarios 
    static class UsuarioHandler extends HandlerBase
    {
        UsuarioHandler() { super("/api/usuario"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }

            // POST /api/usuario: username;clavePublica;clavePrivadaCifrada;salt;iv;authSecret
            String[] d = LeerCuerpo(exchange).split(";", -1);

            if (d.length != 6 || !NombreValido(d[0], MAX_NOMBRE_USUARIO)
                || !Base64Valido(d[1]) || !Base64Valido(d[2]) || !Base64Valido(d[3]) || !Base64Valido(d[4]) || !Base64Valido(d[5]))
            {
                EnviarRespuesta(exchange, 400, "Datos invalidos: username;clavePublica;clavePrivadaCifrada;salt;iv;authSecret");
                return;
            }

            byte[] authSecret = Base64.getDecoder().decode(d[5]);
            if (authSecret.length < 16)
            {
                EnviarRespuesta(exchange, 400, "authSecret demasiado corto (minimo 16 bytes)");
                return;
            }

            //El servidor guarda salt + hash(pimienta, salt, authSecret)
            byte[][] credenciales = PasswordManager.Registro(authSecret);

            boolean creado = ServerDb.guardarUsuario(
                new String[]{d[0], d[1], d[2], d[3], d[4]},
                Base64.getEncoder().encodeToString(credenciales[0]),
                Base64.getEncoder().encodeToString(credenciales[1]));

            if (creado)
            {
                EnviarRespuesta(exchange, 201, "OK");
            }
            else
            {
                EnviarRespuesta(exchange, 409, "El usuario ya existe");
            }
        }
    }

    // SALT: el cliente la necesita antes del login 
    static class SaltHandler extends HandlerBase
    {
        SaltHandler() { super("/api/usuario/salt"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }

            String username = ParsearQuery(exchange.getRequestURI().getRawQuery()).get("username");
            if (username == null || username.isBlank())
            {
                EnviarRespuesta(exchange, 400, "Falta el parametro username");
                return;
            }

            String salt = ServerDb.obtenerSalt(username);
            //Si el usuario no existe se devuelve un salt falso: asi este endpoint no sirve para averiguar quien esta registrado
            EnviarRespuesta(exchange, 200, salt != null ? salt : PasswordManager.SaltPublica(username));
        }
    }

    //login, comprueba la contrasenya y entrega un token de sesion + los datos del usuario
    static class LoginHandler extends HandlerBase
    {
        LoginHandler() { super("/api/login"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }

            // POST /api/login: username;authSecret
            String[] d = LeerCuerpo(exchange).split(";", -1);
            if (d.length != 2 || !NombreValido(d[0], MAX_NOMBRE_USUARIO) || !Base64Valido(d[1]))
            {
                EnviarRespuesta(exchange, 400, "Datos invalidos: username;authSecret");
                return;
            }
            String username = d[0];

            if (SessionManager.bloqueado(username))
            {
                EnviarRespuesta(exchange, 429, "Demasiados intentos fallidos. Prueba de nuevo en unos minutos");
                return;
            }

            if (!PasswordManager.ComprobarPass(Base64.getDecoder().decode(d[1]), username))
            {
                SessionManager.registrarFallo(username);
                EnviarRespuesta(exchange, 401, "Usuario o contrasenya incorrectos");
                return;
            }

            SessionManager.limpiarFallos(username);
            String[] usuario = ServerDb.obtenerUsuario(username); 
            String token = SessionManager.crear(username);

            EnviarRespuesta(exchange, 200, token + ";" + usuario[1] + ";" + usuario[2] + ";" + usuario[3] + ";" + usuario[4]);
        }
    }

    // LOGOUT: invalida el token
    static class LogoutHandler extends HandlerBase
    {
        LogoutHandler() { super("/api/logout"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }
            if (Autenticar(exchange) == null)
            {
                return;
            }
            SessionManager.cerrar(ExtraerToken(exchange));
            EnviarRespuesta(exchange, 200, "OK");
        }
    }

    // CLAVE PUBLICA: Obtener clave publica de otro usuario
    static class ClavePublicaHandler extends HandlerBase
    {
        ClavePublicaHandler() { super("/api/usuario/clave-publica"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }
            if (Autenticar(exchange) == null)
            {
                return;
            }

            String username = ParsearQuery(exchange.getRequestURI().getRawQuery()).get("username");
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
    }

    //Guardar y Descargar archivo cifrado
    static class ArchivoHandler extends HandlerBase
    {
        ArchivoHandler() { super("/api/archivo"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            String metodo = exchange.getRequestMethod();
            if (!"GET".equalsIgnoreCase(metodo) && !"POST".equalsIgnoreCase(metodo))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }

            String usuarioActual = Autenticar(exchange);
            if (usuarioActual == null)
            {
                return;
            }

            // GET /api/archivo?nombre=...[&propietario=...]
            if ("GET".equalsIgnoreCase(metodo))
            {
                Map<String, String> params = ParsearQuery(exchange.getRequestURI().getRawQuery());
                String nombre = params.get("nombre");
                String propietario = params.get("propietario");

                if (nombre == null || nombre.isBlank())
                {
                    EnviarRespuesta(exchange, 400, "Falta el parametro nombre");
                    return;
                }
                if (propietario == null || propietario.isBlank())
                {
                    propietario = usuarioActual; // por defecto, un archivo mio
                }

                // Solo se entrega si el usuario autenticado tiene su clave en claves_archivo (propietario o destinatario)
                String[] datos = ServerDb.obtenerArchivo(nombre, propietario, usuarioActual);
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
            else
            {
                // nombre;archivoCifrado;iv;claveAesCifrada;propietario
                String[] d = LeerCuerpo(exchange).split(";", -1);

                if (d.length != 5 || !NombreValido(d[0], MAX_NOMBRE_ARCHIVO)
                    || d[1].isEmpty() || !Base64Valido(d[1]) || !Base64Valido(d[2]) || !Base64Valido(d[3]))
                {
                    EnviarRespuesta(exchange, 400, "Datos invalidos: nombre;archivoCifrado;iv;claveAesCifrada;propietario");
                    return;
                }
                if (!d[4].equals(usuarioActual))
                {
                    EnviarRespuesta(exchange, 403, "No puedes subir archivos a nombre de otro usuario");
                    return;
                }

                if (ServerDb.guardarArchivo(d))
                {
                    EnviarRespuesta(exchange, 201, "OK");
                }
                else
                {
                    EnviarRespuesta(exchange, 409, "Ya tienes un archivo con ese nombre");
                }
            }
        }
    }

    //Archivos del usuario autenticado (propios y compartidos con el)
    static class ListarArchivosHandler extends HandlerBase
    {
        ListarArchivosHandler() { super("/api/archivos"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }
            String usuarioActual = Autenticar(exchange);
            if (usuarioActual == null)
            {
                return;
            }

            // Una linea por archivo: nombre;propietario
            List<String[]> archivos = ServerDb.listarArchivos(usuarioActual);
            StringBuilder sb = new StringBuilder();
            for (String[] a : archivos)
            {
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append(a[0]).append(";").append(a[1]);
            }
            EnviarRespuesta(exchange, 200, sb.toString());
        }
    }

    //el propietario da acceso a otro usuario o se lo retira
    static class CompartirArchivoHandler extends HandlerBase
    {
        CompartirArchivoHandler() { super("/api/archivo/compartir"); }

        @Override
        void procesar(HttpExchange exchange) throws Exception
        {
            String metodo = exchange.getRequestMethod();
            if (!"POST".equalsIgnoreCase(metodo) && !"DELETE".equalsIgnoreCase(metodo))
            {
                EnviarRespuesta(exchange, 405, "Metodo no permitido");
                return;
            }

            String usuarioActual = Autenticar(exchange);
            if (usuarioActual == null)
            {
                return;
            }

            String[] partes = LeerCuerpo(exchange).split(";", -1);

            // POST: nombreArchivo;usuarioDestino;claveAesCifrada  (E_K(destino)(kf), calculada por el cliente)
            if ("POST".equalsIgnoreCase(metodo))
            {
                if (partes.length != 3 || !NombreValido(partes[0], MAX_NOMBRE_ARCHIVO)
                    || !NombreValido(partes[1], MAX_NOMBRE_USUARIO) || partes[2].isEmpty() || !Base64Valido(partes[2]))
                {
                    EnviarRespuesta(exchange, 400, "Formato invalido: requiere nombreArchivo;usuarioDestino;claveAesCifrada");
                    return;
                }
                if (partes[1].equals(usuarioActual))
                {
                    EnviarRespuesta(exchange, 400, "No tiene sentido compartir un archivo contigo mismo");
                    return;
                }

                // El propietario es SIEMPRE el usuario autenticado: solo se pueden compartir archivos propios
                String error = ServerDb.compartirArchivo(partes[0], usuarioActual, partes[1], partes[2]);
                if (error == null)
                {
                    EnviarRespuesta(exchange, 200, "OK");
                }
                else
                {
                    EnviarRespuesta(exchange, 400, error);
                }
            }
            // DELETE: nombreArchivo;usuarioDestino
            else
            {
                if (partes.length != 2 || !NombreValido(partes[0], MAX_NOMBRE_ARCHIVO) || !NombreValido(partes[1], MAX_NOMBRE_USUARIO))
                {
                    EnviarRespuesta(exchange, 400, "Formato invalido: requiere nombreArchivo;usuarioDestino");
                    return;
                }
                if (ServerDb.dejarDeCompartir(partes[0], usuarioActual, partes[1]))
                {
                    EnviarRespuesta(exchange, 200, "OK");
                }
                else
                {
                    EnviarRespuesta(exchange, 404, "El archivo no estaba compartido con ese usuario");
                }
            }
        }
    }

    // METODOS AUXILIARES (Autenticacion)

    private static String ExtraerToken(HttpExchange exchange)
    {
        String cabecera = exchange.getRequestHeaders().getFirst("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer "))
        {
            return cabecera.substring(7).trim();
        }
        return null;
    }

    // Devuelve el usuario autenticado, o responde 401 y devuelve null
    private static String Autenticar(HttpExchange exchange) throws IOException
    {
        String usuario = SessionManager.validar(ExtraerToken(exchange));
        if (usuario == null)
        {
            EnviarRespuesta(exchange, 401, "No autenticado o sesion caducada");
        }
        return usuario;
    }

    // METODOS AUXILIARES 
    private static boolean NombreValido(String nombre, int max)
    {
        if (nombre == null || nombre.isBlank() || nombre.length() > max)
        {
            return false;
        }
        for (int i = 0; i < nombre.length(); i++)
        {
            char c = nombre.charAt(i);
            if (c == ';' || Character.isISOControl(c))
            {
                return false;
            }
        }
        return true;
    }

    // Comprueba el alfabeto Base64 sin decodificar 
    private static boolean Base64Valido(String s)
    {
        if (s == null)
        {
            return false;
        }
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            boolean ok = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '+' || c == '/' || c == '=';
            if (!ok)
            {
                return false;
            }
        }
        return s.length() % 4 == 0;
    }

    // METODOS AUXILIARES
    static class CuerpoGrandeException extends IOException
    {
        private static final long serialVersionUID = 1L;
    }

    private static String LeerCuerpo(HttpExchange exchange) throws IOException
    {
        try (InputStream in = exchange.getRequestBody())
        {
            byte[] datos = in.readNBytes(MAX_CUERPO + 1); // lee como mucho 1 byte de mas para detectar el exceso
            if (datos.length > MAX_CUERPO)
            {
                throw new CuerpoGrandeException();
            }
            return new String(datos, StandardCharsets.UTF_8).strip();
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

        if (bytes.length == 0)
        {
            exchange.sendResponseHeaders(codigo, -1); 
        }
        else
        {
            exchange.sendResponseHeaders(codigo, bytes.length);
            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
        }

        System.out.println("[" + codigo + "] " + exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
    }
}