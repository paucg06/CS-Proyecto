import cypher.*; //Incluir carpeta cypher
//Librerias
import java.util.Scanner;

public class Main
{

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
            System.out.println("1. Saludar");
            System.out.println("2. Mostrar información");
            System.out.println("3. AES");
            System.out.println("4. RSA");
            System.out.println("5. DB");
            System.out.println("6. UI");

            System.out.println("0. Salir");
            System.out.println("==============================");
            System.out.print("Selecciona una opción: ");

            opcion = scanner.nextInt();

            switch (opcion)
            {
                case 1:
                    System.out.println("\n¡Hola! Bienvenido al programa.\n");
                    break;

                case 2:
                    System.out.println("\nProyecto realizado en Java.\n");
                    break;
                case 3:
                    aes.HelloAES();
                    break;
                case 4:
                    rsa.HelloRSA();
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
}