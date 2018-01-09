package com.thinkbiganalytics.metadata.modeshape.project;

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

import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JcrProject extends AbstractJcrAuditableSystemEntity implements Project, AccessControlledMixin {

    /**
     * JCR node type for projects
     */
    public static final String NODE_TYPE = "tba:project";


    /**
     * The icon choice of the user, can be null in Jcr if not previously set
     */
    public static String ICON = "tba:icon";

    /**
     * The icon color choice of the user, can be null in Jcr if not previously set
     */
    public static String ICON_COLOR = "tba:iconColor";

    /**
     * Name of the {@code containerImage} property
     */
    private static final String CONTAINER_IMAGE = "tba:containerImage";

    /**
     * Constructs a {@code Project} using the specified node.
     *
     * @param node the JCR node for the user
     */
    public JcrProject(@Nonnull final Node node) {
        super(node);
    }

    @Override
    public ProjectId getId() {
        try {
            return new JcrProject.ProjectId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Nullable
    @Override
    public String getProjectName() {
        return getTitle();
    }

    @Override
    public void setProjectName(@Nullable String displayName) {
        setTitle(displayName);
    }

    @Override
    @Nullable
    public String getIconColor() {
        return super.getProperty(ICON_COLOR, String.class, true);
    }

    @Override
    public void setIconColor(String iconColor) {
        super.setProperty(ICON_COLOR, iconColor);
    }

    @Override
    @Nullable
    public String getContainerImage() {
        return super.getProperty(CONTAINER_IMAGE, String.class, false);
    }

    @Override
    public void setContainerImage(String image) {
        super.setProperty(CONTAINER_IMAGE, image);
    }

    @Override
    @Nullable
    public String getIcon() {
        return super.getProperty(ICON, String.class, true);
    }

    @Override
    public void setIcon(String icon) {
        super.setProperty(ICON, icon);
    }

    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrProjectAllowedActions.class;
    }

    public static class ProjectId extends JcrEntity.EntityId implements Project.ID {

        public ProjectId(Serializable ser) {
            super(ser);
        }
    }
}
