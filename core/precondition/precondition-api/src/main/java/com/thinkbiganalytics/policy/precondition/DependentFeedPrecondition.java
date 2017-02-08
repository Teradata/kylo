package com.thinkbiganalytics.policy.precondition;

/*-
 * #%L
 * thinkbig-precondition-api
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

import java.util.List;

/**
 * Base class used for the precondition where a Feed depends upon the successful completion of another feed
 *
 * Refer to the precondition-default module for implementations of this interface
 */
public interface DependentFeedPrecondition {

    /**
     * @return a list of the <systemCategory.systemFeedName> of the feeds that a feed depends upon
     */
    List<String> getDependentFeedNames();

}
