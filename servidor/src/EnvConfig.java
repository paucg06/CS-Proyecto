import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class EnvConfig {

    private static final Properties envProps = new Properties();

    // Rutas donde se busca el archivo de configuracion, por orden de prioridad.
    private static final String[] RUTAS = {
        ".env", "servidor/.env",
        ".env.desarrollo", "servidor/.env.desarrollo"
    };

    static
    {
        for (String ruta : RUTAS)
        {
            Path envPath = Path.of(ruta);
            if (Files.exists(envPath))
            {
                try (FileInputStream fis = new FileInputStream(envPath.toFile()))
                {
                    envProps.load(fis);
                } catch (IOException e)
                {
                    throw new RuntimeException("Error al leer el archivo " + ruta, e);
                }
                break; // se usa solo el primero que exista
            }
        }
    }

    public static String get(String key) {
        //Prioriza variables de entorno reales del sistema operativo (producción)
        String value = System.getenv(key);
        if (value != null && !value.isBlank())
        {
            return value;
        }
        //Si no existe en el sistema, la busca en el archivo .env (desarrollo)
        value = envProps.getProperty(key);
        if (value == null || value.isBlank())
        {
            throw new IllegalStateException("Variable de configuración obligatoria no encontrada: " + key);
        }
        return value.trim();
    }
}