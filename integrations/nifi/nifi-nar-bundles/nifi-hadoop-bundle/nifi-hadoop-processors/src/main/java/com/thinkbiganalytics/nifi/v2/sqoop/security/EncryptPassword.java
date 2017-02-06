package com.thinkbiganalytics.nifi.v2.sqoop.security;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A utility to generate an encrypted password for use with Sqoop. Uses sqoop recommended procedure.
 */

/*
    References:
    1) https://github.com/apache/sqoop/blob/trunk/src/java/org/apache/sqoop/util/password/CryptoFileLoader.java
    2) http://ingest.tips/2015/03/12/managing-passwords-sqoop/
 */

public class EncryptPassword {

    public static void main(String[] args) throws Exception {

        /* Basic check of command line arguments */
        if (args.length != 3) {
            showUsage();
            return;
        }

        /* Get values for command line arguments */
        String plainTextPassword = args[0];
        String passPhrase = args[1];
        String encryptedFileLocation = args[2];

        /* Get a secret key factory that is able to convert secret keys of the Key Derivation Algorithm */
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance(EncryptPasswordConfiguration.KEY_DERIVATION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Can't load SecretKeyFactory", e);
        }

        /*
            1. Get a PBE (password based encryption) Key Spec
            2. Generate a SecretKey using the PBE Key Spec from step 1
            3. Generate a SecretKeySpec using the bytes of SecretKey (step2) and desired encryption algorithm
        */
        SecretKeySpec key;
        try {
            key = new SecretKeySpec(
                factory.generateSecret(
                    new PBEKeySpec(passPhrase.toCharArray(),
                                   EncryptPasswordConfiguration.KEY_SALT.getBytes(StandardCharsets.UTF_8),
                                   EncryptPasswordConfiguration.NUM_PBKDF2_ITERATIONS,
                                   EncryptPasswordConfiguration.KEY_LENGTH)
                )
                    .getEncoded(), EncryptPasswordConfiguration.FILE_ENCRYPTION_ALGORITHM_ONLY);
        } catch (Exception e) {
            throw new IOException("Can't generate secret key", e);
        }


        /*  Get a cipher that implements the desired encryption algorithm */
        Cipher crypto;
        try {
            crypto = Cipher.getInstance(EncryptPasswordConfiguration.FILE_ENCRYPTION_ALGORITHM_FULL);
        } catch (Exception e) {
            throw new IOException("Can't initialize the cipher", e);
        }

        byte[] encryptedBytes;

        /*
            1. Initialize the cipher with the SecretKeySpec (for encrypting)
            2. Encrypt the plain text password using the cipher.
         */
        try {
            crypto.init(Cipher.ENCRYPT_MODE, key);
            encryptedBytes = crypto.doFinal(plainTextPassword.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IOException("Can't encrypt the password", e);
        }

        /* Write the encrypted password to output file */
        FileOutputStream output = new FileOutputStream(new File(encryptedFileLocation));
        output.write(encryptedBytes);
        output.close();

        /* Encoded Base64 string that can be used for configuration */
        String base64EncodedEncryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);

        /* Show summary and next step */
        System.out.println("The encrypted password location: " + encryptedFileLocation);
        System.out.println("The passphrase used (keep this in a safe place): " + passPhrase);
        System.out.println("The base64 encoded encrypted password: " + base64EncodedEncryptedPassword);
        System.out.println("\nFor Sqoop:\n"
                           + "1) Take the encrypted password file and put in HDFS. "
                           + "Refer to the HDFS location and passphrase during job runs.\n OR \n"
                           + "2) Enter the base64 encoded encrypted password in processor/controller-service configuration");
    }

    private static void showUsage() {
        System.out.println("Usage: 3 arguments needed:\n"
                           + "1. Plain Text Password (that needs to be encrypted)\n"
                           + "2. Passphrase (will be required for decryption)\n"
                           + "3. Location of the encrypted password file");
    }
}
