/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

/*-
 * #%L
 * thinkbig-sla-rest-model
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObligationGroup {

    private String condition = "REQUIRED";
    private List<Obligation> obligations = new ArrayList<>();

    public ObligationGroup() {
        super();
    }

    public ObligationGroup(String condition) {
        this();
        this.condition = condition;
    }

    public ObligationGroup(Obligation... obligations) {
        this("REQUIRED", Arrays.asList(obligations));
    }

    public ObligationGroup(String condition, Obligation... obligations) {
        this(condition, Arrays.asList(obligations));
    }

    public ObligationGroup(String condition, List<Obligation> obligations) {
        this(condition);
        this.obligations.addAll(obligations);
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Obligation> getObligations() {
        return obligations;
    }

    public void setObligations(List<Obligation> obligations) {
        this.obligations = obligations;
    }

    public void addObligation(Obligation ob) {
        this.obligations.add(ob);
    }

}
