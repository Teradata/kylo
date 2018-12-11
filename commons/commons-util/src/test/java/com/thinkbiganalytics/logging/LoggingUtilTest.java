/**
 * 
 */
package com.thinkbiganalytics.logging;

/*-
 * #%L
 * kylo-commons-util
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import static org.assertj.core.api.Assertions.assertThat;

import com.thinkbiganalytics.logging.LoggingUtil.LogLevel;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
public class LoggingUtilTest {
    
    public enum TestFields { AAA, BBB, CCC };
    
    private static final Object[] ARGS = { "a", "b", "c" };
    
    private static final Function<TestFields, Object> VALUES_FUNC = (e) -> {
        switch (e) {
            case AAA: return "a";
            case BBB: return "b";
            case CCC: return "c";
            default: return "";
        }
    };
    
    @Mock
    private Logger logger;
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTrace() {
        LoggingUtil.log(this.logger, LogLevel.TRACE, "log message", ARGS);
        
        Mockito.verify(this.logger).trace("log message", ARGS);
    }
    
    @Test
    public void testWarnString() {
        LoggingUtil.log(this.logger, LogLevel.level("warn"), "log message", ARGS);
        
        Mockito.verify(this.logger).warn("log message", ARGS);
    }
    
    @Test
    public void testDynamicSubstitution() {
        LoggingUtil.log(logger, LogLevel.ERROR, TestFields.class, "log message {AAA} {BBB} {CCC}", VALUES_FUNC);
        
        Mockito.verify(this.logger).error("log message {} {} {}", ARGS);
    }
    
    @Test
    public void testDynamicWithException() {
        Exception ex = new IllegalArgumentException();
        LoggingUtil.log(logger, LogLevel.DEBUG, TestFields.class, "log message {AAA} {BBB} {CCC}", ex, VALUES_FUNC);
        
        Object[] expectedArgs = Stream.concat(Arrays.stream(ARGS), Stream.of(ex)).toArray(Object[]::new);
        
        Mockito.verify(this.logger).debug("log message {} {} {}", expectedArgs);
    }
    
    @Test
    public void testDeriveArguments() {
        Object[] values = LoggingUtil.deriveArguments(Arrays.asList(TestFields.AAA, TestFields.BBB, TestFields.CCC), VALUES_FUNC);
        
        assertThat(values).containsExactly(ARGS);
    }
    
    @Test
    public void testDeriveArgumentsWithException() {
        Exception ex = new IllegalArgumentException();
        Object[] values = LoggingUtil.deriveArguments(Arrays.asList(TestFields.AAA, TestFields.BBB, TestFields.CCC), ex, VALUES_FUNC);
        
        Object[] expectedArgs = Stream.concat(Arrays.stream(ARGS), Stream.of(ex)).toArray(Object[]::new);
        
        assertThat(values).containsExactly(expectedArgs);
    }
    
    @Test
    public void testExtractArguments() {
        Object[] values = LoggingUtil.extractArguments(TestFields.class, "log message {AAA} {BBB} {CCC}", VALUES_FUNC);
        
        assertThat(values).containsExactly(ARGS);
    }
    
    @Test
    public void testExtractArgumentsWithException() {
        Exception ex = new IllegalArgumentException();
        Object[] values = LoggingUtil.extractArguments(TestFields.class, "log message {AAA} {BBB} {CCC}", ex, VALUES_FUNC);
        
        Object[] expectedArgs = Stream.concat(Arrays.stream(ARGS), Stream.of(ex)).toArray(Object[]::new);
        
        assertThat(values).containsExactly(expectedArgs);
    }

}
