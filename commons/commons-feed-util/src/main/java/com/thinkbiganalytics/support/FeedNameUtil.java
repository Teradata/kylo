package com.thinkbiganalytics.support;


import org.apache.commons.lang3.StringUtils;

/**
 * Utility to parse and return feed names from a category and feed string to ensure everything returns the same name.
 */
public class FeedNameUtil {


    /**
     * Parse the category name from a full feed name  (category.feed)
     */
    public static String category(String name) {
        return StringUtils.trim(StringUtils.substringBefore(name, "."));
    }

    /**
     * parse the feed name from a full feed name (category.feed)
     * @param name
     * @return
     */
    public static String feed(String name) {
        return StringUtils.trim(StringUtils.substringAfterLast(name, "."));
    }

    /**
     * return the full feed name from a category and feed.
     * @param category
     * @param feed
     * @return returns the (category.feed) name
     */
    public static String fullName(String category, String feed) {
        return StringUtils.trim(category + "." + feed);
    }


}
