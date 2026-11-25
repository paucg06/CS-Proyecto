import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiServer
{
    //Almacenamiento Temporal en Memoria
    private static final Map<String, JSONObject> usuarios = new ConcurrentHashMap<>();
    private static final Map<String, JSONObject> archivos = new ConcurrentHashMap<>();
    private static final AtomicInteger contadorArchivos = new AtomicInteger(1);

    public static void Iniciar(int puerto) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);

        server.createContext("/api/auth/register", ApiServer::handleRegister);
        server.createContext("/api/auth/login", ApiServer::handleLogin);
        server.createContext("/api/auth/refresh", ApiServer::handleRefresh);
        server.createContext("/api/auth/logout", ApiServer::handleLogout);
        server.createContext("/api/users/", ApiServer::handleUsers);
        server.createContext("/api/files", ApiServer::handleFiles);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("[ApiServer] Escuchando en http://localhost:" + puerto);
    }

    /* AUTENTICACION */

    // POST /api/auth/register
    private static void handleRegister(HttpExchange ex) throws IOException
    {
        if(!"POST".equals(ex.getRequestMethod())) 
        { 
            responderError(ex, 405, "Metodo no permitido"); 
            return; 
        }

        JSONObject body = leerJson(ex);

        if(body == null) 
        { 
            responderError(ex, 400, "JSON invalido"); 
            return; 
        }

        String username = body.optString("username", "").trim();
        String authSecret = body.optString("authSecret", "");

        if(username.isEmpty() || authSecret.isEmpty()) 
        { 
            responderError(ex, 400, "Faltan campos obligatorios"); 
            return; 
        }

        if(usuarios.containsKey(username)) 
        { 
            responderError(ex, 409, "El usuario ya existe"); 
            return; 
        }

        // TODO: cambiar por ServerDb
        usuarios.put(username, new JSONObject()
                .put("username", username)
                .put("authSecret", authSecret)
                .put("publicKey", body.optString("publicKey", ""))
                .put("privateKeyEnc", body.optString("privateKeyEnc", ""))
                .put("salt", body.optString("salt", ""))
                .put("iv", body.optString("iv", "")));

        System.out.println("[REGISTER] Usuario creado: " + username);

        responderJson(ex, 201, new JSONObject().put("ok", true).put("mensaje", "Usuario registrado"));
    }

    // POST /api/auth/login
    private static void handleLogin(HttpExchange ex) throws IOException
    {
        if(!"POST".equals(ex.getRequestMethod())) 
        { 
            responderError(ex, 405, "Metodo no permitido"); 
            return; 
        }

        JSONObject body = leerJson(ex);

        if(body == null) 
        { 
            responderError(ex, 400, "JSON invalido"); 
            return;
        }

        String username = body.optString("username", "").trim();
        String authSecret = body.optString("authSecret", "");

        if(username.isEmpty() || authSecret.isEmpty())
        { 
            responderError(ex, 400, "Faltan credenciales"); 
            return;
        }

        if(SessionManager.bloqueado(username)) 
        { 
            responderError(ex, 429, "Demasiados intentos fallidos"); 
            return; 
        }

        // Cambiar por ServerDb + PasswordManager
        JSONObject u = usuarios.get(username);

        if(u == null || !u.getString("authSecret").equals(authSecret))
        {
            SessionManager.registrarFallo(username);
            responderError(ex, 401, "Usuario o contrasena incorrectos");
            return;
        }

        SessionManager.limpiarFallos(username);

        String jwt = JWTManager.GenerateJWT(username, "USR");

        // Cambiar por RefreshTokenManager.GenerateRefreshToken(username) cuando este ServerDb
        String refreshToken = SessionManager.crear(username);

        System.out.println("[LOGIN] Sesion iniciada: " + username);

        JSONObject resp = new JSONObject()
                .put("ok", true)
                .put("jwt", jwt)
                .put("refreshToken", refreshToken)
                .put("publicKey", u.getString("publicKey"))
                .put("privateKeyEnc", u.getString("privateKeyEnc"))
                .put("salt", u.getString("salt"))
                .put("iv", u.getString("iv"));
        responderJson(ex, 200, resp);
    }

    // POST /api/auth/refresh
    private static void handleRefresh(HttpExchange ex) throws IOException
    {
        if(!"POST".equals(ex.getRequestMethod())) 
        { 
            responderError(ex, 405, "Metodo no permitido"); 
            return; 
        }

        JSONObject body = leerJson(ex);

        if(body == null) 
        { 
            responderError(ex, 400, "JSON invalido"); 
            return; 
        }

        String username = body.optString("username", "").trim();
        String refreshToken = body.optString("refreshToken", "");

        // Cambiar por RefreshTokenManager.CheckRefreshToken(...) cuando este ServerDb
        String usuarioValido = SessionManager.validar(refreshToken);

        if(usuarioValido == null || !usuarioValido.equals(username))
        {
            responderError(ex, 401, "Refresh token invalido o caducado");
            return;
        }

        String nuevoJwt = JWTManager.GenerateJWT(username, "USR");

        responderJson(ex, 200, new JSONObject().put("ok", true).put("jwt", nuevoJwt));
    }

    // POST /api/auth/logout
    private static void handleLogout(HttpExchange ex) throws IOException
    {
        if(!"POST".equals(ex.getRequestMethod())) 
        { 
            responderError(ex, 405, "Metodo no permitido"); 
            return; 
        }

        JSONObject body = leerJson(ex);

        if(body != null)
        {
            SessionManager.cerrar(body.optString("refreshToken", null));
        }

        responderJson(ex, 200, new JSONObject().put("ok", true).put("mensaje", "Sesion cerrada"));
    }

    /* USUARIOS */

    // GET /api/users/{username}
    private static void handleUsers(HttpExchange ex) throws IOException
    {
        if(autenticar(ex) == null) return;

        if(!"GET".equals(ex.getRequestMethod())) 
        { 
            responderError(ex, 405, "Metodo no permitido"); 
            return;
        }

        String path = ex.getRequestURI().getPath();
        String username = URLDecoder.decode(path.substring("/api/users/".length()), StandardCharsets.UTF_8);

        JSONObject u = usuarios.get(username);

        if(u == null) 
        { 
            responderError(ex, 404, "Usuario no encontrado"); 
            return; 
        }

        responderJson(ex, 200, new JSONObject()
                .put("ok", true)
                .put("username", u.getString("username"))
                .put("publicKey", u.getString("publicKey")));
    }

    /* ARCHIVOS */

    // /api/files, /api/files/{id}, /api/files/{id}/share
    private static void handleFiles(HttpExchange ex) throws IOException
    {
        String usuario = autenticar(ex);
        if(usuario == null) return;

        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if("/api/files".equals(path) || "/api/files/".equals(path))
        {
            if("GET".equals(method))
            {
                listarArchivos(ex, usuario);
            }
            else if("POST".equals(method))
            {
                subirArchivo(ex, usuario);
            }
            else 
            {
                responderError(ex, 405, "Metodo no permitido");
            }

            return;
        }

        String resto = path.substring("/api/files/".length());

        if(resto.endsWith("/share"))
        {
            if(!"POST".equals(method)) 
            { 
                responderError(ex, 405, "Metodo no permitido"); 
                return; 
            }

            String fileId = URLDecoder.decode(resto.substring(0, resto.length() - "/share".length()), StandardCharsets.UTF_8);
            compartirArchivo(ex, usuario, fileId);
            return;
        }

        String fileId = URLDecoder.decode(resto, StandardCharsets.UTF_8);

        if("GET".equals(method))
        {
            descargarArchivo(ex, usuario, fileId);
        }
        else if("DELETE".equals(method))
        {
            borrarArchivo(ex, usuario, fileId);
        } 
        else
        {
            responderError(ex, 405, "Metodo no permitido");
        } 
    }

    private static void listarArchivos(HttpExchange ex, String usuario) throws IOException
    {
        JSONArray lista = new JSONArray();
        for (JSONObject a : archivos.values())
        {
            if(a.getJSONObject("claves").has(usuario))
            {
                lista.put(new JSONObject()
                        .put("id", a.getString("id"))
                        .put("fileName", a.getString("fileName"))
                        .put("owner", a.getString("owner")));
            }
        }
        responderJson(ex, 200, new JSONObject().put("ok", true).put("files", lista));
    }

    private static void subirArchivo(HttpExchange ex, String usuario) throws IOException
    {
        JSONObject body = leerJson(ex);
        if(body == null) 
        { 
            responderError(ex, 400, "JSON invalido"); 
            return; 
        }

        String fileName = body.optString("fileName", "");
        String content = body.optString("content", "");
        String iv = body.optString("iv", "");
        String aesKey = body.optString("aesKey", "");

        if(fileName.isEmpty() || content.isEmpty() || iv.isEmpty() || aesKey.isEmpty())
        {
            responderError(ex, 400, "Faltan datos del archivo");
            return;
        }

        String id = String.valueOf(contadorArchivos.getAndIncrement());

        JSONObject archivo = new JSONObject()
                .put("id", id)
                .put("fileName", fileName)
                .put("owner", usuario)
                .put("content", content)
                .put("iv", iv)
                .put("claves", new JSONObject().put(usuario, aesKey));

        archivos.put(id, archivo);
        System.out.println("[UPLOAD] " + usuario + " subio " + fileName + " (id=" + id + ")");

        responderJson(ex, 201, new JSONObject().put("ok", true).put("fileId", id));
    }

    private static void descargarArchivo(HttpExchange ex, String usuario, String fileId) throws IOException
    {
        JSONObject a = archivos.get(fileId);

        if(a == null || !a.getJSONObject("claves").has(usuario))
        {
            responderError(ex, 404, "Archivo no encontrado o sin permiso");
            return;
        }

        responderJson(ex, 200, new JSONObject()
                .put("ok", true)
                .put("fileId", a.getString("id"))
                .put("fileName", a.getString("fileName"))
                .put("owner", a.getString("owner"))
                .put("content", a.getString("content"))
                .put("iv", a.getString("iv"))
                .put("aesKey", a.getJSONObject("claves").getString(usuario)));
    }

    private static void compartirArchivo(HttpExchange ex, String usuario, String fileId) throws IOException
    {
        JSONObject a = archivos.get(fileId);
        if(a == null || !a.getString("owner").equals(usuario)) 
        { 
            responderError(ex, 403, "Sin permiso"); 
            return; 
        }

        JSONObject body = leerJson(ex);
        if(body == null)
        { 
            responderError(ex, 400, "JSON invalido"); 
            return;
        }

        String targetUser = body.optString("targetUser", "");
        String aesKey = body.optString("aesKey", "");

        if(targetUser.isEmpty() || aesKey.isEmpty() || !usuarios.containsKey(targetUser))
        {
            responderError(ex, 400, "Usuario destino no valido");
            return;
        }

        a.getJSONObject("claves").put(targetUser, aesKey);

        System.out.println("[SHARE] " + usuario + " compartio archivo " + fileId + " con " + targetUser);
        responderJson(ex, 200, new JSONObject().put("ok", true).put("mensaje", "Archivo compartido"));
    }

    private static void borrarArchivo(HttpExchange ex, String usuario, String fileId) throws IOException
    {
        JSONObject a = archivos.get(fileId);
        if(a == null || !a.getString("owner").equals(usuario)) 
        { 
            responderError(ex, 403, "No puedes borrar este archivo"); 
            return; 
        }

        archivos.remove(fileId);
        System.out.println("[DELETE] " + usuario + " borro archivo " + fileId);
        responderJson(ex, 200, new JSONObject().put("ok", true).put("mensaje", "Archivo eliminado"));
    }

    /* AUXILIARES */

    //Valida el JWT con JWTManager y devuelve el username (sub) o null si no es valido
    private static String autenticar(HttpExchange ex) throws IOException
    {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7).trim() : null;

        if(token == null || !JWTManager.CheckJWT(token))
        {
            responderError(ex, 401, "No autorizado o JWT caducado");
            return null;
        }

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        return new JSONObject(payload).getString("sub");
    }

    private static JSONObject leerJson(HttpExchange ex) throws IOException
    {
        try
        {
            String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(raw);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static void responderError(HttpExchange ex, int status, String msg) throws IOException
    {
        responderJson(ex, status, new JSONObject().put("ok", false).put("error", msg));
    }

    private static void responderJson(HttpExchange ex, int status, JSONObject json) throws IOException
    {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody())
        {
            os.write(bytes);
        }
    }
}