/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.support;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

/**
 * Resolves which type of JcrObject should be used for the given node.  Used for 
 * polymorphic instantiation.
 * 
 * @author Sean Felten
 */
public interface JcrObjectTypeResolver<T extends JcrObject> {

    Class<? extends T> resolve(Node node);
}
