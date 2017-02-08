package com.thinkbiganalytics.feedmgr.rest.support;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.google.common.base.CaseFormat;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Convert a Name into a System name replacing control chars, lowercase, spaces to underscore, CamelCase to underscore
 * Examples:
 * MYFeedName  = my_feed_name
 * myFeedName = my_feed_name
 * MY_TESTFeedName = my_test_feed_name
 */
public class SystemNamingService {

    public static String generateSystemName(String name) {
        //first trim it
        String systemName = StringUtils.trimToEmpty(name);
        if (StringUtils.isBlank(systemName)) {
            return systemName;
        }
        systemName = systemName.replaceAll(" +", "_");
        systemName = systemName.replaceAll("[^\\w_]", "");

        int i = 0;

        StringBuilder s = new StringBuilder();
        CharacterIterator itr = new StringCharacterIterator(systemName);
        for (char c = itr.first(); c != CharacterIterator.DONE; c = itr.next()) {
            if (Character.isUpperCase(c)) {
                //if it is upper, not at the start and not at the end check to see if i am surrounded by upper then lower it.
                if (i > 0 && i != systemName.length() - 1) {
                    char prevChar = systemName.charAt(i - 1);
                    char nextChar = systemName.charAt(i + 1);

                    if (Character.isUpperCase(prevChar) && (Character.isUpperCase(nextChar) || CharUtils.isAsciiNumeric(nextChar) || '_' == nextChar || '-' == nextChar)) {
                        char lowerChar = Character.toLowerCase(systemName.charAt(i));
                        s.append(lowerChar);
                    } else {
                        s.append(c);
                    }
                } else if (i > 0 && i == systemName.length() - 1) {
                    char prevChar = systemName.charAt(i - 1);
                    if (Character.isUpperCase(prevChar) && !CharUtils.isAsciiNumeric(systemName.charAt(i))) {
                        char lowerChar = Character.toLowerCase(systemName.charAt(i));
                        s.append(lowerChar);
                    } else {
                        s.append(c);
                    }
                } else {
                    s.append(c);
                }
            } else {
                s.append(c);
            }

            i++;
        }

        systemName = s.toString();
        systemName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, systemName);
        systemName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, systemName);
        systemName = StringUtils.replace(systemName, "__", "_");
        // Truncate length if exceeds Hive limit
        systemName = (systemName.length() > 128 ? systemName.substring(0, 127) : systemName);
        return systemName;
    }

}
