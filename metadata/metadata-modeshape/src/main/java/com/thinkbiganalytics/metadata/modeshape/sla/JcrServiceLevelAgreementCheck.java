/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;


public class JcrServiceLevelAgreementCheck extends AbstractJcrAuditableSystemEntity implements ServiceLevelAgreementCheck, Serializable {


    private static final long serialVersionUID = 2611479261936214396L;

    public static final String SLA_NODE_NAME = "tba:slaChecks";

    public static final String NODE_TYPE = "tba:slaCheck";
    public static final String DESCRIPTION = "jcr:description";
    public static final String NAME = "jcr:title";
    public static final String CRON_SCHEDULE = "tba:cronSchedule";

    public static final String ACTION_CONFIGURATIONS = "tba:slaActionConfigurations";
    public static final String ACTION_CONFIGURATION_TYPE = "tba:slaActionConfiguration";

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

    public static class SlaCheckId extends EntityId implements ServiceLevelAgreementCheck.ID {

        public SlaCheckId(Serializable ser) {
            super(ser);
        }
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

    public List<? extends ServiceLevelAgreementActionConfiguration> getActionConfigurations(boolean allowClassNotFound) {
        try {
            @SuppressWarnings("unchecked")
            Iterator<Node> itr = (Iterator<Node>) this.node.getNodes(ACTION_CONFIGURATIONS);

            return Lists.newArrayList(Iterators.transform(itr, (actionConfigNode) -> {
                  return JcrUtil.getGenericJson(actionConfigNode, JcrPropertyConstants.JSON,allowClassNotFound);
            }));



        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the metric nodes", e);
        }
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

}
