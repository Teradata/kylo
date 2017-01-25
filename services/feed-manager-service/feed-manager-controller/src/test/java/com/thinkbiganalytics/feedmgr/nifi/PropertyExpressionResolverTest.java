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

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@ContextConfiguration(classes = PropertyExpressionResolverConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PropertyExpressionResolverTest {

    /** Default processor group id or name */
    private static final String DEFAULT_GROUP = "group";

    /** Default property key name */
    private static final String DEFAULT_KEY = "key";

    /** Default processor type */
    private static final String DEFAULT_TYPE = "com.example.UpdateAttributes";

    /** Property key name for static configuration */
    private static final String STATIC_KEY = "App Key";

    /** Resolves expressions in property values */
    @Inject
    private PropertyExpressionResolver resolver;

    /** Verifies resolving expressions in property values. */
    @Test
    public void resolveExpression() {
        final FeedMetadata metadata = new FeedMetadata();
        metadata.setSystemFeedName("myfeed");

        // Verify config variable
        final NifiProperty prop1 = createProperty("${config.test.value}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop1));
        Assert.assertEquals("Hello world!", prop1.getValue());

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
        Assert.assertEquals("Hello world!", prop4.getValue());

        final NifiProperty prop5 = createProperty(STATIC_KEY, "");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop5));
        Assert.assertEquals("myapp", prop5.getValue());

        // Verify multiple variables
        final NifiProperty prop6 = createProperty("${metadata.systemFeedName}.${config.test.value}");
        Assert.assertTrue(resolver.resolveExpression(metadata, prop6));
        Assert.assertEquals("myfeed.Hello world!", prop6.getValue());

        // Verify static text
        final NifiProperty prop7 = createProperty("config.test.value");
        Assert.assertFalse(resolver.resolveExpression(metadata, prop7));
        Assert.assertEquals("config.test.value", prop7.getValue());
    }

    /** Verifies invalid expressions are resolved properly. */
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
     * @param key the key name
     * @param value the value
     * @return the new property
     */
    @Nonnull
    public NifiProperty createProperty(@Nonnull final String key, @Nonnull final String value) {
        final NifiProperty property = new NifiProperty(DEFAULT_GROUP, DEFAULT_GROUP, key, value);
        property.setProcessorType(DEFAULT_TYPE);
        return property;
    }






    @Test
    public void testResolveValues(){

        List<NifiProperty> props = new ArrayList<>();
        props.add(newProperty("test.property","${a}/${b} "));
        props.add(newProperty("test.property2","${a2}/${c} "));
        props.add(newProperty("d","fred"));
        props.add(newProperty("a","${b} "));
        props.add(newProperty("b","${c}"));
        props.add(newProperty("c","${d}"));


        PropertyExpressionResolver.ResolvedVariables variables = resolver.resolveVariables("${a} - ${b} - ${test.property2}", props);
        int i = 0;
    }

    private NifiProperty newProperty(String key, String value){
        NifiProperty p = new NifiProperty();
        p.setKey(key);
        p.setValue(value);
        return p;
    }
}
