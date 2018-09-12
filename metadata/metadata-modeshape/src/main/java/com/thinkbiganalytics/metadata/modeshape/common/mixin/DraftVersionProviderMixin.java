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
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityDraftVersion;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityVersion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.version.Version;

/**
 * A mixin interface to be implemented by any JCR entity provider that supports versionable entities that 
 * may have draft versions.
 */
public interface DraftVersionProviderMixin<T, PK extends Serializable> extends VersionProviderMixin<T, PK>, EntityDraftVersionProvider<T, PK> {

    
    @Override
    default Optional<List<EntityVersion<PK, T>>> findVersions(PK entityId, boolean includeContent) {
        List<EntityVersion<PK, T>> result = new ArrayList<>();
        
        findVersionableNode(entityId)
            .filter(node -> JcrVersionUtil.isCheckedOut(node))
            .ifPresent(draft -> {
                T entity = includeContent ? asEntity(entityId, draft) : null;
                result.add(new JcrEntityDraftVersion<PK, T>(draft, entityId, entity));
            });
        
        VersionProviderMixin.super.findVersions(entityId, includeContent)
            .ifPresent(result::addAll);
        
        return result.size() != 0 ? Optional.of(result) : Optional.empty();
    }
    
    @Override
    default Optional<EntityVersion<PK, T>> findDraftVersion(PK entityId, boolean includeContent) {
        return findVersionableNode(entityId)
            .filter(node -> JcrVersionUtil.isCheckedOut(node))
            .map(draft -> {
                T entity = includeContent ? asEntity(entityId, draft) : null;
                return (EntityVersion<PK, T>) new JcrEntityDraftVersion<PK, T>(draft, entityId, entity);
            });

    }

    @Override
    default Optional<EntityVersion<PK, T>> findLatestVersion(PK entityId, boolean includeContent) {
        Optional<EntityVersion<PK, T>> draftOption = findVersionableNode(entityId)
            .filter(node -> JcrVersionUtil.isCheckedOut(node))
            .map(draft -> {
                T entity = includeContent ? asEntity(entityId, draft) : null;
                return (EntityVersion<PK, T>) new JcrEntityDraftVersion<PK, T>(draft, entityId, entity);
            });
        
        if (draftOption.isPresent()) {
            return draftOption;
        } else {
            return VersionProviderMixin.super.findLatestVersion(entityId, includeContent);
        }
    }

    @Override
    default boolean hasDraftVersion(PK entityId) {
        return findLatestVersion(entityId, false).map(ver -> ver.getName().equals("draft")).orElse(false);
    }

    @Override
    default EntityVersion<PK, T> createDraftVersion(PK entityId, boolean includeContent) {
        Node versionable = createDraftEntity(entityId);
        T entity = includeContent ? asEntity(entityId, versionable) : null;
        return new JcrEntityDraftVersion<>(versionable, entityId, entity);
    }

    @Override
    default EntityVersion<PK, T> createDraftVersion(PK entityId, ID versionId, boolean includeContent) {
        Node versionable = createDraftEntity(entityId, versionId);
        T entity = includeContent ? asEntity(entityId, versionable) : null;
        return new JcrEntityDraftVersion<>(versionable, entityId, entity);
    }

    @Override
    default EntityVersion<PK, T> createVersion(PK entityId, String comment, boolean includeContent) {
        Version version = createVersionedEntity(entityId, comment);
        Node versionable = JcrVersionUtil.getFrozenNode(version);
        T entity = includeContent ? asEntity(entityId, versionable) : null;
        return new JcrEntityVersion<>(version, getChangeComment(entityId, versionable), entityId, entity);
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
     * Implementors should create a new version of the entity based on its draft state, an optionally associate a comment
     * with the version.
     * @param entityId the entity ID
     * @param comment a comment message that an implementation may use choose to use to attach to the version
     * @return the new version node
     * @throws NoDraftVersionException thrown if the no draft version of the entity exists
     */
    Version createVersionedEntity(PK entityId, String comment);
}
