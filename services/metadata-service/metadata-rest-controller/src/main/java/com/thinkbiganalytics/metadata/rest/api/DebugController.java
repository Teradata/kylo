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

import com.google.common.base.Strings;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.OperationStatus;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.security.MetadataAccessControl;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceSchedule;
import com.thinkbiganalytics.metadata.api.sla.WithinSchedule;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPath;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.metadata.modeshape.support.ModeshapeIndexUtil;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.jcr.JcrIndexDefinition;
import com.thinkbiganalytics.metadata.rest.model.jcr.JcrQueryResult;
import com.thinkbiganalytics.metadata.rest.model.jcr.JcrQueryResultColumn;
import com.thinkbiganalytics.metadata.rest.model.jcr.JcrQueryResultColumnValue;
import com.thinkbiganalytics.metadata.rest.model.jcr.JcrQueryResultRow;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.modeshape.jcr.api.JcrTools;
import org.modeshape.jcr.api.Workspace;
import org.modeshape.jcr.api.index.IndexDefinition;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * A controller to use when debugging issues with the UI
 */
@Api(tags = "Internal", produces = "application/json")
@Component
@Path("/v1/metadata/debug")
@SwaggerDefinition(tags = @Tag(name = "Internal", description = "debugging tools"))
public class DebugController {

    @Context
    private UriInfo uriInfo;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private MetadataEventService eventService;
    
    @Inject
    private AccessController accessController;

    /**
     * Allows the caller to update status events for the feed
     *
     * @param feedName the name of the feed
     * @param opIdStr  the operation for the feed
     * @param stateStr the new state to persist
     * @param status   the status of the operation
     * @return the feed operation status event
     */
    @POST
    @Path("feedop/event")
    public String postFeedOperationStatusEvent(@QueryParam("feed") String feedName,
                                               @QueryParam("op") String opIdStr,
                                               @QueryParam("state") String stateStr,
                                               @QueryParam("status") @DefaultValue("") String status) {
        // TODO: Is this a feed ops permission?
        this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
        
        FeedOperation.ID opId = null;
        FeedOperation.State state = FeedOperation.State.valueOf(stateStr.toUpperCase());
        OperationStatus opStatus = new OperationStatus(null,feedName,null, opId, state, status);
        FeedOperationStatusEvent event = new FeedOperationStatusEvent(opStatus);

        this.eventService.notify(event);

        return event.toString();
    }

    /**
     * creates a hive table model object for debugging
     *
     * @return the hive table data source model
     */
    @GET
    @Path("datasource/hivetable")
    @Produces(MediaType.APPLICATION_JSON)
    public Datasource exampleHiveTable() {
        return new HiveTableDatasource("table1", "database1", "table1");
    }

    /**
     * returns a list of metrics for debugging
     *
     * @return a list of metrics
     */
    @GET
    @Path("metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Metric> exampleMetrics() {
        List<Metric> metrics = new ArrayList<>();
        FeedExecutedSinceSchedule feedExecutedSinceSchedule = null;
        try {
            feedExecutedSinceSchedule = new FeedExecutedSinceSchedule("category", "Feed", "* * * * * ? *");
            metrics.add(feedExecutedSinceSchedule);

            WithinSchedule withinSchedule = new com.thinkbiganalytics.metadata.api.sla.WithinSchedule("* * * * * ? *", "4 hours");
            metrics.add(withinSchedule);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return metrics;
    }

    /**
     * returns a new feed precondition model for debugging
     *
     * @return the precondition model
     */
    @GET
    @Path("precondition")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedPrecondition examplePrecondition() {
        FeedPrecondition procond = new FeedPrecondition("DependingPrecondition");
        procond.addMetrics("Feed dependson on execution of another feed",
                           new FeedExecutedSinceFeed("DependentCategory", "DependentFeed", "ExecutedSinceCategory", "ExecutedSinceFeed"));
        return procond;
    }

    /**
     * Prints the nodes of the JCR path given, for debugging.
     *
     * @param abspath the path in JCR
     * @return a printout of the JCR tree
     */
    @GET
    @Path("jcr/{abspath: .*}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public String printJcrTree(@PathParam("abspath") final String abspath) {
        return metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ACCESS_METADATA);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            try {
                Session session = JcrMetadataAccess.getActiveSession();
                java.nio.file.Path path = JcrPath.get(abspath);
                
                try {
                    Item item = getItem(abspath, session);
                    printItem(item, pw);
                } catch (PathNotFoundException e) {
                    return "Path not found: " + path.toString();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            pw.flush();
            return sw.toString();
        });
    }

    /**
     * Puts a new value into the property at the JCR path given, for debugging.
     *
     * @param abspath the path in JCR to a node's property
     * @param value the new value for the property
     * @return a print of the property showing the new current value
     */
    @PUT
    @Path("jcr/{abspath: .*}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public String putPropertyValue(@PathParam("abspath") final String abspath,
                                   @QueryParam("value") final String value) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
    
        try {
            metadata.commit(() -> {
                this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
                
                Session session = JcrMetadataAccess.getActiveSession();
                java.nio.file.Path path = JcrPath.get(abspath);
    
                try {
                    Item item = getItem(abspath, session);
                    
                    if (item instanceof Property) {
                        Property property = (Property) item;
                        
                        // No-op if value not supplied
                        if (value != null) {
                            property.setValue(value);
                        }
                    } else {
                        pw.println("Item at path is not a property: " + path.toString());
                    }
                    
                    printItem(item, pw);
                } catch (PathNotFoundException e) {
                    try {
                        Item item = getItem(path.getParent().toString(), session);
                        
                        if (item instanceof Node && value != null) {
                            Node node = (Node) item;
                            String propName = path.getFileName().toString();
                            item = node.setProperty(propName, value);
                        } else {
                            pw.print("Path not found: " + path.toString());
                        }
                        
                        printItem(item, pw);
                    } catch (PathNotFoundException pnf) {
                        pw.print("Path not found: " + path.toString());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace(pw);
            throw new RuntimeException(e);
        }
    
        pw.flush();
        return sw.toString();
    }
    
    /**
     * Prints the subgraph of the node in JCR with the specified ID.
     *
     * @param jcrId the id of the node in JCR
     * @return the subgraph print out
     */
    @GET
    @Path("jcr")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public String printJcrId(@QueryParam("id") final String jcrId) {
        return metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ACCESS_METADATA);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            // If the ID is over 36 characters long then assume it is a cache key and
            // extract only the node ID portion of the key.
            String nodeId = jcrId.length() > 36 ? jcrId.substring(jcrId.length() - 36, jcrId.length()) : jcrId;
    
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Node node = session.getNodeByIdentifier(nodeId);
                pw.print("Path: ");
                pw.println(node.getPath());
                JcrTools tools = new JcrTool(true, pw);
                tools.printSubgraph(node);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    
            pw.flush();
            return sw.toString();
        });
    }

    /**
     * Delete the JCR tree specified by the absolute path following ".../jcr/".
     *
     * @param abspath the path with JCR to delete
     * @return a confirmation message that the path was deleted
     */
    @DELETE
    @Path("jcr/{abspath: .*}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteJcrTree(@PathParam("abspath") final String abspath) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
    
        try {
            metadata.commit(() -> {
                this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
                
                Session session = JcrMetadataAccess.getActiveSession();
                java.nio.file.Path path = JcrPath.get(abspath);
    
                try {
                    Item item = getItem(abspath, session);
                    
                    item.remove();
                    pw.print("DELETED " + abspath);
                } catch (PathNotFoundException e) {
                    pw.print("Path not found: " + path.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace(pw);
            throw new RuntimeException(e);
        }
    
        pw.flush();
        return sw.toString();
    }

    /**
     * Deletes the subgraph of the node in JCR with the specific ID.
     *
     * @param jcrId the id of the node in JCR
     * @return the subgraph to delete
     */
    @DELETE
    @Path("jcr")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteJcrId(@QueryParam("id") final String jcrId) {
        return metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ACCESS_METADATA);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Node node = session.getNodeByIdentifier(jcrId);
                String absPath = node.getPath();
                node.remove();
                pw.print("DELETED " + absPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    
            pw.flush();
            return sw.toString();
        });
    }

    @DELETE
    @Path("jcr/node-reference/{abspath: .*}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteJcrReference(@PathParam("abspath") final String abspath, @QueryParam("referenceId") String referenceId) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
    
        try {
            metadata.commit(() -> {
                this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
                Session session = JcrMetadataAccess.getActiveSession();
                Item item = getItem(abspath, session);
    
                if (item instanceof Node) {
                    pw.println("Item at the path is not a property: " + abspath);
                } else {
                    Property property = (Property) item;
                    String referenceNodeName = property.getName();
                    Node node = property.getParent();
                    
                    if (property.isMultiple()){
                        Set<Object> removal = new HashSet<>();
                        JcrPropertyUtil.getSetProperty(node, referenceNodeName).stream().forEach(n -> {
                            try {
                                if (n instanceof Node) {
                                    String id = ((Node) n).getIdentifier();
                                    if (id.equalsIgnoreCase(referenceId)) {
                                        removal.add(n);
                                    }
                                }
                            }catch (RepositoryException e){
                                e.printStackTrace();
                                pw.print("Unable to delete node for reference " + abspath+"/"+referenceId+".  "+e.getMessage());
                            }
                        });
                        
                        if(!removal.isEmpty()) {
                            removal.stream().forEach(n -> JcrPropertyUtil.removeFromSetProperty(node, referenceNodeName, n));
    
                            pw.print("DELETED " + abspath+"/"+referenceId);
                        }
                        else {
                            pw.print("Not Deleted.  Unable to find reference id " +referenceId+" in set for path "+ abspath);
                        }
                    } else {
                        property.remove();
                        pw.print("Path is not a set of nodes.  Setting value of property to null.  DELETED " + abspath+"/"+referenceId);
                    }
                }
    
            });
        } catch (Exception e) {
            e.printStackTrace(pw);
            throw new RuntimeException(e);
        }
    
        pw.flush();
        return sw.toString();
    }

    /**
     * Prints the nodes of the JCR path given, for debugging.
     *
     * @param query the jcr query
     * @return a printout of the JCR tree
     */
    @GET
    @Path("jcr-sql")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public JcrQueryResult queryJcr(@QueryParam("query") final String query) {
        return metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
            
            List<List<String>> rows = new ArrayList<>();
            Long startTime = System.currentTimeMillis();
            JcrQueryResult jcrQueryResult = new JcrQueryResult();
    
    
            try {
                Session session = JcrMetadataAccess.getActiveSession();
    
                Workspace workspace = (Workspace) session.getWorkspace();
    
                String explainPlain = JcrQueryUtil.explainPlain(session,query);
                //start the timer now:
                startTime = System.currentTimeMillis();
    
                QueryResult result = JcrQueryUtil.query(session, query);
                jcrQueryResult.setExplainPlan(explainPlain);
                RowIterator rowItr = result.getRows();
                List<JcrQueryResultColumn> columns = new ArrayList<>();
                String colsStr = StringUtils.substringAfter(query.toLowerCase(),"select");
                colsStr = StringUtils.substringBefore(colsStr,"from");
                if(StringUtils.isNotBlank(colsStr)){
                    colsStr = colsStr.trim();
                    columns = Arrays.asList(colsStr.split(",")).stream().map(c ->{
                        String columnName =c;
                        if(c.contains("as ")){
                            columnName = StringUtils.substringAfter(c,"as ");
                        }else if(c.contains(" ")){
                            columnName = StringUtils.substringAfter(c, " ");
                        }
                       return new JcrQueryResultColumn(columnName);
                    }).collect(Collectors.toList());
                }
                jcrQueryResult.setColumns(columns);
    
                while (rowItr.hasNext()) {
                   Row row =rowItr.nextRow();
                   Value[] rowValues = row.getValues();
                    if(rowValues != null){
                        if(rowValues.length != columns.size()){
                           columns = IntStream.range(0, rowValues.length)
                                .mapToObj(i ->  new JcrQueryResultColumn("Column "+i)).collect(Collectors.toList());
                           jcrQueryResult.setColumns(columns);
                        }
                        JcrQueryResultRow jcrQueryResultRow = new JcrQueryResultRow();
                        jcrQueryResult.addRow(jcrQueryResultRow);
                      List<JcrQueryResultColumnValue> jcrQueryResultColumnValues = Arrays.asList(rowValues).stream().map(v->{
                           try {
                               String value = v.getString();
                               return new JcrQueryResultColumnValue(value);
                           }catch (Exception e){
                               return new JcrQueryResultColumnValue("ERROR: "+e.getMessage());
                           }
                       }).collect(Collectors.toList());
                       jcrQueryResultRow.setColumnValues(jcrQueryResultColumnValues);
                   }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            long totalTime = System.currentTimeMillis() - startTime;
            jcrQueryResult.setQueryTime(totalTime);
            return jcrQueryResult;
    
        });
    }

    @GET
    @Path("jcr-index")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JcrIndexDefinition> getIndexes(){
        return  metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ACCESS_METADATA);
          
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Workspace workspace = (Workspace) session.getWorkspace();
                Map<String, IndexDefinition> indexDefinitionMap = workspace.getIndexManager().getIndexDefinitions();
                if (indexDefinitionMap != null) {
                    return indexDefinitionMap.entrySet().stream().map((Map.Entry<String, IndexDefinition> e) -> {
                        JcrIndexDefinition indexDefinition = new JcrIndexDefinition();
                          StringBuffer names = new StringBuffer();
                        StringBuffer types = new StringBuffer();
                                for(int i=0; i< e.getValue().size(); i++){
                                    if(i >0){
                                        names.append(",");
                                        types.append(",");
                                    }
                                    int columnType =e.getValue().getColumnDefinition(i).getColumnType();
                                    String propertyName =e.getValue().getColumnDefinition(i).getPropertyName();
                                    names.append(propertyName);
                                    types.append(PropertyType.nameFromValue(columnType));
                                }
                                indexDefinition.setIndexKind(e.getValue().getKind().name());
                        indexDefinition.setIndexName(e.getKey());
                        indexDefinition.setNodeType(e.getValue().getNodeTypeName());
                        indexDefinition.setPropertyName(names.toString());
                        indexDefinition.setPropertyTypes(types.toString());

                        return indexDefinition;
                    }).collect(Collectors.toList());
                } else {
                    return Collections.emptyList();
                }

            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @POST
    @Path("jcr-index/{indexName}/unregister")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponseStatus unregisterIndex(@PathParam("indexName") String indexName){
        return  metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
            
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Workspace workspace = (Workspace) session.getWorkspace();
                ModeshapeIndexUtil.unregisterIndex(workspace.getIndexManager(), indexName);
                return RestResponseStatus.SUCCESS;
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @POST
    @Path("jcr-index/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("registers an index with modeshape")
    @ApiResponses(
        @ApiResponse(code = 200, message = "registers an index with modeshape", response = String.class)
    )
    public RestResponseStatus registerIndex(JcrIndexDefinition indexDefinition){
        return  metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);

            Session session = JcrMetadataAccess.getActiveSession();
            Workspace workspace = (Workspace) session.getWorkspace();
            String nodeType = indexDefinition.getNodeType();
            try {
                if (workspace.getNodeTypeManager().hasNodeType(nodeType)) {
                    ModeshapeIndexUtil.registerIndex(workspace.getIndexManager(), indexDefinition.getIndexName(), IndexDefinition.IndexKind.valueOf(indexDefinition.getIndexKind().toUpperCase()),
                                                     "local", nodeType, indexDefinition.getDescription(), null, indexDefinition.getPropertyName(),
                                                     indexDefinition.getPropertyType());
                    return RestResponseStatus.SUCCESS;
                } else {
                    throw new IllegalArgumentException(String.format("Unknown Index Node Type %s", nodeType));
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @POST
    @Path("jcr-index/reindex")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponseStatus postReindex() {
        return reindex();
    }

    @GET
    @Path("jcr-index/reindex")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponseStatus getReindex() {
        return reindex();
    }

    private Item getItem(final String abspath, Session session) throws PathNotFoundException, RepositoryException {
        String path = abspath.startsWith("/") ? abspath : "/" + abspath;
        return session.getItem(path);
    }

    private void printItem(Item item, PrintWriter pw) throws RepositoryException, ValueFormatException {
        if (item instanceof Node) {
            Node node = (Node) item;
            JcrTools tools = new JcrTool(true, pw);
            tools.printSubgraph(node);
        } else {
            Property property = (Property) item;
            String value = property.getString();
            pw.println(" - " + property.getName() + "=" + value);
        }
    }

    private RestResponseStatus reindex() {
    
        return  metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
            
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Workspace workspace = (Workspace) session.getWorkspace();
                workspace.reindex();
                return RestResponseStatus.SUCCESS;
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
