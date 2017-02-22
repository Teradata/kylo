/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action.feed;

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
