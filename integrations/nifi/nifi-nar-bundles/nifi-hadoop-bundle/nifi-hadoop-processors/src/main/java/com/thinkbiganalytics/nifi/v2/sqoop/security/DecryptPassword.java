package com.thinkbiganalytics.nifi.v2.sqoop.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A utility to decrypt passwords, provided in Base64 encoding.
 * @author jagrut sharma
 */
/* Uses Sqoop's decryption algorithm */

public class DecryptPassword {

    /**
     * Decrypt a password encrypted using Sqoop encryption utility
     * @param base64EncodedEncryptedPassword base64 string representing encrypted password
     * @param passPhrase passphrase used to encrypt password
     * @return decrypted password
     * @throws IOException if encounters issues with decryption
     */
    public static String decryptPassword (String base64EncodedEncryptedPassword, String passPhrase) throws IOException {

        if (base64EncodedEncryptedPassword == null || base64EncodedEncryptedPassword.isEmpty() || passPhrase == null || passPhrase.isEmpty()) {
            throw new IllegalArgumentException("Either encoded password or passphrase is provided as null/empty.");
        }

        byte[] encryptedPassword = Base64.getDecoder().decode(base64EncodedEncryptedPassword);
        String algorithmFull = EncryptPasswordConfiguration.FILE_ENCRYPTION_ALGORITHM_FULL;
        String algorithmOnly = EncryptPasswordConfiguration.FILE_ENCRYPTION_ALGORITHM_ONLY;
        String keySalt =  EncryptPasswordConfiguration.KEY_SALT;
        int numPbkdf2Iterations = EncryptPasswordConfiguration.NUM_PBKDF2_ITERATIONS;
        int keyLength = EncryptPasswordConfiguration.KEY_LENGTH;

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance(EncryptPasswordConfiguration.KEY_DERIVATION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Can't load SecretKeyFactory", e);
        }

        SecretKeySpec key;
        try {
            key = new SecretKeySpec(factory.generateSecret(new PBEKeySpec(passPhrase.toCharArray(),
                                                                          keySalt.getBytes(StandardCharsets.UTF_8),
                                                                          numPbkdf2Iterations,
                                                                          keyLength)).getEncoded(),
                                    algorithmOnly);
        } catch (Exception e) {
            throw new IOException("Can't generate secret key", e);
        }

        Cipher crypto;

        try {
            crypto = Cipher.getInstance(algorithmFull);
        } catch (Exception e) {
            throw new IOException("Can't initialize the decryptor", e);
        }

        byte[] decryptedBytes;

        try {
            crypto.init(Cipher.DECRYPT_MODE, key);
            decryptedBytes = crypto.doFinal(encryptedPassword);
        } catch (Exception e) {
            throw new IOException("Can't decrypt the password", e);
        }

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

