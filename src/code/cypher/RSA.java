package cypher;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSA {

    //genera y devuelve el par de claves RSA
    public static KeyPair GenerarParDeClaves() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar claves RSA", e);
        }
    }

    //recibe las claves aes y las cifra 
    public static byte[] CifrarClaveAES(byte[] claveAES, PublicKey clavePublica) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, clavePublica);
            return rsaCipher.doFinal(claveAES);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar la clave AES con RSA", e);
        }
    }
}