import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RefreshTokenManager {
    // Clave de al menos 32 bytes (256 bits)
    private static byte[] SECRET = null;

    //Funcion base de acceso a esta clase.
    //Recibe por parametro un string con el id del usuario
    public static String GenerateRefreshToken(String userId)
    {
        try
        {
            long now = Instant.now().getEpochSecond();
            long exp = now + (60 * 60 * 24 *7); // caduca en 7 días
            byte[] tokenOpaco = RandKeyGenerator.GenKey(32);
            String token64 = Base64.getEncoder().encodeToString(tokenOpaco);
            byte[] tokenCodificado = HmacSha256(tokenOpaco, GetSECURITY());
            return SaveToken(userId, tokenCodificado, exp) ? token64 : null;
        }
        catch (Exception e)
        {
            System.err.println("Error al generar el refresh token. " + e);
            return null;
        }
    }

    //Implementar cuando este la base de datos
    public static boolean CheckRefreshToken(String userId, byte[] tokenOpaco)
    {
        return false;
    }

    private static byte[] HmacSha256(byte[] data, byte[] key) throws Exception //Genera el hash con el algoritmo HmacSha256
    {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data);
    }

    private static synchronized byte[] GetSECURITY()//Lee la clave secreta del entorno usando la clase EnvConfig
    {
        if (SECRET == null)
        {
            String keyName = "REFRESH_TOKEN_SECURITY_KEY";
            String encodedKey = EnvConfig.get(keyName);
            if (encodedKey == null || encodedKey.isBlank())
            {
                throw new IllegalStateException("Falta o está vacía la variable de configuración: " + keyName);
            }
            byte[] decoded = Base64.getDecoder().decode(encodedKey);
            if (decoded.length < 32)
            {
                throw new IllegalStateException("REFRESH_TOKEN_SECURITY_KEY debe tener al menos 32 bytes decodificados");
            }
            SECRET = decoded;
        }
        return SECRET;
    }

    //Implementar cuando este la base de datos
    private static boolean SaveToken(String userId, byte[] tokenCodificado, long exp)
    {
        return false;
    }
}