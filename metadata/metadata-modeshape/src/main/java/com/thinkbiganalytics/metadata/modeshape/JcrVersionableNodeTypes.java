package com.thinkbiganalytics.metadata.modeshape;

import org.reflections.Reflections;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Look for all classes that have the JcrVersionable annotation and then add their nodeType() to the Set This is then used later when determining if the Node should be checked in/checked out and
 * versioned by the JcrUtil.isVersionable() method
 *
 * @see com.thinkbiganalytics.metadata.modeshape.support.JcrUtil#isVersionable(Node)
 */
public class JcrVersionableNodeTypes {

    Set<String> versionableNodeTypes = new HashSet<>();

    private void setVersionableNodeTypes() {

        Set<Class<?>>
            jcrClasses = new Reflections("com.thinkbiganalytics").getTypesAnnotatedWith(JcrVersionable.class);
        for (Class c : jcrClasses) {
            JcrVersionable versionable = AnnotationUtils.findAnnotation(c, JcrVersionable.class);
            String nodeType = versionable.nodeType();
            versionableNodeTypes.add(nodeType);
        }
    }

    @PostConstruct
    public void init() {
        setVersionableNodeTypes();
    }

    public Set<String> getVersionableNodeTypes() {
        return versionableNodeTypes;
    }

    public boolean isVersionable(String type) {
        return versionableNodeTypes.contains(type);
    }

    public boolean isVersionable(Node node) throws RepositoryException {
        String primaryType = null;
        primaryType = node.getPrimaryNodeType().getName();
        return isVersionable(primaryType);
    }
}
