package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;


/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorErrors;

import java.io.Serializable;
import java.util.Set;

/**
 * Cluster Message that is sent to other cluser members indicating any errors captured when receiving jms feed processor stats
 */
public class NifiFeedProcessorStatsErrorClusterMessage implements Serializable {
    
    private static final long serialVersionUID = -753740878011117863L;

    private Set<? extends NifiFeedProcessorErrors> errors;

    public NifiFeedProcessorStatsErrorClusterMessage(Set<? extends NifiFeedProcessorErrors> errors) {
        this.errors = errors;
    }

    public Set<? extends NifiFeedProcessorErrors> getErrors() {
        return errors;
    }

}
