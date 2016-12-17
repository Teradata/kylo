package com.thinkbiganalytics.nifi.rest.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by sr186054 on 8/18/16.
 */
public class NifiTemplateNameUtil {

     static String VERSION_NAME_REGEX = "(.*) - (\\d{13})";

    public static String getVersionedProcessGroupName(String name) {
        return name + " - " + new Date().getTime();
    }

    public static String parseVersionedProcessGroupName(String name) {
        if (isVersionedProcessGroup(name)) {
            return StringUtils.substringBefore(name, " - ");
        }
        return name;
    }

    public static boolean isVersionedProcessGroup(String name) {
        return StringUtils.isNotBlank(name) && name.matches(VERSION_NAME_REGEX);
    }
}
