package com.thinkbiganalytics.nifi.rest.model.visitor;

/*-
 * #%L
 * thinkbig-nifi-flow-visitor-model
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

import org.apache.nifi.web.api.dto.ConnectionDTO;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiVisitableConnection implements NifiVisitable {

    private ConnectionDTO dto;
    private NifiVisitableProcessGroup group;

    public NifiVisitableConnection(NifiVisitableProcessGroup group, ConnectionDTO dto) {
        this.group = group;
        this.dto = dto;
    }

    //"INPUT_PORT","OUTPUT_PORT

    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {

        nifiVisitor.visitConnection(this);

    }

    public ConnectionDTO getDto() {
        return dto;
    }

    public NifiVisitableProcessGroup getGroup() {
        return group;
    }
}
