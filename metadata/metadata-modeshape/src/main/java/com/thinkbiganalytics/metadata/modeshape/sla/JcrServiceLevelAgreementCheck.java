/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

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

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;


public class JcrServiceLevelAgreementCheck extends AbstractJcrAuditableSystemEntity implements ServiceLevelAgreementCheck, Serializable {


    public static final String SLA_NODE_NAME = "tba:slaChecks";
    public static final String NODE_TYPE = "tba:slaCheck";
    public static final String DESCRIPTION = "jcr:description";
    public static final String NAME = "jcr:title";
    public static final String CRON_SCHEDULE = "tba:cronSchedule";
    public static final String ACTION_CONFIGURATIONS = "tba:slaActionConfigurations";
    public static final String ACTION_CONFIGURATION_TYPE = "tba:slaActionConfiguration";
    private static final long serialVersionUID = 2611479261936214396L;

    /**
     *
     */
    public JcrServiceLevelAgreementCheck(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getId()
     */
    @Override
    public SlaCheckId getId() {
        try {
            return new SlaCheckId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the SLA ID", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getName()
     */
    @Override
    public String getName() {
        return JcrPropertyUtil.getString(this.node, NAME);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getDescription()
     */
    @Override
    public String getDescription() {
        return JcrPropertyUtil.getString(this.node, DESCRIPTION);
    }


    @Override
    public String getCronSchedule() {
        return JcrPropertyUtil.getString(this.node, CRON_SCHEDULE);
    }

    public ServiceLevelAgreement getServiceLevelAgreement() {
        try {
            return new JcrServiceLevelAgreement(this.node.getParent());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the SLA Node", e);
        }
    }

    public List<? extends ServiceLevelAgreementActionConfiguration> getActionConfigurations() {
        return getActionConfigurations(false);
    }

    public void setActionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations) {
        try {
            NodeIterator nodes = this.node.getNodes(ACTION_CONFIGURATIONS);
            while (nodes.hasNext()) {
                Node metricNode = (Node) nodes.next();
                metricNode.remove();
            }

            for (ServiceLevelAgreementActionConfiguration actionConfiguration : actionConfigurations) {
                Node node = this.node.addNode(ACTION_CONFIGURATIONS, ACTION_CONFIGURATION_TYPE);

                JcrPropertyUtil.setProperty(node, JcrPropertyConstants.TITLE, actionConfiguration.getClass().getSimpleName());
                ServiceLevelAgreementActionConfig annotation = actionConfiguration.getClass().getAnnotation(ServiceLevelAgreementActionConfig.class);
                String desc = actionConfiguration.getClass().getSimpleName();
                if (annotation != null) {
                    desc = annotation.description();
                }
                JcrPropertyUtil.setProperty(node, DESCRIPTION, desc);
                JcrUtil.addGenericJson(node, JcrPropertyConstants.JSON, actionConfiguration);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the metric nodes", e);
        }
    }

    public List<? extends ServiceLevelAgreementActionConfiguration> getActionConfigurations(boolean allowClassNotFound) {
        try {
            @SuppressWarnings("unchecked")
            Iterator<Node> itr = (Iterator<Node>) this.node.getNodes(ACTION_CONFIGURATIONS);

            List<Node> list = new ArrayList<>();
            itr.forEachRemaining((e) -> list.add(e));
            return list.stream().map((actionConfigNode) -> {
                return (ServiceLevelAgreementActionConfiguration) JcrUtil.getGenericJson(actionConfigNode, JcrPropertyConstants.JSON, allowClassNotFound);
            }).filter(configuration -> configuration != null).collect(Collectors.toList());


        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the metric nodes", e);
        }
    }

    public static class SlaCheckId extends EntityId implements ServiceLevelAgreementCheck.ID {

        public SlaCheckId(Serializable ser) {
            super(ser);
        }
    }

}
