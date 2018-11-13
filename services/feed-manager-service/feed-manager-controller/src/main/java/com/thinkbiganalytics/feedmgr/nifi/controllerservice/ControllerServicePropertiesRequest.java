package com.thinkbiganalytics.feedmgr.nifi.controllerservice;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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
import com.thinkbiganalytics.jdbc.util.DatabaseType;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;

import javax.annotation.Nonnull;

public class ControllerServicePropertiesRequest extends AbstractControllerServiceRequest {


    static ControllerServicePropertiesRequest newRequest(ControllerServiceDTO controllerServiceDTO){
        return new ControllerServicePropertiesRequestBuilder(controllerServiceDTO).build();
    }

    public static class ControllerServicePropertiesRequestBuilder extends AbstractControllerServiceRequest.AbstractControllerServiceRequestBuilder<ControllerServicePropertiesRequest.ControllerServicePropertiesRequestBuilder> {


        public ControllerServicePropertiesRequestBuilder(@Nonnull final ControllerServiceDTO controllerServiceDTO) {
            super(controllerServiceDTO);
        }

        @Nonnull
        public ControllerServicePropertiesRequest build() {
            final ControllerServicePropertiesRequest request = super.build(new ControllerServicePropertiesRequest());
            return request;
        }
    }
}
