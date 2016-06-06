/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.generic;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

/**
 *
 * @author Sean Felten
 */
public class JcrGenericEntity  extends JcrEntity implements GenericEntity {


    /**
     * 
     */
    public JcrGenericEntity(Node node) {

        super(node);
    }

}
