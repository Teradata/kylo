package com.thinkbiganalytics.nifi.feedmgr;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.ProcessGroupDTO;

/**
 * A callback that can be wired in to perform an action prior to a Reusable template being marked as Running
 */
public interface ReusableTemplateCreationCallback {


    /**
     * do some work prior to marking the processors in the reusable template as running
     *
     * @param templateName    the name of the template
     * @param processGroupDTO the group where this template resides (under the reusable_templates) group
     */
    void beforeMarkAsRunning(String templateName, ProcessGroupDTO processGroupDTO);


}
