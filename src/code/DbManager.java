// ============================================================
// DbManager: guarda y lee CLAVES en el archivo database.csv
//
// Esta clase NO cifra nada. Solo guarda y lee texto.
//
// Un CSV es un archivo de texto donde cada línea es un registro
// y las columnas se separan con ";". Ejemplo de database.csv:
//
//   USUARIO;u;clavePublica;clavePrivadaCifrada;salt;iv
//   ARCHIVO;foto.jpg;archivoCifrado;iv;claveAESCifrada;u
//
// Las columnas son:  tipo ; campo1 ; campo2 ; campo3 ; campo4 ; campo5
//
// Hay dos tipos de línea:
//   USUARIO -> las claves RSA (pública y privada protegida)
//   ARCHIVO -> un archivo cifrado y su clave AES
//
// IMPORTANTE: la carpeta src/database se crea sola si no existe.
// ============================================================

import java.io.*; // clases para leer y escribir archivos
import java.util.ArrayList;


public class DbManager
{
    // Dónde está el archivo CSV
    private static String rutaCsv = "src/database/database.csv";

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
        if (dataline[0].contains(";") || ObtenerUsuario(dataline[0]) != null)
        {
            return false; // nombre no valido o ya existe, no lo guardamos
        }
        return EscribirLinea("USUARIO;" + String.join(";", dataline));
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
        if (dataline[0].contains(";") || ObtenerArchivo(dataline[0]) != null)
        {
            return false; // nombre no valido o ya existe, no lo guardamos
        }
        return EscribirLinea("ARCHIVO;" + String.join(";", dataline));
    }

    //Devuelve los nombres de todos los archivos de un usuario (array vacio si no tiene)
    public static String[] ObtenerArchivos(String userName)
    {
        return BuscarAll(userName);
    }

    // --------------------------------------------------------
    // MÉTODOS PRIVADOS (solo se usan dentro de esta clase)
    // --------------------------------------------------------

    // Crea la carpeta y el archivo CSV si no existen
    private static void AsegurarCsv() throws IOException
    {
        File csv = new File(rutaCsv);
        if (csv.getParentFile() != null)
        {
            csv.getParentFile().mkdirs();
        }
        csv.createNewFile(); // no hace nada si ya existe
    }

    // Añade una línea al final del CSV.
    // El "true" significa "añadir al final" (sin él se borraría todo el archivo).
    private static boolean EscribirLinea(String linea)
    {
        try
        {
            AsegurarCsv();
            try (FileWriter escritor = new FileWriter(rutaCsv, true))
            {
                escritor.write(linea + "\n");
            }
            return true;
        }
        catch (IOException e)
        {
            System.out.println("Error al escribir: " + e.getMessage());
            return false;
        }
    }

    // Recorre el CSV línea a línea buscando una del tipo indicado.
    // Devuelve las columnas de la línea encontrada (sin el tipo), o null si no hay ninguna.
    private static String[] Buscar(String tipo, String nombre)
    {
        try
        {
            AsegurarCsv();
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
        }
        catch (IOException e)
        {
            System.out.println("Error al leer: " + e.getMessage());
        }

        return null; // no se ha encontrado nada
    }

    private static String[] BuscarAll(String userName)
    {
        ArrayList<String> archivos = new ArrayList<>();
        try
        {
            AsegurarCsv();
            try (BufferedReader lector = new BufferedReader(new FileReader(rutaCsv)))
            {
                String linea;

                while ((linea = lector.readLine()) != null)
                {
                    String[] c = linea.split(";", -1);

                    // Si la línea está mal formada, la saltamos
                    if (c.length != 6)
                    {
                        continue;
                    }

                    // En las lineas ARCHIVO la ultima columna (c[5]) es el propietario
                    if (c[0].equals("ARCHIVO") && c[5].equals(userName))
                    {
                        archivos.add(c[1]); // nombre original del archivo
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Error al leer: " + e.getMessage());
        }

        return archivos.toArray(String[]::new);
    }
}
