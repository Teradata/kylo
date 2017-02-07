package com.thinkbiganalytics.nifi.rest;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.config.SpringNifiRestConfiguration;

import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringNifiRestConfiguration.class})
@PropertySource("classpath:application.properties")
public class NifiRestTest2 {


    @Inject
    LegacyNifiRestClient restClient;

    // @Test
    public void testEvent() {
        try {
            ProcessGroupDTO dto = restClient.getProcessGroupByName("root", "jhim");
            //ProcessGroupEntity entity= restClient.getProcessGroup("3813381f-2205-414c-bfcf-5900f45fcf601234",true,true);
            int i = 0;
        } catch (Exception e) {

            if (e instanceof NotFoundException) {
                int i = 0;
            } else if (e instanceof ProcessingException) {
                if (e.getCause() instanceof NoHttpResponseException) {
                    //connection error

                } else if (e.getCause() instanceof HttpHostConnectException) {
                    //connection error
                }
            }
        }

    }


}
