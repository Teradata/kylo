/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.model;

import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 *
 */
@Component
public class CatalogModelTransform {

    /**
     * 
     */
    public CatalogModelTransform() {
        super();
    }

    public Function<com.thinkbiganalytics.metadata.api.catalog.Connector, Connector> connectorToRestModel() {
        return (domain) -> {
            com.thinkbiganalytics.kylo.catalog.rest.model.Connector model = new com.thinkbiganalytics.kylo.catalog.rest.model.Connector();
            model.setId(domain.getId().toString());
            model.setTitle(domain.getTitle());
            model.setDescription(domain.getDescription());
            model.setPluginId(domain.getPluginId());
            model.setIcon(domain.getIcon());
            model.setColor(domain.getColor());
            model.setTemplate(sparkParamsToRestModel().apply(domain.getSparkParameters()));
            return model;
        };
    }

    /**
     * @return
     */
    private Function<DataSetSparkParameters, DataSetTemplate> sparkParamsToRestModel() {
        return (domain) -> {
            DefaultDataSetTemplate model = new DefaultDataSetTemplate();
            model.setFormat(domain.getFormat());
            model.setPaths(domain.getPaths());
            model.setFiles(domain.getFiles());
            model.setJars(domain.getJars());
            model.setOptions(domain.getOptions());
            return model;
        };
    }
    
    public void updateDataSet(DataSet model, com.thinkbiganalytics.metadata.api.catalog.DataSet domain) {
        domain.setDescription(generateDescription(model));
        domain.setTitle(generateTitle(model));
    }
    
    public BiConsumer<DataSet, com.thinkbiganalytics.metadata.api.catalog.DataSet> updateDataSet() {
        return (model, domain) -> {
            domain.setDescription(generateDescription(model));
            domain.setTitle(generateTitle(model));
        };
    }

    /**
     * @param dataSet
     * @return
     */
    public UnaryOperator<com.thinkbiganalytics.metadata.api.catalog.DataSet> updateWithRestModel(DataSet dataSet) {
        return (domainDs) -> {
            domainDs.setDescription(generateDescription(dataSet));
            domainDs.setTitle(generateTitle(dataSet));
            return domainDs;
        };
    }
    
    /**
     * @param dataSet
     * @return
     */
    public String generateSystemName(DataSet dataSet) {
        return generateTitle(dataSet).replaceAll("\\s+", "_");
    }

    /**
     * @param dataSet
     * @return
     */
    public String generateTitle(DataSet dataSet) {
        return dataSet.getDataSource().getTitle() + "-" + UUID.randomUUID();
    }

    /**
     * @param dataSet
     * @return
     */
    public String generateDescription(DataSet dataSet) {
        return "";
    }

    /**
     * @return
     */
    public Function<com.thinkbiganalytics.metadata.api.catalog.DataSet, DataSet> dataSetToRestModel() {
        return (domain) -> {
            DataSet dataSet = new DataSet();
            dataSet.setId(domain.getId().toString());
            dataSet.setDataSource(dataSourceToRestModel().apply(domain.getDataSource()));
            // TODO: add title and description
            DataSetTemplate template = sparkParamsToRestModel().apply(domain.getSparkParameters());
            dataSet.setFormat(template.getFormat());
            dataSet.setOptions(template.getOptions());
            dataSet.setPaths(template.getPaths());
            return dataSet;
        };
    }

    /**
     * @return
     */
    public Function<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> dataSourceToRestModel() {
        return (domain) -> {
            DataSource model = new DataSource();
            model.setId(domain.getId().toString());
            model.setTitle(domain.getTitle());
            model.setConnector(connectorToRestModel().apply(domain.getConnector()));
            model.setTemplate(sparkParamsToRestModel().apply(domain.getSparkParameters()));
            return model;
        };
    }
    
    /**
     * @return
     */
    public Converter<com.thinkbiganalytics.metadata.api.catalog.DataSource, DataSource> convertDataSourceToRestModel() {
        return (domain) -> dataSourceToRestModel().apply(domain);
    }
}
