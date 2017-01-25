package com.thinkbiganalytics.metadata.rest.api;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceCriteria;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;

@Component
@Path("/metadata/datasource")
public class DatasourceController {

    @Inject
    private DatasourceProvider datasetProvider;

    @Inject
    private DatasourceDefinitionProvider datasourceDefinitionProvider;

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
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria criteria = createDatasourceCriteria(name, owner, on, after, before, type);
//            List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources();
            List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources(criteria);

            return new ArrayList<>(Collections2.transform(existing, Model.DOMAIN_TO_DS));
        });
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Datasource getDatasource(@PathParam("id") String idStr) {
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id = this.datasetProvider.resolve(idStr);
            com.thinkbiganalytics.metadata.api.datasource.Datasource ds = this.datasetProvider.getDatasource(id);

            if (ds != null) {
                return Model.DOMAIN_TO_DS.apply(ds);
            } else {
                throw new WebApplicationException("No datasource exists with the given ID: " + idStr, Status.NOT_FOUND);
            }
        });
    }


    @GET
    @Path("/datasource-definitions")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<DatasourceDefinition> getDatasourceDefinitions() {
        return this.metadata.read(() -> {
            Set<com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition> datasourceDefinitions = this.datasourceDefinitionProvider.getDatasourceDefinitions();
            if (datasourceDefinitions != null) {
                return new HashSet<>(Collections2.transform(datasourceDefinitions, Model.DOMAIN_TO_DS_DEFINITION));
            }
            return null;
        });
    }


//    @POST
//    @Path("/hivetable")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public HiveTableDatasource createHiveTable(final HiveTableDatasource ds,
//                                               @QueryParam("ensure") @DefaultValue("false") final boolean ensure) {
//        Model.validateCreate(ds);
//
//        return this.metadata.commit(() -> {
//            com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria crit = datasetProvider.datasetCriteria()
//                .name(ds.getName())
//                .type(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource.class);
//            List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources(crit);
//
//            if (existing.isEmpty()) {
//                com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource table
//                    = datasetProvider.ensureDatasource(ds.getName(),
//                                                       ds.getDescription(),
//                                                       com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource.class);
//                table.setDatabase(ds.getDatabase());
//                table.setTableName(ds.getTableName());
////                    = datasetProvider.ensureHiveTableDatasource(ds.getName(),
////                                                                ds.getDescription(),
////                                                                ds.getDatabase(),
////                                                                ds.getTableName());
//                return Model.DOMAIN_TO_TABLE_DS.apply(table);
//            } else if (ensure) {
//                return Model.DOMAIN_TO_TABLE_DS.apply((com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource) existing.iterator().next());
//            } else {
//                throw new WebApplicationException("A hive table datasource with the given name already exists: " + ds.getName(), Status.CONFLICT);
//            }
//        });
//    }

//    @POST
//    @Path("/directory")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Datasource createDirectory(final DirectoryDatasource ds,
//                                               @QueryParam("ensure") @DefaultValue("true") final boolean ensure) {
//        Model.validateCreate(ds);
//
//        return this.metadata.commit(() -> {
//            com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria crit
//                = datasetProvider.datasetCriteria()
//                    .name(ds.getName())
//                    .type(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource.class);
//            List<com.thinkbiganalytics.metadata.api.datasource.Datasource> existing = datasetProvider.getDatasources(crit);
//
//            if (existing.isEmpty()) {
//                com.thinkbiganalytics.metadata.api.datasource.Datasource dir
//                    = datasetProvider.ensureDatasource(ds.getName(),
//                                                       ds.getDescription(),
//                                                       com.thinkbiganalytics.metadata.api.datasource.Datasource.class);
//                // TODO add paths
//                return Model.DOMAIN_TO_DS.apply(dir);
//            } else if (ensure) {
//                return Model.DOMAIN_TO_DIR_DS.apply((com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource) existing.iterator().next());
//            } else {
//                throw new WebApplicationException("A directory datasource with the given name already exists: " + ds.getName(), Status.BAD_REQUEST);
//            }
//        });
//    }

    private com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria createDatasourceCriteria(String name,
                                                                                                      String owner,
                                                                                                      String on,
                                                                                                      String after,
                                                                                                      String before,
                                                                                                      String type) {
            com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria criteria = datasetProvider.datasetCriteria();

            if (StringUtils.isNotEmpty(name)) criteria.name(name);
    //        if (StringUtils.isNotEmpty(owner)) criteria.owner(owner);  // TODO implement
            if (StringUtils.isNotEmpty(on)) criteria.createdOn(Formatters.parseDateTime(on));
            if (StringUtils.isNotEmpty(after)) criteria.createdAfter(Formatters.parseDateTime(after));
            if (StringUtils.isNotEmpty(before)) criteria.createdBefore(Formatters.parseDateTime(before));
    //        if (StringUtils.isNotEmpty(type)) criteria.type(name);  // TODO figure out model type mapping

            return criteria;
        }
}
