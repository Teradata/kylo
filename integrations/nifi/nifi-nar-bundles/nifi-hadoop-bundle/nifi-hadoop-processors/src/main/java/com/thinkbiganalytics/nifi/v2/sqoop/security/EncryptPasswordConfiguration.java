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

/**
 * Configuration for encrypting/decrypting password for use with sqoop
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
