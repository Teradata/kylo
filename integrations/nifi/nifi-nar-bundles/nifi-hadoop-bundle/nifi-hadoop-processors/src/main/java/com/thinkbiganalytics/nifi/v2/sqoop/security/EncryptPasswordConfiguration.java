package com.thinkbiganalytics.nifi.v2.sqoop.security;

/**
 * Configuration for encrypting/decrypting password for use with sqoop
 * @author jagrut sharma
 */
class EncryptPasswordConfiguration {
    public static final String FILE_ENCRYPTION_ALGORITHM_FULL = "AES/ECB/PKCS5Padding";
    public static final String FILE_ENCRYPTION_ALGORITHM_ONLY = FILE_ENCRYPTION_ALGORITHM_FULL.split("/")[0];
    public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final int NUM_PBKDF2_ITERATIONS = 10000;
    public static final int KEY_LENGTH = 128;
    public static final String KEY_SALT = "SALT";

    /* no instantiation */
    private EncryptPasswordConfiguration() {

    }
}
