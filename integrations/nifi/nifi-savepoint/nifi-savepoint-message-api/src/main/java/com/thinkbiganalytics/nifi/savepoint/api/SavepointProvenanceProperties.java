package com.thinkbiganalytics.nifi.savepoint.api;
/*-
 * #%L
 * kylo-nifi-savepoint-message-api
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
public class SavepointProvenanceProperties {

    public static final String CLONE_FLOWFILE_ID = "savepoint.clone.flowfile.id";
    public static final String PARENT_FLOWFILE_ID = "savepoint.parent.flowfile.id";

    /**
     * Status flag that is added to provenance to track history in kylo
     */
    public static final String SAVE_POINT_BEHAVIOR_STATUS = "savepoint.behavior.status";

    /**
     * The parent flow file that is to be used to trigger this last savepoint
     */
    public static final String SAVE_POINT_TRIGGER_FLOWFILE= "savepoint.trigger.flowfile";


    public static final String RELEASE_STATUS_KEY = "savepoint.release";

    public enum RELEASE_STATUS {
        SUCCESS, FAILURE
    }

    public enum TRIGGER_SAVE_POINT_STATE {
        RETRY,RELEASE,FAIL
    }

}
