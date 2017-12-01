package com.thinkbiganalytics.hashing;

/*-
 * #%L
 * kylo-commons-util
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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Hashing Utility
 */
public class HashingUtilTest {

    private static final Logger log = LoggerFactory.getLogger(HashingUtilTest.class);

    @Test
    public void testHashValue_NormalString() {
        String hash = HashingUtil.getHashMD5("{\"fname\":\"Mango\",\"fcolor\":\"Yellow\",\"ftaste\":\"Sweet\",\"kylo_schema\":\"fruit_items\",\"kylo_table\":\"fruits_feed_01\"}");
        Assert.assertEquals("de66d7905e2c92cb205e4070e101038f", hash);
        log.info("Test hash for normal string: OK");
    }

    @Test
    public void testHashValue_NullString() {
        String hash = HashingUtil.getHashMD5(null);
        Assert.assertNull(hash);
        log.info("Test hash for null string: OK");
    }

    @Test
    public void testHashValue_EmptyString() {
        String hash = HashingUtil.getHashMD5("");
        Assert.assertEquals("d41d8cd98f00b204e9800998ecf8427e", hash);
        log.info("Test hash for empty string: OK");
    }
}
