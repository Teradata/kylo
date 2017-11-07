package com.thinkbiganalytics.metadata.modeshape.security;

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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.Optional;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.security.Privilege;

/**
 * Ensures that the AllowedActions.Services prototype nodes are available under the Services noded
 */
@Order(PostMetadataConfigAction.LATE_ORDER)
public class EnsureServicesAccessControlAction implements PostMetadataConfigAction {


    @Inject
    private MetadataAccess metadata;


    @Inject
    private AllowedEntityActionsProvider allowedEntityActionsProvider;

    private static final Logger log = LoggerFactory.getLogger(EnsureServicesAccessControlAction.class);

    @Override
    public void run() {
        log.info("Ensuring the Services prototype access control permissions are members of the actual Services access control node");
        metadata.commit(() -> {
            //find services entity node
            Optional<AllowedActions> option = this.allowedEntityActionsProvider.getAllowedActions(AllowedActions.SERVICES);
            Node servicesNode = ((JcrAllowedActions) option.get()).getNode();
            JcrAllowedActions allowedAction = ((JcrAllowedActions) allowedEntityActionsProvider.getAvailableActions(AllowedActions.SERVICES).get());
            allowedAction.copy(servicesNode, MetadataAccess.ADMIN, Privilege.JCR_ALL);
        }, MetadataAccess.SERVICE);
    }


}
