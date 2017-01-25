package com.thinkbiganalytics.rest.model;

/*-
 * #%L
 * thinkbig-commons-rest-model
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/22/15.
 */
public class RestResponseStatus {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    private String status;
    private String message;
    private String developerMessage;
    private String url;
    private boolean validationError;
    private Map<String, String> properties;
    private List<ValidationError> validationErrors;

    private RestResponseStatus(ResponseStatusBuilder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.developerMessage = builder.developerMessage;
        this.url = builder.url;
        this.properties = builder.properties;
        this.validationError = builder.validationError;
        this.validationErrors = builder.validationErrors;
    }

    public static class ResponseStatusBuilder {

        private String status;
        private String message;
        private String developerMessage;
        private String url;
        private boolean validationError;

        private Map<String, String> properties = new HashMap<>();
        private List<ValidationError> validationErrors = new ArrayList<ValidationError>();

        public ResponseStatusBuilder() {

        }

        public ResponseStatusBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ResponseStatusBuilder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public ResponseStatusBuilder property(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public ResponseStatusBuilder validationErrors(ValidationError error) {
            validationErrors.add(error);
            return this;
        }

        public ResponseStatusBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ResponseStatusBuilder validationError(boolean validationError) {
            this.validationError = validationError;
            return this;
        }

        public ResponseStatusBuilder setDeveloperMessage(Throwable e) {
            this.developerMessage = e.getMessage();
            return this;
        }

        public RestResponseStatus buildSuccess() {
            this.status = RestResponseStatus.STATUS_SUCCESS;
            return new RestResponseStatus(this);
        }


        public RestResponseStatus buildError() {
            this.status = RestResponseStatus.STATUS_ERROR;
            return new RestResponseStatus(this);
        }


    }

    public RestResponseStatus(String status) {
        this.status = status;
    }

    public static RestResponseStatus SUCCESS = new RestResponseStatus.ResponseStatusBuilder().buildSuccess();

    public static RestResponseStatus ERROR = new RestResponseStatus.ResponseStatusBuilder().buildError();

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public boolean isValidationError() {
        return validationError;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
