package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.es.ElasticSearch;
import com.thinkbiganalytics.es.SearchResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(tags = "Feed Manager: Search", produces = "application/json")
@Path("/v1/feedmgr/search")
@Component
public class ElasticSearchRestController {

    @Autowired
    ElasticSearch elasticSearch;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response search(@QueryParam("q") String query, @QueryParam("rows") @DefaultValue("20") Integer rows, @QueryParam("start") @DefaultValue("0") Integer start) {

        SearchResult result = elasticSearch.search(query, rows, start);

        return Response.ok(result).build();
    }


}
