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

    //Recibe encImg64 codificada y en base64 y key en claro
    public static byte[] Decode(String encImg64, String key) throws Exception //Devuelve la imagen descifrada por la key en array de bytes
    {
        byte[] result;
        //Transformar la clave recibida
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        //Configurar el cipher con la clave
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        //Recuperar de base64
        byte[] encImg = Base64.getDecoder().decode(encImg64);

        //Desencriptar imagen
        result = cipher.doFinal(encImg);

        return result;
    }
}