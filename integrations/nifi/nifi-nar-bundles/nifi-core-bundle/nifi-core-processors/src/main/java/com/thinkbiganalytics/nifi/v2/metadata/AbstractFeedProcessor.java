/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.Relationship;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.v2.common.BaseProcessor;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;

/**
 * @author Sean Felten
 */
public abstract class AbstractFeedProcessor extends BaseProcessor {

    protected void addProperties(List<PropertyDescriptor> props) {
        props.add(CommonProperties.METADATA_SERVICE);
    }

    protected void addRelationships(Set<Relationship> relationships2) {
    }

    protected MetadataProviderService getProviderService(ProcessContext context) {
        return context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class);
    }

    protected Datasource findDatasource(ProcessContext context, String dsName) {
        MetadataProvider datasetProvider = getProviderService(context).getProvider();
        return datasetProvider.getDatasourceByName(dsName);
    }
}
