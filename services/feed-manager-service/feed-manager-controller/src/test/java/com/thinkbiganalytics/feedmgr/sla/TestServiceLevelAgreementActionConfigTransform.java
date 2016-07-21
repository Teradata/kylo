package com.thinkbiganalytics.feedmgr.sla;

import com.thinkbiganalytics.metadata.sla.alerts.EmailServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.policy.PolicyTransformException;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by sr186054 on 7/20/16.
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
