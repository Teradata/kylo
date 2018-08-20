package com.thinkbiganalytics.search.transform;

/*-
 * #%L
 * kylo-search-elasticsearch-rest
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

import com.thinkbiganalytics.search.rest.model.Pair;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for tabular rendering of user property search results
 */
public class UserPropertySearchResultRenderTest {

    @Test
    public void testUserPropertySearchResultRender_KeyMatches() {
        String s = "<font style='font-weight:bold'>pk10A</font> common=Aa1000x";
        ElasticSearchRestSearchResultTransform elasticSearchRestSearchResultTransform = new ElasticSearchRestSearchResultTransform();
        Pair actualPair = elasticSearchRestSearchResultTransform.getPropertyNameValuePair(s);
        Assert.assertEquals("<font style='font-weight:bold'>pk10A</font> common", actualPair.getKey());
        Assert.assertEquals("Aa1000x", actualPair.getValue());
    }

    @Test
    public void testUserPropertySearchResultRender_ValueMatches() {
        String s = "pk10A common=<font style='font-weight:bold'>Aa1000x</font>";
        ElasticSearchRestSearchResultTransform elasticSearchRestSearchResultTransform = new ElasticSearchRestSearchResultTransform();
        Pair actualPair = elasticSearchRestSearchResultTransform.getPropertyNameValuePair(s);
        Assert.assertEquals("pk10A common", actualPair.getKey());
        Assert.assertEquals("<font style='font-weight:bold'>Aa1000x</font>", actualPair.getValue());
    }

    @Test
    public void testUserPropertySearchResultRender_NeitherKeyNorValueMatches() {
        String s = "pk10A common=Aa1000x";
        ElasticSearchRestSearchResultTransform elasticSearchRestSearchResultTransform = new ElasticSearchRestSearchResultTransform();
        Pair actualPair = elasticSearchRestSearchResultTransform.getPropertyNameValuePair(s);
        Assert.assertEquals("pk10A common", actualPair.getKey());
        Assert.assertEquals("Aa1000x", actualPair.getValue());
    }

    @Test
    public void testUserPropertySearchResultRender_BothKeyAndValueMatches() {
        String s = "<font style='font-weight:bold'>pk10A</font> common=<font style='font-weight:bold'>Aa1000x</font>";
        ElasticSearchRestSearchResultTransform elasticSearchRestSearchResultTransform = new ElasticSearchRestSearchResultTransform();
        Pair actualPair = elasticSearchRestSearchResultTransform.getPropertyNameValuePair(s);
        Assert.assertEquals("<font style='font-weight:bold'>pk10A</font> common", actualPair.getKey());
        Assert.assertEquals("<font style='font-weight:bold'>Aa1000x</font>", actualPair.getValue());
    }
}
