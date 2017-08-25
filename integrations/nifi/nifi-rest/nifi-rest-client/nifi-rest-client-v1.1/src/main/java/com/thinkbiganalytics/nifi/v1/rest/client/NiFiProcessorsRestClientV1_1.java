package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1.1
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

import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;

import org.apache.nifi.web.api.dto.ProcessorDTO;

import javax.annotation.Nonnull;

/**
 * Created by sr186054 on 8/25/17.
 */
public class NiFiProcessorsRestClientV1_1  extends NiFiProcessorsRestClientV1{

    public NiFiProcessorsRestClientV1_1(@Nonnull NiFiRestClientV1 client) {
        super(client);
    }

    @Override
    protected ProcessorDTO applySchedule(NifiProcessorSchedule schedule) {
        ProcessorDTO input = super.applySchedule(schedule);
        if(input != null) {
            input.getConfig().setExecutionNode(schedule.getExecutionNode());
        }
        return input;
    }
}
