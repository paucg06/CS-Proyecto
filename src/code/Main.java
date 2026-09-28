//Librerias
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main
{
    public static String DIREC_ARCHIVOS = "src/img"; //seleccionar src y img (¡¡Debe ser modificado si se modifica la estructura de carpetas o se mueve el .bat!!)
    public static Object[][] matrizArchivos = new Object[0][2];;

    public static void main(String[] args)
    {
        ObtenerValoresAES();
        UI ui = new UI();
        ui.AbrirVentana(); //Abrimos directamente la ventana, sin menu de consola
    }

    // GUARDA [nombre][valor] de los ARCHIVOS de "DIREC_ARCHIVOS"
    public static void cargarArchivos()
    {
        //Crea Instancia "DIREC_ARCHIVOS"
        Path ruta  = Paths.get(DIREC_ARCHIVOS);

        
        try(Stream<Path> stream = Files.list(ruta))
        {
            // Obtenemos TODOS los Archivos del Directorio
            List<Path> archivos = stream.filter(Files::isRegularFile).toList();

            // INSTANCIAMOS/ACTUALIZAMOS Matriz [nombre] [contenido]
            matrizArchivos = new Object[archivos.size()][2];

            // GUARDA los valores en una MATRIZ[nombre][valor]
            for(int i = 0; i < archivos.size(); i++)
            {
                Path archivo = archivos.get(i);

                // EXTRAEMOS Columna 0: NOMBRE
                matrizArchivos[i][0] = archivo.getFileName().toString();

                // EXTRAEMOS Columna 1: CONTENIDO BYTES
                matrizArchivos[i][1] = Files.readAllBytes(archivo);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            matrizArchivos = new Object[0][2];
        }
    }
    
    public static void MostrarArchivos()
    {
        for (int i = 0; i < matrizArchivos.length; i++)
        {
            String nombre = (String) matrizArchivos[i][0];
            byte[] contenido = (byte[]) matrizArchivos[i][1];

            System.out.println(nombre);
            System.out.println(contenido.length + " bytes");
        }
    }

    public static void ObtenerValoresAES()
    {
        // Cargar los archivos en matrizArchivos
        cargarArchivos();

        // Comprobar que existe al menos un archivo
        if (matrizArchivos.length == 0)
        {
            System.out.println("No hay archivos para cifrar.");
            return;
        }

        // Obtener la imagen del primer archivo
        byte[] imagen = (byte[]) matrizArchivos[0][1];

        // Generar clave AES de 16 bytes
        byte[] keyAES = RandKeyGenerator.GenKey(16);

        // Generar IV de 12 bytes
        byte[] iv = RandKeyGenerator.GenKey(12);

        // Cifrar la imagen
        String imagenCifrada = AES.Encode(imagen, keyAES, iv);

        // Mostrar información
        System.out.println("Archivo: " + matrizArchivos[0][0]);
        System.out.println("Tamaño original: " + imagen.length + " bytes");
        System.out.println("Imagen cifrada: " + imagenCifrada);
    }
}