/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.datasource.directory;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.TableColumn;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.hive.JcrTableColumn;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPath;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrDirectoryDatasource  extends JcrDatasource implements DirectoryDatasource {

    private static final String PATH_NAME = "directory";

    // Masks field in superclass
    public static final String NODE_TYPE = "tba:directoryDatasource";

    private static final String PATH = "tba:directory";

    /**
     *
     */
    public JcrDirectoryDatasource(Node node) {
        super(node);
    }


    @Override
    public Path getDirectory() {
        String path = super.getProperty(PATH, String.class);
        if(StringUtils.isNotBlank(path)){
            return JcrPath.get(path);
        }
        return null;
    }

    public void setDirectory(Path path) {
        super.setProperty(PATH,path != null ? path.toString() : null);
    }

}
