package cypher; //Declarar paquete

//Librerias
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AES
{
    public void HelloAES()
    {
        System.out.println("\n¡Hola, soy AES.\n");
    }

    public static String Encode(byte[] img, String key) throws Exception //Devuelve la imagen cifrada con AES en base64
    {
        String result;
        //Transformar la clave recibida
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        //Configurar el cipher con la clave
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        //Encriptar imagen
        byte[] encImg = cipher.doFinal(img);

        //Pasar a base64
        result = Base64.getEncoder().encodeToString(encImg);

        return result;
    }

    public static byte[] Decode(String encImg, String key) throws Exception
    {
        byte[] result = new byte[0];
        return result;
    }
}