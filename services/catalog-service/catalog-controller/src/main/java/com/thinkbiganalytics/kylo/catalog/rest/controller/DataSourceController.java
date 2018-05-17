package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path(DataSourceController.BASE)
public class DataSourceController {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSourceController.class);

    static final String BASE = "/v1/catalog/datasource";

    private static final MessageSource MESSAGES;

    static {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("DataSourceMessages");
        MESSAGES = messageSource;
    }

    @Inject
    private HttpServletRequest request;

    @GET
    @Path("{id}/browse")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists files on path")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of files on path", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Datasource does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list files", response = RestResponseStatus.class)
                  })
    public Response getUploads(@PathParam("id") @UUID final String dataSourceId, @QueryParam("path") String path) {
        log.entry(dataSourceId);

        final List<DataSetFile> files;
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            files = new ArrayList<>(0);
        } else {
            files = new ArrayList<>(list.length);
            for (File file : list) {
                DataSetFile dataSetFile = new DataSetFile();
                dataSetFile.setDirectory(file.isDirectory());
                dataSetFile.setLength(file.length());
                dataSetFile.setModificationTime(file.lastModified());
                dataSetFile.setName(file.getName());
                dataSetFile.setPath(file.getPath());
                files.add(dataSetFile);
            }
        }

        return log.exit(Response.ok(files).build());
    }

    /**
     * Gets the specified message in the current locale.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code) {
        return MESSAGES.getMessage(code, null, RequestContextUtils.getLocale(request));
    }
}
