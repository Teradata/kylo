package com.thinkbiganalytics.jms;

/*-
 * #%L
 * kylo-jms-service-amazon-sqs
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
 * Created by ru186002 on 21/07/2017.
 */
public interface Queues {

    String FEED_MANAGER_QUEUE = "thinkbig.feed-manager";
    String PROVENANCE_EVENT_STATS_QUEUE = "thinkbig.provenance-event-stats";
}
