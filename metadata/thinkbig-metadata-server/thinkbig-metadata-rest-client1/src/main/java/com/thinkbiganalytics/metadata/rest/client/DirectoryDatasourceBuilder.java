/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;

/**
 *
 * @author Sean Felten
 */
public interface DirectoryDatasourceBuilder extends DatasourceBuilder<DirectoryDatasourceBuilder, DirectoryDatasource> {

    DirectoryDatasourceBuilder path(String path);
    DirectoryDatasourceBuilder regexPattern(String pattern);
    DirectoryDatasourceBuilder globPattern(String pattern);
}
