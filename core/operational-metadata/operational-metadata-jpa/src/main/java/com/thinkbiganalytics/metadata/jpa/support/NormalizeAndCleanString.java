package com.thinkbiganalytics.metadata.jpa.support;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.Normalizer;

/**
 * MySQL doesnt like unicode strings, and other chars.
 * This will normalize the string and strip the bad chars
 */
public class NormalizeAndCleanString {
    private static final Logger log = LoggerFactory.getLogger(NormalizeAndCleanString.class);


    public static String normalizeAndClean(String str){
        try {
            if(StringUtils.isNotBlank(str)) {
                String testStr = Normalizer.normalize(str, Normalizer.Form.NFD);
                String clean = testStr.replaceAll("\\P{InBasic_Latin}", "");
                return clean;
            }
        }catch (Exception e){
            log.error("Unable to replace non UTF-8 characters for {}. {} ",str,e.getMessage(),e);
        }
        return null;
    }





}
