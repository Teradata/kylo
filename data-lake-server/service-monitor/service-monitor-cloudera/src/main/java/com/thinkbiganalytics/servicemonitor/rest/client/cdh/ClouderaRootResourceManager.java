package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.DataView;
import com.cloudera.api.v1.RootResourceV1;

/**
 * Created by sr186054 on 10/8/15.
 */
public class ClouderaRootResourceManager {

  private static DataView dataView = DataView.FULL;


  public static ClouderaRootResource getRootResource(ApiRootResource apiRootResource) {

    String version = apiRootResource.getCurrentVersion();
    RootResourceV1 rootResource = null;

    if ("v1".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV1();
    } else if ("v2".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV2();
    } else if ("v3".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV3();
    } else if ("v4".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV4();
    } else if ("v5".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV5();
    } else if ("v6".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV6();
    } else if ("v7".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV7();
    } else if ("v8".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV8();
    } else if ("v9".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV8();
    } else if ("v10".equalsIgnoreCase(version)) {
      rootResource = apiRootResource.getRootV10();
    } else {
      rootResource = apiRootResource.getRootV1();
    }

    return new DefaultClouderaRootResource(rootResource);


  }


}
