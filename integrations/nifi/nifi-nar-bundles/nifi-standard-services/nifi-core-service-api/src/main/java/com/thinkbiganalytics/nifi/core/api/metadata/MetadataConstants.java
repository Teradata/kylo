package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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
 * a lookup for the property names relating to metadata
 */
public interface MetadataConstants {

    String FEED_ID_PROP = "feed.id";
    String FEED_NAME_PROP = "feed.name";
    String SRC_DATASET_ID_PROP = "src.dataset.id";
    String DEST_DATASET_ID_PROP = "dest.dataset.id";
    String OPERATON_START_PROP = "operation.start.time";
    String OPERATON_STOP_PROP = "operation.stop.time";
    String LAST_LOAD_TIME_PROP = "last.load.time";

}
