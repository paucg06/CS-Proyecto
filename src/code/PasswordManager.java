import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class PasswordManager
{
    public byte[] CodePass(String password, byte[] salt) //Aplica hash+salt a la contrasenya
    {        
        try
        {
            //Preparo la funcion sha3-256
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");

            //Anyadir el salt a la funcion hash
            sha.update(salt);
            //Transformo la entrada a bytes y la codifico con sha
            byte[] hashedPassword = sha.digest(password.getBytes(StandardCharsets.UTF_8));

            //Anyadir contrasenya y salt al resultado.
            return hashedPassword;
        } catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("El algoritmo SHA3-256 no está disponible en este entorno.", e);
        }
    }
}