package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.DataView;
import com.cloudera.api.v1.RootResourceV1;
import com.google.common.reflect.Reflection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sr186054 on 10/8/15.
 */
public class ClouderaRootResourceManager {

  private static final Logger LOG = LoggerFactory.getLogger(ClouderaRootResourceManager.class);

  private static DataView dataView = DataView.FULL;


  public static ClouderaRootResource getRootResource(ApiRootResource apiRootResource) {

    String version = apiRootResource.getCurrentVersion();
    Integer numericVersion = new Integer(StringUtils.substringAfter(version,"v"));
    RootResourceV1 rootResource = null;
    Integer maxVersion = 10;
    if(numericVersion > maxVersion){
      numericVersion = maxVersion;
    }

    try {
      rootResource = (RootResourceV1)MethodUtils.invokeMethod(apiRootResource,"getRootV"+numericVersion);
      } catch (Exception  e) {

    }
    if(rootResource == null){
      LOG.info("Unable to get RootResource for version {}, returning version 1",numericVersion);
      rootResource = apiRootResource.getRootV1();
    }




    return new DefaultClouderaRootResource(rootResource);


  }


}
