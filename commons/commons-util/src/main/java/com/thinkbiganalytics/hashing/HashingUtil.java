package com.thinkbiganalytics.hashing;

/*-
 * #%L
 * kylo-commons-util
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashing utility class
 */
public class HashingUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HashingUtil.class);
    private static final String sourceEncoding = "UTF-8";
    private static final String md5Algorithm = "MD5";

    public static String getHashMD5(String source) {
        if (source == null) {
            LOG.debug("Hashing: null value received for hashing");
            return null;
        }
        LOG.debug("Hash MD5: source {}", source);

        byte[] bytesOfSource;
        MessageDigest messageDigest;
        byte[] hash;

        try {
            bytesOfSource = source.getBytes(sourceEncoding);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Source encoding not supported: {}", sourceEncoding);
            return null;
        }

        try {
            messageDigest = MessageDigest.getInstance(md5Algorithm);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Algorithm not supported: {}", md5Algorithm);
            return null;
        }

        hash = messageDigest.digest(bytesOfSource);

        final StringBuilder stringBuilder = new StringBuilder(hash.length * 2);
        for (byte hashByte : hash) {
            stringBuilder.append(Integer.toHexString((hashByte & 0xFF) | 0x100).substring(1, 3));
        }

        LOG.debug("Hash MD5: hash {}", stringBuilder.toString());
        return stringBuilder.toString();
    }
}
