/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource.filesys;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface DirectoryDatasource extends Datasource {

    Path getDirectory();
}
