/**
 *
 */
package com.thinkbiganalytics.feedmgr.service.feed;

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

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 */
public class WaterMarkNotFoundExcepton extends MetadataException {

    private static final long serialVersionUID = 1L;

    private final Feed.ID id;
    private final String waterMarkName;

    public WaterMarkNotFoundExcepton(Feed.ID id, String waterMark) {
        super();
        this.id = id;
        this.waterMarkName = waterMark;
    }

    public WaterMarkNotFoundExcepton(String message, Feed.ID id, String waterMark) {
        super(message);
        this.id = id;
        this.waterMarkName = waterMark;
    }

    public Feed.ID getId() {
        return id;
    }
    
    public String getWaterMarkName() {
        return waterMarkName;
    }
}
