package com.thinkbiganalytics.feedmgr.nifi.controllerservice;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import org.apache.nifi.web.api.dto.ControllerServiceDTO;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Manages the properties for connecting to a data source managed by a controller service.
 */
public abstract class AbstractControllerServiceRequest {

    private String connectionStringPropertyKey;
    private String userNamePropertyKey;
    private String passwordPropertyKey;
    private String driverClassNamePropertyKey;
    private String controllerServiceType;
    private String controllerServiceName;
    private String controllerServiceId;
    private ControllerServiceDTO controllerServiceDTO;
    private String password;
    private boolean useEnvironmentProperties;

    public String getConnectionStringPropertyKey() {
        return connectionStringPropertyKey;
    }

    public void setConnectionStringPropertyKey(String connectionStringPropertyKey) {
        this.connectionStringPropertyKey = connectionStringPropertyKey;
    }

    public String getUserNamePropertyKey() {
        return userNamePropertyKey;
    }

    public void setUserNamePropertyKey(String userNamePropertyKey) {
        this.userNamePropertyKey = userNamePropertyKey;
    }

    public String getPasswordPropertyKey() {
        return passwordPropertyKey;
    }

    public void setPasswordPropertyKey(String passwordPropertyKey) {
        this.passwordPropertyKey = passwordPropertyKey;
    }

    public String getDriverClassNamePropertyKey() {
        return driverClassNamePropertyKey;
    }

    public void setDriverClassNamePropertyKey(String driverClassNamePropertyKey) {
        this.driverClassNamePropertyKey = driverClassNamePropertyKey;
    }

    public String getControllerServiceType() {
        return controllerServiceType;
    }

    public void setControllerServiceType(String controllerServiceType) {
        this.controllerServiceType = controllerServiceType;
    }

    public String getControllerServiceName() {
        return controllerServiceName;
    }

    public void setControllerServiceName(String controllerServiceName) {
        this.controllerServiceName = controllerServiceName;
    }

    public String getControllerServiceId() {
        return controllerServiceId;
    }

    public void setControllerServiceId(String controllerServiceId) {
        this.controllerServiceId = controllerServiceId;
    }

    public ControllerServiceDTO getControllerServiceDTO() {
        return controllerServiceDTO;
    }

    public void setControllerServiceDTO(ControllerServiceDTO controllerServiceDTO) {
        this.controllerServiceDTO = controllerServiceDTO;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean useEnvironmentProperties() {
        return useEnvironmentProperties;
    }

    public void setUseEnvironmentProperties(boolean useEnvironmentProperties) {
        this.useEnvironmentProperties = useEnvironmentProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractControllerServiceRequest that = (AbstractControllerServiceRequest) o;
        return Objects.equals(connectionStringPropertyKey, that.connectionStringPropertyKey) &&
               Objects.equals(controllerServiceName, that.controllerServiceName) &&
               Objects.equals(controllerServiceId, that.controllerServiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionStringPropertyKey, controllerServiceName, controllerServiceId);
    }

    public abstract static class AbstractControllerServiceRequestBuilder<T extends AbstractControllerServiceRequestBuilder> {

        private String connectionStringPropertyKey;
        private String userNamePropertyKey;
        private String passwordPropertyKey;
        private String driverClassNamePropertyKey;
        private String controllerServiceType;
        private String controllerServiceName;
        private String controllerServiceId;
        private ControllerServiceDTO controllerServiceDTO;
        private String password;
        private boolean useEnvironmentProperties = true;


        protected AbstractControllerServiceRequestBuilder(@Nonnull final ControllerServiceDTO controllerServiceDTO) {
            this.controllerServiceDTO = controllerServiceDTO;
            this.controllerServiceType = controllerServiceDTO != null ? controllerServiceDTO.getType() : null;
            this.controllerServiceId = controllerServiceDTO != null ? controllerServiceDTO.getId() : null;
            this.controllerServiceName = controllerServiceDTO != null ? controllerServiceDTO.getName() : null;
            initializePropertiesFromControllerServiceType();
        }

        public T connectionStringPropertyKey(String connectionStringPropertyKey) {
            this.connectionStringPropertyKey = connectionStringPropertyKey;
            return (T) this;
        }

        public T userNamePropertyKey(String userNamePropertyKey) {
            this.userNamePropertyKey = userNamePropertyKey;
            return (T) this;
        }

        public T passwordPropertyKey(String passwordPropertyKey) {
            this.passwordPropertyKey = passwordPropertyKey;
            return (T) this;
        }

        public T driverClassNamePropertyKey(String driverClassNamePropertyKey) {
            this.driverClassNamePropertyKey = driverClassNamePropertyKey;
            return (T) this;
        }

        public T controllerServiceType(String controllerServiceType) {
            this.controllerServiceType = controllerServiceType;
            return (T) this;
        }

        private void initializePropertiesFromControllerServiceType() {
            if ("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(controllerServiceType)) {
                this.connectionStringPropertyKey = "Database Connection URL";
                this.userNamePropertyKey = "Database User";
                this.passwordPropertyKey = "Password";
                this.driverClassNamePropertyKey = "Database Driver Class Name";
            } else if ("com.thinkbiganalytics.nifi.v2.sqoop.StandardSqoopConnectionService".equalsIgnoreCase(controllerServiceType)) {
                this.connectionStringPropertyKey = "Source Connection String";
                this.userNamePropertyKey = "Source User Name";
                this.passwordPropertyKey = "Password";  // users will need to add this as a different property to the application.properties file
            }
        }

        public T controllerService(ControllerServiceDTO controllerServiceDTO) {
            this.controllerServiceDTO = controllerServiceDTO;
            return (T) this;
        }

        public T controllerServiceName(String controllerServiceName) {
            this.controllerServiceName = controllerServiceName;
            return (T) this;
        }


        public T controllerServiceId(String controllerServiceId) {
            this.controllerServiceId = controllerServiceId;
            return (T) this;
        }

        public T password(String password) {
            this.password = password;
            return (T) this;
        }

        public T useEnvironmentProperties(boolean useEnvironmentProperties) {
            this.useEnvironmentProperties = useEnvironmentProperties;
            return (T) this;
        }

        @Nonnull
        protected <R extends AbstractControllerServiceRequest> R build(@Nonnull final R request) {
            request.setConnectionStringPropertyKey(this.connectionStringPropertyKey);
            request.setControllerServiceName(this.controllerServiceName);
            request.setControllerServiceId(this.controllerServiceId);
            request.setControllerServiceType(this.controllerServiceType);
            request.setUserNamePropertyKey(userNamePropertyKey);
            request.setPasswordPropertyKey(passwordPropertyKey);
            request.setControllerServiceDTO(this.controllerServiceDTO);
            request.setDriverClassNamePropertyKey(this.driverClassNamePropertyKey);
            request.setPassword(password);
            request.setUseEnvironmentProperties(useEnvironmentProperties);
            return request;
        }
    }
}
