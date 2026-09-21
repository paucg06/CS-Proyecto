// ============================================================
// DbManager: guarda y lee CLAVES en el archivo database.csv
//
// Esta clase NO cifra nada. Solo guarda y lee texto.
//
// Un CSV es un archivo de texto donde cada línea es un registro
// y las columnas se separan con ";". Ejemplo de database.csv:
//
//   USUARIO;u;clavePublica;clavePrivadaCifrada;
//   ARCHIVO;u;foto.jpg;foto.jpg.enc;claveAES
//
// Las columnas son:  tipo ; usuario ; campo1 ; campo2 ; campo3
//
// Hay dos tipos de línea:
//   USUARIO -> las claves RSA (pública y privada protegida)
//   ARCHIVO -> un archivo cifrado y su clave AES
//
// De momento hay un solo usuario, llamado "u".
//
// IMPORTANTE: la carpeta src/database y el archivo database.csv
// ya deben existir (el archivo puede estar vacío).
// ============================================================

import java.io.*; // clases para leer y escribir archivos

public class DbManager
{
    // Dónde está el archivo CSV
    private String rutaCsv = "src/database/database.csv";

    // Nombre del único usuario por ahora
    private String usuario = "u";

    // Método de prueba original
    public void HelloDbManager()
    {
        System.out.println("\n¡Hola, soy DbManager.\n");
    }

    // --------------------------------------------------------
    // USUARIO
    // --------------------------------------------------------

    // Guarda las claves RSA del usuario.
    // Devuelve true si se ha guardado, false si ya había un usuario guardado.
    public boolean guardarUsuario(String clavePublica, String clavePrivadaCifrada)
    {
        if (obtenerUsuario() != null)
        {
            return false; // ya existe, no lo guardamos otra vez
        }
        escribirLinea("USUARIO;" + usuario + ";" + clavePublica + ";" + clavePrivadaCifrada + ";");
        return true;
    }

    // Devuelve los datos del usuario, o null si no existe:
    //   resultado[2] = clave pública
    //   resultado[3] = clave privada cifrada
    public String[] obtenerUsuario()
    {
        return buscar("USUARIO", null);
    }

    // --------------------------------------------------------
    // ARCHIVOS
    // --------------------------------------------------------

    // Guarda un archivo cifrado y su clave AES
    public void guardarArchivo(String nombreOriginal, String nombreCifrado, String claveAes)
    {
        escribirLinea("ARCHIVO;" + usuario + ";" + nombreOriginal + ";" + nombreCifrado + ";" + claveAes);
    }

    // Busca un archivo por su nombre original. Devuelve null si no existe:
    //   resultado[2] = nombre original
    //   resultado[3] = nombre del archivo cifrado
    //   resultado[4] = clave AES
    public String[] obtenerArchivo(String nombreOriginal)
    {
        return buscar("ARCHIVO", nombreOriginal);
    }

    // --------------------------------------------------------
    // MÉTODOS PRIVADOS (solo se usan dentro de esta clase)
    // --------------------------------------------------------

    // Añade una línea al final del CSV.
    // El "true" significa "añadir al final" (sin él se borraría todo el archivo).
    private void escribirLinea(String linea)
    {
        try (FileWriter escritor = new FileWriter(rutaCsv, true))
        {
            escritor.write(linea + "\n");
        }
        catch (IOException e)
        {
            System.out.println("Error al escribir: " + e.getMessage());
        }
    }

    // Recorre el CSV línea a línea buscando una del tipo indicado.
    // Si nombreArchivo no es null, también tiene que coincidir el nombre del archivo.
    // Devuelve las columnas de la línea encontrada, o null si no hay ninguna.
    private String[] buscar(String tipo, String nombreArchivo)
    {
        try (BufferedReader lector = new BufferedReader(new FileReader(rutaCsv)))
        {
            String linea;

            // Leemos línea a línea hasta que no queden (readLine devuelve null)
            while ((linea = lector.readLine()) != null)
            {
                // Separamos la línea en columnas. El -1 hace que no se pierdan
                // las columnas vacías del final.
                String[] c = linea.split(";", -1);

                // Si la línea está mal formada, la saltamos
                if (c.length != 5)
                {
                    continue;
                }

                boolean mismoTipo = c[0].equals(tipo);
                boolean mismoUsuario = c[1].equals(usuario);
                boolean mismoArchivo = (nombreArchivo == null) || c[2].equals(nombreArchivo);

                if (mismoTipo && mismoUsuario && mismoArchivo)
                {
                    return c; // encontrada
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Error al leer: " + e.getMessage());
        }

        return null; // no se ha encontrado nada
    }
}
