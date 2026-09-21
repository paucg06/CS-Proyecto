import cypher.*; //Incluir carpeta cypher

//Librerias
import java.util.Scanner;
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
        //Conexion a otras clases
        AES aes = new AES();
        RSA rsa = new RSA();
        DbManager db = new DbManager();
        UI ui = new UI();

        Scanner scanner = new Scanner(System.in);

        int opcion;

        do
        {
            System.out.println("==============================");
            System.out.println("       MI PROYECTO JAVA       ");
            System.out.println("==============================");
            System.out.println("1. Actualizar");
            System.out.println("2. Mostrar Archivos");
            System.out.println("3. AES");
            System.out.println("5. DB");
            System.out.println("6. UI");

            System.out.println("0. Salir");
            System.out.println("==============================");
            System.out.print("Selecciona una opción: ");

            opcion = scanner.nextInt();

            switch (opcion)
            {
                case 1:
                    cargarArchivos();
                    break;

                case 2:
                    cargarArchivos();
                    mostrarArchivos();
                    break;
                case 3:
                    aes.HelloAES();
                    break;
                case 5:
                    db.HelloDbManager();
                    break;
                case 6:
                    ui.HelloUI();
                    break;

                case 0:
                    System.out.println("\nSaliendo...");
                    break;

                default:
                    System.out.println("\nOpción no válida.\n");
            }

        } while (opcion != 0);

        scanner.close();
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
    
    public static void mostrarArchivos()
    {
        for (int i = 0; i < matrizArchivos.length; i++)
        {
            String nombre = (String) matrizArchivos[i][0];
            byte[] contenido = (byte[]) matrizArchivos[i][1];

            System.out.println(nombre);
            System.out.println(contenido.length + " bytes");
        }
    }
}
