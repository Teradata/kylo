package com.thinkbiganalytics.support;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by sr186054 on 8/11/16.
 */
public class FeedNameUtil {


    public static String category(String name) {
        return StringUtils.trim(StringUtils.substringBefore(name, "."));
    }

    public static String feed(String name) {
        return StringUtils.trim(StringUtils.substringAfterLast(name, "."));
    }

    public static String fullName(String category, String feed) {
        return StringUtils.trim(category + "." + feed);
    }


}
