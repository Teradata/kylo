package com.thinkbiganalytics.servicemonitor.rest.controller;

/*-
 * #%L
 * kylo-service-monitor-controller
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

import com.thinkbiganalytics.test.FunctionalTest;

import org.junit.Test;

/**
 * Functional Test
 */
public class ServiceMonitorRestControllerFT extends FunctionalTest {

    @Test
    public void basicPingTest() {
        given()
            .when().get(ServiceMonitorRestController.V1_SERVICE_MONITOR)
            .then().log().all().statusCode(200);
    }
}
