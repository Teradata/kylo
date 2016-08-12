/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.support;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

/**
 *
 * @author Sean Felten
 */
public interface JcrObjectTypeResolver<T extends JcrObject> {

    Class<? extends T> resolve(Node node);
}
