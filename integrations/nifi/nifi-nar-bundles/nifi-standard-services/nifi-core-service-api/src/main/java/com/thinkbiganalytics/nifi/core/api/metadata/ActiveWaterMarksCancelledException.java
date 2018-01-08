/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * kylo-nifi-core-service-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Thrown when a water mark commit was attempted on a cancelled water mark.
 */
public class ActiveWaterMarksCancelledException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String feedId;
    private final Set<String> waterMarkNames;
    
    public ActiveWaterMarksCancelledException(String feedId, String waterMarkName) {
        this(feedId, Collections.singleton(waterMarkName));
    }
    
    public ActiveWaterMarksCancelledException(String feedId, String... waterMarkNames) {
        this(feedId, Arrays.asList(waterMarkNames));
    }
    
    public ActiveWaterMarksCancelledException(String feedId, Collection<String> waterMarkNames) {
        super("One or more water marks were cancelled for feed: " + feedId);
        this.feedId = feedId;
        this.waterMarkNames = Collections.unmodifiableSet(new HashSet<>(waterMarkNames));
    }

    public String getFeedId() {
        return feedId;
    }

    public Set<String> getWaterMarkNames() {
        return this.waterMarkNames;
    }
    
}
