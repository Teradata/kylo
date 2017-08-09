/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.support;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import com.google.common.collect.Sets;

/**
 *
 */
public class VersionableNodeInvocationHandler implements InvocationHandler {
    
    private static final Set<String> CHILD_NODE = Sets.newHashSet("getParent", "addNode", "getNode");
    private static final Set<String> NODE_ITERATOR = Sets.newHashSet("getNodes", "merge", "getSharedSet");

    private final Node versionable;
    
    /**
     * @param versionable
     */
    public VersionableNodeInvocationHandler(Node node) {
        this.versionable = node;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (CHILD_NODE.contains(method.getName())) {
            return JcrVersionUtil.createAutoCheckoutProxy((Node) method.invoke(this.versionable, args));
        } else if (NODE_ITERATOR.contains(method.getName())) {
            return createNodeIterator((NodeIterator) method.invoke(this.versionable, args));
        } else if (method.getName().equals("getProperties")) {
            return createPropertyIterator((PropertyIterator) method.invoke(this.versionable, args));
        } else if (method.getName().equals("getProperty")) {
            return createProperty((Property) method.invoke(this.versionable, args));
        } else if (method.getName().startsWith("set")) {
            ensureCheckout();
        }
        
        return method.invoke(this.versionable, args);
    }
    
    private void ensureCheckout() {
        JcrVersionUtil.ensureCheckoutNode(this.versionable);
    }

    
    private NodeIterator createNodeIterator(final NodeIterator itr) {
        return (NodeIterator) Proxy.newProxyInstance(NodeIterator.class.getClassLoader(), new Class<?>[] { NodeIterator.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("nextNode")) {
                    return JcrVersionUtil.createAutoCheckoutProxy(itr.nextNode());
                } else {
                    return method.invoke(itr, args);
                }
            }
        });
    }
    
    private PropertyIterator createPropertyIterator(final PropertyIterator itr) {
        return (PropertyIterator) Proxy.newProxyInstance(PropertyIterator.class.getClassLoader(), new Class<?>[] { PropertyIterator.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("nextProperty")) {
                    return createProperty(itr.nextProperty());
                } else {
                    return method.invoke(itr, args);
                }
            }
        });
    }
    
    private Property createProperty(final Property prop) {
        return (Property) Proxy.newProxyInstance(Property.class.getClassLoader(), new Class<?>[] { Property.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("setValue")) {
                    ensureCheckout();
                }
                
                return method.invoke(prop, args);
            }
        });
    }
}
