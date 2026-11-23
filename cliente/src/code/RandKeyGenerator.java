import java.security.SecureRandom;

public class RandKeyGenerator
{
    private final static SecureRandom random = new SecureRandom();
    public static byte[] GenKey(int bytes) //16 para el AES
    {
        byte[] result = new byte[bytes];
        random.nextBytes(result);
        return result;
    }
}