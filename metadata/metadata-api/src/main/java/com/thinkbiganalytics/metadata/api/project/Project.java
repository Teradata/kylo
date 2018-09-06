package com.thinkbiganalytics.metadata.api.project;

import com.thinkbiganalytics.metadata.api.Auditable;
import com.thinkbiganalytics.metadata.api.Iconable;
import com.thinkbiganalytics.metadata.api.SystemEntity;
import com.thinkbiganalytics.metadata.api.Taggable;
import com.thinkbiganalytics.security.AccessControlled;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Project extends AccessControlled, Auditable, Iconable, SystemEntity, Taggable {

    Project.ID getId();

    @Nullable
    String getProjectName();

    void setProjectName(@Nullable final String displayName);

    @Nonnull
    String getContainerImage();

    void setContainerImage(String image);

    interface ID extends Serializable {

    }
}
