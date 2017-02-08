/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedPrecondition implements Serializable {

    private ServiceLevelAgreement sla;

    public FeedPrecondition() {
        this("Feed preconditon");
    }

    public FeedPrecondition(String name) {
        super();
        this.sla = new ServiceLevelAgreement("Feed " + name + " precondition");
    }

    public FeedPrecondition(String name, String description, List<Metric> metrics) {
        this(name);
        this.addMetrics(description, metrics);
    }

    public void addMetrics(String description, Metric... metrics) {
        addMetrics(description, Arrays.asList(metrics));
    }

    public void addMetrics(String description, List<Metric> metrics) {
        Obligation ob = new Obligation(description, metrics);
        this.sla.addObligation(ob);
    }

    public ServiceLevelAgreement getSla() {
        return sla;
    }

    public void setSla(ServiceLevelAgreement sla) {
        this.sla = sla;
    }
}
