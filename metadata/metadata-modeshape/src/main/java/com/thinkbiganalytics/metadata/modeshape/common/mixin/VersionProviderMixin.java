/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

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

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersionProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityVersion;

/**
 * A mixin interface to be implemented by any JCR entity provider that supports versionable entities.
 */
public interface VersionProviderMixin<T, PK extends Serializable> extends EntityVersionProvider<T, PK> {
    
    @Override
    default EntityVersion.ID resolveVersion(Serializable ser) {
        return new JcrEntityVersion.VersionId(ser);
    }

    @Override
    default Optional<List<EntityVersion<PK, T>>> findVersions(PK entityId, boolean includeContent) {
        BiFunction<EntityVersion<PK, T>, EntityVersion<PK, T>, Integer> desc = (v1, v2) -> v2.getCreatedDate().compareTo(v1.getCreatedDate());
        
        return findVersionableNode(entityId)
                        .map(node -> JcrVersionUtil.getVersions(node).stream()
                                        .filter(ver -> ! JcrUtil.getName(ver).equals("jcr:rootVersion"))
                                        .map(ver -> new JcrEntityVersion<>(ver, entityId, includeContent ? asEntity(entityId, JcrVersionUtil.getFrozenNode(ver)) : null))
                                        .sorted(desc::apply)
                                        .collect(Collectors.toList()));
    }
    
    @Override
    default Optional<EntityVersion<PK, T>> findVersion(PK entityId, EntityVersion.ID versionId, boolean includedContent) {
        return findVersions(entityId, includedContent)
                .flatMap(list -> list.stream()
                             .filter(ver -> ver.getId().equals(versionId))
                             .findFirst());
    }

    @Override
    default Optional<EntityVersion<PK, T>> findLatestVersion(PK entityId, boolean includeContent) {
        return findVersionableNode(entityId)
                .flatMap(node -> Optional.ofNullable(JcrVersionUtil.getLatestVersion(node)))
                .map(ver -> new JcrEntityVersion<>(ver, entityId, includeContent ? asEntity(entityId, JcrVersionUtil.getFrozenNode(ver)) : null));
    }
    

    /**
     * Implementers should return an optional containing the node considered to be the one
     * that is the root of the versionable hierarchy of the entity.
     * @param id the entity ID
     * @return an optional containing the versionable node, or an empty optional if no entity exists with the given ID
     */
    Optional<Node> findVersionableNode(PK id);
    
    /**
     * Implementers should construct an entity based on the state of the versionable node argument,
     * which will either be the node returned from findVersionableNode() or its frozen node equivalent
     * from one of the Versions
     * @param id the entity ID
     * @param versionable the versionable node
     * @return the entity
     */
    T asEntity(PK id, Node versionable);
}
