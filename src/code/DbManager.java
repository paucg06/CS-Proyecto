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
import java.util.ArrayList;


public class DbManager
{
    // Dónde está el archivo CSV
    private static String rutaCsv = "src/database/database.csv";

    // Nombre del único usuario por ahora
    private String usuario = "u";

    // --------------------------------------------------------
    // USUARIO
    // --------------------------------------------------------

    // Devuelve los datos del usuario, o null si no existe:
    public static String[] ObtenerUsuario(String userName)
    {
        return Buscar("USUARIO", userName);
    }

    public static boolean GuardarUsuario(String[] dataline)
    {
        if (ObtenerUsuario(dataline[0]) != null)
        {
            return false; // ya existe, no lo guardamos otra vez
        }
        String toWrite = "USUARIO;";
        for(int i = 0; i < dataline.length; i++)
        {
            toWrite += dataline[i];
        }
        EscribirLinea(toWrite);
        return true;
    }

    // --------------------------------------------------------
    // ARCHIVOS
    // --------------------------------------------------------

    // Busca un archivo por su nombre original. Devuelve null si no existe:
    public static String[] ObtenerArchivo(String nombreOriginal)
    {
        return Buscar("ARCHIVO", nombreOriginal);
    }

    // Guarda un archivo cifrado y su clave AES
    public static boolean GuardarArchivo(String[] dataline)
    {
        if (ObtenerArchivo(dataline[3]) != null)
        {
            return false; // ya existe, no lo guardamos otra vez
        }
        String toWrite = "ARCHIVO;";
        for(int i = 0; i < dataline.length; i++)
        {
            toWrite += dataline[i];
        }
        EscribirLinea(toWrite);
        return true;
    }

    //Devuelve los nombres de todos los archivos de un usuario
    public static String[] ObtenerArchivos(String userName)
    {
        return BuscarAll(userName);
    }

    // --------------------------------------------------------
    // MÉTODOS PRIVADOS (solo se usan dentro de esta clase)
    // --------------------------------------------------------

    // Añade una línea al final del CSV.
    // El "true" significa "añadir al final" (sin él se borraría todo el archivo).
    private static void EscribirLinea(String linea)
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
    private static String[] Buscar(String tipo, String nombre)
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
                if (c.length != 6)
                {
                    continue;
                }

                //Por ahora no puede haber 2 archivos con el mismo nombre ni 2 usuarios con el mismo nombre
                boolean mismoTipo = c[0].equals(tipo);
                boolean mismoNombre = c[1].equals(nombre);

                if (mismoTipo && mismoNombre)
                {
                    String[] result = new String[5];
                    for(int i = 0; i < 5; i++)
                    {
                        result[i] = c[i+1];
                    }
                    return result; // encontrada
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Error al leer: " + e.getMessage());
        }

        return null; // no se ha encontrado nada
    }

    private static String[] BuscarAll(String userName)
    {
        try (BufferedReader lector = new BufferedReader(new FileReader(rutaCsv)))
        {
            String linea;
            ArrayList<String> archivos = new ArrayList<>();

            // Leemos línea a línea hasta que no queden (readLine devuelve null)
            while ((linea = lector.readLine()) != null)
            {
                // Separamos la línea en columnas. El -1 hace que no se pierdan
                // las columnas vacías del final.
                String[] c = linea.split(";", -1);

                // Si la línea está mal formada, la saltamos
                if (c.length != 6)
                {
                    continue;
                }

                //Por ahora no puede haber 2 archivos con el mismo nombre ni 2 usuarios con el mismo nombre
                boolean mismoTipo = c[0].equals("ARCHIVO");
                boolean mismoUser = c[5].equals(userName);

                if (mismoTipo && mismoUser)
                {
                    archivos.add(c[1]); // encontrada
                }
            }
            if(archivos.size() < 0)
            {
                return archivos.toArray(String[]::new);
            }
        }
        catch (IOException e)
        {
            System.out.println("Error al leer: " + e.getMessage());
        }

        return null; // no se ha encontrado nada
    }
}
