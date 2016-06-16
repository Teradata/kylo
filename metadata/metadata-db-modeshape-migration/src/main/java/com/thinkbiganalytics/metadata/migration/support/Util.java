package com.thinkbiganalytics.metadata.migration.support;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Created by sr186054 on 6/15/16.
 */
public class Util {

    /**
     * Converts a UUID returned from a mysql HEX(UUID) that does not have any dashes to a valid UUID with dashes
     */
    public static String uuidNoSpacesToString(String hexuuid) {
        try {
            BigInteger bi = new BigInteger(hexuuid, 16);
            UUID uuid = new UUID(bi.shiftRight(64).longValue(), bi.longValue());
            return uuid.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get Id from Binary Stream for " + hexuuid);
        }
    }
}
