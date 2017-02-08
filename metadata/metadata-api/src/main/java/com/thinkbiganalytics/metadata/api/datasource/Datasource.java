/**
 *
 */
package com.thinkbiganalytics.metadata.api.datasource;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

/**
 *
 */
public interface Datasource extends Serializable {

    ID getId();

    ;

    String getName();

    String getDescription();

    DateTime getCreatedTime();

    Set<? extends FeedSource> getFeedSources();

    Set<? extends FeedDestination> getFeedDestinations();

    interface ID extends Serializable {

    }


}
