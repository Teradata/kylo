package com.thinkbiganalytics.metadata.jpa.support;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import java.nio.charset.Charset;

/**
 * Created by sr186054 on 7/4/17.
 */
public class TestUtf8Decoder {


    @Test
    public void testInvalidStrings() throws Exception{

        String invalidStr = "This is an invalid string test öäü 有效地针 \u040A \u0404 \ufeff0021 \uD83D\uDC95 \u25E2  rl moe \uD83D\uDCAF https://t.co/tOaZ01kVup'with some other strings";
        String newStr = NormalizeAndCleanString.normalizeAndClean(invalidStr);
        Charset charset = Charset.forName("UTF-8");
        String s = new String(invalidStr.getBytes(), charset);
        int i = 0;
        Assert.assertNotNull(newStr);


    }

    @Test
    public void testNullStrings() throws Exception{

        String invalidStr = null;
        String newStr = NormalizeAndCleanString.normalizeAndClean(invalidStr);
        Assert.assertNull(newStr);


    }


}
