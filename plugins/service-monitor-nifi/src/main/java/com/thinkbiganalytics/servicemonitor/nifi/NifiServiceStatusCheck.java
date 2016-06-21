package com.thinkbiganalytics.servicemonitor.nifi;


import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.servicemonitor.check.ServiceStatusCheck;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.apache.nifi.web.api.entity.AboutEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 9/30/15.
 */

public class NifiServiceStatusCheck implements ServiceStatusCheck {

  @Autowired
  @Qualifier("nifiRestClient")
 private NifiRestClient nifiRestClient;

  public NifiServiceStatusCheck(){
  }


  @Override
  public ServiceStatusResponse healthCheck() {

    String serviceName = "Nifi";
    String componentName = "Nifi";
    ServiceComponent component = null;

    Map<String, Object> properties = new HashMap<>();

    try {
      AboutEntity aboutEntity = nifiRestClient.getNifiVersion();

      String nifiVersion = aboutEntity.getAbout().getVersion();
      component =
          new DefaultServiceComponent.Builder(componentName + " - " + nifiVersion, ServiceComponent.STATE.UP)
              .message("Nifi is up.").properties(properties).build();
    } catch (Exception e) {
      component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).exception(e).build();
    }

    return new DefaultServiceStatusResponse(serviceName, Arrays.asList(component));
  }

  public void setNifiRestClient(NifiRestClient nifiRestClient) {
    this.nifiRestClient = nifiRestClient;
  }
}
