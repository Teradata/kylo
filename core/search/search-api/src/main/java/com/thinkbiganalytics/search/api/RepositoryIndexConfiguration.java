package com.thinkbiganalytics.search.api;

/*-
 * #%L
 * kylo-search-api
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

import org.modeshape.jcr.RepositoryConfiguration;

import java.io.Serializable;

/**
 * ModeShape repository configuration for integrating with a search engine.
 */
public interface RepositoryIndexConfiguration extends Serializable {

    /**
     * Build the ModeShape repository configuration
     *
     * @return {@link RepositoryConfiguration}
     */
    RepositoryConfiguration build();
}
