package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.rest.model.IconColor;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.service.UIService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.feedmgr.security.EncryptionAccessControl;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.spring.FileResourceService;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Feed Manager - Utilities", produces = "application/json")
@Path("/v1/feedmgr/util")
@Component
public class UtilityRestController {

    private static final Logger log = LoggerFactory.getLogger(UtilityRestController.class);

    @Inject
    Environment env;

    @Inject
    FileResourceService fileResourceService;

    @Inject
    private EncryptionService encryptionService;

    @Inject
    private AccessController accessController;

    @GET
    @Path("/cron-expression/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Validates the specified cron expression.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the result.", response = Map.class)
    )
    public Response validateCronExpression(@QueryParam("cronExpression") String cronExpression) {
        boolean valid = CronExpression.isValidExpression(cronExpression);
        if(valid){
            try {
                CronExpression e = new CronExpression(cronExpression);
                valid = CronExpressionUtil.getNextFireTime(e) != null;
            }catch (Exception e){
                valid = false;
            }
        }
        return Response.ok("{\"valid\":" + valid + "}").build();
    }

    @GET
    @Path("/cron-expression/preview")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the next matching times of the cron expression.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the times.", response = String.class, responseContainer = "List")
    )
    public Response previewCronExpression(@QueryParam("cronExpression") String cronExpression, @QueryParam("number") @DefaultValue("3") Integer number) {
        List<Date> dates = new ArrayList<>();
        List<String> dateStrings = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

        try {
            dates = CronExpressionUtil.getNextFireTimes(cronExpression, number);
            for (Date date : dates) {
                dateStrings.add(format.format(date));
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return Response.ok(dateStrings).build();
    }

    @GET
    @Path("/system-name")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Generates a system name from the specified name.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the system name.", response = String.class)
    )
    public Response generateSystemName(@QueryParam("name") String name) {
        String systemName = SystemNamingService.generateSystemName(name);
        return Response.ok(systemName).build();
    }

    @GET
    @Path("/codemirror-types")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the languages supported by CodeMirror.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns a mime-type to language mapping.", response = Map.class)
    )
    public Response codeMirrorTypes() {
        Map<String, String> types = UIService.getInstance().getCodeMirrorTypes();
        return Response.ok(types).build();
    }

    @GET
    @Path("/icon-colors")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available icon colors.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the icon colors.", response = Map.class, responseContainer = "List")
    )
    public Response iconColors() {

        String colorsJson = fileResourceService.getResourceAsString("classpath:/icon-colors.json");
        List<IconColor> colors = null;
        if (StringUtils.isNotBlank(colorsJson)) {
            //attempt to convert it to a list
            try {
                colors = Arrays.asList(ObjectMapperSerializer.deserialize(colorsJson, IconColor[].class));
            } catch (Exception e) {
                log.error(
                    "Unable to parse JSON for icon-colors.json file.  Reverting to using default colors.  Please check the icon-colors.json file in the /conf directory for propery JSON format Error: {}",
                    e.getMessage());
            }
        }
        if (colors == null || colors.isEmpty() || StringUtils.isBlank(colorsJson)) {
            colorsJson =
                "[{\"name\":\"Purple\",\"color\":\"#AB47BC\"},{\"name\":\"Orange\",\"color\":\"#FFCA28\"},{\"name\":\"Deep Orange\",\"color\":\"#FF8A65\"},{\"name\":\"Red\",\"color\":\"#FF5252\"},{\"name\":\"Blue\",\"color\":\"#90CAF9\"},{\"name\":\"Green\",\"color\":\"#66BB6A\"},{\"name\":\"Blue Grey\",\"color\":\"#90A4AE\"},{\"name\":\"Teal\",\"color\":\"#80CBC4\"},{\"name\":\"Pink\",\"color\":\"#F06292\"},{\"name\":\"Yellow\",\"color\":\"#FFF176\"}]";
            colors = Arrays.asList(ObjectMapperSerializer.deserialize(colorsJson, IconColor[].class));
        }
        return Response.ok(colors).build();
    }

    @GET
    @Path("/icons")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available icons.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the icons.", response = String.class, responseContainer = "List")
    )
    public Response icons() {
        String iconJson = fileResourceService.getResourceAsString("classpath:/icons.json");
        List<String> icons = null;
        if (StringUtils.isNotBlank(iconJson)) {
            //attempt to convert it to a list
            try {
                String[] iconArray = ObjectMapperSerializer.deserialize(iconJson, String[].class);
                icons = Arrays.asList(iconArray);
            } catch (Exception e) {
                log.error("Unable to parse JSON for icon.json file.  Reverting to using default icons.  Please check the icon.json file in the /conf directory for propery JSON format. Error: {}",
                          e.getMessage());
            }

        }
        if (icons == null || StringUtils.isBlank(iconJson)) {
            iconJson =
                "[\"local_airport\",\"phone_android\",\"web\",\"forward\",\"star\",\"attach_money\",\"location_city\",\"style\",\"insert_chart\",\"merge_type\",\"local_dining\",\"people\",\"directions_run\",\"traffic\",\"format_paint\",\"email\",\"cloud\",\"build\",\"favorite\",\"face\",\"http\",\"info\",\"input\",\"lock\",\"message\",\"highlight\",\"computer\",\"toys\",\"security\"]";
            icons = Arrays.asList(ObjectMapperSerializer.deserialize(iconJson, String[].class));
        }
        return Response.ok(icons).build();
    }

    /**
     * Gets the list of functions that can be used to produce partition values.
     *
     * @return an HTTP response containing the list of formulas
     */
    @GET
    @Path("/partition-functions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of partition functions.", notes = "These functions can be used to produce partition values.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the partition functions.", response = String.class, responseContainer = "Set")
    )
    @Nonnull
    public Response partitionFunctions() {
        final Stream<String> kyloFunctions = Stream.of("val", "to_date", "year", "month", "day", "hour", "minute");
        final Stream<String> userFunctions = Arrays.stream(env.getProperty("kylo.metadata.udfs", "").split(",")).map(String::trim).filter(StringUtils::isNotEmpty);
        return Response.ok(Stream.concat(kyloFunctions, userFunctions).collect(Collectors.toSet())).build();
    }

    @POST
    @Path("/encrypt")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Encrypts provided value")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns encrypted value", response = String.class)
    )
    public Response encrypt(String unencrypted) {
        this.accessController.checkPermission(AccessController.SERVICES, EncryptionAccessControl.ACCESS_ENCRYPTION);

        String encrypted = encryptionService.encrypt(unencrypted);
        return Response.ok(encrypted).build();
    }

    /**
     * Gets information about the current user.
     */
    @POST
    @Path("/decrypt")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Decrypts provided value")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns decrypted value", response = String.class)
    )
    public Response decrypt(String encrypted) {
        this.accessController.checkPermission(AccessController.SERVICES, EncryptionAccessControl.ACCESS_ENCRYPTION);

        String decrypted = encryptionService.decrypt(encrypted);
        return Response.ok(decrypted).build();
    }

}
