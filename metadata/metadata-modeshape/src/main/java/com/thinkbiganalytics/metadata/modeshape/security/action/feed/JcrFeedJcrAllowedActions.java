/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action.feed;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;

/**
 * A type of allowed actions that applies to feeds.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrFeedJcrAllowedActions extends JcrAllowedActions {

    /**
     * @param allowedActionsNode
     */
    public JcrFeedJcrAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
    }

}
