import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JWTCreator {

    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    // Clave de al menos 32 bytes (256 bits)
    private static byte[] SECRET = null;

    //Funcion base de acceso a esta clase.
    //Recibe por parametro un string con el id del usuario y otro con su rol (USR para los usuarios normales, por ahora no hay mas roles)
    public static String GenerateHs256Token(String subject, String role) throws Exception
    {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

        long now = Instant.now().getEpochSecond();
        long exp = now + 300; // caduca en 5 minutos
        String jti = UUID.randomUUID().toString(); //Id

        String payloadJson = String.format(
                "{\"iss\":\"cliente-java-local\",\"sub\":\"%s\",\"iat\":%d,\"exp\":%d,\"jti\":\"%s\",\"role\":\"%s\"}",
                EscapeJson(subject), now, exp, jti, EscapeJson(role)
        );

        String encodedHeader = BASE64_URL.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = BASE64_URL.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        String signingInput = encodedHeader + "." + encodedPayload;
        byte[] signatureBytes = SignHmacSha256(signingInput, GetSECURITY());
        String encodedSignature = BASE64_URL.encodeToString(signatureBytes);

        return signingInput + "." + encodedSignature;
    }

    private static byte[] SignHmacSha256(String data, byte[] key) throws Exception //Firma el contenido con HmacSha256
    {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String EscapeJson(String value) //Formatea el texto para adecuarlo a json
    {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static synchronized byte[] GetSECURITY()//Lee la pimienta del entorno usando la clase EnvConfig
    {
        if (SECRET == null)
        {
            String keyName = "JWT_SECURITY_KEY";
            String encodedKey = EnvConfig.get(keyName);
            if (encodedKey == null || encodedKey.isBlank())
            {
                throw new IllegalStateException("Falta o está vacía la variable de configuración: " + keyName);
            }
            byte[] decoded = Base64.getDecoder().decode(encodedKey);
            if (decoded.length < 32)
            {
                throw new IllegalStateException("JWT_SECURITY_KEY debe tener al menos 32 bytes decodificados");
            }
            SECRET = decoded;
        }
        return SECRET;
    }
}