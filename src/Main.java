import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        int opcion;

        do {
            System.out.println("==============================");
            System.out.println("       MI PROYECTO JAVA       ");
            System.out.println("==============================");
            System.out.println("1. Saludar");
            System.out.println("2. Mostrar información");
            System.out.println("0. Salir");
            System.out.println("==============================");
            System.out.print("Selecciona una opción: ");

            opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    System.out.println("\n¡Hola! Bienvenido al programa.\n");
                    break;

                case 2:
                    System.out.println("\nProyecto realizado en Java.\n");
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