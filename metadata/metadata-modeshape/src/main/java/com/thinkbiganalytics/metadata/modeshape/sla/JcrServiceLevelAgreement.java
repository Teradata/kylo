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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrServiceLevelAgreement extends AbstractJcrAuditableSystemEntity implements ServiceLevelAgreement, Serializable {


    public static final String NODE_TYPE = "tba:sla";
    public static final String DESCRIPTION = "jcr:description";
    public static final String NAME = "jcr:title";
    public static final String DEFAULT_GROUP = "tba:defaultGroup";
    public static final String GROUPS = "tba:groups";
    public static final String GROUP_TYPE = "tba:obligationGroup";
    public static final String JSON = "tba:json";
    public static final String ENABLED = "tba:enabled";
    public static final String SLA_CHECKS = "tba:slaChecks";
    private static final long serialVersionUID = 2611479261936214396L;


    /**
     *
     */
    public JcrServiceLevelAgreement(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getId()
     */
    @Override
    public SlaId getId() {
        try {
            return new SlaId(getObjectId());
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
    public boolean isEnabled() {
        return JcrPropertyUtil.getBooleanOrDefault(this.node, ENABLED, true);
    }

    public void setEnabled(boolean enabled) {
        JcrPropertyUtil.setProperty(this.node, ENABLED, enabled);
    }

    public void enable() {
        setEnabled(true);
    }

    public void disabled() {
        setEnabled(false);
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getObligationGroups()
         */
    @Override
    public List<ObligationGroup> getObligationGroups() {
        try {
            @SuppressWarnings("unchecked")
            Iterator<Node> defItr = (Iterator<Node>) this.node.getNodes(DEFAULT_GROUP);
            @SuppressWarnings("unchecked")
            Iterator<Node> grpItr = (Iterator<Node>) this.node.getNodes(GROUPS);

            return Lists.newArrayList(Iterators.concat(
                Iterators.transform(defItr, (groupNode) -> {
                    return JcrUtil.createJcrObject(groupNode, JcrObligationGroup.class, JcrServiceLevelAgreement.this);
                }),
                Iterators.transform(grpItr, (groupNode) -> {
                    return JcrUtil.createJcrObject(groupNode, JcrObligationGroup.class, JcrServiceLevelAgreement.this);
                })));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the obligation nodes", e);
        }

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getObligations()
     */
    @Override
    public List<Obligation> getObligations() {
        List<Obligation> list = new ArrayList<>();

        for (ObligationGroup group : getObligationGroups()) {
            list.addAll(group.getObligations());
        }

        return list;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getAllMetrics()
     */
    @Override
    public Set<Metric> getAllMetrics() {
        return this.getObligationGroups().stream()
            .flatMap(obligationGroup -> obligationGroup.getObligations().stream())
            .flatMap(obligation -> obligation.getMetrics().stream())
            .collect(Collectors.toSet());
    }

    public JcrObligationGroup getDefaultGroup() {
        return JcrUtil.getOrCreateNode(this.node, DEFAULT_GROUP, GROUP_TYPE, JcrObligationGroup.class, JcrServiceLevelAgreement.this);
    }

    public void addGroup(JcrObligationGroup group) {
        try {
            this.node.addNode(GROUPS, GROUP_TYPE);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create the obligation group node", e);
        }
    }


    public void clear() {
        try {
            Iterator<Node> grpItr = (Iterator<Node>) this.node.getNodes(GROUPS);
            while (grpItr.hasNext()) {
                Node group = grpItr.next();
                group.remove();
            }
            grpItr = (Iterator<Node>) this.node.getNodes(DEFAULT_GROUP);
            while (grpItr.hasNext()) {
                Node group = grpItr.next();
                group.remove();
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to clear the SLA obligation group nodes", e);
        }
    }

    public List<ServiceLevelAgreementCheck> getSlaChecks() {

        List<ServiceLevelAgreementCheck> list = new ArrayList<>();

        try {
            @SuppressWarnings("unchecked")
            Iterator<Node> itr = (Iterator<Node>) this.node.getNodes(SLA_CHECKS);

            return Lists.newArrayList(
                Iterators.transform(itr, (checkNode) -> {
                    return JcrUtil.createJcrObject(checkNode, JcrServiceLevelAgreementCheck.class);
                }));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the obligation nodes", e);
        }


    }

    public static class SlaId extends JcrEntity.EntityId implements ServiceLevelAgreement.ID {

        public SlaId(Serializable ser) {
            super(ser);
        }
    }


}
