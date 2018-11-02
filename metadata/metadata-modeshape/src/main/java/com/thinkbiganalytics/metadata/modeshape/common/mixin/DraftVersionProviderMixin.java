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
import com.thinkbiganalytics.metadata.api.versioning.VersionNotFoundException;
import com.thinkbiganalytics.metadata.api.versioning.VersionableEntityNotFoundException;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion.ID;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityDraftVersion;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityVersion;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.version.Version;

/**
 * A mixin interface to be implemented by any JCR entity provider that supports versionable entities that 
 * may have draft versions.
 */
public interface DraftVersionProviderMixin<T, PK extends Serializable> extends VersionProviderMixin<T, PK>, EntityDraftVersionProvider<T, PK> {
    
    @Override
    default List<EntityVersion<PK, T>> findVersions(PK entityId, boolean includeContent) {
        Optional<EntityVersion<PK, T>> draftVersion = findVersionableNode(entityId)
            .map(versionable -> {
                if (JcrVersionUtil.isCheckedOut(versionable)) {
                    T entity = includeContent ? asEntity(entityId, versionable) : null;
                    return Optional.<EntityVersion<PK, T>>of(new JcrEntityDraftVersion<PK, T>(versionable, entityId, entity));
                } else {
                    return Optional.<EntityVersion<PK, T>>empty();
                }
            })
            .orElseThrow(() -> new VersionableEntityNotFoundException(entityId));
        
        return draftVersion
            .map(draft -> Stream.concat(Stream.of(draft), VersionProviderMixin.super.findVersions(entityId, includeContent).stream())
                 .collect(Collectors.toList()))
            .orElseGet(() -> VersionProviderMixin.super.findVersions(entityId, includeContent));
    }
    
    @Override
    default Optional<EntityVersion<PK, T>> findDraftVersion(PK entityId, boolean includeContent) {
        return findVersionableNode(entityId)
            .map(versionable -> {
                if (JcrVersionUtil.isCheckedOut(versionable)) {
                    T entity = includeContent ? asEntity(entityId, versionable) : null;
                    return Optional.<EntityVersion<PK, T>>of(new JcrEntityDraftVersion<PK, T>(versionable, entityId, entity));
                } else {
                    return Optional.<EntityVersion<PK, T>>empty();
                }
            })
            .orElseThrow(() -> new VersionableEntityNotFoundException(entityId));
    }
    
    @Override
    default Optional<EntityVersion<PK, T>> findVersion(PK entityId, ID versionId, boolean includeContent) {
        return findVersionableNode(entityId)
            .filter(versionable -> JcrEntityDraftVersion.matchesId(versionable, versionId))
            .map(versionable -> {
                T entity = includeContent ? asEntity(entityId, versionable) : null;
                return Optional.<EntityVersion<PK, T>>of(new JcrEntityDraftVersion<PK, T>(versionable, entityId, entity));
            })
            .orElseGet(() -> VersionProviderMixin.super.findVersion(entityId, versionId, includeContent));
    }

    @Override
    default Optional<EntityVersion<PK, T>> findLatestVersion(PK entityId, boolean includeContent) {
        Optional<EntityVersion<PK, T>> draftVersion = findVersionableNode(entityId)
            .map(versionable -> {
                if (JcrVersionUtil.isCheckedOut(versionable)) {
                    T entity = includeContent ? asEntity(entityId, versionable) : null;
                    return Optional.<EntityVersion<PK, T>>of(new JcrEntityDraftVersion<PK, T>(versionable, entityId, entity));
                } else {
                    return Optional.<EntityVersion<PK, T>>empty();
                }
            })
            .orElseThrow(() -> new VersionableEntityNotFoundException(entityId));
        
        if (draftVersion.isPresent()) {
            return draftVersion;
        } else {
            return VersionProviderMixin.super.findLatestVersion(entityId, includeContent);
        }
    }

    @Override
    default boolean hasDraftVersion(PK entityId) {
        return findLatestVersion(entityId, false).map(ver -> ver.getName().equals(EntityVersion.DRAFT_NAME)).orElse(false);
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.versioning.EntityDraftVersionProvider#revertDraftVersion(java.io.Serializable, boolean)
     */
    @Override
    default Optional<EntityVersion<PK, T>> revertDraftVersion(PK entityId, boolean includeContent) {
        return findDraftVersion(entityId, includeContent)
            .map(draft -> {
                return revertDraftEntity(entityId).map(version -> {
                    Node versionable = JcrVersionUtil.getFrozenNode(version);
                    T entity = includeContent ? asEntity(entityId, versionable) : null;
                    EntityVersion<PK, T> entVer = new JcrEntityVersion<>(version, getChangeComment(entityId, versionable), entityId, entity);
                    return entVer;
                });
            })
            .orElseGet(() -> findLatestVersion(entityId, includeContent));
    }
    
    /**
     * Implementors should create a draft version of the entity based
     * off the latest entity version, and return the node
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
     * @return An optional of the new version node, empty if there are no more versions remaining
     * @throws NoDraftVersionException thrown if the no draft version of the entity exists
     */
    Version createVersionedEntity(PK entityId, String comment);

    /**
     * Implementors should revert the draft version of the entity back to the version state it came from,
     * and return an optional of that version.  Reverting an entity that has no draft state should have no effect
     * and the latest version should be returned.  If there are no more versions (i.e. there was only a draft
     * version) then return and empty optional.
     * @param entityId the entity ID
     * @return the current version node the entity after the revert
     */
    Optional<Version> revertDraftEntity(PK entityId);
}
