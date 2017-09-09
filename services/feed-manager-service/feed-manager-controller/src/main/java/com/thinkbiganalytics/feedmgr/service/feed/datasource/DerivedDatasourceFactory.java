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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Create and assign {@link DerivedDatasource} based upon a template or feed
 */
public class DerivedDatasourceFactory {

    private static String DATA_TRANSFORMATION_DEFINITION = "datatransformation.template";

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

    public void populateDatasources(FeedMetadata feedMetadata, RegisteredTemplate template, Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> sources,
                                    Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> dest) {

        if (isDataTransformation(feedMetadata)) {
            Set<Datasource.ID> ids = ensureDataTransformationSourceDatasources(feedMetadata);
            if (ids != null && !ids.isEmpty()) {
                sources.addAll(ids);
            }
            //ensure this feed gets set as a hive table dest
            DatasourceDefinition datasourceDefinition = datasourceDefinitionProvider.findByProcessorType(DATA_TRANSFORMATION_DEFINITION);
            String identityString = datasourceDefinition.getIdentityString();
            Map<String, String> props = new HashMap<String, String>();
            props.put("schema", feedMetadata.getSystemCategoryName());
            props.put("table", feedMetadata.getSystemFeedName());
            identityString = propertyExpressionResolver.resolveVariables(identityString, props);
            String desc = datasourceDefinition.getDescription();
            if (desc != null) {
                desc = propertyExpressionResolver.resolveVariables(desc, props);
            }
            String title = identityString;
            DerivedDatasource
                derivedDatasource =
                datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc,
                                                           new HashMap<String, Object>(props));
            if (derivedDatasource != null) {
                dest.add(derivedDatasource.getId());
            }

        }
        //see if its in the cache first
        List<RegisteredTemplate.Processor> processors = registeredTemplateCache.getProcessors(feedMetadata.getTemplateId());
        //if not add it
        if (processors == null) {
            processors = feedManagerTemplateService.getRegisteredTemplateProcessors(feedMetadata.getTemplateId(), true);
            registeredTemplateCache.putProcessors(feedMetadata.getTemplateId(), processors);
        }

        List<NifiProperty> allProperties = processors.stream().flatMap(processor -> processor.getProperties().stream()).collect(Collectors.toList());

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
     * Builds the list of data sources for the specified data transformation feed.
     *
     * @param feed the feed
     * @return the list of data sources
     */
    @Nonnull
    private Set<Datasource.ID> ensureDataTransformationSourceDatasources(@Nonnull final FeedMetadata feed) {
        // Build the data sources from the view model
        final Set<Datasource.ID> datasources = new HashSet<>();
        final Set<String> tableNames = Optional.ofNullable(feed.getDataTransformation()).map(FeedDataTransformation::getTableNamesFromViewModel).orElse(Collections.emptySet());

        if (!tableNames.isEmpty()) {
            DatasourceDefinition datasourceDefinition = datasourceDefinitionProvider.findByProcessorType(DATA_TRANSFORMATION_DEFINITION);
            if (datasourceDefinition != null) {
                tableNames.forEach(hiveTable -> {
                    String schema = StringUtils.trim(StringUtils.substringBefore(hiveTable, "."));
                    String table = StringUtils.trim(StringUtils.substringAfterLast(hiveTable, "."));
                    String identityString = datasourceDefinition.getIdentityString();
                    Map<String, String> props = new HashMap<String, String>();
                    props.put("schema", schema);
                    props.put("table", table);
                    identityString = propertyExpressionResolver.resolveVariables(identityString, props);
                    String desc = datasourceDefinition.getDescription();
                    if (desc != null) {
                        desc = propertyExpressionResolver.resolveVariables(desc, props);
                    }
                    String title = identityString;

                    DerivedDatasource
                        derivedDatasource =
                        datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc,
                                                                   new HashMap<String, Object>(props));
                    if (derivedDatasource != null) {
                        datasources.add(derivedDatasource.getId());
                    }
                });
            }
        }

        // Build the data sources from the data source ids
        final List<String> datasourceIds = Optional.ofNullable(feed.getDataTransformation()).map(FeedDataTransformation::getDatasourceIds).orElse(Collections.emptyList());
        datasourceIds.stream()
            .map(datasourceProvider::resolve)
            .forEach(datasources::add);

        return datasources;
    }

    public boolean isDataTransformation(FeedMetadata feedMetadata) {
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
                    DerivedDatasource
                        derivedDatasource =
                        datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc,
                                                                   new HashMap<String, Object>(identityStringPropertyResolution.getResolvedVariables()));
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
        if (feedMetadata.getInputProcessorType().equals("com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMark")) {
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


}
