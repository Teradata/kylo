/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleEntity  extends JcrEntity implements ExtensibleEntity {


    /**
     * 
     */
    public JcrExtensibleEntity(Node node) {

        super(node);
    }

}
