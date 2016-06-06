/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.io.IOException;

import javax.jcr.Session;

import org.modeshape.jcr.api.JcrTools;
import org.springframework.core.io.Resource;

/**
 *
 * @author Sean Felten
 */
public class MetadataJcrConfigurer {

    public void initialize(Session session, Resource resource) {
        JcrTools tools = new JcrTools();
        
        tools.registerNodeTypes(session, resource.getFilename());
    }
}
