/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity.ID;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 *
 */
public class JcrExtensibleEntityProvider implements ExtensibleEntityProvider {

    @Inject
    ExtensibleTypeProvider typeProvider;

    @Override
    public ExtensibleEntity createEntity(ExtensibleType type, Map<String, Object> props) {
        JcrExtensibleType typeImpl = (JcrExtensibleType) type;
        Session session = getSession();

        try {
            String path = EntityUtil.pathForExtensibleEntity();
            Node entitiesNode = session.getNode(path);
            Node typesNode = null;

            if (entitiesNode.hasNode(typeImpl.getJcrName())) {
                typesNode = entitiesNode.getNode(typeImpl.getJcrName());
            } else {
                typesNode = entitiesNode.addNode(typeImpl.getJcrName(), "nt:folder");
            }

            Node entNode = typesNode.addNode(typeImpl.getName() + "-" + UUID.randomUUID().toString(), typeImpl.getJcrName());

            Map<String, Object> reassignedProps = mapCollectionProperties(typeImpl, props);

            entNode = JcrPropertyUtil.setProperties(session, entNode, reassignedProps);

            return new JcrExtensibleEntity(entNode);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new extensible entity of type: " + typeImpl.getJcrName(), e);
        }
    }


    /**
     * If the incoming set of props contains a collection it needs to be converted into an object that knows the correct JCRType for that collection
     */
    private Map<String, Object> mapCollectionProperties(JcrExtensibleType typeImpl, Map<String, Object> props) {
        //remap any Collections into JcrExtensiblePropertyCollection
        Map<String, Object> reassignedProps = new HashMap<>();
        if (props != null) {

            for (Map.Entry<String, Object> prop : props.entrySet()) {
                String key = prop.getKey();
                Object v = prop.getValue();
                if (v instanceof Collection) {
                    int typeCode = ((JcrExtensibleTypeProvider) typeProvider).asCode(typeImpl.getFieldDescriptor(key).getType());
                    JcrExtensiblePropertyCollection collection = new JcrExtensiblePropertyCollection(typeCode, (Collection) v);
                    reassignedProps.put(key, collection);
                } else {
                    reassignedProps.put(key, v);
                }
            }
        }
        return reassignedProps;
    }

    public ExtensibleEntity updateEntity(ExtensibleEntity extensibleEntity, Map<String, Object> props) {
        JcrExtensibleEntity jcrExtensibleEntity = (JcrExtensibleEntity) extensibleEntity;
        JcrExtensibleType typeImpl = (JcrExtensibleType) typeProvider.getType(jcrExtensibleEntity.getTypeName());
        Map<String, Object> reassignedProps = mapCollectionProperties(typeImpl, props);
        JcrPropertyUtil.setProperties(getSession(), jcrExtensibleEntity.getNode(), reassignedProps);
        return extensibleEntity;
    }


    @Override
    public List<ExtensibleEntity> getEntities() {
        List<ExtensibleEntity> list = new ArrayList<>();
        Session session = getSession();
        try {
            String path = EntityUtil.pathForExtensibleEntity();
            Node genericsNode = session.getNode(path);
            NodeIterator typeNameItr = genericsNode.getNodes();
            while (typeNameItr.hasNext()) {
                Node typeNameNode = (Node) typeNameItr.next();
                NodeIterator entityItr = typeNameNode.getNodes();

                while (entityItr.hasNext()) {
                    Node entNode = (Node) entityItr.next();
                    list.add(new JcrExtensibleEntity(entNode));
                }
            }

            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve list of extensible entities", e);
        }
    }


    public List<ExtensibleEntity> getEntities(String typeName) {
        List<ExtensibleEntity> list = new ArrayList<>();
        Session session = getSession();

        try {
            String path = EntityUtil.pathForExtensibleEntity(typeName);
            Node typeNameNode = session.getNode(path);
            NodeIterator entityItr = typeNameNode.getNodes();
            while (entityItr.hasNext()) {
                Node entNode = (Node) entityItr.next();
                list.add(new JcrExtensibleEntity(entNode));
            }

            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve list of extensible entities", e);
        }
    }

    /**
     * Return a list of the ExtensibleEntity objects that match a given ExtensibleEntity property and value
     * restricting to a specific jcr extension type
     */
    public List<? extends ExtensibleEntity> findEntitiesMatchingProperty(String typeName, String propName, Object value) {
        String path = EntityUtil.pathForExtensibleEntity(typeName);
        HashMap<String, String> params = new HashMap<>();
        String query = "SELECT * FROM [" + typeName + "] as t WHERE t.[" + propName + "] = $v";
        params.put("v", value.toString());

        QueryResult result = null;
        try {
            result = JcrQueryUtil.query(getSession(), query, params);
            List<JcrExtensibleEntity> entities = JcrQueryUtil.queryResultToList(result, JcrExtensibleEntity.class);
            return entities;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to execute Criteria Query for " + typeName + ".  Query is: " + query, e);
        }
    }


    @Override
    public ExtensibleEntity getEntity(ID id) {
        JcrExtensibleEntity.EntityId idImpl = (JcrExtensibleEntity.EntityId) id;

        try {
            Session session = getSession();
            Node node = session.getNodeByIdentifier(idImpl.getIdValue());

            if (node != null) {
                return new JcrExtensibleEntity(node);
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding entity by ID: " + idImpl.getIdValue(), e);
        }
    }

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

}
