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

    //Recibe img en claro y key en claro
    public static String Encode(byte[] img, byte[] key) //Devuelve la imagen cifrada con AES en base64
    {
        try
        {
            String result;
            //Transformar la clave recibida
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            //Configurar el cipher con la clave
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            //Encriptar imagen
            byte[] encImg = cipher.doFinal(img);

            //Pasar a base64
            result = Base64.getEncoder().encodeToString(encImg);

            return result;
        } catch (Exception e)
        {
            throw new IllegalStateException("Error inesperado en el cifrado AES", e);
        }   
    }

    //Recibe encImg64 codificada y en base64 y key en claro
    public static byte[] Decode(String encImg64, byte[] key)//Devuelve la imagen descifrada por la key en array de bytes
    {
        try
        {
            byte[] result;
            //Transformar la clave recibida
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            //Configurar el cipher con la clave
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            //Recuperar de base64
            byte[] encImg = Base64.getDecoder().decode(encImg64);

            //Desencriptar imagen
            result = cipher.doFinal(encImg);

            return result;
        } catch
        {
            throw new IllegalStateException("Error inesperado en el descifrado AES", e);
        }
    }
}