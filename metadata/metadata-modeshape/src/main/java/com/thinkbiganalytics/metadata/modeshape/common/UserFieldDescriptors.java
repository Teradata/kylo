/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;

/**
 * Wraps a tba:userFieldDescriptors node and manages the field descriptors in it.
 */
public class UserFieldDescriptors extends JcrObject {

    public static final String NODE_TYPE = "tba:userFieldDescriptors";
    public static final String FIELD_DESCR_TYPE = "tba:fieldDescriptor";
    public static final String TITLE = "jcr:title";
    public static final String DESCRIPTION = "jcr:description";
    public static final String REQUIRED = "usr:mandatory";
    public static final String ORDER = "usr:order";
    
    public UserFieldDescriptors(Node node) {
        super(node);
    }

    public Set<UserFieldDescriptor> getFields() {
        return JcrUtil.getNodesOfType(getNode(), FIELD_DESCR_TYPE).stream()
                        .map(node -> JcrUtil.createJcrObject(node, FieldDescriptor.class))
                        .collect(Collectors.toSet());
    }
    
    public void setFields(Set<UserFieldDescriptor> fieldDescrs) {
        setFieldDescriptors(getNode(), fieldDescrs);;
    }

    private void setFieldDescriptors(Node fieldsNode, Set<UserFieldDescriptor> fieldDescrs) {
        //Remove any existing descriptors first before setting the new ones
        JcrUtil.getNodesOfType(fieldsNode, FIELD_DESCR_TYPE).forEach(node -> JcrUtil.removeNode(node));
        //add the new ones
        fieldDescrs.forEach(fieldDescr -> {
            Node descrNode = JcrUtil.getOrCreateNode(fieldsNode, fieldDescr.getSystemName(), FIELD_DESCR_TYPE);
            JcrPropertyUtil.setProperty(descrNode, TITLE, fieldDescr.getDisplayName());
            JcrPropertyUtil.setProperty(descrNode, DESCRIPTION, fieldDescr.getDescription());
            JcrPropertyUtil.setProperty(descrNode, REQUIRED, fieldDescr.isRequired());
            JcrPropertyUtil.setProperty(descrNode, ORDER, fieldDescr.getOrder());
        });
    }
    

    public static class FieldDescriptor extends JcrObject implements UserFieldDescriptor {

        public FieldDescriptor(Node node) {
            super(node);
        }

        @Override
        public String getSystemName() {
            return JcrUtil.getName(getNode());
        }

        @Override
        public String getDisplayName() {
            return JcrPropertyUtil.getProperty(getNode(), TITLE, null);
        }

        @Override
        public String getDescription() {
            return JcrPropertyUtil.getProperty(getNode(), DESCRIPTION, null);
        }

        @Override
        public boolean isRequired() {
            return JcrPropertyUtil.getProperty(getNode(), REQUIRED, null);
        }
        
        @Override
        public int getOrder() {
            return Math.toIntExact(JcrPropertyUtil.getProperty(getNode(), ORDER, 0L));
        }
    }

}
