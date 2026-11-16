import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class EnvConfig {

    private static final Properties envProps = new Properties();

    static
    {
        Path envPath = Path.of(".env.desarrollo");
        if (Files.exists(envPath))
        {
            try (FileInputStream fis = new FileInputStream(envPath.toFile()))
            {
                envProps.load(fis);
            } catch (IOException e)
            {
                throw new RuntimeException("Error al leer el archivo .env", e);
            }
        }
    }

    public static String get(String key) {
        // 1. Prioriza variables de entorno reales del sistema operativo (producción)
        String value = System.getenv(key);
        if (value != null && !value.isBlank())
        {
            return value;
        }
        // 2. Si no existe en el sistema, la busca en el archivo .env (desarrollo)
        value = envProps.getProperty(key);
        if (value == null || value.isBlank())
        {
            throw new IllegalStateException("Variable de configuración obligatoria no encontrada: " + key);
        }
        return value.trim();
    }
}