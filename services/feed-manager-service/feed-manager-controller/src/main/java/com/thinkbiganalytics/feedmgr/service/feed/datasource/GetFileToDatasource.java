package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;

import javax.inject.Inject;

/**
 * Property Reference Doc
 * https://nifi.apache.org/docs/nifi-docs/components/org.apache.nifi.processors.standard.GetFile/index.html
 *
 */
@NifiFeedSourceProcessor(nifiProcessorType = "org.apache.nifi.processors.standard.GetFile")
public class GetFileToDatasource extends  AbstractNifiProcessorToFeedSource{

    @Inject
    DatasourceProvider datasourceProvider;

    public GetFileToDatasource(FeedMetadata feedMetadata) {
        super(feedMetadata);
    }

    public Datasource transform(){
        String inputDirectory = getSourceProperty("Input Directory").getValue();
        String fileFilter = getSourceProperty("File Filter").getValue();
        String path = inputDirectory+"/"+fileFilter;
     return null;
        //JpaDirectoryDatasource directoryDatasource = new JpaDirectoryDatasource(this.metadata.getSystemFeedName()+"-"+getNifiProcessorType(),"GetFile datasource",Paths.get(inputDirectory));
       //return directoryDatasource;
    }

}
