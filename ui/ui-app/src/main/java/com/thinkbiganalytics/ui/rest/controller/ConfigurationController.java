package com.thinkbiganalytics.ui.rest.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.swagger.annotations.Api;

@Api(value = "configuration")
@Path("/v1/configuration")
public class ConfigurationController {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

  @Autowired
  Environment env;

	@Inject
	private ApplicationContext appContext;

	/**
	 * Get the configuration information
	 * @return A map of name value key pairs
	 */
	@GET
	@Path("/properties")
	@Produces({MediaType.APPLICATION_JSON })
	public Response getConfiguration() {
		Map<String,Object> response;

		try {
			Map<String, Object> map = new HashMap();
			for(Iterator it = ((AbstractEnvironment) this.appContext.getEnvironment()).getPropertySources().iterator(); it.hasNext(); ) {
				PropertySource propertySource = (PropertySource) it.next();
				if (propertySource instanceof PropertiesPropertySource) {
					map.putAll(((PropertiesPropertySource) propertySource).getSource());
				}
			}
			return Response.ok(map).build();
		}
		catch(Throwable t) {
			throw new WebApplicationException("Error getting Configuration Properties with error message "+t.getMessage(),Response.Status.BAD_REQUEST);
		}
	}

  @GET
  @Path("/module-urls")
  @Produces({MediaType.APPLICATION_JSON })
  public Response pipelineControllerUrl(){

    String url = "";
    String contextPath = env.getProperty("server.contextPath");
    if(StringUtils.isNotBlank(contextPath)){
      url = contextPath;
    }
    Map<String,String> map = new HashMap<>();
    map.put("opsMgr",url+"/ops-mgr/index.html");
    map.put("feedMgr",url+"/feed-mgr/index.html");
    return Response.ok(map).build();
  }



}
