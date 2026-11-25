package service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiClient
{
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();

    //Token corto (10 min) funciona dentro de Authorization
    private static String jwtToken = null; 

    //Token largo (7 dias) pide el nuevo JWT
    private static String refreshToken = null;

    private static String currentUser = null;


    /* GESTION DE TOKENS */
    public static String getJwtToken()
    { 
        return jwtToken; 
    }
    
    public static String getRefreshToken()
    {
        return refreshToken;
    }

    public static void clearSession()
    {
        jwtToken = null;
        refreshToken = null;
        currentUser = null;
    }

    /* AUTENTICACION */

    //Crear un USUARIO (POST)
    public static String Register(String username, String authSecret, String publicKey, String privateKeyEnc, String salt, String iv) //Crear usuario
    {
        try
        {
            String json = "{"
                + "\"username\":\"" + esc(username) + "\","
                + "\"authSecret\":\"" + esc(authSecret) + "\","
                + "\"publicKey\":\"" + esc(publicKey) + "\","
                + "\"privateKeyEnc\":\"" + esc(privateKeyEnc) + "\","
                + "\"salt\":\"" + esc(salt) + "\","
                + "\"iv\":\"" + esc(iv) + "\""
                + "}";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en Register: " + e.getMessage(), e);
        }
    }

    //Iniciar Sesion como USUARIO (POST)
    public static String Login(String username, String authSecret)
    {
        try
        {
            String json = "{"
                + "\"username\":\"" + esc(username) + "\","
                + "\"authSecret\":\"" + esc(authSecret) + "\""
                + "}";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            String respuesta = enviar(req);

            String jwt = extraerCampo(respuesta, "jwt");
            String refresh = extraerCampo(respuesta, "refreshToken");

            if(jwt != null && !jwt.isEmpty())
            {
                jwtToken = jwt;
                refreshToken = refresh;
                currentUser = username;
            }

            return respuesta;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en Login: " + e.getMessage(), e);
        }
    }

    //Pedir Nuevo JWT de API Cliente (POST)
    public static String Refresh()
    {
        try
        {
            String json = "{"
                + "\"username\":\"" + esc(currentUser) + "\","
                + "\"refreshToken\":\"" + esc(refreshToken) + "\""
                + "}";
            
            HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/auth/refresh"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

            String respuesta = enviar(req);

            String nuevoJwt = extraerCampo(respuesta, "jwt");

            if(nuevoJwt != null && !nuevoJwt.isEmpty())
            {
                jwtToken = nuevoJwt;
            }

            return respuesta;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en Refresh: " + e.getMessage(), e);
        }
    }

    //Cerrar Sesion USUARIO (POST)
    public static String Logout() //Comprobar credenciales
    {
        try
        {
            String json = "{\"refreshToken\":\"" 
                + esc(refreshToken) 
                + "\"}";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/logout"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            String respuesta = enviar(req);
            clearSession();

            return respuesta;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en Logout: " + e.getMessage(), e);
        }
    }

    //Obtener Clave Publica de Usuario (GET)
    public static String GetPublicKey(String username)
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + encUrl(username)))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en GetPublicKey: " + e.getMessage(), e);
        }
    }


    /* ARCHIVOS */

    //Listar Archivos Usuario
    public static String ListFiles()
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/files"))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en ListFiles: " + e.getMessage(), e);
        }
    }

    //Subir Archivos Cifrado
    public static String PostFile(String fileName, String content, String iv, String aesKey)
    {
        try
        {
            String json = "{"
                + "\"fileName\":\"" + esc(fileName) + "\","
                + "\"content\":\"" + esc(content) + "\","
                + "\"iv\":\"" + esc(iv) + "\","
                + "\"aesKey\":\"" + esc(aesKey) + "\""
                + "}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/files"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en PostFile: " + e.getMessage(), e);
        }
    }

    //Descargar Archivo por ID (GET)
    public static String GetFile(String fileId)
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/files/" + encUrl(fileId)))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en GetFile: " + e.getMessage(), e);
        }
    }

    //Compartir Archivo con Otro Usuario (POST)
    public static String ShareFile(String fileId, String targetUser, String aesKey)
    {
        try
        {
            String json = "{"
                    + "\"targetUser\":\"" + esc(targetUser) + "\","
                    + "\"aesKey\":\"" + esc(aesKey) + "\""
                    + "}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/files/" + encUrl(fileId) + "/share"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en ShareFile: " + e.getMessage(), e);
        }
    }

    //Borrar Archivo (DELETE)
    public static String DeleteFile(String fileId) //Borrar archivo
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/files/" + encUrl(fileId)))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();

            return enviar(req);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error en DeleteFile: " + e.getMessage(), e);
        }
    }


    /* AUXILIARES */

    private static String enviar(HttpRequest req)
    {
        try
        {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        }
        catch (Exception e)
        {
            throw new RuntimeException("No se pudo conectar con el servidor: " + e.getMessage(), e);
        }
    }

    private static String encUrl(String valor)
    {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static String esc(String s)
    {
        if (s == null) return "";

        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String extraerCampo(String json, String clave)
    {
        if (json == null) return null;

        String patron = "\"" + clave + "\":\"";
        int inicio = json.indexOf(patron);

        if (inicio == -1) return null;

        inicio += patron.length();
        int fin = json.indexOf("\"", inicio);

        if (fin == -1) return null;

        return json.substring(inicio, fin);
    }
}