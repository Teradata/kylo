/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;

/**
 * Wraps a tba:userFieldDescriptors node and manages the field descriptors in it.
 */
public class UserFieldDescriptors extends JcrObject {

    public static final String FIELD_DESCR_TYPE = "tba:fieldDescriptor";
    public static final String TITLE = "jcr:title";
    public static final String DESCRIPTION = "jcr:description";
    public static final String REQUIRED = "usr:mandatory";
    public static final String ORDER = "usr:order";
    
    public UserFieldDescriptors(Node node) {
        super(node);
    }

    public Set<UserFieldDescriptor> getFields() {
        return JcrUtil.getChildrenMatchingNodeType(getNode(), FIELD_DESCR_TYPE, FieldDescriptor.class).stream().collect(Collectors.toSet());
    }
    
    public void setFields(Set<UserFieldDescriptor> fieldDescrs) {
        setFieldDesriptors(getNode(), fieldDescrs);;
    }

    private void setFieldDesriptors(Node fieldsNode, Set<UserFieldDescriptor> fieldDescrs) {
        fieldDescrs.forEach(fieldDescr -> {
            Node descrNode = JcrUtil.getOrCreateNode(fieldsNode, fieldDescr.getSystemName(), FIELD_DESCR_TYPE);
            JcrPropertyUtil.setProperty(descrNode, TITLE, fieldDescr.getDisplayName());
            JcrPropertyUtil.setProperty(descrNode, DESCRIPTION, fieldDescr.getDescription());
            JcrPropertyUtil.setProperty(descrNode, REQUIRED, fieldDescr.isRequired());
            JcrPropertyUtil.setProperty(descrNode, ORDER, fieldDescr.getOrder());
        });
    }
    

    private static class FieldDescriptor extends JcrObject implements UserFieldDescriptor {

        public FieldDescriptor(Node node) {
            super(node);
        }

        @Override
        public String getSystemName() {
            return JcrUtil.getName(getNode());
        }

        @Override
        public String getDisplayName() {
            return JcrPropertyUtil.getProperty(getNode(), TITLE);
        }

        @Override
        public String getDescription() {
            return JcrPropertyUtil.getProperty(getNode(), DESCRIPTION);
        }

        @Override
        public boolean isRequired() {
            return JcrPropertyUtil.getProperty(getNode(), REQUIRED);
        }
        
        @Override
        public int getOrder() {
            return JcrPropertyUtil.getProperty(getNode(), ORDER);
        }
    }

}
