package com.thinkbiganalytics.policy.validation;

/**
 * Created by sr186054 on 7/23/16.
 */
public interface PolicyPropertyTypes {


    enum PROPERTY_TYPE {
        number, string, select, regex, date, chips, feedChips, currentFeed, currentFeedCronSchedule, feedSelect, email, cron
    }
}
