package com.thinkbiganalytics.nifi.rest;

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
 * Created by sr186054 on 6/20/16.
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
