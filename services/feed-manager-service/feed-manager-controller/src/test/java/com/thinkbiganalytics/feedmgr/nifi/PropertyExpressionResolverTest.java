package com.thinkbiganalytics.feedmgr.nifi;

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

import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@ContextConfiguration(classes = PropertyExpressionResolverConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PropertyExpressionResolverTest {

    /**
     * Default processor group id or name
     */
    private static final String DEFAULT_GROUP = "group";

    /**
     * Default property key name
     */
    private static final String DEFAULT_KEY = "key";

    /**
     * Default processor type
     */
    private static final String DEFAULT_TYPE = "com.example.UpdateAttributes";

    private static final String DEFAULT_PROCESSOR_NAME="Processor Name";

    /**
     * Property key name for static configuration
     */
    private static final String STATIC_KEY = "App Key";

    /**
     * Resolves expressions in property values
     */
    @Inject
    private PropertyExpressionResolver resolver;

    @Test
    public void testContainsVariable() {
        Assert.assertTrue(resolver.containsVariablesPatterns("abc 123 ${var}"));
    }


    @Test
    public void testStrSubstitutor() {
        String template1 = "${metadata.feedName}_config.xx";
        Map<String, String> map1 = new HashMap<>();
        map1.put("metadata.feedName", "category.feed");
        StrSubstitutor ss1 = new StrSubstitutor(map1);
        ss1.setEnableSubstitutionInVariables(true);
        Assert.assertEquals("category.feed_config.xx", ss1.replace(template1));

        String template2 = "$${${metadata.feedName}_config.xx}";
        StrSubstitutor ss2 = new StrSubstitutor(map1);
        ss2.setEnableSubstitutionInVariables(true);
        Assert.assertEquals("${category.feed_config.xx}", ss2.replace(template2));

        String template3 = "${${metadata.feedName}_config.xx}";
        map1.put("category.feed_config.xx", "runtime value");
        StrSubstitutor ss3 = new StrSubstitutor(map1);
        ss3.setEnableSubstitutionInVariables(true);
        Assert.assertEquals("runtime value", ss3.replace(template3));

    }

    /**
     * Verifies resolving expressions in property values.
     */
    @Test
    public void resolveExpression() {
        final FeedMetadata metadata = new FeedMetadata();
        metadata.setSystemFeedName("myfeed");

        // Verify config variable
        final NifiProperty prop1 = createProperty("${config.test.value}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop1));
        Assert.assertEquals("hello-world", prop1.getValue());

        // Verify metadata variable
        final NifiProperty prop2 = createProperty("${metadata.systemFeedName}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop2));
        Assert.assertEquals("myfeed", prop2.getValue());

        // Verify static config
        final NifiProperty prop3 = createProperty(STATIC_KEY, "${metadata.systemFeedName}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop3));
        Assert.assertEquals("myapp", prop3.getValue());

        final NifiProperty prop4 = createProperty(STATIC_KEY, "${config.test.value}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop4));
        Assert.assertEquals("hello-world", prop4.getValue());

        final NifiProperty prop5 = createProperty(STATIC_KEY, "");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop5));
        Assert.assertEquals("myapp", prop5.getValue());

        // Verify multiple variables
        final NifiProperty prop6 = createProperty("${metadata.systemFeedName}.${config.test.value}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop6));
        Assert.assertEquals("myfeed.hello-world", prop6.getValue());

        // Verify multiple variables
        final NifiProperty prop7 = createProperty("$${${metadata.systemFeedName}.${config.test.value}}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop7));
        Assert.assertEquals("${myfeed.hello-world}", prop7.getValue());

        // Verify multiple variables
        final NifiProperty prop8 = createProperty("${config.${metadata.systemFeedName}.${config.test.value}}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop8));
        Assert.assertEquals("runtime value", prop8.getValue());

        // Verify static text
        final NifiProperty prop9 = createProperty("config.test.value");
        Assert.assertFalse(resolver.resolveExpression(metadata, prop9));
        Assert.assertEquals("config.test.value", prop9.getValue());

        //verify replacement with NiFi el
        final NifiProperty prop10 = createProperty("property1","a value");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop10));
        Assert.assertEquals("/path/to/property1,${nifi.expression.property}", prop10.getValue());

        //verify replacement without NiFi el
        final NifiProperty prop11 = createProperty("Another Processor","property1","a value");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop11));
        Assert.assertEquals("/path/to/another_processor/property1/location", prop11.getValue());

        //verify replacement without NiFi el using default processor type replacement
        final NifiProperty prop12 = createProperty("My New Processor","property1","a value");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop12));
        Assert.assertEquals("/path/to/property1/location", prop12.getValue());


        //verify replacement without NiFi el using default processor type replacement
        final NifiProperty extraFiles = createProperty("extra_files","a value");
        Assert.assertTrue(resolver.resolveExpression(metadata, extraFiles));
        Assert.assertEquals("${table_field_policy_json_file},/usr/hdp/current/spark-client/conf/hive-site.xml",extraFiles.getValue());
        Assert.assertTrue(resolver.resolveExpression(metadata, extraFiles));
        Assert.assertEquals("${table_field_policy_json_file},/usr/hdp/current/spark-client/conf/hive-site.xml",extraFiles.getValue());


        final NifiProperty hiveSchema = createProperty(STATIC_KEY,"${config.hive.schema}");
        Assert.assertTrue(resolver.resolveExpression(metadata, hiveSchema));
        Assert.assertEquals("hive",hiveSchema.getValue());


    }


    /**
     * Verifies invalid expressions are resolved properly.
     */
    @Test
    public void resolveExpressionInvalid() {
        final FeedMetadata metadata = new FeedMetadata();

        // Verify missing config variable
        final NifiProperty prop1 = createProperty("${config.test.invalid}");
        Assert.assertFalse(resolver.resolveExpression(metadata, prop1));
        Assert.assertEquals("${config.test.invalid}", prop1.getValue());

        // Verify missing metadata variable
        final NifiProperty prop2 = createProperty("${metadata.invalid}");
        Assert.assertFalse(resolver.resolveExpression(metadata, prop2));
        Assert.assertEquals("${metadata.invalid}", prop2.getValue());

        // Verify empty variable
        final NifiProperty prop3 = createProperty("${}");
        Assert.assertFalse(resolver.resolveExpression(metadata, prop3));
        Assert.assertEquals("${}", prop3.getValue());
    }

    /**
     * Creates a new property with the specified value.
     *
     * @param value the value
     * @return the new property
     */
    @Nonnull
    public NifiProperty createProperty(@Nonnull final String value) {
        return createProperty(DEFAULT_KEY, value);
    }

    /**
     * Creates a new property with the specified key and value.
     *
     * @param key   the key name
     * @param value the value
     * @return the new property
     */
    @Nonnull
    public NifiProperty createProperty(@Nonnull final String key, @Nonnull final String value) {
       return createProperty(DEFAULT_PROCESSOR_NAME,key,value);
    }


    /**
     * Creates a new property with the specified key and value.
     *@param processorName the name of the processor
     * @param key   the key name
     * @param value the value
     * @return the new property
     */
    @Nonnull
    public NifiProperty createProperty(@Nonnull final String processorName,@Nonnull final String key, @Nonnull final String value) {
        final NifiProperty property = new NifiProperty(DEFAULT_GROUP, DEFAULT_GROUP, key, value);
        property.setProcessorType(DEFAULT_TYPE);
        property.setProcessorName(processorName);
        return property;
    }


    @Test
    public void testFeedMetadataProperties() {
        FeedMetadata metadata = new FeedMetadata();
        metadata.setSystemFeedName("feedSystemName");
        metadata.setCategory(new FeedCategory());
        metadata.setTable(new TableSetup());
        metadata.getTable().setSourceTableSchema(new DefaultTableSchema());
        metadata.getTable().setTableSchema(new DefaultTableSchema());
        metadata.getTable().getSourceTableSchema().setName("sourceTableName");
        metadata.getTable().getTableSchema().setName("tableSchemaName");

        final NifiProperty prop1 = createProperty("${metadata.table.sourceTableSchema.name}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop1));
        Assert.assertEquals("sourceTableName", prop1.getValue());


    }

    @Test
    public void testResolveValues() {

        List<NifiProperty> props = new ArrayList<>();
        props.add(newProperty("test.property", "${a}/${b} "));
        props.add(newProperty("test.property2", "${a2}/${c} "));
        props.add(newProperty("d", "fred"));
        props.add(newProperty("a", "${b} "));
        props.add(newProperty("b", "${c}"));
        props.add(newProperty("c", "${d}"));

        PropertyExpressionResolver.ResolvedVariables variables = resolver.resolveVariables("${a} - ${b} - ${test.property2}", props);
        Assert.assertEquals("fred - fred - ${a2}/fred", variables.getResolvedString());
        Map<String, String> resolvedVariables = variables.getResolvedVariables();
        Assert.assertEquals("fred", resolvedVariables.get("a"));
        Assert.assertEquals("fred", resolvedVariables.get("b"));
        Assert.assertEquals("fred", resolvedVariables.get("c"));
        Assert.assertEquals("fred", resolvedVariables.get("d"));
        Assert.assertEquals("${a2}/fred", resolvedVariables.get("test.property2"));
        Assert.assertEquals(5, resolvedVariables.size());
        System.out.println(resolvedVariables);
    }

    private NifiProperty newProperty(String key, String value) {
        NifiProperty p = new NifiProperty();
        p.setKey(key);
        p.setValue(value);
        return p;
    }
}
