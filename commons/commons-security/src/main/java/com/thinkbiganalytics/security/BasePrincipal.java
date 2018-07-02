/**
 *
 */
package com.thinkbiganalytics.security;

/*-
 * #%L
 * thinkbig-security-api
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

import java.io.Serializable;
import java.security.Principal;

/**
 * Base principal type that is serializable and provides default implementations of equals and
 * hashCode that should cover most subclass requirements.
 */
public abstract class BasePrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public BasePrincipal() {
    }

    public BasePrincipal(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && this.getClass().isInstance(obj)) {
            BasePrincipal that = (BasePrincipal) obj;
            return this.name.equals(that.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ this.name.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + this.name;
    }
}
