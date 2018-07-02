package com.thinkbiganalytics.integration.domaintype;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.model.DomainType;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.junit.Assert.assertEquals;

/**
 * Creates and updates a data source
 */
public class DomainTypeIT extends IntegrationTestBase {

    @Test
    public void testCreateAndUpdateDatasource() {
        FieldStandardizationRule toUpperCase = new FieldStandardizationRule();
        FieldValidationRule email = new FieldValidationRule();

        toUpperCase.setName("Uppercase");
        toUpperCase.setDisplayName("Uppercase");
        toUpperCase.setDescription("Convert string to uppercase");
        toUpperCase.setObjectClassType("com.thinkbiganalytics.policy.standardization.UppercaseStandardizer");
        toUpperCase.setObjectShortClassType("UppercaseStandardizer");

        email.setName("email");
        email.setDisplayName("Email");
        email.setDescription("Valid email address");
        email.setObjectClassType("com.thinkbiganalytics.policy.validation.EmailValidator");
        email.setObjectShortClassType("EmailValidator");


        DomainType[] initialDomainTypes = getDomainTypes();


        //create new domain type
        DomainType dt = new DomainType();
        dt.setTitle("Domain Type 1");
        dt.setDescription("domain type created by integration tests");

        DomainType response = createDomainType(dt);
        assertEquals(dt.getTitle(), response.getTitle());
        assertEquals(dt.getDescription(), response.getDescription());
        assertEquals(null, response.getIcon());
        assertEquals(null, response.getIconColor());


        //assert new domain type was added
        DomainType[] currentDomainTypes = getDomainTypes();
        assertEquals(initialDomainTypes.length + 1, currentDomainTypes.length);


        //update existing domain type
        dt = getDomainType(response.getId());
        dt.setTitle("Domain Type 1 with updated title");
        dt.setDescription("domain type description updated by integration tests");
        dt.setIcon("stars");
        dt.setIconColor("green");
        DefaultField field = new DefaultField();
        Tag tag1 = new DefaultTag("tag1");
        Tag tag2 = new DefaultTag("tag2");
        field.setTags(Arrays.asList(tag1, tag2));
        field.setName("field-name");
        field.setDerivedDataType("decimal");
        field.setPrecisionScale("9, 3");
        dt.setField(field);
        dt.setFieldNamePattern("field-name-pattern");
        dt.setFieldPolicy(newPolicyBuilder(null).withStandardisation(toUpperCase).withValidation(email).toPolicy());
        dt.setRegexPattern("regex-pattern");


        DomainType updated = createDomainType(dt);
        assertEquals(dt.getTitle(), updated.getTitle());
        assertEquals(dt.getDescription(), updated.getDescription());
        assertEquals("stars", updated.getIcon());
        assertEquals("green", updated.getIconColor());

        Field updatedField = updated.getField();
        assertEquals(field.getName(), updatedField.getName());
        assertEquals(field.getDerivedDataType(), updatedField.getDerivedDataType());
        assertEquals(field.getPrecisionScale(), updatedField.getPrecisionScale());
        assertEquals(field.getTags().size(), updatedField.getTags().size());
        assertEquals(tag1.getName(), updatedField.getTags().get(0).getName());
        assertEquals(tag2.getName(), updatedField.getTags().get(1).getName());
        assertEquals(dt.getFieldNamePattern(), updated.getFieldNamePattern());
        assertEquals(dt.getRegexPattern(), updated.getRegexPattern());

        FieldStandardizationRule updatedStandardisation = updated.getFieldPolicy().getStandardization().get(0);
        assertEquals(toUpperCase.getName(), updatedStandardisation.getName());
        assertEquals(toUpperCase.getObjectShortClassType(), updatedStandardisation.getObjectShortClassType());

        FieldValidationRule updatedValidation = updated.getFieldPolicy().getValidation().get(0);
        assertEquals(email.getName(), updatedValidation.getName());
        assertEquals(email.getObjectShortClassType(), updatedValidation.getObjectShortClassType());


        //assert domain type was updated, rather than added
        currentDomainTypes = getDomainTypes();
        assertEquals(initialDomainTypes.length + 1, currentDomainTypes.length);

        //delete domain type
        deleteDomainType(dt.getId());
        currentDomainTypes = getDomainTypes();
        assertEquals(initialDomainTypes.length , currentDomainTypes.length);

        //assert domain type was removed
        getDomainTypeExpectingStatus(dt.getId(), HTTP_NOT_FOUND);
    }
}
