package com.thinkbiganalytics.feedmgr.service.feed.datasource;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.NifiControllerServiceProperties;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedDataTransformation;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateProcessorDatasourceDefinition;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Create and assign {@link DerivedDatasource} based upon a template or feed
 */
public class DerivedDatasourceFactory {

    private static final Logger log = LoggerFactory.getLogger(DerivedDatasourceFactory.class);

    /**
     * Processor type for a Hive datasource in a data transformation feed.
     */
    private static String DATA_TRANSFORMATION_HIVE_DEFINITION = "datatransformation.hive.template";

    /**
     * Processor type for a JDBC datasource in a data transformation feed.
     */
    private static String DATA_TRANSFORMATION_JDBC_DEFINITION = "datatransformation.jdbc.template";

    /**
     * Property for the table name of a HiveDatasource.
     */
    private static String HIVE_TABLE_KEY = "table";

    /**
     * Property for the schema name of a HiveDatasource.
     */
    private static String HIVE_SCHEMA_KEY = "schema";

    /**
     * Property for the database connection name of a DatabaseDatasource.
     */
    private static String JDBC_CONNECTION_KEY = "Database Connection";

    /**
     * Property for the schema and table name of a DatabaseDatasource.
     */
    private static String JDBC_TABLE_KEY = "Table";

    @Inject
    DatasourceDefinitionProvider datasourceDefinitionProvider;

    @Inject
    DatasourceProvider datasourceProvider;

    @Inject
    PropertyExpressionResolver propertyExpressionResolver;

    @Inject
    FeedManagerTemplateService feedManagerTemplateService;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    RegisteredTemplateCache registeredTemplateCache;

    @Inject
    private NifiControllerServiceProperties nifiControllerServiceProperties;

    public void populateDatasources(FeedMetadata feedMetadata, RegisteredTemplate template, Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> sources,
                                    Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> dest) {
        // Extract source and destination datasources from data transformation feeds
        if (isDataTransformation(feedMetadata)) {
            if (noneMatch(template.getRegisteredDatasourceDefinitions(), DatasourceDefinition.ConnectionType.SOURCE)) {
                sources.addAll(ensureDataTransformationSourceDatasources(feedMetadata));
            }
            if (noneMatch(template.getRegisteredDatasourceDefinitions(), DatasourceDefinition.ConnectionType.DESTINATION)) {
                dest.addAll(ensureDataTransformationDestinationDatasources(feedMetadata));
            }
        }

        //see if its in the cache first
        List<RegisteredTemplate.Processor> processors = registeredTemplateCache.getProcessors(feedMetadata.getTemplateId());
        //if not add it
        if (processors == null) {
            processors = feedManagerTemplateService.getRegisteredTemplateProcessors(feedMetadata.getTemplateId(), true);
            registeredTemplateCache.putProcessors(feedMetadata.getTemplateId(), processors);
        }
        //COPY the properties since they will be replaced when evaluated
        List<NifiProperty> allProperties = processors.stream().flatMap(processor -> processor.getProperties().stream())
            .map(property -> new NifiProperty(property)).collect(Collectors.toList());

        template.getRegisteredDatasourceDefinitions().stream().forEach(definition -> {
            Datasource.ID id = ensureDatasource(definition, feedMetadata, allProperties);
            if (id != null) {
                if (com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition.ConnectionType.SOURCE.equals(definition.getDatasourceDefinition().getConnectionType())) {
                    //ensure this is the selected one for the feed
                    if (template != null && template.getInputProcessors() != null && getFeedInputProcessorTypes(feedMetadata).contains(definition.getProcessorType())) {
                        sources.add(id);
                    }
                } else {
                    dest.add(id);
                }
            }
        });
    }

    public boolean matchesDefinition(TemplateProcessorDatasourceDefinition definition, NifiProperty nifiProperty) {
        return nifiProperty.getProcessorType().equals(definition.getProcessorType()) && (nifiProperty.getProcessorId().equals(definition.getProcessorId()) || nifiProperty.getProcessorName()
            .equalsIgnoreCase(definition.getProcessorName()));
    }

    /**
     * Builds the list of destinations for the specified data transformation feed.
     *
     * <p>The data source type is determined based on the sources used in the transformation. If only one source is used then it is assumed that the source and destination are the same. Otherwise it
     * is assumed that the destination is Hive.</p>
     *
     * @param feed the feed
     * @return the list of destinations
     * @throws NullPointerException if the feed has no data transformation
     */
    @Nonnull
    private Set<Datasource.ID> ensureDataTransformationDestinationDatasources(@Nonnull final FeedMetadata feed) {
        // Set properties based on data source type
        final String processorType;
        final Map<String, String> properties = new HashMap<>();

        if (feed.getDataTransformation().getDatasourceIds() != null && feed.getDataTransformation().getDatasourceIds().size() == 1) {
            final Datasource datasource = datasourceProvider.getDatasource(datasourceProvider.resolve(feed.getDataTransformation().getDatasourceIds().get(0)));

            processorType = DATA_TRANSFORMATION_JDBC_DEFINITION;
            properties.put(JDBC_CONNECTION_KEY, datasource.getName());
            properties.put(JDBC_TABLE_KEY, feed.getSystemCategoryName() + "." + feed.getSystemFeedName());
        } else {
            processorType = DATA_TRANSFORMATION_HIVE_DEFINITION;
            properties.put(HIVE_SCHEMA_KEY, feed.getSystemCategoryName());
            properties.put(HIVE_TABLE_KEY, feed.getSystemFeedName());
        }

        // Create datasource
        final DatasourceDefinition datasourceDefinition = datasourceDefinitionProvider.findByProcessorType(processorType);
        if(datasourceDefinition != null) {
            final String identityString = propertyExpressionResolver.resolveVariables(datasourceDefinition.getIdentityString(), properties);
            final String title = datasourceDefinition.getTitle() != null ? propertyExpressionResolver.resolveVariables(datasourceDefinition.getTitle(), properties) : identityString;
            final String desc = propertyExpressionResolver.resolveVariables(datasourceDefinition.getDescription(), properties);

            if (processorType.equals(DATA_TRANSFORMATION_JDBC_DEFINITION)) {
                properties.putAll(parseDataTransformControllerServiceProperties(datasourceDefinition, properties.get(JDBC_CONNECTION_KEY)));
            }

            final DerivedDatasource datasource = datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc, new HashMap<>(properties));
            return Collections.singleton(datasource.getId());
        }
        else {
            return Collections.emptySet();
        }
    }

    /**
     * Builds the list of data sources for the specified data transformation feed.
     *
     * @param feed the feed
     * @return the list of data sources
     * @throws NullPointerException if the feed has no data transformation
     */
    @Nonnull
    private Set<Datasource.ID> ensureDataTransformationSourceDatasources(@Nonnull final FeedMetadata feed) {
        final Set<Datasource.ID> datasources = new HashSet<>();

        // Extract nodes in chart view model
        @SuppressWarnings("unchecked") final Stream<Map<String, Object>> nodes = Optional.ofNullable(feed.getDataTransformation().getChartViewModel())
            .map(model -> (List<Map<String, Object>>) model.get("nodes"))
            .map(Collection::stream)
            .orElse(Stream.empty());

        // Create a data source for each node
        final DatasourceDefinition hiveDefinition = datasourceDefinitionProvider.findByProcessorType(DATA_TRANSFORMATION_HIVE_DEFINITION);
        final DatasourceDefinition jdbcDefinition = datasourceDefinitionProvider.findByProcessorType(DATA_TRANSFORMATION_JDBC_DEFINITION);

        nodes.forEach(node -> {
            // Extract properties from node
            final DatasourceDefinition datasourceDefinition;
            final Map<String, String> properties = new HashMap<>();

            if (node.get("datasourceId") == null || node.get("datasourceId").equals("HIVE")) {
                final String name = (String) node.get("name");
                datasourceDefinition = hiveDefinition;
                properties.put(HIVE_SCHEMA_KEY, StringUtils.trim(StringUtils.substringBefore(name, ".")));
                properties.put(HIVE_TABLE_KEY, StringUtils.trim(StringUtils.substringAfterLast(name, ".")));
            } else {
                final Datasource datasource = datasourceProvider.getDatasource(datasourceProvider.resolve((String) node.get("datasourceId")));
                datasourceDefinition = jdbcDefinition;
                properties.put(JDBC_CONNECTION_KEY, datasource.getName());
                properties.put(JDBC_TABLE_KEY, (String) node.get("name"));
                properties.putAll(parseDataTransformControllerServiceProperties(datasourceDefinition,datasource.getName()));

            }
            if(datasourceDefinition != null) {
                // Create the derived data source
                final String identityString = propertyExpressionResolver.resolveVariables(datasourceDefinition.getIdentityString(), properties);
                final String title = datasourceDefinition.getTitle() != null ? propertyExpressionResolver.resolveVariables(datasourceDefinition.getTitle(), properties) : identityString;
                final String desc = propertyExpressionResolver.resolveVariables(datasourceDefinition.getDescription(), properties);

                final DerivedDatasource datasource = datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc, new HashMap<>(properties));
                datasources.add(datasource.getId());
            }
        });

        // Build the data sources from the data source ids
        final List<String> datasourceIds = Optional.ofNullable(feed.getDataTransformation()).map(FeedDataTransformation::getDatasourceIds).orElse(Collections.emptyList());
        datasourceIds.stream()
            .map(datasourceProvider::resolve)
            .forEach(datasources::add);

        return datasources;
    }

    /**
     * Indicates if the feed contains a data transformation.
     */
    private boolean isDataTransformation(@Nonnull final FeedMetadata feedMetadata) {
        return feedMetadata.getDataTransformation() != null && StringUtils.isNotEmpty(feedMetadata.getDataTransformation().getDataTransformScript());
    }


    public Datasource.ID ensureDatasource(TemplateProcessorDatasourceDefinition definition, FeedMetadata feedMetadata, List<NifiProperty> allProperties) {
        return metadataAccess.commit(() -> {

            List<NifiProperty> propertiesToEvalulate = new ArrayList<NifiProperty>();

            //fetch the def
            DatasourceDefinition datasourceDefinition = datasourceDefinitionProvider.findByProcessorType(definition.getProcessorType());
            if (datasourceDefinition != null) {

                //find out if there are any saved properties on the Feed that match the datasourceDef
                List<NifiProperty> feedProperties = feedMetadata.getProperties().stream().filter(
                    property -> matchesDefinition(definition, property) && datasourceDefinition.getDatasourcePropertyKeys().contains(property.getKey())).collect(
                    Collectors.toList());

                //resolve any ${metadata.} properties
                List<NifiProperty> resolvedFeedProperties = propertyExpressionResolver.resolvePropertyExpressions(feedProperties, feedMetadata);

                List<NifiProperty> resolvedAllProperties = propertyExpressionResolver.resolvePropertyExpressions(allProperties, feedMetadata);

                //propetyHash
                propertiesToEvalulate.addAll(feedProperties);
                propertiesToEvalulate.addAll(allProperties);

                propertyExpressionResolver.resolveStaticProperties(propertiesToEvalulate);

                String identityString = datasourceDefinition.getIdentityString();
                String desc = datasourceDefinition.getDescription();
                String title = datasourceDefinition.getTitle();

                PropertyExpressionResolver.ResolvedVariables identityStringPropertyResolution = propertyExpressionResolver.resolveVariables(identityString, propertiesToEvalulate);
                identityString = identityStringPropertyResolution.getResolvedString();

                PropertyExpressionResolver.ResolvedVariables titlePropertyResolution = propertyExpressionResolver.resolveVariables(title, propertiesToEvalulate);
                title = titlePropertyResolution.getResolvedString();

                if (desc != null) {
                    PropertyExpressionResolver.ResolvedVariables descriptionPropertyResolution = propertyExpressionResolver.resolveVariables(desc, propertiesToEvalulate);
                    desc = descriptionPropertyResolution.getResolvedString();
                }

                //if the identityString still contains unresolved variables then make the title readable and replace the idstring with the feed.id
                if (propertyExpressionResolver.containsVariablesPatterns(identityString)) {
                    title = propertyExpressionResolver.replaceAll(title, " {runtime variable} ");
                    identityString = propertyExpressionResolver.replaceAll(identityString, feedMetadata.getId());
                }

                //find any datasource matching this DsName and identity String, if not create one
                //if it is the Source ensure the feed matches this ds
                if (isCreateDatasource(datasourceDefinition, feedMetadata)) {
                    Map<String, String> controllerServiceProperties = parseControllerServiceProperties(datasourceDefinition, feedProperties);
                    Map<String, Object> properties = new HashMap<String, Object>(identityStringPropertyResolution.getResolvedVariables());
                    properties.putAll(controllerServiceProperties);
                    DerivedDatasource
                        derivedDatasource =
                        datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc, properties);
                    if (derivedDatasource != null) {
                        if ("HiveDatasource".equals(derivedDatasource.getDatasourceType())
                            && Optional.ofNullable(feedMetadata.getTable()).map(TableSetup::getTableSchema).map(TableSchema::getFields).isPresent()) {
                            derivedDatasource.setGenericProperties(Collections.singletonMap("columns", (Serializable) feedMetadata.getTable().getTableSchema().getFields()));
                        }
                        return derivedDatasource.getId();
                    }
                }
                return null;


            } else {
                return null;
            }


        }, MetadataAccess.SERVICE);

    }


    private List<String> getFeedInputProcessorTypes(FeedMetadata feedMetadata) {
        List<String> types = new ArrayList<>();
        types.add(feedMetadata.getInputProcessorType());
        if (feedMetadata != null && "com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMark".equalsIgnoreCase(feedMetadata.getInputProcessorType())) {
            types.add("com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop");
            types.add("com.thinkbiganalytics.nifi.v2.ingest.GetTableData");
        }
        return types;
    }

    /**
     * Create Datasources for all DESTINATIONS and only if the SOURCE matches the assigned source for this feed.
     */
    private boolean isCreateDatasource(DatasourceDefinition datasourceDefinition, FeedMetadata feedMetadata) {
        return DatasourceDefinition.ConnectionType.DESTINATION.equals(datasourceDefinition.getConnectionType()) ||
               (DatasourceDefinition.ConnectionType.SOURCE.equals(datasourceDefinition.getConnectionType()) && (
                   getFeedInputProcessorTypes(feedMetadata).contains(datasourceDefinition.getProcessorType())));
    }

    private Map<String, String> parseDataTransformControllerServiceProperties(DatasourceDefinition datasourceDefinition, String controllerServiceName) {
        Map<String, String> properties = new HashMap<>();
        if(datasourceDefinition != null) {
            try {
                if (StringUtils.isNotBlank(controllerServiceName)) {
                    //{Source Database Connection:Database Connection URL}
                    List<String>
                        controllerServiceProperties =
                        datasourceDefinition.getDatasourcePropertyKeys().stream().filter(k -> k.matches("\\{" + JDBC_CONNECTION_KEY + ":(.*)\\}")).collect(Collectors.toList());
                    List<String> serviceProperties = new ArrayList<>();
                    controllerServiceProperties.stream().forEach(p -> {
                        String property = p.substring(StringUtils.indexOf(p, ":") + 1, p.length() - 1);
                        serviceProperties.add(property);
                    });
                    ControllerServiceDTO csDto = nifiControllerServiceProperties.getControllerServiceByName(controllerServiceName);
                    if (csDto != null) {

                        serviceProperties.stream().forEach(p -> {

                            if (csDto != null) {
                                String value = csDto.getProperties().get(p);
                                if (value != null) {
                                    properties.put(p, value);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                log.warn("An error occurred trying to parse controller service properties for data transformation when deriving the datasource for {}, {}. {} ",
                         datasourceDefinition.getDatasourceType(),
                         datasourceDefinition.getConnectionType(), e.getMessage(), e);
            }
        }

        return properties;
    }

    /**
     * Parse the defintion metadata for the {propertyKey:CS Property Key} objects and pick out the values in the controller service
     *
     * @param datasourceDefinition the definition to use
     * @param feedProperties       the feed properties that match this definition
     * @return a Map of the Controller Service Property Key, Value
     */
    private Map<String, String> parseControllerServiceProperties(DatasourceDefinition datasourceDefinition, List<NifiProperty> feedProperties) {
        Map<String, String> properties = new HashMap<>();
        try {
            //{Source Database Connection:Database Connection URL}
            List<String> controllerServiceProperties = datasourceDefinition.getDatasourcePropertyKeys().stream().filter(k -> k.matches("\\{(.*):(.*)\\}")).collect(Collectors.toList());
            Map<String, List<String>> serviceProperties = new HashMap<>();
            controllerServiceProperties.stream().forEach(p -> {
                String service = p.substring(1, StringUtils.indexOf(p, ":"));
                String property = p.substring(StringUtils.indexOf(p, ":") + 1, p.length() - 1);
                if (!serviceProperties.containsKey(service)) {
                    serviceProperties.put(service, new ArrayList<>());
                }
                serviceProperties.get(service).add(property);
            });

            serviceProperties.entrySet().stream().forEach(e -> {

                String service = e.getKey();
                String controllerServiceId = feedProperties.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getValue())
                                 && p.getPropertyDescriptor() != null
                                 && p.getPropertyDescriptor().getName().equalsIgnoreCase(service)
                                 && StringUtils.isNotBlank(p.getPropertyDescriptor().getIdentifiesControllerService())).map(p -> p.getValue()).findFirst().orElse(null);
                if (controllerServiceId != null) {
                    ControllerServiceDTO csDto = nifiControllerServiceProperties.getControllerServiceById(controllerServiceId);
                    if(csDto != null) {
                        e.getValue().stream().forEach(propertyKey -> {
                            String value = csDto.getProperties().get(propertyKey);
                            if (value != null) {
                                properties.put(propertyKey, value);
                            }
                        });
                    }

                }

            });
        } catch (Exception e) {
            log.warn("An error occurred trying to parse controller service properties when deriving the datasource for {}, {}. {} ", datasourceDefinition.getDatasourceType(),
                     datasourceDefinition.getConnectionType(), e.getMessage(), e);
        }

        return properties;
    }

    /**
     * Indicates if there are no definitions with the specified connection type in the collection.
     */
    private boolean noneMatch(@Nonnull final Collection<TemplateProcessorDatasourceDefinition> definitions, @Nonnull final DatasourceDefinition.ConnectionType connectionType) {
        return definitions.stream()
            .map(definition -> datasourceDefinitionProvider.findByProcessorType(definition.getProcessorType()))
            .map(DatasourceDefinition::getConnectionType)
            .noneMatch(connectionType::equals);
    }
}
