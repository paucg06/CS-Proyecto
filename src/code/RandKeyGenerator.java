import java.security.SecureRandom;


public class RandKeyGenerator
{
    private static byte[] GenKey(int bytes) //16 para el AES
    {
        SecureRandom random = new SecureRandom();
        byte[] result = new byte[bytes];
        random.nextBytes(result);
        return result;
    }
}