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
    public static String token = null;

    //Tokens Verificar Identidad USUARIO protocolo HTTP
    public static void setToken(String nuevoToken)
    {
        token = nuevoToken;
    }

    public static void clearToken()
    {
        token = null;
    }

    //Crear un USUARIO (POST)
    public static String Register(String username, String authSecret, String publicKey, String privateKeyEnc, String salt, String iv) //Crear usuario
    {
        String json = String.format("PRUEBA REGISTER");

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/auth/register"))
            .header("Content-Type", "aplication/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return enviar(req);
    }

    //Acceder como USUARIO (POST)
    public static String Login(String username, String authSecret) //Comprobar credenciales
    {
        String json = String.format("PRUEBA LOGIN");

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublisher.ofString(json))
            .build();
        return enviar(req);
    }

    //Cerrar Sesion USUARIO (POST)
    public static String Logout() //Comprobar credenciales
    {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/auth/logout"))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublisher.noBody())
            .build();
        enviar(req);
        token = null;
    }

    public static void Get() //Obtener archivo
    {

    }

    public static void Post() //Subir archivo
    {
         String json = String.format("PRUEBA COMPARTIR ARCHIVOS");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/files/" + fileId + "/share"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return enviar(req);
    }

    public static String GetKP(String username) //Obtener clave publica de un usuario
    {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/users/" + encUrl(username)))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublisher.noBody())
            .build();
        enviar(req);
    }

    public static String Delete() //Borrar archivo
    {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/files/" + fileId))
            .header("Authorization", "Bearer " + token)
            .DELETE()
            .build();
        return enviar(req);
    } 
}