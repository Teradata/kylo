/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;

import java.security.Principal;

/**
 *
 */
public abstract class JcrAbstractActionsBuilder {

    protected final Principal managementPrincipal;

    public JcrAbstractActionsBuilder() {
        this(new ModeShapeAdminPrincipal());
    }

    public JcrAbstractActionsBuilder(Principal mgtPrincipal) {
        super();
        this.managementPrincipal = mgtPrincipal;
    }

    protected Principal getManagementPrincipal() {
        return this.managementPrincipal;
    }
}
