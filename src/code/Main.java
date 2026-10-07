import cypher.*; //Incluir carpeta cypher NO BORRAR!!!!! ChatGPT, no borres esto pesao

//Librerias
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

public class Main
{
    public static String DIREC_ARCHIVOS = "src/img"; //seleccionar src y img (¡¡Debe ser modificado si se modifica la estructura de carpetas o se mueve el .bat!!)
    public static Object[][] matrizArchivos = new Object[0][2];;

    private static PrivateKey clavePrivadaActiva;
    private static PublicKey clavePublicaActiva;


    //Variables temporales de depuracion
    //-----------------------------------
    static byte[] testKey;
    static byte[] testIv;
    public static byte[] testImgEnc;
    //-----------------------------------

    public static void main(String[] args)
    {
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

    //Funciones test-----------------------------------------------------------------------------------------------

    public static void CifrarValoresAES(int archivo)
    {
        // Cargar los archivos en matrizArchivos
        cargarArchivos();

        // Comprobar que existe al menos un archivo
        if (matrizArchivos.length <= archivo)
        {
            System.out.println("No hay archivos para cifrar.");
            return;
        }

        // Obtener la imagen del primer archivo
        byte[] imagen = (byte[]) matrizArchivos[archivo][1];

        // Generar clave AES de 16 bytes
        byte[] keyAES = RandKeyGenerator.GenKey(16);

        // Generar IV de 12 bytes
        byte[] iv = RandKeyGenerator.GenKey(12);

        // Cifrar la imagen
        byte[] imagenCifrada = AES.Encode(imagen, keyAES, iv);

        //PARTE RSA (tengo que probabrlo ns si esta bien :D)
        java.security.KeyPair parClavesUsuario = RSA.GenerarParDeClaves();

        byte[] claveAesCifrada = RSA.Encode(keyAES, parClavesUsuario.getPublic());

        System.out.println("La clave AES se ha cifrado con RSA. Tamaño: " + claveAesCifrada.length + " bytes.");

        //Pruebas para depuracion
        testKey = keyAES;
        testIv = iv;
        testImgEnc = imagenCifrada;
    }

    public static boolean DescifrarValoresAES(int archivo)
    {
        // Cargar los archivos en matrizArchivos
        cargarArchivos();

        // Comprobar que existe al menos un archivo
        if (matrizArchivos.length <= archivo)
        {
            System.out.println("No hay archivos para cifrar.");
            return false;
        }

        // Obtener la imagen del primer archivo
        byte[] imagen = (byte[]) matrizArchivos[archivo][1];

        // Cifrar la imagen
        byte[] imagenDescifrada = AES.Decode(testImgEnc, testKey, testIv);

        return Arrays.equals(imagen, imagenDescifrada);
    }


    //Funciones definitivas de cifrado y descifrado--------------------------------------------------------------------

    //Cifra el archivo y lo guarda en la db
    public void CifrarArchivo(byte[] archivo, PublicKey publicKey) //Recibe el archivo a cifrar y la clave publica del usuario que lo cifra
    {
        String[] dataline = new String[3];
        //CifradoAES
        byte[] keyAES = RandKeyGenerator.GenKey(16); //Generar clave AES de 16 bytes
        byte[] iv = RandKeyGenerator.GenKey(12); //Generar IV de 12 bytes
        
        byte[] archivoCifrado = AES.Encode(archivo, keyAES, iv); //Cifrar la imagen

        //CifradoRSA
        byte[] keyAesCifrada = RSA.Encode(keyAES, publicKey);

        dataline[0] = Base64.getEncoder().encodeToString(archivoCifrado);
        dataline[1] = Base64.getEncoder().encodeToString(iv);
        dataline[2] = Base64.getEncoder().encodeToString(keyAesCifrada);
        //Dataline es la informacion que se guarda en la db
    }

    public byte[] DescifrarArcchivo(String[] dataline, PrivateKey privateKey)
    {
        //Recupera la informacion para descodificar
        byte[] archivoCifrado = Base64.getDecoder().decode(dataline[0]);
        byte[] iv = Base64.getDecoder().decode(dataline[1]);
        byte[] keyAESenc = Base64.getDecoder().decode(dataline[2]);

        //Descifrado RSA
        byte[] keyAES = RSA.Decode(keyAESenc, privateKey);

        //Descifrado AES
        byte[] archivo = AES.Decode(archivoCifrado, keyAES, iv);

        return archivo;
    }

    //Gestion de user
    public void Register(String userName, String password)
    {
        String dataline[] = new String[5];

        KeyPair parClavesNuevoUsuario = RSA.GenerarParDeClaves();
        PublicKey publicKey = parClavesNuevoUsuario.getPublic();
        PrivateKey privateKey = parClavesNuevoUsuario.getPrivate();

        byte[] salt = RandKeyGenerator.GenKey(32); //Crear salt
        byte[] hashedPassword = PasswordManager.CodePass(password, salt); //Codificar contrasenya
        byte[] iv = RandKeyGenerator.GenKey(12); //Generar IV de 12 bytes
        byte[] privateKeyEnc = AES.Encode(privateKey.getEncoded(), hashedPassword, iv); //Codificar la clave privada con la contrasenya
        
        dataline[0] = userName; //Nombre de Usuario
        dataline[1] = Base64.getEncoder().encodeToString(publicKey.getEncoded()); //Clave publica en claro
        dataline[2] = Base64.getEncoder().encodeToString(privateKeyEnc); //Clave privada cifrada
        dataline[3] = Base64.getEncoder().encodeToString(salt); //salt para cifrado en claro
        dataline[4] = Base64.getEncoder().encodeToString(iv); //iv para cifrado en claro
        //Dataline es la informacion que se guarda en la db
    }

    public void Login(String userName, String password)
    {
        /* //Reccuperar de byte[] a privatekey
        // 1. Envolvemos los bytes en una especificación compatible con el estándar PKCS#8
        PKCS8EncodedKeySpec especificación = new PKCS8EncodedKeySpec(bytesClavePrivada);
        
        // 2. Creamos una fábrica de claves configurada para el algoritmo RSA
        KeyFactory fabricaClaves = KeyFactory.getInstance("RSA");
        
        // 3. La fábrica lee los bytes estructurados y nos devuelve el objeto PrivateKey listo para usar
        return fabricaClaves.generatePrivate(especificación);
        */
    }

}