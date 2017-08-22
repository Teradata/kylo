package com.thinkbiganalytics.feedmgr.nifi.controllerservice;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;

import javax.annotation.Nonnull;

/**
 * Executes a query on a data source managed by a controller service.
 */
public class ExecuteQueryControllerServiceRequest extends AbstractControllerServiceRequest {

    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public static class ExecuteQueryControllerServiceRequestBuilder extends AbstractControllerServiceRequest.AbstractControllerServiceRequestBuilder<ExecuteQueryControllerServiceRequestBuilder> {

        private String query;

        public ExecuteQueryControllerServiceRequestBuilder(@Nonnull final ControllerServiceDTO controllerServiceDTO) {
            super(controllerServiceDTO);
        }

        public ExecuteQueryControllerServiceRequestBuilder query(String query) {
            this.query = query;
            return this;
        }

        @Nonnull
        public ExecuteQueryControllerServiceRequest build() {
            final ExecuteQueryControllerServiceRequest request = super.build(new ExecuteQueryControllerServiceRequest());
            request.setQuery(query);
            return request;
        }
    }
}
