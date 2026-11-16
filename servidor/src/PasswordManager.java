import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/*
    INCOMPLETO
    
    Esta clase se dedica al cifrado y comprobacion contra bd de contrasenyas de usuario, se accede a ella con las funciones publicas:
    Registro(): Devuelve contrasenya hasheada y el salt.
    ComprobarPass(): Devuelve cierto si la contrasenya es correcta para el usuario indicado.
*/

public class PasswordManager
{
    private static byte[] CodePass(byte[] password, byte[] salt, byte[] pepper) //Aplica hash+salt a la contrasenya
    {
        try
        {
            //Preparo la funcion sha3-256
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");

            //Anyadir el salt a la funcion hash
            sha.update(pepper);
            //Anyadir el salt a la funcion hash
            sha.update(salt);
            //Transformo la entrada a bytes y la codifico con sha
            byte[] hashedPassword = sha.digest(password);

            //Devolver contrasenya hasheada
            return hashedPassword;
        } catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("El algoritmo SHA3-256 no está disponible en este entorno.", e);
        }
    }

    //!Implementar cuando este el CRUD implementado
    private static byte[][] GetUserData(String user) //Lee el salt y la contrasenya cifrada del usuario solicitado
    {
        return null;
    }

    private static byte[] GetPepper()//Lee la pimienta del entorno usando la clase EnvConfig
    {
        String key = "APP_SECURITY_PEPPER";
        return Base64.getDecoder().decode(EnvConfig.get(key));
    }

    public static byte[][] Registro(byte[] password) //Devuelve un salt aleatorio generado y la contrasenya pasada por un hash+salt+pepper
    {
        byte[][] result = new byte[2][];
        result[0] = RandKeyGenerator.GenKey(32);
        result[1] = CodePass(password, result[0], GetPepper());
        return result;
    }

    public static boolean ComprobarPass(byte[] password, String user)//Comprueba la contrasenya con la base de datos aplicandole hash+salt+pepper
    {
        byte[][] userData = GetUserData(user);
        byte[] toCheck = CodePass(password, userData[0], GetPepper());
        return MessageDigest.isEqual(toCheck, userData[1]); //Se usa este metodo para evitar Timming Attacks con Arrays.equal()
    }
}
