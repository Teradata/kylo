/**
 *
 */
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
 *
 */
public interface MetadataConstants {

    public static final String FEED_ID_PROP = "feed.id";
    public static final String FEED_NAME_PROP = "feed.name";
    public static final String SRC_DATASET_ID_PROP = "src.dataset.id";
    public static final String DEST_DATASET_ID_PROP = "dest.dataset.id";
    public static final String OPERATON_START_PROP = "operation.start.time";
    public static final String OPERATON_STOP_PROP = "operation.stop.time";
    public static final String LAST_LOAD_TIME_PROP = "last.load.time";


}
