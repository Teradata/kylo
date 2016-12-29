/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Component;

import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.rest.model.AlertRange;

import io.swagger.annotations.Api;

/**
 *
 * @author Sean Felten
 */
@Component
@Api(value = "alerts", produces = "application/json")
@Path("/v1/alerts")
public class AlertsController {
    
    @Inject
    private AlertProvider provider;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AlertRange getAlerts(@Context UriInfo uriInfo) {
        List<Alert> alerts = new ArrayList<>();
        AlertCriteria criteria = createCriteria(uriInfo);
        
        provider.getAlerts(criteria).forEachRemaining(a -> alerts.add(a));
        return new AlertRange(alerts);
    }

    /**
     * @param uriInfo
     * @return
     */
    private AlertCriteria createCriteria(UriInfo uriInfo) {
        // Query params: limit, state, level, before-time, after-time, before-alert, after-alert
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        AlertCriteria criteria = provider.criteria();
        
        try {
            Optional.ofNullable(params.get("limit")).ifPresent(list -> list.forEach(limitStr -> criteria.limit(Integer.parseInt(limitStr))));
            Optional.ofNullable(params.get("state")).ifPresent(list -> list.forEach(stateStr -> criteria.state(Alert.State.valueOf(stateStr.toUpperCase()))));
            Optional.ofNullable(params.get("level")).ifPresent(list -> list.forEach(limitStr -> criteria.level(Alert.Level.valueOf(limitStr.toUpperCase()))));
            Optional.ofNullable(params.get("before-time")).ifPresent(list -> list.forEach(timeStr -> criteria.before(Formatters.parseDateTime(timeStr))));
            Optional.ofNullable(params.get("after-time")).ifPresent(list -> list.forEach(timeStr -> criteria.after(Formatters.parseDateTime(timeStr))));
            Optional.ofNullable(params.get("before-alert")).ifPresent(list -> list.forEach(idStr -> criteria.before(provider.resolve(idStr))));
            Optional.ofNullable(params.get("after-alert")).ifPresent(list -> list.forEach(idStr -> criteria.after(provider.resolve(idStr))));
            
            return criteria;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid query parameter: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }
}
