/**
 *
 */
package com.thinkbiganalytics.metadata.sla.api;

/*-
 * #%L
 * thinkbig-sla-api
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

/**
 *
 */
public class AgreementNotFoundException extends ServiceLevelAgreementException {

    private static final long serialVersionUID = 3471025919178590016L;

    private final ServiceLevelAgreement.ID id;

    public AgreementNotFoundException(ServiceLevelAgreement.ID id) {
        this("No service level agreement was found with the specified ID", id);
    }

    public AgreementNotFoundException(String message, ServiceLevelAgreement.ID id) {
        super(message);
        this.id = id;
    }

    public ServiceLevelAgreement.ID getId() {
        return id;
    }
}
