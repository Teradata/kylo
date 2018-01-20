package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

/**
 * NiFi constants used by Kylo
 */
public class NifiFeedConstants {

    public static final String TRIGGER_FEED_PROCESSOR_CLASS = "com.thinkbiganalytics.nifi.v2.metadata.TriggerFeed";

    public static final String DEFAULT_TIGGER_FEED_PROCESSOR_SCHEDULE = "5 sec";

    public enum SCHEDULE_STRATEGIES {
        CRON_DRIVEN("Cron Expression"), TIMER_DRIVEN("Timer"), TRIGGER_DRIVEN("Trigger Event");
        private String displayName;

        SCHEDULE_STRATEGIES(String displayName) {
            this.displayName = displayName;
        }
    }

}
