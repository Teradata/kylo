package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.policy.rest.model.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.standardization.DateTimeStandardizer;
import com.thinkbiganalytics.policy.standardization.DefaultValueStandardizer;
import com.thinkbiganalytics.policy.validation.RangeValidator;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
public class TestJsonPolicies {


    @Test
    public void testJson() throws IOException {
        List<com.thinkbiganalytics.policy.rest.model.FieldPolicy> fieldPolicies = new ArrayList<>();
        List<FieldStandardizationRule> standardizationPolicyList = new ArrayList<>();
        List<FieldValidationRule> validationRules = new ArrayList<>();

        DefaultValueStandardizer defaultValueStandardizer = new DefaultValueStandardizer("My Default");
        standardizationPolicyList.add(StandardizationAnnotationTransformer.instance().toUIModel(defaultValueStandardizer));

        DateTimeStandardizer
            dateTimeStandardizer =
            new DateTimeStandardizer("MM/DD/YYYY", DateTimeStandardizer.OutputFormats.DATETIME_NOMILLIS);
        standardizationPolicyList.add(StandardizationAnnotationTransformer.instance().toUIModel(dateTimeStandardizer));

        RangeValidator validator = new RangeValidator(10, 20);
        validationRules.add(ValidatorAnnotationTransformer.instance().toUIModel(validator));

        fieldPolicies.add(new FieldPolicyBuilder("field1").addStandardization(standardizationPolicyList).addValidations(
            validationRules).build());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(fieldPolicies);

        FieldPoliciesJsonTransformer fieldPolicyTransformer = new FieldPoliciesJsonTransformer(json);
        Map<String, com.thinkbiganalytics.policy.FieldPolicy> policyMap = fieldPolicyTransformer.buildPolicies();
        com.thinkbiganalytics.policy.FieldPolicy field1Policy = policyMap.get("field1");
        Assert.assertEquals(2, field1Policy.getStandardizationPolicies().size());
        Assert.assertEquals(1, field1Policy.getValidators().size());

    }


    @Test
    public void test2() {
        String
            json =
            "[{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"registration_dttm\",\"standardization\":[],\"validation\":[{\"name\":\"Timestamp\",\"displayName\":\"Timestamp\",\"description\":\"Validate ISO8601 format\",\"properties\":[],\"objectClassType\":\"com.thinkbiganalytics.policy.validation.TimestampValidator\",\"regex\":null,\"type\":null}]},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"id\",\"standardization\":null,\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":true,\"fieldName\":\"first_name\",\"standardization\":[{\"name\":\"Uppercase\",\"displayName\":\"Uppercase\",\"description\":\"Convert string to uppercase\",\"properties\":[],\"objectClassType\":\"com.thinkbiganalytics.policy.standardization.UppercaseStandardizer\"}],\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":true,\"fieldName\":\"last_name\",\"standardization\":null,\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"email\",\"standardization\":null,\"validation\":[{\"name\":\"Email\",\"displayName\":\"Email\",\"description\":\"Valid email address\",\"properties\":[],\"objectClassType\":\"com.thinkbiganalytics.policy.validation.EmailValidator\",\"regex\":null,\"type\":null}]},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"gender\",\"standardization\":null,\"validation\":[{\"name\":\"Lookup\",\"displayName\":\"Lookup\",\"description\":\"Must be contained in the list\",\"properties\":[{\"name\":\"List\",\"displayName\":\"List\",\"value\":\"Male,Female\",\"placeholder\":\"\",\"type\":\"string\",\"hint\":\"Comma separated list of values\",\"objectProperty\":\"lookupList\",\"selectableValues\":[]}],\"objectClassType\":\"com.thinkbiganalytics.policy.validation.LookupValidator\",\"regex\":null,\"type\":null}]},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"ip_address\",\"standardization\":null,\"validation\":[{\"name\":\"IP Address\",\"displayName\":\"IP Address\",\"description\":\"Valid IP Address\",\"properties\":[],\"objectClassType\":\"com.thinkbiganalytics.policy.validation.IPAddressValidator\",\"regex\":null,\"type\":null}]},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"cc\",\"standardization\":[],\"validation\":[]},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"country\",\"standardization\":[],\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"birthdate\",\"standardization\":[{\"name\":\"Date/Time\",\"displayName\":\"Date/Time\",\"description\":\"Converts any date to ISO8601\",\"properties\":[{\"name\":\"Date Format\",\"displayName\":\"Date Format\",\"value\":\"MM/dd/YYYY\",\"placeholder\":\"\",\"type\":\"string\",\"hint\":\"Format Example: MM/DD/YYYY\",\"objectProperty\":\"inputDateFormat\",\"selectableValues\":[]},{\"name\":\"Output Format\",\"displayName\":\"Output Format\",\"value\":\"DATE_ONLY\",\"placeholder\":\"\",\"type\":\"select\",\"hint\":\"Choose an output format\",\"objectProperty\":\"outputFormat\",\"selectableValues\":[{\"label\":\"DATE_ONLY\",\"value\":\"DATE_ONLY\"},{\"label\":\"DATETIME\",\"value\":\"DATETIME\"},{\"label\":\"DATETIME_NOMILLIS\",\"value\":\"DATETIME_NOMILLIS\"}]}],\"objectClassType\":\"com.thinkbiganalytics.policy.standardization.DateTimeStandardizer\"}],\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"salary\",\"standardization\":[{\"name\":\"Strip Non Numeric\",\"displayName\":\"Strip Non Numeric\",\"description\":\"Remove any characters that are not numeric\",\"properties\":[],\"objectClassType\":\"com.thinkbiganalytics.policy.standardization.StripNonNumeric\"}],\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"title\",\"standardization\":null,\"validation\":null},{\"partition\":false,\"profile\":true,\"index\":false,\"fieldName\":\"comments\",\"standardization\":[{\"name\":\"Uppercase\",\"displayName\":\"Uppercase\",\"description\":\"Convert string to uppercase\",\"properties\":[],\"objectClassType\":\"com.thinkbiganalytics.policy.standardization.UppercaseStandardizer\"}],\"validation\":null}]";
        FieldPoliciesJsonTransformer fieldPolicyTransformer = new FieldPoliciesJsonTransformer(json);
        Map<String, com.thinkbiganalytics.policy.FieldPolicy> policyMap = fieldPolicyTransformer.buildPolicies();
        int i = 0;

    }
}
