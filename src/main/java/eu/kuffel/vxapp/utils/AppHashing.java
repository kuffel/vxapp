package eu.kuffel.vxapp.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Useful methods for MD5,SHA and password hashing.
 * @author kuffel
 */
public class AppHashing {

    /**
     * Calculate a MD5 hash from the given input string.
     * @param data String with some data
     * @return 32 Bytes MD5 Hash
     */
    public static String getHashMD5( String data ){
        String md5 = null;
        try {
            if(data != null){
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.reset();
                m.update(data.getBytes());
                byte[] digest = m.digest();
                BigInteger bigInt = new BigInteger(1,digest);
                md5 = bigInt.toString(16);
                while(md5.length() < 32 ){
                    md5 = "0"+md5;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            return md5;
        }
    }

    /**
     * Calculate a SHA-512 hash from the given input string.
     * @param data String with some data
     * @return 128 Bytes SHA-512 Hash
     */
    public static String getHashSHA512(String data){
        return getHashSHA(data,"SHA-512");
    }

    /**
     * Calculate a SHA-256 hash from the given input string.
     * @param data String with some data
     * @return 64 Bytes SHA-256 Hash
     */
    public static String getHashSHA256(String data){
        return getHashSHA(data,"SHA-256");
    }

    /**
     * Calculate a SHA based hash from the given input string.
     * @param data String with some data
     * @param algorithm SHA-256, SHA-512
     * @return 64 Bytes SHA Hash
     */
    public static String getHashSHA( String data, String algorithm ){
        if(algorithm == null){
            algorithm = "SHA-256";
        }
        String encoding = "UTF-8";
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data.getBytes(encoding));
            byte[] digest = md.digest();
            if(algorithm.equalsIgnoreCase("SHA-512")){
                return String.format("%0128x", new java.math.BigInteger(1, digest));
            } else {
                return String.format("%064x", new java.math.BigInteger(1, digest));
            }

        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Hashing algorithm "+algorithm+" not available.");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding "+encoding);
        }
    }

    /**
     * Hashing function for secure password storage, source: https://www.owasp.org/index.php/Hashing_Java
     * @param password Password
     * @param salt Salt Should be at least 32 bytes long, must be saved with the password.
     * @param iterations Iterations Depends on Hardware, must be saved with the password.
     * @param keyLength Key Length, should be at least 256
     * @return Byte Array with an hashed password.
     */
    public static byte[] getHashPassword(final char[] password, final byte[] salt, final int iterations, final int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();
            return res;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }


}
