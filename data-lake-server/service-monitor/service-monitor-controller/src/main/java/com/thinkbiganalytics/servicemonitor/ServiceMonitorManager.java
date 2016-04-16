package com.thinkbiganalytics.servicemonitor;

import com.thinkbiganalytics.servicemonitor.check.*;
import com.thinkbiganalytics.servicemonitor.model.*;
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
 *

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
     * Run a service check on the system
     *
     * @return
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
