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
public class DuplicateAgreementNameException extends ServiceLevelAgreementException {

    private static final long serialVersionUID = -4161629217364927555L;

    private final String name;

    /**
     * @param name the duplicate name that was tried.
     */
    public DuplicateAgreementNameException(String name) {
        this("A service level agreement already exists with the given name", name);
    }

    /**
     * @param message the messate
     * @param name    the duplicate name that was tried.
     */
    public DuplicateAgreementNameException(String message, String name) {
        super(message);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
