/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertProvider;

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
    public List<Alert> getAlerts(@QueryParam("since-time") @DefaultValue("1970-01-01T00:00:00Z") String sinceTimeStr,
                                 @QueryParam("since-alert") String sinceIdStr) {
        List<Alert> alerts = new ArrayList<>();
        
        // A supplied alert ID take precedence if both ID and time supplied.
        if (sinceIdStr != null) {
            Alert.ID id = provider.resolve(sinceIdStr);
            provider.getAlerts(id).forEachRemaining(a -> alerts.add(a));
        } else {
            DateTime since = Formatters.parseDateTime(sinceTimeStr);
            provider.getAlerts(since).forEachRemaining(a -> alerts.add(a));
        }
        
        return alerts;
    }
}
