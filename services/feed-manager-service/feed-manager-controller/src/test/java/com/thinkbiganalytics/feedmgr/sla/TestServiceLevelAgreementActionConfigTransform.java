package com.thinkbiganalytics.feedmgr.sla;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.metadata.sla.EmailServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.policy.PolicyTransformException;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 */
public class TestServiceLevelAgreementActionConfigTransform {

    @Test
    public void testEmailResponderConfiguration() throws IOException {
        String email = "scott@home.com";
        EmailServiceLevelAgreementActionConfiguration config = new EmailServiceLevelAgreementActionConfiguration(email);
        ServiceLevelAgreementActionUiConfigurationItem uiModel = ServiceLevelAgreementActionConfigTransformer.instance().toUIModel(config);
        uiModel.getProperty("EmailAddresses").setValue(email);
        EmailServiceLevelAgreementActionConfiguration convertedPolicy = fromUI(uiModel, EmailServiceLevelAgreementActionConfiguration.class);
        Assert.assertEquals(email, convertedPolicy.getEmailAddresses());


    }


    private <T extends ServiceLevelAgreementActionConfiguration> T fromUI(ServiceLevelAgreementActionUiConfigurationItem uiModel, Class<T> policyClass) {
        try {
            ServiceLevelAgreementActionConfiguration policy = ServiceLevelAgreementActionConfigTransformer.instance().fromUiModel(uiModel);
            return (T) policy;
        } catch (PolicyTransformException e) {
            e.printStackTrace();

        }
        return null;
    }


}
