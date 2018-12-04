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

import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * An InvocationHandler that may be used by proxies that wrap JCR nodes and react to changes made to that node
 * or any of its children. 
 */
public abstract class NodeModificationInvocationHandler implements InvocationHandler {

    private static final Set<String> PARENT_CHILD = Sets.newHashSet("getParent", "addNode", "getNode");
    private static final Set<String> NODE_ITERATOR = Sets.newHashSet("getNodes", "merge", "getSharedSet");

    private final Node wrappedNode;
    private final Class<?>[] types;

    public NodeModificationInvocationHandler(Node node, Class<?>[] types) {
        this.wrappedNode = node;
        this.types = Arrays.stream(types).toArray(Class<?>[]::new);
    }
    
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            String methodName = method.getName();
    
            if (methodName.startsWith("set")) {
                if (isValueChange(methodName, args)) {
                    // If the setter value has changed then ensure checkout.
                    beforeUpdate(method, args);
                    Object result = method.invoke(this.wrappedNode, dereference(args));  // An argument may be a node so dereference any proxy of one
                    return afterUpdate(method, args, result);
                } else {
                    // If the setter value has not changed then do nothing and return immediately.
                    return null;
                }
            } else if (methodName.startsWith("add") || methodName.equals("remove")) {
                beforeUpdate(method, args);
                Object result = method.invoke(this.wrappedNode, args);
                return afterUpdate(method, args, result);
            } else if (PARENT_CHILD.contains(methodName)) {
                return createChildProxy((Node) method.invoke(this.wrappedNode, args));
            } else if (NODE_ITERATOR.contains(methodName)) {
                return createNodeIterator((NodeIterator) method.invoke(this.wrappedNode, args));
            } else if (methodName.equals("getProperties")) {
                return createPropertyIterator((PropertyIterator) method.invoke(this.wrappedNode, args));
            } else if (methodName.equals("getProperty")) {
                return createProperty((Property) method.invoke(this.wrappedNode, args));
            } else if (methodName.equals("equals")) {
                return method.invoke(this.wrappedNode, dereference(args));  // Compare the dereferenced node
            } else if (methodName.equals("hashcode")) {
                return getWrappedNode().hashCode();
            } else {
                // Some boring ol' method
                return method.invoke(this.wrappedNode, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * @return the wrappedNode
     */
    protected Node getWrappedNode() {
        return wrappedNode;
    }
    
    /**
     * Invoked just before the specified method is executed.
     */
    protected void beforeUpdate(Method method, Object[] args) {
        // Do nothing by default
    }

    /**
     * Invoked with the result of executing the specified method.  This method
     * may return the supplied method result (the default) or an alternative value.
     */
    protected Object afterUpdate(Method method, Object[] args, Object object) {
        // Do nothing by default
        return object;
    }
    
    protected Class<?>[] getTypes() {
        return types;
    }

    protected Node createChildProxy(Node node) {
        return (Node) Proxy.newProxyInstance(Node.class.getClassLoader(), this.types, this);
    }

    protected Object[] dereference(Object[] args) {
        Object[] resultArgs = args;
       
        if (args.length > 0) {
            for (int idx = 0; idx < args.length; idx++) {
                if (args[idx] != null && Proxy.isProxyClass(args[idx].getClass())) {
                    InvocationHandler handler = Proxy.getInvocationHandler(args[idx]);

                    if (resultArgs == args) {
                        resultArgs = Arrays.copyOfRange(args, 0, args.length);
                    }

                    if (handler instanceof NodeModificationInvocationHandler) {
                        resultArgs[idx] = ((NodeModificationInvocationHandler) handler).getWrappedNode();
                    }
                }
            } 
        }
        return resultArgs;
    }

    private boolean isValueChange(String methodName, Object[] args) throws RepositoryException {
        if (methodName.equals("setProperty")) {
            if (this.wrappedNode.hasProperty((String) args[0])) {
                Property prop = this.wrappedNode.getProperty((String) args[0]);
                if (prop.isMultiple()) {
                    Value[] values = prop.getValues();
                    final int propertyType = prop.getType();
                    Object[] propValues = Arrays.stream(values).map(v -> {
                        if(propertyType == PropertyType.WEAKREFERENCE || propertyType == PropertyType.REFERENCE){
                            //just store the node id.  user might not have access to the entire collection
                            try {
                            return v.getString();
                            } catch (RepositoryException e) {
                                throw new MetadataRepositoryException("Failed to access property type", e);
                            }
                        }
                        else {
                            try {
                                return JcrPropertyUtil.asValue(v, JcrMetadataAccess.getActiveSession());
                            } catch (AccessDeniedException e) {
                                throw new AccessControlException("Unauthorized to access property: " + args[0]);
                            }
                        }
                    }).toArray();
                    Object[] argValues = (Object[]) args[1];
                    return !Arrays.equals(argValues, propValues);
                } else {
                    Object value = JcrPropertyUtil.asValue(prop);
                    return !Objects.equals(args[1], value);
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private NodeIterator createNodeIterator(final NodeIterator itr) {
        return (NodeIterator) Proxy.newProxyInstance(NodeIterator.class.getClassLoader(), new Class<?>[]{NodeIterator.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    if (method.getName().equals("nextNode")) {
                        return createChildProxy(itr.nextNode());
                    } else {
                        return method.invoke(itr, args);
                    }
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        });
    }

    private PropertyIterator createPropertyIterator(final PropertyIterator itr) {
        return (PropertyIterator) Proxy.newProxyInstance(PropertyIterator.class.getClassLoader(), new Class<?>[]{PropertyIterator.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    if (method.getName().equals("nextProperty")) {
                        return createProperty(itr.nextProperty());
                    } else {
                        return method.invoke(itr, args);
                    }
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        });
    }

    private Property createProperty(final Property prop) {
        return (Property) Proxy.newProxyInstance(Property.class.getClassLoader(), new Class<?>[]{Property.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    if (method.getName().equals("setValue")) {
                        beforeUpdate(method, args);
                        method.invoke(prop, args);
                        afterUpdate(method, args, null);
                        return null;
                    } else {
                        return method.invoke(prop, args);
                    }
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        });
    }
}
