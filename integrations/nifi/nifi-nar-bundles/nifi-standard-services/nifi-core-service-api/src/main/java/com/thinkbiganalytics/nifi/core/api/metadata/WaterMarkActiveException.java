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
 * Thrown when an attempt to load a high-water mark fails due to it being already active pending release.
 */
public class WaterMarkActiveException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * constructs an exception for the load water mark failure
     *
     * @param waterMarkName the name for the watermark
     */
    public WaterMarkActiveException(String waterMarkName) {
        super(waterMarkName);
    }
}
