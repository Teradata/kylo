package com.thinkbiganalytics.servicemonitor;

/*-
 * #%L
 * thinkbig-service-monitor-controller
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

import com.thinkbiganalytics.servicemonitor.check.ServiceStatusCheck;
import com.thinkbiganalytics.servicemonitor.check.ServicesStatusCheck;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Application Bean that looks for all beans implementing either ServiceStatusCheck or ServicesStatusCheck
 */
@Configuration
public class ServiceMonitorManager implements ApplicationContextAware, InitializingBean {

  private List<ServiceStatusCheck> services;
  private List<ServicesStatusCheck> servicesHealth;
  private ApplicationContext applicationContext;
  private int totalServices;

  public ServiceMonitorManager() {
    this.services = new ArrayList<>();
    this.servicesHealth = new ArrayList<>();

  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Map<String, ServiceStatusCheck> map = applicationContext.getBeansOfType(ServiceStatusCheck.class);
    if (map != null) {
      this.services.addAll(map.values());
    }
    Map<String, ServicesStatusCheck> servicesMap = applicationContext.getBeansOfType(ServicesStatusCheck.class);
    if (servicesMap != null) {
      this.servicesHealth.addAll(servicesMap.values());
    }
    totalServices = this.services.size() + this.servicesHealth.size();

  }


  private Callable<List<ServiceStatusResponse>> servicesCheckAsCallable(final ServicesStatusCheck servicesStatusCheck) {
    return new Callable<List<ServiceStatusResponse>>() {
      @Override
      public List<ServiceStatusResponse> call() throws Exception {
        return servicesStatusCheck.healthCheck();
      }
    };
  }

  private Callable<List<ServiceStatusResponse>> serviceCheckAsCallable(final ServiceStatusCheck servicesStatusCheck) {
    return new Callable<List<ServiceStatusResponse>>() {
      @Override
      public List<ServiceStatusResponse> call() throws Exception {
        return Arrays.asList(servicesStatusCheck.healthCheck());
      }
    };
  }


  /**
   *  Run a service check on the system.
   *  Each service will run in a separate thread and return status back
   * @return a list of service status objects
   */
  public List<ServiceStatusResponse> doServiceCheck() {
    List<ServiceStatusResponse> serviceHealthResponseList = new ArrayList<ServiceStatusResponse>();
    if (totalServices > 0) {
      ExecutorService pool = Executors.newFixedThreadPool(totalServices);
      List<Callable<List<ServiceStatusResponse>>> tasks = new ArrayList<>();
      for (ServiceStatusCheck serviceHealth : services) {
        tasks.add(serviceCheckAsCallable(serviceHealth));
      }
      for (ServicesStatusCheck serviceHealth : servicesHealth) {
        tasks.add(servicesCheckAsCallable(serviceHealth));
      }

      try {
        List<Future<List<ServiceStatusResponse>>> results = pool.invokeAll(tasks);

        for (Future<List<ServiceStatusResponse>> result : results) {
          try {
            List<ServiceStatusResponse> responses = result.get();
            if (responses != null) {
              for (ServiceStatusResponse response : responses) {
                if (response != null) {
                  serviceHealthResponseList.add(response);
                }
              }
            }

          } catch (Exception e) {

          }
        }
      } catch (Exception e) {

      }
      pool.shutdown();

    }
    return serviceHealthResponseList;
  }


}
