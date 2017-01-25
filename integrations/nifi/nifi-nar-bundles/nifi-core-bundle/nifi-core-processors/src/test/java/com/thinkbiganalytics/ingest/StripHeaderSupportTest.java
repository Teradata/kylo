package com.thinkbiganalytics.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;


public class StripHeaderSupportTest {

    private StripHeaderSupport headerSupport = new StripHeaderSupport();

    @Test
    public void testDoStripHeader() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("name,phone,zip\n");
        sb.append("Joe,phone,95121\n");
        sb.append("Sally,phone,95121\n");
        sb.append("Sam,phone,95120\n");
        sb.append("Michael,phone,94550\n");
        long bytes = headerSupport.findHeaderBoundary(1, new ByteArrayInputStream(sb.toString().getBytes()));
        assertEquals(15, bytes);
    }

}
