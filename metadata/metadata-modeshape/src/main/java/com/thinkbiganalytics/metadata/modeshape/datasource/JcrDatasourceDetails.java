package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.datasource.DatasourceDetails;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;

import javax.annotation.Nonnull;
import javax.jcr.Node;

/**
 * A Data Source Details node stored in JCR.
 */
public abstract class JcrDatasourceDetails extends JcrPropertiesEntity implements DatasourceDetails {

    /**
     * Constructs a {@code JcrDatasourceDetails} with the specified JCR node.
     *
     * @param node the JCR node
     */
    public JcrDatasourceDetails(@Nonnull final Node node) {
        super(node);
    }
}
