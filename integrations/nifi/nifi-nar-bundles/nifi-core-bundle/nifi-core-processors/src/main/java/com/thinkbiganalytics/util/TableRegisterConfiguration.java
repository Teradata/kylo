package com.thinkbiganalytics.util;

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

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TableRegisterConfiguration {

    /*
    Note: These defaults exist for legacy support and are typically overridden in the service-app modules application.properties
     */
    private final String DEFAULT_FEED = "/model.db/";
    private final String DEFAULT_MASTER = "/app/warehouse/";
    private final String DEFAULT_PROFILE = "/model.db/";

    private String feedRoot;
    private String profileRoot;
    private String masterRoot;

    public TableRegisterConfiguration() {
        this(null, null, null);
    }

    public TableRegisterConfiguration(String feedRoot, String profileRoot, String masterRoot) {
        super();
        this.feedRoot = StringUtils.defaultIfEmpty(feedRoot, DEFAULT_FEED);
        this.profileRoot = StringUtils.defaultIfEmpty(profileRoot, DEFAULT_PROFILE);
        this.masterRoot = StringUtils.defaultIfEmpty(masterRoot, DEFAULT_MASTER);
    }

    public Path pathForTableType(TableType type) {
        switch (type) {
            case FEED:
            case INVALID:
            case VALID:
                return Paths.get(feedRoot);
            case PROFILE:
                return Paths.get(profileRoot);
            case MASTER:
                return Paths.get(masterRoot);
            default:
                throw new RuntimeException("Unsupported table type [" + type.toString() + "]. Needs to be added to class?");
        }
    }
}
