package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * kylo-schema-discovery-model2
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultFieldTest {

    @Test
    public void testCommentsNoNewLine() {
        DefaultField defaultField = new DefaultField();
        String desc = "identifier of person";
        defaultField.setDescription(desc);
        assertEquals(desc, defaultField.getDescriptionWithoutNewLines());
    }

    @Test
    public void testCommentsWithNewLine() {
        DefaultField defaultField = new DefaultField();
        String desc = "identifier\nof\nperson";
        String descExpected = "identifier\\nof\\nperson";
        defaultField.setDescription(desc);
        assertEquals(descExpected, defaultField.getDescriptionWithoutNewLines());
    }
}
