/**
 *
 */
package com.thinkbiganalytics.metadata.api.event.sla;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.Objects;

/**
 *
 */
public class ServiceLevelAgreementChange extends MetadataChange {

    private static final long serialVersionUID = 1L;

    private final ServiceLevelAgreement.ID id;
    private final String name;

    public ServiceLevelAgreementChange(ChangeType change, ServiceLevelAgreement.ID id, String name) {
        this(change, "", id, name);
    }

    public ServiceLevelAgreementChange(ChangeType change, String descr, ServiceLevelAgreement.ID id, String name) {
        super(change, descr);
        this.id = id;
        this.name = name;
    }

    public ServiceLevelAgreement.ID getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.id, this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceLevelAgreementChange) {
            ServiceLevelAgreementChange that = (ServiceLevelAgreementChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.id, that.id) &&
                   Objects.equals(this.name, that.name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SLA change ");
        return sb
            .append("(").append(getChange()).append(") - ")
            .append("ID: ").append(this.id)
            .append(" name: ").append(this.name)
            .toString();
    }

}
