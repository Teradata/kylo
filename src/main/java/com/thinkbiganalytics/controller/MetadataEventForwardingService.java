/**
 * 
 */
package com.thinkbiganalytics.controller;

import java.util.Set;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.reporting.InitializationException;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
public class MetadataEventForwardingService extends AbstractControllerService implements MetadataEventService {
    
    private DataOperationsProvider operationsProvider;
    
    @Override
    protected void init(ControllerServiceInitializationContext config) throws InitializationException {
        super.init(config);
        Set<String> ids = config.getControllerServiceLookup().getControllerServiceIdentifiers(MetadataProviderService.class);
        if (ids.size() > 0) {
            // Should be just one MetadataProviderService
            String svcId = ids.iterator().next();
            MetadataProviderService provSvc = (MetadataProviderService) config.getControllerServiceLookup().getControllerService(svcId);
            this.operationsProvider = provSvc.getDataOperationsProvider();
        } else {
            throw new InitializationException("No MetadataProviderService service available");
        }
    }

    @Override
    public void addListener(DirectoryDataset ds, DataChangeEventListener<DirectoryDataset, FileList> listener) {
        this.operationsProvider.addListener(ds, listener);
    }

    @Override
    public void addListener(HiveTableDataset ds, DataChangeEventListener<HiveTableDataset, HiveTableUpdate> listener) {
        this.operationsProvider.addListener(ds, listener);
    }

}
