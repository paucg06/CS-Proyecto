import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionManager
{
    private static final long DURACION_MS = 60L * 60 * 1000; // 1 hora de inactividad
    private static final int MAX_FALLOS = 5;
    private static final long BLOQUEO_MS = 5L * 60 * 1000;   // 5 minutos

    private static final SecureRandom random = new SecureRandom();

    private static class Sesion
    {
        final String usuario;
        volatile long caduca;

        Sesion(String usuario, long caduca)
        {
            this.usuario = usuario;
            this.caduca = caduca;
        }
    }

    private static class Fallos
    {
        int cuenta = 0;
        long bloqueadoHasta = 0;
    }

    private static final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private static final Map<String, Fallos> fallos = new HashMap<>(); // acceso siempre synchronized

    // SESIONES

    public static String crear(String usuario)
    {
        long ahora = System.currentTimeMillis();
        sesiones.values().removeIf(s -> s.caduca < ahora); // limpieza de sesiones caducadas

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new Sesion(usuario, ahora + DURACION_MS));
        return token;
    }

    // Devuelve el usuario del token, o null si no existe o ha caducado. Renueva la caducidad.
    public static String validar(String token)
    {
        if (token == null)
        {
            return null;
        }
        Sesion s = sesiones.get(token);
        if (s == null)
        {
            return null;
        }
        long ahora = System.currentTimeMillis();
        if (s.caduca < ahora)
        {
            sesiones.remove(token);
            return null;
        }
        s.caduca = ahora + DURACION_MS;
        return s.usuario;
    }

    public static void cerrar(String token)
    {
        if (token != null)
        {
            sesiones.remove(token);
        }
    }

    public static synchronized boolean bloqueado(String usuario)
    {
        Fallos f = fallos.get(usuario);
        if (f == null || f.cuenta < MAX_FALLOS)
        {
            return false;
        }
        if (System.currentTimeMillis() >= f.bloqueadoHasta)
        {
            fallos.remove(usuario); // el bloqueo ha terminado
            return false;
        }
        return true;
    }

    public static synchronized void registrarFallo(String usuario)
    {
        Fallos f = fallos.computeIfAbsent(usuario, k -> new Fallos());
        f.cuenta++;
        if (f.cuenta >= MAX_FALLOS)
        {
            f.bloqueadoHasta = System.currentTimeMillis() + BLOQUEO_MS;
        }
    }

    public static synchronized void limpiarFallos(String usuario)
    {
        fallos.remove(usuario);
    }
}
