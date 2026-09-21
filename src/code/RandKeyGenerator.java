import java.util.Random;


public class RandKeyGenerator
{
    private static String GenKey(int bits) //16 para el AES
    {
        Random rnd = new Random();
        String result = "";
        for(int i = 0; i < bits; i++)
        {
            result += Integer.toString(rnd.nextInt(9));
        }
        return result;
    }
}