package com.thinkbiganalytics.feedmgr.service;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.encryption.EncryptionController;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.http.MediaType;

public class EncryptionService {

    @Autowired
    private TextEncryptorLocator encryptor;

    private String encryptedPrefix = "{cipher}";

    public boolean isEncrypted(String str) {
        return StringUtils.startsWith(str, encryptedPrefix);
    }

    public String encrypt(String str) {
        String encrypted;
        if (str != null && !isEncrypted(str)) {
            encrypted = encrypt(str, MediaType.TEXT_PLAIN);
            if (!StringUtils.startsWith(encrypted, encryptedPrefix)) {
                encrypted = encryptedPrefix + encrypted;
            }
        } else {
            encrypted = str;
        }
        return encrypted;
    }

    private String encrypt(String unencrypted, MediaType mediaType) {
        EncryptionController ec = new EncryptionController(encryptor);
        return ec.encrypt(unencrypted, mediaType);
    }

    public String decrypt(String str) {
        String decrypted = null;
        if (str != null) {
            if (!StringUtils.startsWith(str, encryptedPrefix)) {
                str = encryptedPrefix + str;
            }
            decrypted = decrypt(str, MediaType.TEXT_PLAIN);
        }
        return decrypted;
    }

    private String decrypt(String encrypted, MediaType mediaType) {
        EncryptionController ec = new EncryptionController(encryptor);
        return ec.decrypt(encrypted, mediaType);
    }
}
