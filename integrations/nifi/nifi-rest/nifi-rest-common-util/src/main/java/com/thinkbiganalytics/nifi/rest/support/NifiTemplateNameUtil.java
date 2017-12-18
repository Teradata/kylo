package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-common-util
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

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Utility class to support common naming of versioned process groups
 */
public class NifiTemplateNameUtil {

    /**
     * the regex pattern to match when looking for versioned process groups
     **/
    static String VERSION_NAME_REGEX = "(.*) - (\\d{13})";

    static String KYLO_VERSION_ID_PREFIX = "kyloId:";

    /**
     * Return the new name of the versioned process group
     *
     * @param name the process group name with out the version timestamp
     * @return the new name that has the new version timestamp
     */
    public static String getVersionedProcessGroupName(String name) {
        return getVersionedProcessGroupName(name,null);
    }

    /**
     * Return the new name of the versioned process group
     *
     * @param name the process group name with out the version timestamp
     * @return the new name that has the new version timestamp
     */
    public static String getVersionedProcessGroupName(String name, String versionIdentifier) {
        return name + " "+ KYLO_VERSION_ID_PREFIX+versionIdentifier+" - "+new Date().getTime();
    }

    /**
     * Return true if the name has the kyloVersionId in it
     * @param kyloVersionId
     * @param name
     * @return
     */
    public static boolean belongsToVersion(String name,String kyloVersionId){
        String versionId = getKyloVersionIdentifier(name);
        return versionId != null && versionId.equalsIgnoreCase(kyloVersionId);
    }

    @Nullable
    public static String getKyloVersionIdentifier(String name){
        if (isVersionedProcessGroup(name) && StringUtils.contains(name,KYLO_VERSION_ID_PREFIX)){
                String versionId = StringUtils.substringBetween(name,KYLO_VERSION_ID_PREFIX,"-");
              return StringUtils.trim(versionId);
        }
        else {
            return null;
        }
    }

    /**
     * Return the process group name, removing the versioned name and timestamp if one exists
     *
     * @param name a process group name
     * @return the process group name, removing the versioned timestamp if one exists
     */
    public static String parseVersionedProcessGroupName(String name) {
        if (isVersionedProcessGroup(name)) {
            String processGroupName = StringUtils.substringBefore(name, " - ");
            if(StringUtils.contains(processGroupName,KYLO_VERSION_ID_PREFIX)){
                processGroupName = StringUtils.trim(StringUtils.substringBefore(processGroupName,KYLO_VERSION_ID_PREFIX));
            }
            return processGroupName;
        }
        return name;
    }

    /**
     * Check to see if the incoming name includes a versioned timestamp
     *
     * @param name the process group name
     * @return {@code true} if the incoming name contains the version timestamp, {@code false} if the name is not versioned.
     */
    public static boolean isVersionedProcessGroup(String name) {
        return StringUtils.isNotBlank(name) && name.matches(VERSION_NAME_REGEX);
    }
}
