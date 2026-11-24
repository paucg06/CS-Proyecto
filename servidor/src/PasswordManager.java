import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordManager
{
    private static byte[] pepper = null; // se carga la primera vez que se necesita

    private static byte[] CodePass(byte[] password, byte[] salt, byte[] pepper) //Aplica hash+salt+pimienta a la contrasenya
    {
        try
        {
            //Preparo la funcion sha3-256
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");

            //Anyadir la pimienta a la funcion hash
            sha.update(pepper);
            //Anyadir el salt a la funcion hash
            sha.update(salt);
            //Codifico la entrada con sha
            byte[] hashedPassword = sha.digest(password);

            //Devolver contrasenya hasheada
            return hashedPassword;
        } catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("El algoritmo SHA3-256 no está disponible en este entorno.", e);
        }
    }

    //Lee {salt, hash} del usuario solicitado. null si no existe o no tiene credenciales
    private static byte[][] GetUserData(String user)
    {
        /*try
        {
            String[] datos = ServerDb.obtenerAuth(user);
            if (datos == null)
            {
                return null;
            }
            return new byte[][]{Base64.getDecoder().decode(datos[0]), Base64.getDecoder().decode(datos[1])};
        } catch (SQLException e)
        {
            throw new IllegalStateException("No se pudo leer el usuario de la base de datos.", e);
        }*/
       return null;
    }

    private static synchronized byte[] GetPepper()//Lee la pimienta del entorno usando la clase EnvConfig
    {
        if (pepper == null)
        {
            String key = "APP_SECURITY_PEPPER";
            pepper = Base64.getDecoder().decode(EnvConfig.get(key));
        }
        return pepper;
    }

    //Comprueba al arrancar que la pimienta existe (falla pronto en vez de en el primer login)
    public static void ComprobarConfiguracion()
    {
        GetPepper();
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
        if (userData == null)
        {
            //Usuario inexistente: se hace el mismo calculo para no delatarlo por el tiempo de respuesta
            byte fake[] = CodePass(password, new byte[32], GetPepper());
            MessageDigest.isEqual(fake, fake);
            return false;
        }
        byte[] toCheck = CodePass(password, userData[0], GetPepper());
        return MessageDigest.isEqual(toCheck, userData[1]); //Se usa este metodo para evitar Timming Attacks con Arrays.equal()
    }

    public static String SaltPublica(String user)
    {
        byte[] falso = CodePass(user.getBytes(StandardCharsets.UTF_8), "salt-falso".getBytes(StandardCharsets.UTF_8), GetPepper());
        return Base64.getEncoder().encodeToString(falso);
    }
}