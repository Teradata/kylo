/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.metadata.api.versioning.EntityDraftVersionProvider;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.api.versioning.NoDraftVersionException;
import com.thinkbiganalytics.metadata.api.versioning.VersionAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion.ID;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityDraftVersion;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityVersion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.version.Version;

/**
 * A mixin interface to be implemented by any JCR entity provider that supports versionable entities that 
 * may have draft versions.
 */
public interface DraftVersionProviderMixin<T, PK extends Serializable> extends VersionProviderMixin<T, PK>, EntityDraftVersionProvider<T, PK> {

    
    @Override
    default Optional<List<EntityVersion<T>>> findVersions(PK entityId, boolean includeContent) {
        BiFunction<EntityVersion<T>,EntityVersion<T>,Integer> desc = (v1, v2) -> v2.getCreatedDate().compareTo(v1.getCreatedDate());
        
        return findVersionableNode(entityId)
                        .map(node -> JcrVersionUtil.getVersions(node).stream()
                                        .map(ver -> new JcrEntityVersion<>(ver, includeContent ? asEntity(entityId, JcrVersionUtil.getFrozenNode(ver)) : null))
                                        .sorted(desc::apply)
                                        .collect(Collectors.toList()));
//        return findVersionableNode(entityId)
//                .map(node -> {
//                    List<EntityVersion<T>> result = new ArrayList<>();
//                    if (JcrVersionUtil.isCheckedOut(node)) {
//                        result.add(new JcrEntityDraftVersion<>(node, includeContent ? asEntity(entityId, node) : null));
//                    }
//                    VersionProviderMixin.super.findVersions(entityId, includeContent).ifPresent(result::addAll);
//                    return result;
//                });
    }

    @Override
    default Optional<EntityVersion<T>> findLatestVersion(PK entityId, boolean includeContent) {
        // TODO Auto-generated method stub
        return VersionProviderMixin.super.findLatestVersion(entityId, includeContent);
    }

    @Override
    default boolean hasDraftVersion(PK entityId) {
        return findLatestVersion(entityId, false).map(ver -> ver.getName().equals("draft")).orElse(false);
    }

    @Override
    default EntityVersion<T> createDraftVersion(PK entityId, boolean includeContent) {
        Node versionable = createDraftEntity(entityId);
        T entity = includeContent ? asEntity(entityId, versionable) : null;
        return new JcrEntityDraftVersion<>(versionable, entity);
    }

    @Override
    default EntityVersion<T> createDraftVersion(PK entityId, ID versionId, boolean includeContent) {
        Node versionable = createDraftEntity(entityId, versionId);
        T entity = includeContent ? asEntity(entityId, versionable) : null;
        return new JcrEntityDraftVersion<>(versionable, entity);
    }

    @Override
    default EntityVersion<T> createVersion(PK entityId, boolean includeContent) {
        Version version = createVersionedEntity(entityId);
        T entity = includeContent ? asEntity(entityId, JcrVersionUtil.getFrozenNode(version)) : null;
        return new JcrEntityVersion<>(version, entity);
    }
    
    /**
     * Implementors should create a draft version of the entity and return the node
     * that is the root of the versionable hierarchy of the entity.
     * @param id the entity ID
     * @return the versionable node
     * @throws VersionAlreadyExistsException thrown when a draft version already exists for the entity
     */
    Node createDraftEntity(PK entityId);

    /**
     * Implementors should create a draft version of the entity based 
     * off the entity version with the given ID, and return the node
     * that is the root of the versionable hierarchy of the entity.
     * @param entityId the entity ID
     * @param versionId the version ID
     * @return the versionable node
     * @throws VersionAlreadyExistsException thrown when a draft version already exists for the entity
     */
    Node createDraftEntity(PK entityId, ID versionId);
    
    /**
     * Implementors should create a new version of the entity based on its draft state.
     * @param entityId the entity ID
     * @return the new version node
     * @throws NoDraftVersionException thrown if the no draft version of the entity exists
     */
    Version createVersionedEntity(PK entityId);
}
