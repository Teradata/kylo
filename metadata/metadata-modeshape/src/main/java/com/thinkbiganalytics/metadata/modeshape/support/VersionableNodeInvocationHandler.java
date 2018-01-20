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
import javax.jcr.RepositoryException;
import javax.jcr.Value;

public class VersionableNodeInvocationHandler implements InvocationHandler {

    private static final Set<String> PARENT_CHILD = Sets.newHashSet("getParent", "addNode", "getNode");
    private static final Set<String> NODE_ITERATOR = Sets.newHashSet("getNodes", "merge", "getSharedSet");

    private final Node versionable;

    public VersionableNodeInvocationHandler(Node node) {
        this.versionable = node;
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
                    ensureCheckout();
                } else {
                    // If the setter value has not changed then do nothing and return immediately.
                    return null;
                }
            } else if (methodName.startsWith("add") || methodName.equals("remove")) {
                ensureCheckout();
            }

            if (PARENT_CHILD.contains(methodName)) {
                return JcrVersionUtil.createAutoCheckoutProxy((Node) method.invoke(this.versionable, args));
            } else if (NODE_ITERATOR.contains(methodName)) {
                return createNodeIterator((NodeIterator) method.invoke(this.versionable, args));
            } else if (methodName.equals("getProperties")) {
                return createPropertyIterator((PropertyIterator) method.invoke(this.versionable, args));
            } else if (methodName.equals("getProperty")) {
                return createProperty((Property) method.invoke(this.versionable, args));
            } else {
                return method.invoke(this.versionable, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private boolean isValueChange(String methodName, Object[] args) throws RepositoryException {
        if (methodName.equals("setProperty")) {
            if (this.versionable.hasProperty((String) args[0])) {
                Property prop = this.versionable.getProperty((String) args[0]);
                if (prop.isMultiple()) {
                    Value[] values = prop.getValues();
                    Object[] propValues = Arrays.stream(values).map(v -> {
                        try {
                            return JcrPropertyUtil.asValue(v, JcrMetadataAccess.getActiveSession());
                        } catch (AccessDeniedException e) {
                            throw new AccessControlException("Unauthorized to access property: " + args[0]);
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

    private void ensureCheckout() {
        JcrVersionUtil.ensureCheckoutNode(this.versionable);
    }


    private NodeIterator createNodeIterator(final NodeIterator itr) {
        return (NodeIterator) Proxy.newProxyInstance(NodeIterator.class.getClassLoader(), new Class<?>[]{NodeIterator.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    if (method.getName().equals("nextNode")) {
                        return JcrVersionUtil.createAutoCheckoutProxy(itr.nextNode());
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
                        ensureCheckout();
                    }

                    return method.invoke(prop, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        });
    }
}
