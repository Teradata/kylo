package com.thinkbiganalytics.metadata.modeshape.common;

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

import com.thinkbiganalytics.metadata.modeshape.common.mixin.WrappedNodeMixin;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 */
public class JcrObject implements WrappedNodeMixin {

    private final Node node;

    private String versionName;

    private String versionableIdentifier;

    /**
     *
     */
    public JcrObject(Node node) {
        this.node = node;
    }

    public String getObjectId() throws RepositoryException {
        if (this.node.isNodeType("nt:frozenNode")) {
            return this.versionableIdentifier;
        } else {
            return this.node.getIdentifier();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + ": " + this.node.getName();
        } catch (RepositoryException e) {
            return getClass().getSimpleName() + " - error: " + e.getMessage();
        }
    }

    public Node getNode() {
        return this.node;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionableIdentifier() {
        return versionableIdentifier;
    }

    public void setVersionableIdentifier(String versionableIdentifier) {
        this.versionableIdentifier = versionableIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass().isInstance(obj)) {
            JcrObject that = (JcrObject) obj;
            return this.node.equals(that.node);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }
}
