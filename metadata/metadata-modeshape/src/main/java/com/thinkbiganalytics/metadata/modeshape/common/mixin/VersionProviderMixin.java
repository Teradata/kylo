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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersionProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrEntityVersion;
import com.thinkbiganalytics.metadata.modeshape.versioning.JcrLatestEntityVersion;

/**
 *
 */
public interface VersionProviderMixin<T, PK extends Serializable> extends EntityVersionProvider<T, PK> {

    default Optional<List<EntityVersion<T>>> findVersions(PK entityId, boolean includeEntity) {
        return findVersionableNode(entityId)
                        .map(node -> {
                            List<EntityVersion<T>> result = new ArrayList<>();
                            result.add(new JcrLatestEntityVersion<>(node, includeEntity ? asEntity(node) : null));
                            
                            List<EntityVersion<T>> versions = JcrVersionUtil.getVersions(node).stream()
                                            .map(ver -> new JcrEntityVersion<>(ver, includeEntity ? asEntity(JcrVersionUtil.getFrozenNode(ver)) : null))
                                            .collect(Collectors.toList());
                            result.addAll(versions);
                            
                            return result;
                        });
    }
    
    default Optional<EntityVersion<T>> findVersion(PK entityId, EntityVersion.ID versionId, boolean includeEntity) {
        return findVersions(entityId, includeEntity)
                        .flatMap(list -> {
                            return list.stream()
                                .filter(ver -> ver.getId().equals(versionId))
                                .findFirst();
                        });
    }

    default Optional<EntityVersion<T>> findLatestVersion(PK entityId, boolean includeEntity) {
        return findVersionableNode(entityId)
                        .map(node -> new JcrLatestEntityVersion<>(node, includeEntity ? asEntity(node) : null));
    }
    

    Optional<Node> findVersionableNode(PK id);
    
    T asEntity(Node versionable);
}
