package com.thinkbiganalytics.feedmgr.support;

import org.apache.commons.lang.StringUtils;

/**
 * Created by sr186054 on 8/11/16.
 */
public class FeedNameUtil {


    public static String category(String name) {
        return StringUtils.substringBefore(name, ".");
    }

    public static String feed(String name) {
        return StringUtils.substringAfterLast(name, ".");
    }

    public static String fullName(String category, String feed) {
        return category + "." + feed;
    }


}
