package com.thinkbiganalytics.metadata.rest.api;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceCriteria;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;

@Component
@Path("/datasource")
public class DatasourceController {
    
    @Inject
    private DatasourceProvider datasetProvider;
    
    @Inject
    private MetadataAccess metadata;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Datasource> getDatasources(@QueryParam(DatasourceCriteria.NAME)final String name,
                                           @QueryParam(DatasourceCriteria.OWNER)final String owner,
                                           @QueryParam(DatasourceCriteria.ON)final String on,
                                           @QueryParam(DatasourceCriteria.AFTER)final String after,
                                           @QueryParam(DatasourceCriteria.BEFORE)final String before,
                                           @QueryParam(DatasourceCriteria.TYPE)final String type ) {
        return this.metadata.read(new Command<List<Datasource>>() {
            @Override
            public List<Datasource> execute() {
                com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria criteria = createDatasourceCriteria(name, owner, on, after, before, type);
                List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources(criteria);
                
                return new ArrayList<>(Collections2.transform(existing, Model.DOMAIN_TO_DS));
            }
        });
    }

    @POST
    @Path("/hivetable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HiveTableDatasource createHiveTable(final HiveTableDatasource ds,
                                               @QueryParam("ensure") @DefaultValue("true") final boolean ensure) {
        Model.validateCreate(ds);
        
        return this.metadata.commit(new Command<HiveTableDatasource>() {
            @Override
            public HiveTableDatasource execute() {
                com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria crit = datasetProvider.datasetCriteria()
                                .name(ds.getName())
                                .type(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource.class);
                List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources(crit);
                
                if (existing.isEmpty()) {
                    com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource table 
                        = datasetProvider.ensureHiveTableDatasource(ds.getName(), 
                                                                    ds.getDescription(), 
                                                                    ds.getDatabase(), 
                                                                    ds.getTableName());
                    return Model.DOMAIN_TO_TABLE_DS.apply(table);
                } else if (ensure) {
                    return Model.DOMAIN_TO_TABLE_DS.apply((com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource) existing.iterator().next());
                } else {
                    throw new WebApplicationException("A hive table datasource with the given name already exists: " + ds.getName(), Status.CONFLICT);
                }
            }
        });
    }
    
    @POST
    @Path("/directory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DirectoryDatasource createDirectory(final DirectoryDatasource ds,
                                               @QueryParam("ensure") @DefaultValue("true") final boolean ensure) {
        Model.validateCreate(ds);
        
        return this.metadata.commit(new Command<DirectoryDatasource>() {
            @Override
            public DirectoryDatasource execute() {
                com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria crit 
                    = datasetProvider.datasetCriteria()
                        .name(ds.getName())
                        .type(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource.class);
                List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources(crit);
                
                if (existing.isEmpty()) {
                    com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource dir 
                        = datasetProvider.ensureDirectoryDatasource(ds.getName(), ds.getDescription(), Paths.get(ds.getPath()));
                    return Model.DOMAIN_TO_DIR_DS.apply(dir);
                } else if (ensure) {
                    return Model.DOMAIN_TO_DIR_DS.apply((com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource) existing.iterator().next());
                } else {
                    throw new WebApplicationException("A directory datasource with the given name already exists: " + ds.getName(), Status.BAD_REQUEST);
                }
            }
        });
    }

    private com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria createDatasourceCriteria(String name, 
                                                                                                      String owner, 
                                                                                                      String on, 
                                                                                                      String after, 
                                                                                                      String before, 
                                                                                                      String type) {
            com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria criteria = datasetProvider.datasetCriteria();
            
            if (StringUtils.isNotEmpty(name)) criteria.name(name);
    //        if (StringUtils.isNotEmpty(owner)) criteria.owner(owner);  // TODO implement
            if (StringUtils.isNotEmpty(on)) criteria.createdOn(Formatters.TIME_FORMATTER.parseDateTime(on));
            if (StringUtils.isNotEmpty(after)) criteria.createdAfter(Formatters.TIME_FORMATTER.parseDateTime(after));
            if (StringUtils.isNotEmpty(before)) criteria.createdBefore(Formatters.TIME_FORMATTER.parseDateTime(before));
    //        if (StringUtils.isNotEmpty(type)) criteria.type(name);  // TODO figure out model type mapping
            
            return criteria;
        }
}
