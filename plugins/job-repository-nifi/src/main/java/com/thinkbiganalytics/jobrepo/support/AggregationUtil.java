package com.thinkbiganalytics.jobrepo.support;


/**
 * Created by sr186054 on 8/24/16.
 */
public class AggregationUtil {

    public static Double avg(Long long1, Long totalCount) {
        Long value = 0L;
        if (long1 != null) {
            value = long1;
        }
        if (totalCount == 0) {
            return 0d;
        } else {
            return value.doubleValue() / totalCount.doubleValue();
        }
    }


}
