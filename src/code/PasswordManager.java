import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordManager
{
    public String[] CodePass(String password) //Aplica hash+salt a la contrasenya
    {
        String result[] = new String[2];
        
        try
        {
            //Preparo la funcion sha3-256
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");

            //Generar un salt aleatorio
            byte[] salt = RandKeyGenerator.GenKey(16);

            //Anyadir el salt a la funcion hash
            sha.update(salt);
            //Transformo la entrada a bytes y la codifico con sha
            byte[] hashedPassword = sha.digest(password.getBytes(StandardCharsets.UTF_8));

            String hashedPassword64 = Base64.getEncoder().encodeToString(hashedPassword);
            String salt64 = Base64.getEncoder().encodeToString(salt);

            //Anyadir contrasenya y salt al resultado.
            result[0] = hashedPassword64;
            result[1] = salt64;

        } catch (NoSuchAlgorithmException e)
        {
            System.err.println("El algoritmo SHA3-256 no está disponible en este entorno.");
        }
        return result; //Devuelve la contrasenya y su salt
    }
}