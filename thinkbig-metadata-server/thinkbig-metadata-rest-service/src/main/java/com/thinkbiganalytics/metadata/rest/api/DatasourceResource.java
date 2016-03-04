package com.thinkbiganalytics.metadata.rest.api;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;

@Component
@Path("/datasource")
public class DatasourceResource {
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DatasetProvider datasetProvider;
    
    @GET
    public List<Datasource> getAllDatasources() {
        Set<Dataset> existing = this.datasetProvider.getDatasets();
        
        List<Datasource> list = new ArrayList<>(Collections2.transform(existing, Model.DOMAIN_TO_DS));
        return list;
    }

    @POST
    @Path("/hivetable")
    public HiveTableDatasource createHiveTable(HiveTableDatasource ds) {
        Model.validateCreate(ds);
        
        DatasetCriteria crit = this.datasetProvider.datasetCriteria()
                .name(ds.getName())
                .type(HiveTableDataset.class);
        Set<Dataset> existing = this.datasetProvider.getDatasets(crit);
        
        if (existing.isEmpty()) {
            HiveTableDataset table = this.datasetProvider.ensureHiveTableDataset(ds.getName(), 
                                                                                 ds.getDescription(), 
                                                                                 ds.getDatabase(), 
                                                                                 ds.getTableName());
            return Model.DOMAIN_TO_TABLE_DS.apply(table);
        } else {
            throw new WebApplicationException("A hive table datasource with the given name already exists: " + ds.getName(), Status.BAD_REQUEST);
        }
    }
    
    @POST
    @Path("/directory")
    public DirectoryDatasource createDirectory(DirectoryDatasource ds) {
        Model.validateCreate(ds);
        
        DatasetCriteria crit = this.datasetProvider.datasetCriteria()
                .name(ds.getName())
                .type(DirectoryDataset.class);
        Set<Dataset> existing = this.datasetProvider.getDatasets(crit);
        
        if (existing.isEmpty()) {
            DirectoryDataset dir = this.datasetProvider.ensureDirectoryDataset(ds.getName(), ds.getDescription(), Paths.get(ds.getPath()));
            return Model.DOMAIN_TO_DIR_DS.apply(dir);
        } else {
            throw new WebApplicationException("A directory datasource with the given name already exists: " + ds.getName(), Status.BAD_REQUEST);
        }
    }
}
