package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.files.JpaDirectoryDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.files.JpaFileList;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Property Reference Doc
 * https://nifi.apache.org/docs/nifi-docs/components/org.apache.nifi.processors.standard.GetFile/index.html
 *
 */
@NifiFeedSourceProcessor(nifiProcessorType = "org.apache.nifi.processors.standard.GetFile")
public class GetFileToDatasource extends  AbstractNifiProcessorToFeedSource{

    public GetFileToDatasource(FeedMetadata feedMetadata) {
        super(feedMetadata);
    }

    public Datasource transform(){
        String inputDirectory = getSourceProperty("Input Directory").getValue();
        String fileFilter = getSourceProperty("File Filter").getValue();
        String path = inputDirectory+"/"+fileFilter;
        JpaDirectoryDatasource directoryDatasource = new JpaDirectoryDatasource(this.metadata.getSystemFeedName()+"-"+getNifiProcessorType(),"GetFile datasource",Paths.get(inputDirectory));
       return directoryDatasource;
    }

}
