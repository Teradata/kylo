package com.thinkbiganalytics.alerts.rest.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/*-
 * #%L
 * thinkbig-alerts-controller
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

import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.alerts.AlertConstants;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.rest.model.Alert.Level;
import com.thinkbiganalytics.alerts.rest.model.Alert.State;
import com.thinkbiganalytics.alerts.rest.model.AlertCreateRequest;
import com.thinkbiganalytics.alerts.rest.model.AlertRange;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.alerts.rest.model.AlertUpdateRequest;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Operations Manager - Alerts", produces = "application/json")
@Path("/v1/alerts")
public class AlertsController {

    @Inject
    private AlertProvider provider;

    @Inject
    @Named("kyloAlertManager")
    private AlertManager alertManager;

    @Inject
    private AccessController accessController;

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists the current alerts.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the alerts.", response = AlertRange.class)
    )
    public AlertRange getAlerts(@QueryParam("limit") Integer limit,
                                @QueryParam("type") String type,
                                @QueryParam("subtype") String subtype,
                                @QueryParam("state") String state,
                                @QueryParam("level") String level,
                                @QueryParam("before") String before,
                                @QueryParam("after") String after,
                                @QueryParam("cleared") @DefaultValue("false") String cleared,
                                @QueryParam("filter") String filter) {
        List<Alert> alerts = new ArrayList<>();
        AlertCriteria criteria = createCriteria(limit,type,subtype, state, level, before, after, cleared);
        criteria.orFilter(filter);
        provider.getAlerts(criteria).forEachRemaining(alerts::add);
        return new AlertRange(alerts.stream().map(this::toModel).collect(Collectors.toList()));
    }


    @GET
    @Path("/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists summary grouped alerts.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns summary of the alerts grouped.", response = AlertRange.class)
    )
    public Collection<AlertSummaryGrouped> getAlertSummary(@QueryParam("state") String state,
                                      @QueryParam("level") String level,
                                      @QueryParam("type") String type,
                                      @QueryParam("subtype") String subtype,
                                      @QueryParam("cleared") @DefaultValue("false") String cleared) {
        List<AlertSummary> alerts = new ArrayList<>();
        AlertCriteria criteria = createCriteria(null, type,subtype,state, level, null, null, cleared);

        provider.getAlertsSummary(criteria).forEachRemaining(alerts::add);
        return groupAlertSummaries(alerts);
    }

    @GET
    @Path("/summary/unhandled")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists summary grouped alerts.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns summary of the alerts grouped.", response = AlertRange.class)
    )
    public Collection<AlertSummaryGrouped> getAlertSummaryUnhandled( @QueryParam("type") String type,
                                                                     @QueryParam("subtype") String subtype) {
        List<AlertSummary> alerts = new ArrayList<>();
        AlertCriteria criteria = createCriteria(null, type,subtype,Alert.State.UNHANDLED.name(), null, null, null, "false");

        provider.getAlertsSummary(criteria).forEachRemaining(alerts::add);
        return groupAlertSummaries(alerts);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified alert.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the alert.", response = com.thinkbiganalytics.alerts.rest.model.Alert.class),
                      @ApiResponse(code = 404, message = "The alert could not be found.", response = RestResponseStatus.class)
                  })
    public com.thinkbiganalytics.alerts.rest.model.Alert getAlert(@PathParam("id") String idStr) {
        Alert.ID id = provider.resolve(idStr);

        return provider.getAlert(id)
            .map(this::toModel)
            .orElseThrow(() -> new WebApplicationException("An alert with the given ID does not exists: " + idStr, Status.NOT_FOUND));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates a new alert.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the alert.", response = com.thinkbiganalytics.alerts.rest.model.Alert.class)
    )
    public com.thinkbiganalytics.alerts.rest.model.Alert createAlert(AlertCreateRequest req) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        Alert.Level level = toDomain(req.getLevel());
        Alert alert = alertManager.create(req.getType(), req.getSubtype(),level, req.getDescription(), null);
        return toModel(alert);
    }


    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Modifies the specified alert.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the alert.", response = com.thinkbiganalytics.alerts.rest.model.Alert.class),
                      @ApiResponse(code = 400, message = "The alert could not be found.", response = RestResponseStatus.class)
                  })
    public com.thinkbiganalytics.alerts.rest.model.Alert updateAlert(@PathParam("id") String idStr,
                                                                     AlertUpdateRequest req) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        Alert.ID id = provider.resolve(idStr);
        final Alert.State state = toDomain(req.getState());

        class UpdateResponder implements AlertResponder {

            private Alert result = null;

            @Override
            public void alertChange(Alert alert, AlertResponse response) {
                result = alert;

                switch (state) {
                    case HANDLED:
                        result = response.handle(req.getDescription());
                        break;
                    case IN_PROGRESS:
                        result = response.inProgress(req.getDescription());
                        break;
                    case UNHANDLED:
                        result = response.unhandle(req.getDescription());
                        break;
                    default:
                        break;
                }

                if (req.isClear()) {
                    response.clear();
                }
            }
        }

        UpdateResponder responder = new UpdateResponder();

        provider.respondTo(id, responder);
        return toModel(responder.result);
    }

    private  String alertSummaryDisplayName(AlertSummary alertSummary){
        String type = alertSummary.getType();
        String part = type;
        if(part.startsWith(AlertConstants.KYLO_ALERT_TYPE_PREFIX)) {
            part = StringUtils.substringAfter(part, AlertConstants.KYLO_ALERT_TYPE_PREFIX);
        }
        else {
            int idx = StringUtils.lastOrdinalIndexOf(part, "/", 2);
            part = StringUtils.substring(part, idx);
        }
        String[] parts = part.split("/");
        StringBuffer displayNameSb = new StringBuffer();
       return Arrays.asList(parts).stream().map(s ->StringUtils.capitalize(s)).collect(Collectors.joining(" "));
    }

    private Collection<AlertSummaryGrouped> groupAlertSummaries(List<AlertSummary> alertSummaries) {
        Map<String,AlertSummaryGrouped> group = new HashMap<>();
        alertSummaries.forEach(alertSummary -> {
            String key = alertSummary.getType()+":"+alertSummary.getSubtype();
            String displayName = alertSummaryDisplayName(alertSummary);
            group.computeIfAbsent(key,key1->new AlertSummaryGrouped(alertSummary.getType(),alertSummary.getSubtype(),displayName)).add(toModel(alertSummary.getLevel()),alertSummary.getCount(),alertSummary.getLastAlertTimestamp());
        });
        return group.values();
    }


    private com.thinkbiganalytics.alerts.rest.model.Alert toModel(Alert alert) {
        com.thinkbiganalytics.alerts.rest.model.Alert result = new com.thinkbiganalytics.alerts.rest.model.Alert();
        result.setId(alert.getId().toString());
        result.setActionable(alert.isActionable());
        result.setCreatedTime(alert.getCreatedTime());
        result.setLevel(toModel(alert.getLevel()));
        result.setState(toModel(alert.getState()));
        result.setType(alert.getType());
        result.setDescription(alert.getDescription());
        result.setCleared(alert.isCleared());
        alert.getEvents().forEach(e -> result.getEvents().add(toModel(e)));
        return result;
    }

    private com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent toModel(AlertChangeEvent event) {
        com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent result = new com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent();
        result.setCreatedTime(event.getChangeTime());
        result.setDescription(event.getDescription());
        result.setState(toModel(event.getState()));
        result.setUser(event.getUser() != null ? event.getUser().getName() : null);
        return result;
    }

    private Level toModel(Alert.Level level) {
        // Currently identical
        return Level.valueOf(level.name());
    }

    private State toModel(Alert.State state) {
        // Currently identical
        return State.valueOf(state.name());
    }

    private Alert.State toDomain(State state) {
        return Alert.State.valueOf(state.name());
    }

    private Alert.Level toDomain(Level level) {
        // Currently identical
        return Alert.Level.valueOf(level.name());
    }

    private AlertCriteria createCriteria(Integer limit, String type, String subtype,String stateStr, String levelStr, String before, String after, String cleared) {
        AlertCriteria criteria = provider.criteria();

        if (limit != null) {
            criteria.limit(limit);
        }
        if (type != null) {
            try {
                criteria.type(URI.create(type));
            }catch (IllegalArgumentException e){

            }
        }
        if (subtype != null) {
            criteria.subtype(subtype);
        }
        if (stateStr != null) {
            try {
                criteria.state(Alert.State.valueOf(stateStr.toUpperCase()));
            }catch (IllegalArgumentException e){

            }
        }
        if (levelStr != null) {
            try {
            criteria.level(Alert.Level.valueOf(levelStr.toUpperCase()));
            }catch (IllegalArgumentException e){

            }
        }
        if (before != null) {
            try {
            criteria.before(Formatters.parseDateTime(before));
            }catch (IllegalArgumentException e){

            }
        }
        if (after != null) {
            try {
            criteria.after(Formatters.parseDateTime(after));
            }catch (IllegalArgumentException e){

            }
        }
        if (cleared != null) {
            criteria.includedCleared(Boolean.parseBoolean(cleared));
        }

        return criteria;
    }

    private AlertCriteria createCriteria(UriInfo uriInfo) {
        // Query params: limit, state, level, before-time, after-time, before-alert, after-alert
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        AlertCriteria criteria = provider.criteria();

        try {
            Optional.ofNullable(params.get("type")).ifPresent(list -> list.forEach(typeStr -> criteria.type(URI.create(typeStr))));
            Optional.ofNullable(params.get("subtype")).ifPresent(list -> list.forEach(subtype -> criteria.subtype(subtype)));
            Optional.ofNullable(params.get("limit")).ifPresent(list -> list.forEach(limitStr -> criteria.limit(Integer.parseInt(limitStr))));
            Optional.ofNullable(params.get("state")).ifPresent(list -> list.forEach(stateStr -> criteria.state(Alert.State.valueOf(stateStr.toUpperCase()))));
            Optional.ofNullable(params.get("level")).ifPresent(list -> list.forEach(levelStr -> criteria.level(Alert.Level.valueOf(levelStr.toUpperCase()))));
            Optional.ofNullable(params.get("before")).ifPresent(list -> list.forEach(timeStr -> criteria.before(Formatters.parseDateTime(timeStr))));
            Optional.ofNullable(params.get("after")).ifPresent(list -> list.forEach(timeStr -> criteria.after(Formatters.parseDateTime(timeStr))));

            return criteria;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid query parameter: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }
}
