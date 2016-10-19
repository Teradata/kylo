package com.thinkbiganalytics.nifi.v2.sqoop.security;

/**
 * @author jagrut sharma
 */
/*
Configuration for encrypting sqoop password
 */
public class EncryptPasswordConfiguration {

    public static final String FILE_ENCRYPTION_ALGORITHM_FULL = "AES/ECB/PKCS5Padding";
    public static String FILE_ENCRYPTION_ALGORITHM_ONLY = FILE_ENCRYPTION_ALGORITHM_FULL.split("/")[0];
    public static String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static int NUM_PBKDF2_ITERATIONS = 10000;
    public static int KEY_LENGTH = 128;
    public static String KEY_SALT = "SALT";


    /* no instantiation */
    private EncryptPasswordConfiguration() {

    }

}
