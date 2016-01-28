/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset.filesys;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface DirectoryDataset extends Dataset {

    Path getDirectory();
}
