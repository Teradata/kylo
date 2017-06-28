package com.thinkbiganalytics.ui.rest.controller;

/*-
 * #%L
 * kylo-ui-controller
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

import com.thinkbiganalytics.ui.api.template.TemplateTableOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Path("v1/ui")
public class UiRestController {

    @Autowired(required = false)
    private List<TemplateTableOption> templateTableOptions;

    @GET
    @Path("template-table-options")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TemplateTableOption> getTemplateTableOptions() {
        return (templateTableOptions != null) ? templateTableOptions : Collections.emptyList();
    }
}
