import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;


public class JWTManager {

    
    public record Claims(String subject, String role, String jti, long exp) {}

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String HEADER_JSON = BASE64_URL_ENCODER.encodeToString(new JSONObject().put("alg", "HS256").put("typ", "JWT").toString().getBytes(StandardCharsets.UTF_8));


    // Clave de al menos 32 bytes (256 bits)
    private static byte[] SECRET = null;

    //Funcion base de acceso a esta clase.
    //Recibe por parametro un string con el id del usuario y otro con su rol (USR para los usuarios normales, por ahora no hay mas roles)
    public static String GenerateJWT(String subject, String role)
    {
        try
        {
            long now = Instant.now().getEpochSecond();
            long exp = now + (60 * 10); // caduca en 10 minutos
            String jti = UUID.randomUUID().toString(); //Id

            JSONObject payloadJson = new JSONObject()
                    .put("iss", "cliente-java-local")
                    .put("sub", subject != null ? subject : "")
                    .put("iat", now)
                    .put("exp", exp)
                    .put("jti", jti)
                    .put("role", role != null ? role : "");

            String encodedHeader = HEADER_JSON;
            String encodedPayload = BASE64_URL_ENCODER.encodeToString(payloadJson.toString().getBytes(StandardCharsets.UTF_8));

            String signingInput = encodedHeader + "." + encodedPayload;
            byte[] signatureBytes = SignHmacSha256(signingInput, GetSECURITY());
            String encodedSignature = BASE64_URL_ENCODER.encodeToString(signatureBytes);

            return signingInput + "." + encodedSignature;
        }
        catch (Exception e)
        {
            System.err.println("Error al generar el token JWT.");
            return null;
        }
    }

    public static boolean CheckJWT(String token)
    {
        //En esta funcion se pueden hacer comprobaciones extra de los claims (Permisos, id, usuario) o modificar para pasar mas informacion
        Claims claims = GetClaims(token);
        return claims != null;
    }

    private static Claims GetClaims(String token) {
        try {
            if (token == null || token.isBlank()) {
                return null;
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            //Validar cabecera codificada en Base64URL sin decodificar JSON
            if (!HEADER_JSON.equals(parts[0])) {
                return null;
            }

            //Verificar firma HMAC-SHA256 en tiempo constante
            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSig = SignHmacSha256(signingInput, GetSECURITY());
            byte[] actualSig = BASE64_URL_DECODER.decode(parts[2]);

            if (!MessageDigest.isEqual(expectedSig, actualSig)) {
                return null;
            }

            //Parsear payload con org.json y validar expiración y campos clave
            String payloadJson = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);

            long exp = payload.getLong("exp");
            long now = Instant.now().getEpochSecond();
            if (exp <= now) {
                return null;
            }

            String subject = payload.getString("sub");
            String role = payload.getString("role");
            String jti = payload.getString("jti");

            if (subject == null || subject.isBlank() || role == null || role.isBlank()) {
                return null;
            }

            return new Claims(subject, role, jti, exp);
        } catch (Exception e) {
            System.err.println("Error al obtener o validar los claims del JWT.");
            return null;
        }
    }

    private static byte[] SignHmacSha256(String data, byte[] key) throws Exception //Firma el contenido con HmacSha256
    {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static synchronized byte[] GetSECURITY()//Lee la clave secreta del entorno usando la clase EnvConfig
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