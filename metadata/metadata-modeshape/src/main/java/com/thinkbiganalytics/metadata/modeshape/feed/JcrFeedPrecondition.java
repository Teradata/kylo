/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

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

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrFeedPrecondition extends JcrObject implements FeedPrecondition {

    public static final String NODE_TYPE = "tba:feedPrecondition";

    //  public static final String LAST_ASSESSMENT = "tba:lastAssessment";
    public static final String SLA_REF = "tba:slaRef";
    public static final String SLA = "tba:sla";

    public static final String SLA_TYPE = "tba:sla";

    private JcrFeed feed;

    /**
     *
     */
    public JcrFeedPrecondition(Node node, JcrFeed feed) {
        super(node);
        this.feed = feed;
    }

    public void clear() {
        try {
            if (this.node.hasProperty(SLA_REF)) {
                this.node.getProperty(SLA_REF).remove();
            }
            if (this.node.hasNode(SLA)) {
                this.node.getNode(SLA).remove();
            }

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to cler the precondition", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedPrecondition#getFeed()
     */
    @Override
    public Feed getFeed() {
        return this.feed;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedPrecondition#getAgreement()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        try {
            if (this.node.hasNode(SLA)) {
                return new JcrServiceLevelAgreement(this.node.getNode(SLA));
            } else if (this.node.hasProperty(SLA_REF)) {
                return new JcrServiceLevelAgreement(this.node.getProperty(SLA_REF).getNode());
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the precondition SLA", e);
        }
    }

    @Override
    public ServiceLevelAssessment getLastAssessment() {
        return null;
    }

    @Override
    public void setLastAssessment(ServiceLevelAssessment assmnt) {
        // TODO create assessment
    }

}
