/**
 *
 */
package com.thinkbiganalytics.nifi.v2.common;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import org.apache.nifi.processor.exception.ProcessException;

/**
 * Thrown when the lookup of the feed ID fails.
 */
public class FeedIdNotFoundException extends ProcessException {

    private static final long serialVersionUID = 1L;

    private final String category;
    private final String feedName;

    /**
     * @param message
     */
    public FeedIdNotFoundException(String message) {
        this(message, null, null);
    }

    /**
     * @param message
     */
    public FeedIdNotFoundException(String category, String name) {
        this("ID for feed " + category + "/" + name + " could not be located", category, name);
    }

    /**
     * @param message
     */
    public FeedIdNotFoundException(String message, String category, String name) {
        super(message);
        this.category = category;
        this.feedName = name;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return the feedName
     */
    public String getFeedName() {
        return feedName;
    }
}
