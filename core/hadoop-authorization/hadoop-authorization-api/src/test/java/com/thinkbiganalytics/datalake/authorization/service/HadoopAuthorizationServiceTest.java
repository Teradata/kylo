package com.thinkbiganalytics.datalake.authorization.service;

/*-
 * #%L
 * thinkbig-hadoop-authorization-api
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

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 */
public class HadoopAuthorizationServiceTest {

    @Test
    public void testConvertNewlineDelimetedTextToList() throws Exception {
        String stringWithNewLines = "this\nis\na\ntest";
        List<String> result = HadoopAuthorizationService.convertNewlineDelimetedTextToList(stringWithNewLines);
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(0).equals("this"));
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(1).equals("is"));
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(2).equals("a"));
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(3).equals("test"));
    }

    @Test
    public void testConvertNewlineDelimetedTextToListForNull() throws Exception {
        String stringWithNewLines = null;
        List<String> result = HadoopAuthorizationService.convertNewlineDelimetedTextToList(stringWithNewLines);
        assertTrue("The list should not be null and should be empty", result != null && result.size() == 0);
    }

    @Test
    public void testConvertNewlineDelimetedTextToListForEmptyString() throws Exception {
        String stringWithNewLines = "";
        List<String> result = HadoopAuthorizationService.convertNewlineDelimetedTextToList(stringWithNewLines);
        assertTrue("The list should should be empty", result != null && result.size() == 0);
    }
}
