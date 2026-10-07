package cypher; //Declarar paquete

//Librerias
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AES
{
    public void HelloAES()
    {
        System.out.println("\n¡Hola, soy AES.\n");
    }

    //Recibe img en claro y key en claro
    public static byte[] Encode(byte[] archivo, byte[] key, byte[] iv) //Devuelve la imagen cifrada con AES
    {
        try
        {
            byte[] result;
            //Crear specs con la clave e iv
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);

            //Configurar el cipher con la clave y el iv
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            //Encriptar imagen
            result = cipher.doFinal(archivo);

            return result;
        } catch (Exception e)
        {
            throw new IllegalStateException("Error inesperado en el cifrado AES", e);
        }
    }

    //Recibe encImg64 codificada y en base64 y key en claro
    public static byte[] Decode(byte[] encArchivo, byte[] key, byte[] iv)//Devuelve la imagen descifrada por la key en array de bytes
    {
        try
        {
            byte[] result;
            //Crear specs con la clave e iv
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);

            //Configurar el cipher con la clave
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            //Desencriptar imagen
            result = cipher.doFinal(encArchivo);

            return result;
        } catch (Exception e)
        {
            throw new IllegalStateException("Error inesperado en el descifrado AES", e);
        }
    }
}