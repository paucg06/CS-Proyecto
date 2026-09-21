package cypher; //Declarar paquete

//Librerias
import javax.crypto.Cipher;
import java.util.Base64;
import java.security.KeyPair; //para la creación de las claves públicas y privadas 
import java.security.KeyPairGenerator; //para crear pares de claves privadas y publicas 
import java.security.PrivateKey; 
import java.security.PublicKey; 
import java.security.SecureRandom; 
import java.util.Base64; 





public class RSA
{
    public static void main(String[] args){

        try{
            //generamos las claves públicas y privadas 
            //se crea el generador de claves para el algoritmo RSA
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); 
            keyGen.initialize(2048); //inicializamos el tamaño de la clave en 2048 bits 

            KeyPair parDeClaves = keyGen.generateKeyPair(); 
            PublicKey clavePublica = parDeClaves.getPublic(); 
            PrivateKey clavePrivada = parDeClaves.getPrivate(); 

            System.out.println("Prueba de la generación d elas claves"); 
            System.out.println("Formato de la clave pública:"+ clavePublica.getFormat());

            String fragmentoClave = Base64.getEncoder().encodeToString(clavePublica.getEncoded()); 
            System.out.println("Fragmento de la clave pública: " + fragmentoClave.substring(0, 30) + "...");

            String fragmentoClavePriv = Base64.getEncoder().encodeToString(clavePrivada.getEncoded()); 
            System.out.println("Fragmento de la clave privada: " + fragmentoClavePriv.substring(0, 30) + "...");

        }catch(Exception e){
            System.err.println("No se ha podido generar la clave"); 
        }

    }
}