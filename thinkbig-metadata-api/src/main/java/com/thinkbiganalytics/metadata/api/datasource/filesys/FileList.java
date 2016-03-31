/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource.filesys;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public interface FileList extends ChangeSet {

    List<Path> getFilePaths();
}
