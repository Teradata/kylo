package com.thinkbiganalytics.project.controller;

/*-
 * #%L
 * project-service
 * %%
 * Copyright (C) 2017 - 2019 ThinkBig Analytics, a Teradata Company
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

public final class ProjectRequest {
    private String projectId;
    private String projectName;


    public ProjectRequest(ProjectRequest request) {
        this.projectName = request.getProjectName();
        this.projectId = request.getProjectId();
    }

    public ProjectRequest(String projectId, String projectName) {
        this.projectName = projectName;
        this.projectId = projectId;
    }

    public static class Builder {
        private String projectId;
        private String projectName;

        public Builder(){}



        public Builder projectId(String projectId){
            this.projectId = projectId;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ProjectRequest build() {
            return new ProjectRequest(projectId, projectName);
        }
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public static ProjectRequest byProjectName(String projectName) {
        return new ProjectRequest.Builder().projectName(projectName).build();
    }

    public static ProjectRequest byProjectId(String projectId) {
        return new ProjectRequest.Builder().projectName(projectId).build();
    }
}
