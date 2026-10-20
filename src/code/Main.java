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
import java.security.spec.X509EncodedKeySpec;

public class Main
{
    public static String DIREC_ARCHIVOS = "src/img"; //seleccionar src y img (¡¡Debe ser modificado si se modifica la estructura de carpetas o se mueve el .bat!!)
    public static String DIREC_DESCIFRADOS = "descifrados"; //Carpeta (junto al .bat) donde se guardan los archivos recuperados
    public static String DIREC_CIFRADOS = "cifrados"; //Carpeta (junto al .bat) donde se guardan los archivos cifrados (.enc)
    public static final String USUARIO_UNICO = "u"; //Fase I: un unico usuario
    public static Object[][] matrizArchivos = new Object[0][2];;

    private static PrivateKey clavePrivadaActiva;
    private static PublicKey clavePublicaActiva;
    private static String usuarioActivo;


    //Variables temporales de depuracion
    //-----------------------------------
    static byte[] testKey;
    static byte[] testIv;
    public static byte[] testImgEnc;

    private static String[] user = new String[5];
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

    //Estas dos son para usar las variables de clase (La interfaz llama a estas)
    //Devuelve false si ya existe un archivo con ese nombre (o el nombre no es valido)
    public static boolean GuardarMiArchivo(byte[] archivo, String nombre)
    {
        return CifrarArchivo(archivo, clavePublicaActiva, nombre);
    }

    public static byte[] RecuperarMiArchivo(String nombre)
    {
        return DescifrarArcchivo(nombre, clavePrivadaActiva);
    }

    //Devuelve los nombres de los archivos cifrados del usuario activo
    public static String[] ListarMisArchivos()
    {
        return DbManager.ObtenerArchivos(usuarioActivo);
    }

    //Descifra un archivo del usuario activo y lo guarda en DIREC_DESCIFRADOS. Devuelve la ruta donde queda guardado
    public static Path GuardarArchivoDescifrado(String nombre) throws IOException
    {
        byte[] contenido = RecuperarMiArchivo(nombre);

        Path carpeta = Paths.get(DIREC_DESCIFRADOS);
        Files.createDirectories(carpeta);

        //getFileName evita que un nombre con "../" escriba fuera de la carpeta
        Path destino = carpeta.resolve(Paths.get(nombre).getFileName());
        Files.write(destino, contenido);
        return destino;
    }

    public static boolean HaySesion()
    {
        return usuarioActivo != null && clavePrivadaActiva != null;
    }

    public static String UsuarioActivo()
    {
        return usuarioActivo;
    }

    //A futuro se implementaran funciones que usen otra clave que no es la propia

    //Cifra el archivo y lo guarda en la db
    public static boolean CifrarArchivo(byte[] archivo, PublicKey publicKey, String nombreArchivo) //Recibe el archivo a cifrar y la clave publica del usuario que lo cifra
    {
        if (publicKey == null || usuarioActivo == null)
        {
            throw new IllegalStateException("No hay ninguna sesion iniciada.");
        }

        String[] dataline = new String[5];
        //CifradoAES
        byte[] keyAES = RandKeyGenerator.GenKey(16); //Generar clave AES de 16 bytes
        byte[] iv = RandKeyGenerator.GenKey(12); //Generar IV de 12 bytes

        byte[] archivoCifrado = AES.Encode(archivo, keyAES, iv); //Cifrar la imagen

        //CifradoRSA
        byte[] keyAesCifrada = RSA.Encode(keyAES, publicKey);

        dataline[0] = nombreArchivo;
        dataline[1] = Base64.getEncoder().encodeToString(archivoCifrado);
        dataline[2] = Base64.getEncoder().encodeToString(iv);
        dataline[3] = Base64.getEncoder().encodeToString(keyAesCifrada);
        dataline[4] = usuarioActivo;
        //Dataline es la informacion que se guarda en la db
        if (!DbManager.GuardarArchivo(dataline))
        {
            return false;
        }

        //Ademas, se guarda el archivo cifrado en la carpeta de cifrados (mismo criterio que los descifrados)
        try
        {
            Path carpeta = Paths.get(DIREC_CIFRADOS);
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(Paths.get(nombreArchivo).getFileName() + ".enc"), archivoCifrado);
        } catch (IOException e)
        {
            System.out.println("No se pudo guardar el archivo cifrado en disco: " + e.getMessage());
        }
        return true;
    }

    public static byte[] DescifrarArcchivo(String nombreArchivo, PrivateKey privateKey)
    {
        String[] dataline = DbManager.ObtenerArchivo(nombreArchivo);
        if (dataline == null || !dataline[4].equals(usuarioActivo))
        {
            throw new IllegalArgumentException("El archivo '" + nombreArchivo + "' no existe o no pertenece al usuario.");
        }
        //Recupera la informacion para descodificar
        byte[] archivoCifrado = Base64.getDecoder().decode(dataline[1]);
        byte[] iv = Base64.getDecoder().decode(dataline[2]);
        byte[] keyAESenc = Base64.getDecoder().decode(dataline[3]);

        //Descifrado RSA
        byte[] keyAES = RSA.Decode(keyAESenc, privateKey);

        //Descifrado AES
        byte[] archivo = AES.Decode(archivoCifrado, keyAES, iv);

        return archivo;
    }

    //Gestion de user

    //Fase I (un solo usuario): true si el usuario unico ya esta registrado
    public static boolean ExisteUsuarioUnico()
    {
        return DbManager.ObtenerUsuario(USUARIO_UNICO) != null;
    }

    //Fase I: si el usuario unico no existe lo crea con esta contrasenya (primer uso); despues inicia sesion.
    //Devuelve false si la contrasenya es incorrecta.
    public static boolean AccederUsuarioUnico(String password)
    {
        if (!ExisteUsuarioUnico() && !Register(USUARIO_UNICO, password))
        {
            return false;
        }
        return Login(USUARIO_UNICO, password);
    }

    //AES128: la clave de proteccion de la clave privada son los 16 primeros bytes del hash SHA3-256 de la contrasenya
    private static byte[] ClaveAes128(byte[] hashedPassword)
    {
        return Arrays.copyOf(hashedPassword, 16);
    }

    //Devuelve false si el usuario ya existe o el nombre no es valido
    public static boolean Register(String userName, String password)
    {
        if (userName == null || userName.isBlank() || userName.contains(";") || password == null || password.isEmpty())
        {
            return false;
        }

        String dataline[] = new String[5];

        KeyPair parClavesNuevoUsuario = RSA.GenerarParDeClaves();
        PublicKey publicKey = parClavesNuevoUsuario.getPublic();
        PrivateKey privateKey = parClavesNuevoUsuario.getPrivate();

        byte[] salt = RandKeyGenerator.GenKey(32); //Crear salt
        byte[] hashedPassword = PasswordManager.CodePass(password, salt); //Codificar contrasenya
        byte[] iv = RandKeyGenerator.GenKey(12); //Generar IV de 12 bytes
        byte[] privateKeyEnc = AES.Encode(privateKey.getEncoded(), ClaveAes128(hashedPassword), iv); //Codificar la clave privada con la contrasenya

        dataline[0] = userName; //Nombre de Usuario
        dataline[1] = Base64.getEncoder().encodeToString(publicKey.getEncoded()); //Clave publica en claro
        dataline[2] = Base64.getEncoder().encodeToString(privateKeyEnc); //Clave privada cifrada
        dataline[3] = Base64.getEncoder().encodeToString(salt); //salt para cifrado en claro
        dataline[4] = Base64.getEncoder().encodeToString(iv); //iv para cifrado en claro
        //Dataline es la informacion que se guarda en la db

        return DbManager.GuardarUsuario(dataline);
    }

    //Devuelve true si el usuario existe y la contrasenya es correcta (deja la sesion iniciada)
    public static boolean Login(String userName, String password)
    {
        String dataline[] = DbManager.ObtenerUsuario(userName);//Esta variable se obtendra directamente de la base de datos utilizando userName.
        if (dataline == null)
        {
            return false; //El usuario no existe
        }

        try
        {
            //Recuperar datos de la db
            byte[] publicKey = Base64.getDecoder().decode(dataline[1]); //Clave publica por recuperar
            byte[] privateKeyEnc = Base64.getDecoder().decode(dataline[2]); //Clave privada que descifrar
            byte[] salt = Base64.getDecoder().decode(dataline[3]); //Salt para hashear la contrasenya
            byte[] iv = Base64.getDecoder().decode(dataline[4]); //IV para decofificar la clave privada

            //Codificar contrasenya
            byte[] hashedPassword = PasswordManager.CodePass(password, salt);

            //Descifrar la clave privada (con GCM, una contrasenya incorrecta lanza excepcion)
            byte[] privateKey = AES.Decode(privateKeyEnc, ClaveAes128(hashedPassword), iv);

            //Recuperar par de claves
            KeyFactory fabricaClaves = KeyFactory.getInstance("RSA"); //Obtener el creador de claves

            //Reccuperar de byte[] a privatekey
            PKCS8EncodedKeySpec especPriv = new PKCS8EncodedKeySpec(privateKey); //Formatear la clave
            PrivateKey nuevaPrivada = fabricaClaves.generatePrivate(especPriv);

            //Reccuperar de byte[] a publickey (la clave publica se guarda en formato X.509)
            X509EncodedKeySpec especPubl = new X509EncodedKeySpec(publicKey); //Formatear la clave
            PublicKey nuevaPublica = fabricaClaves.generatePublic(especPubl);

            //Solo si todo ha ido bien se guarda la sesion en las variables de clase
            clavePrivadaActiva = nuevaPrivada;
            clavePublicaActiva = nuevaPublica;
            usuarioActivo = userName;
            return true;
        } catch (Exception e)
        {
            return false; //Contrasenya incorrecta o datos corruptos
        }
    }
}
