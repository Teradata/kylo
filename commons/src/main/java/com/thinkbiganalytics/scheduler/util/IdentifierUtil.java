package com.thinkbiganalytics.scheduler.util;

import java.util.UUID;

/**
 * Created by sr186054 on 9/25/15.
 */
public class IdentifierUtil {
    public static String createUniqueName(String item) {

        String n1 = UUID.randomUUID().toString();
        String n2 = UUID.nameUUIDFromBytes(item.getBytes()).toString();
        return String.format("%s-%s", new Object[]{n2.substring(24), n1});
    }

    public static String createUniqueName(String item, int length) {

        String n1 = UUID.randomUUID().toString();
        String n2 = UUID.nameUUIDFromBytes(item.getBytes()).toString();
        return String.format("%s-%s", new Object[]{n2.substring(24), n1});
    }
}

