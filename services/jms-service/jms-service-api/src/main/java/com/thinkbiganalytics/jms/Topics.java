package com.thinkbiganalytics.jms;
/*-
 * #%L
 * kylo-jms-service-api
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
public interface Topics {

    /**
     * Topic used to notify Nifi of the Streaming feeds and their input processor ids.
     * this is used to determine what provenance events should be sent to the batch provenance queue, vs just the stats streaming queue.
     * This is a topic because if NiFi is clustered all cluster nodes need to get notified of the processorIds associated with streaming feeds
     */
     String NIFI_STREAMING_FEEDS_TOPIC = "kylo.nifi-streaming-feeds";
}
