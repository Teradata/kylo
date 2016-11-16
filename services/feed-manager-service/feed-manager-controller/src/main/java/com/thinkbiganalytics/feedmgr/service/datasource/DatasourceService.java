package com.thinkbiganalytics.feedmgr.service.datasource;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.feedmgr.rest.model.IconColor;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailability;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailabilityListener;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.spring.FileResourceService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 11/15/16.
 */
public class DatasourceService  {

    @Inject
    ServicesApplicationStartup startup;

    @Inject
    ModeShapeAvailability modeShapeAvailability;

    @Inject
    private DatasourceDefinitionProvider datasourceDefinitionProvider;

    @Inject
    FileResourceService fileResourceService;

    @Inject
    MetadataAccess metadataAccess;

    @PostConstruct
    private void init() {
        modeShapeAvailability.subscribe(new DatasourceLoadStartupListener());
    }



    public void loadDefinitionsFromFile() {


            String json = fileResourceService.getResourceAsString("classpath:/datasource-definitions.json");
            List<DatasourceDefinition> defs = null;
            if (StringUtils.isNotBlank(json)) {
                defs = Arrays.asList(ObjectMapperSerializer.deserialize(json, DatasourceDefinition[].class));
            }

            if(defs != null){
                final   List<DatasourceDefinition> datasourceDefinitions = defs;



                    metadataAccess.commit(() -> {
                        datasourceDefinitions.stream().forEach(def ->
                                              {
                                                  com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition domainDef = datasourceDefinitionProvider.ensureDatasourceDefinition(def.getProcessorType());
                                                  domainDef.setDatasourcePropertyKeys(def.getDatasourcePropertyKeys());
                                                  domainDef.setIdentityString(def.getIdentityString());
                                                  domainDef.setConnectionType(com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition.ConnectionType.valueOf(def.getConnectionType().name()));
                                                  domainDef.setProcessorType(def.getProcessorType());
                                                  domainDef.setDatasourceType(def.getDatasourceType());
                                                  domainDef.setDescription(def.getDescription());
                                                  domainDef.setTile(def.getTitle());
                                                  datasourceDefinitionProvider.update(domainDef);
                                              });
                        return null;
                    }, MetadataAccess.SERVICE);
             }


    }

    public Set<DatasourceDefinition> getDatasourceDefinitions(){
       return  metadataAccess.read(() -> {
          Set<com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition> datasourceDefinitions =  datasourceDefinitionProvider.getDatasourceDefinitions();
            if(datasourceDefinitions != null){
                return new HashSet<>(Collections2.transform(datasourceDefinitions, Model.DOMAIN_TO_DS_DEFINITION));
            }
            return null;
        }, MetadataAccess.SERVICE);
    }


    public class DatasourceLoadStartupListener implements ModeShapeAvailabilityListener {

        @Override
        public void modeShapeAvailable() {
            loadDefinitionsFromFile();
        }
    }


}
