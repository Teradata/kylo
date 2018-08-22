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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.jcr.Node;

public class LockableNodeInvocationHandler extends NodeModificationInvocationHandler {
    
    private final boolean deep;

    public LockableNodeInvocationHandler(Node node, Class<?>[] types) {
        this(node, types, false);
    }
    
    public LockableNodeInvocationHandler(Node node, Class<?>[] types, boolean isDeep) {
        super(node, types);
        this.deep = isDeep;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.support.NodeModificationInvocationHandler#beforeUpdate(java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    protected void beforeUpdate(Method method, Object[] args) {
        JcrLockingUtil.lock(getWrappedNode(), this.deep);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.support.NodeModificationInvocationHandler#createChildProxy(javax.jcr.Node)
     */
    @Override
    protected Node createChildProxy(Node node) {
        return (Node) Proxy.newProxyInstance(Node.class.getClassLoader(), getTypes(), new LockableNodeInvocationHandler(node, getTypes()));
    }
}
