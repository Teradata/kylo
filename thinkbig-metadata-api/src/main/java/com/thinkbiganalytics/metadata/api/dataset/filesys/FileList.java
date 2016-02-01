/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset.filesys;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface FileList extends ChangedContent {

    List<Path> getFilePaths();
}
