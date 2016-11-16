package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateProcessorDatasourceDefinition;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 11/15/16.
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

    public void populateDatasources(FeedMetadata feedMetadata, RegisteredTemplate template, Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> sources,
                                    Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> dest) {

        if (isDataTransformation(feedMetadata)) {
            Set<Datasource.ID> ids = ensureDataTransformationSourceDatasources(feedMetadata);
            if (ids != null && !ids.isEmpty()) {
                sources.addAll(ids);
            }
        }

        template.getRegisteredDatasourceDefinitions().stream().forEach(definition -> {
            Datasource.ID id = ensureDatasource(definition, feedMetadata);
            if (id != null) {
                if (com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition.ConnectionType.SOURCE.equals(definition.getDatasourceDefinition().getConnectionType())) {
                    //ensure this is the selected one for the feed
                    if (template != null && template.getInputProcessors() != null && definition.getProcessorType().equalsIgnoreCase(feedMetadata.getInputProcessorType())) {
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

    private Set<Datasource.ID> ensureDataTransformationSourceDatasources(FeedMetadata feed) {
        Set<Datasource.ID> list = new HashSet<>();

        if (feed.getDataTransformation() != null && !feed.getDataTransformation().getTableNamesFromViewModel().isEmpty()) {

            DatasourceDefinition datasourceDefinition = datasourceDefinitionProvider.findByProcessorType(DATA_TRANSFORMATION_DEFINITION);
            if (datasourceDefinition != null) {
                Set<String> hiveTableSources = feed.getDataTransformation().getTableNamesFromViewModel();
                hiveTableSources.stream().forEach(hiveTable -> {
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
                        list.add(derivedDatasource.getId());
                    }
                });
            }

        }
        return list;

    }

    public boolean isDataTransformation(FeedMetadata feedMetadata) {
        return feedMetadata.getDataTransformation() != null && !feedMetadata.getDataTransformation().getTableNamesFromViewModel().isEmpty();
    }


    public Datasource.ID ensureDatasource(TemplateProcessorDatasourceDefinition definition, FeedMetadata feedMetadata) {
        return metadataAccess.commit(() -> {

            List<RegisteredTemplate.Processor> processors = feedManagerTemplateService.getRegisteredTemplateProcessors(feedMetadata.getTemplateId(), true);
            List<NifiProperty> allProperties = processors.stream().flatMap(processor -> processor.getProperties().stream()).collect(Collectors.toList());

            List<NifiProperty> propertiesToEvalulate = new ArrayList<NifiProperty>();

            //fetch the def
            DatasourceDefinition datasourceDefinition = datasourceDefinitionProvider.findByProcessorType(definition.getProcessorType());
            if (datasourceDefinition != null) {

                datasourceDefinition.getDatasourcePropertyKeys().size();

                //find out if there are any saved properties on the Feed that match the datasourceDef
                List<NifiProperty> feedProperties = feedMetadata.getProperties().stream().filter(
                    property -> matchesDefinition(definition, property) && datasourceDefinition.getDatasourcePropertyKeys().contains(property.getKey())).collect(
                    Collectors.toList());
                Set<String>
                    matchingFeedProperties = feedProperties.stream().map(p -> p.getKey()).collect(Collectors.toSet());

                //template Props
                List<NifiProperty> templateProperties = allProperties.stream().filter(
                    property -> !matchingFeedProperties.contains(property.getKey()) && matchesDefinition(definition, property) && datasourceDefinition.getDatasourcePropertyKeys()
                        .contains(property.getKey())).collect(Collectors.toList());

                //resolve any ${metadata.} properties
                List<NifiProperty> resolvedFeedProperties = propertyExpressionResolver.resolvePropertyExpressions(feedProperties, feedMetadata);

                //  List<NifiProperty> resolvedTemplateProperties = propertyExpressionResolver.resolvePropertyExpressions(templateProperties, feedMetadata);

                List<NifiProperty> resolvedAllProperties = propertyExpressionResolver.resolvePropertyExpressions(allProperties, feedMetadata);

                //propetyHash
                //List<NifiProperty> allProperties = new ArrayList<NifiProperty>();
                propertiesToEvalulate.addAll(feedProperties);
                propertiesToEvalulate.addAll(allProperties);
                //remove any properties in the templateProperties that match the property from the feed
                //  allProperties.addAll(templateProperties);
                propertyExpressionResolver.resolveStaticProperties(propertiesToEvalulate);

                //if properties contain expressions then mark this as unique prop relative to this feed and processorType

                //build the identitystring
                String identityString = datasourceDefinition.getIdentityString();
                String desc = datasourceDefinition.getDescription();

                //for each ${}
                //find the property that has a key == to that
                // find that properties value
                PropertyExpressionResolver.ResolvedVariables identityStringPropertyResolution = propertyExpressionResolver.resolveVariables(identityString, propertiesToEvalulate);
                identityString = identityStringPropertyResolution.getResolvedString();
                if (desc != null) {
                    PropertyExpressionResolver.ResolvedVariables descriptionPropertyResolution = propertyExpressionResolver.resolveVariables(desc, propertiesToEvalulate);
                    desc = descriptionPropertyResolution.getResolvedString();
                }
                String title = identityString;
                //if the identityString still contains unresolved variables then make the title readable and replace the idstring with the feed.id
                if (propertyExpressionResolver.containsVariablesPatterns(identityString)) {
                    title = propertyExpressionResolver.replaceAll(title, " {runtime variable} ");
                    identityString = propertyExpressionResolver.replaceAll(identityString, feedMetadata.getId());
                }

                NifiProperty feedProp = allProperties.stream().filter(p -> p.getKey().equals("feed")).findFirst().orElse(null);

                //find any datasource matching this DsName and identity String, if not create one
                //if it is the Source ensure the feed matches this ds
                if (isCreateDatasource(datasourceDefinition, feedMetadata)) {
                    DerivedDatasource
                        derivedDatasource =
                        datasourceProvider.ensureDerivedDatasource(datasourceDefinition.getDatasourceType(), identityString, title, desc,
                                                                   new HashMap<String, Object>(identityStringPropertyResolution.getResolvedVariables()));
                    if (derivedDatasource != null) {
                        return derivedDatasource.getId();
                    }
                }
                return null;


            } else {
                return null;
            }


        }, MetadataAccess.SERVICE);


    }


    /**
     * Create Datasources for all DESTINATIONS and only if the SOURCE matches the assigned source for this feed.
     */
    private boolean isCreateDatasource(DatasourceDefinition datasourceDefinition, FeedMetadata feedMetadata) {
        return DatasourceDefinition.ConnectionType.DESTINATION.equals(datasourceDefinition.getConnectionType()) ||
               (DatasourceDefinition.ConnectionType.SOURCE.equals(datasourceDefinition.getConnectionType()) && (
                   datasourceDefinition.getProcessorType().equalsIgnoreCase(feedMetadata.getInputProcessorType())));
    }


}
