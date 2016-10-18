package com.thinkbiganalytics.util;

/**
 * Created by sr186054 on 10/14/16.
 */
public class HiveColumnUtil {

    /**
     * Surround the field with ` marks if it contians a space.
     */
    public static String surroundWithTick(String field) {
        if (!field.contains("`") && field.contains(" ")) {
            return "`" + field + "`";
        }
        return field;
    }

}
