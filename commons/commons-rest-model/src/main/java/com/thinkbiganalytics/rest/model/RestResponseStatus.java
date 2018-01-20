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
 * A common class and builder to help return status information and/or validation and error information for various REST endpoints
 */
public class RestResponseStatus {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    /**
     * static response indicating success
     */
    public static final RestResponseStatus SUCCESS = new RestResponseStatus.ResponseStatusBuilder().buildSuccess();
    /**
     * static response indicating an error
     */
    public static final RestResponseStatus ERROR = new RestResponseStatus.ResponseStatusBuilder().buildError();
    /**
     * the status of the response
     */
    private String status;
    /**
     * a message describing this response
     */
    private String message;
    /**
     * a more detailed message with developer information (i.e. stack trace) that can be included
     */
    private String developerMessage;
    /**
     * the url for the response
     */
    private String url;
    /**
     * a flag indicating this has some validation errors
     */
    private boolean validationError;
    /**
     * any additional properties that can be surfaced back to the caller
     */
    private Map<String, String> properties;
    /**
     * the list of validation errors
     */
    private List<ValidationError> validationErrors;

    private RestResponseStatus() {}

    private RestResponseStatus(ResponseStatusBuilder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.developerMessage = builder.developerMessage;
        this.url = builder.url;
        this.properties = builder.properties;
        this.validationError = builder.validationError;
        this.validationErrors = builder.validationErrors;
    }

    public RestResponseStatus(String status) {
        this.status = status;
    }

    /**
     * get a status string for the response.
     */
    public String getStatus() {
        return status;
    }

    /**
     * gets the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * get the Developer detailed message
     */
    public String getDeveloperMessage() {
        return developerMessage;
    }

    /**
     * Check if the response came from a validation error
     *
     * @return true if the response came from validation errors, false if not
     */
    public boolean isValidationError() {
        return validationError;
    }

    /**
     * get the URL string
     */
    public String getUrl() {
        return url;
    }

    /**
     * get a map of arbitrary properties that were assigned to the respoinse
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * get any specific validation errors on the response
     */
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
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

        /**
         * add a message to the response
         *
         * @param message the message to add
         */
        public ResponseStatusBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * add a map of properties to the response
         *
         * @param properties a map of properties
         */
        public ResponseStatusBuilder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        /**
         * add a specific property to the map of properties
         */
        public ResponseStatusBuilder property(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        /**
         * Add a specific validation error to the response
         */
        public ResponseStatusBuilder addValidationError(ValidationError error) {
            validationErrors.add(error);
            return this;
        }

        /**
         * set the Url to the response
         */
        public ResponseStatusBuilder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Flag to indicate in the response that it came from a validation error
         */
        public ResponseStatusBuilder validationError(boolean validationError) {
            this.validationError = validationError;
            return this;
        }

        /**
         * A more detailed message meant for developers adding the exception message to the developer message string
         *
         * @param e an exception to parse the message from
         */
        public ResponseStatusBuilder setDeveloperMessage(Throwable e) {
            this.developerMessage = e.getMessage();
            return this;
        }

        /**
         * Return a response object that sets the status to "success"
         */
        public RestResponseStatus buildSuccess() {
            this.status = RestResponseStatus.STATUS_SUCCESS;
            return new RestResponseStatus(this);
        }


        /**
         * Return a response object that sets the status to "error"
         */
        public RestResponseStatus buildError() {
            this.status = RestResponseStatus.STATUS_ERROR;
            return new RestResponseStatus(this);
        }


    }
}
