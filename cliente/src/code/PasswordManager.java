import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PasswordManager
{
    //Esta funcion se elminara una vez refactorizado cliente.
    //Old
    public static byte[] CodePass(String password, byte[] salt) //Aplica hash+salt a la contrasenya
    {
        try
        {
            //Preparo la funcion sha3-256
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");

            //Anyadir el salt a la funcion hash
            sha.update(salt);
            //Transformo la entrada a bytes y la codifico con sha
            byte[] hashedPassword = sha.digest(password.getBytes(StandardCharsets.UTF_8));

            //Devolver contrasenya hasheada
            return hashedPassword;
        } catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("El algoritmo SHA3-256 no está disponible en este entorno.", e);
        }
    }

    //New
    public static byte[] CodePass(byte[] password) //Aplica hash a la contrasenya
    {
        try
        {
            //Preparo la funcion sha3-256
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");
            
            //Codifico la entrada con sha
            byte[] hashedPassword = sha.digest(password);

            //Devolver contrasenya hasheada
            return hashedPassword;
        } catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("El algoritmo SHA3-256 no está disponible en este entorno.", e);
        }
    }

    //Toma la contrasenya, le aplica hash y la divide en dos byte[].
    public static byte[][] SplitPass(String password)
    {
        byte[][] result = new byte[2][];
        byte[] hashed = CodePass(password.getBytes(StandardCharsets.UTF_8));
        int mitad = hashed.length / 2;

        result[0] = Arrays.copyOfRange(hashed, 0, mitad);
        result[1] = Arrays.copyOfRange(hashed, mitad, hashed.length);

        return result;
    }
}
