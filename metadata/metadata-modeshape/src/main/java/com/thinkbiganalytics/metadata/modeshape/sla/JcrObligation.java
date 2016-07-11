/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public class JcrObligation extends JcrObject implements Obligation, Serializable {

    public static final String DESCRIPTION = "jcr:description";
    public static final String NAME = "jcr:title";
    public static final String JSON = "tba:json";
    public static final String METRICS = "tba:metrics";
    
    public static final String METRIC_TYPE = "tba:metric";

    private static final long serialVersionUID = -6415493614683081403L;
    
    private final JcrObligationGroup group;
    
    
    public JcrObligation(Node node, JcrObligationGroup group) {
        super(node);
        this.group = group;
    }
    
    
    @Override
    public String getDescription() {
        return JcrPropertyUtil.getString(this.node, "tba:description");
    }

    @Override
    public ServiceLevelAgreement getAgreement() {
        return getGroup().getAgreement();
    }

    @Override
    public ObligationGroup getGroup() {
        return group;
    }

    @Override
    public Set<Metric> getMetrics() {
        try {
            @SuppressWarnings("unchecked")
            Iterator<Node> itr = (Iterator<Node>) this.node.getNodes(METRICS);
            
            return Sets.newHashSet(Iterators.transform(itr, (metricNode) -> {
                return JcrUtil.getGenericJson(metricNode, JSON);
            }));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the metric nodes", e);
        }
    }

    public void setMetrics(Set<Metric> metrics) {
        try {
            NodeIterator nodes = this.node.getNodes(METRICS);
            while (nodes.hasNext()) {
                Node metricNode = (Node) nodes.next();
                metricNode.remove();
            }
            
            for (Metric metric : metrics) {
                Node metricNode = this.node.addNode(METRICS, METRIC_TYPE);
                
                JcrPropertyUtil.setProperty(metricNode, NAME, metric.getClass().getSimpleName());
                JcrPropertyUtil.setProperty(metricNode, DESCRIPTION, metric.getDescription());
                JcrUtil.addGenericJson(metricNode, JSON, metric);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the metric nodes", e);
        }
    }

    public void setDescription(String description) {
        JcrPropertyUtil.setProperty(this.node, "tba:description", description);
    }
    
    
}
