/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.datasource.hive;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.datasource.hive.TableColumn;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

/**
 *
 * @author Sean Felten
 */
public class JcrTableColumn extends JcrObject implements TableColumn {

    private static final String NAME = "tba:name";
    private static final String TYPE = "tba:type";

    /**
     * @param node
     */
    public JcrTableColumn(Node node) {
        super(node);
    }

    @Override
    public String getName() {
        return super.getProperty(NAME, String.class);
    }

    @Override
    public String getType() {
        return super.getProperty(TYPE, String.class);
    }

}
