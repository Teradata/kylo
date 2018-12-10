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

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 */
public class LoggingUtil {
    
    public enum LogLevel { 
        ERROR(logger -> (msg, args) -> logger.error(msg, args)), 
        WARN(logger -> (msg, args) -> logger.warn(msg, args)), 
        INFO(logger -> (msg, args) -> logger.info(msg, args)), 
        DEBUG(logger -> (msg, args) -> logger.debug(msg, args)), 
        TRACE(logger -> (msg, args) -> logger.trace(msg, args)); 
        
        private final Function<Logger, BiConsumer<String, Object[]>> loggingFunc;
        
        LogLevel(Function<Logger, BiConsumer<String, Object[]>> func) {
            loggingFunc = func;
        }
        
        public static LogLevel level(String value) {
            return LogLevel.valueOf(value.toUpperCase());
        }
        
        public void log(Logger logger, String message, Object... args) {
            loggingFunc.apply(logger).accept(message, args);
        }
    }
    
    private static final String TOKEN_REGEX = "\\{\\s*(\\w+)\\s*\\}";
    private static final Pattern TOKENS_PATTERN = Pattern.compile("\\s*" + TOKEN_REGEX + "\\s*", Pattern.CASE_INSENSITIVE);

    public static void log(Logger logger, LogLevel level, String message, Object... args) {
        level.log(logger, message, args);
    }
    
    public static <E extends Enum<E>> void log(Logger logger, LogLevel level, Class<E> tokensType, String message, Function<E, Object> tokenValueFunct) {
        log(logger, level, tokensType, message, null, tokenValueFunct);
    }
    
    public static <E extends Enum<E>> void log(Logger logger, LogLevel level, Class<E> tokensType, String message, Throwable th, Function<E, Object> tokenValueFunct) {
        String logMsg = toLogMessage(message);
        Object[] args = extractArguments(tokensType, message, th, tokenValueFunct);
        
        level.log(logger, logMsg, args);
    }
    
    public static <E extends Enum<E>> Object[] extractArguments(Class<E> tokensType, String message, Function<E, Object> tokenValueFunct) {
        return extractTokens(tokensType, message).stream()
            .map(tokenValueFunct)
            .toArray(Object[]::new);
    }
    
    public static <E extends Enum<E>> Object[] extractArguments(Class<E> tokensType, String message, Throwable th, Function<E, Object> tokenValueFunct) {
        return Stream.concat(extractTokens(tokensType, message).stream().map(tokenValueFunct),
                             th != null ? Stream.of(th) : Stream.empty())
            .toArray(Object[]::new);
    }
    
    public static <E extends Enum<E>> Object[] deriveArguments(List<E> tokens, Function<E, Object> tokenValueFunct) {
        return tokens.stream()
            .map(tokenValueFunct)
            .toArray(Object[]::new);
    }
    
    public static <E extends Enum<E>> Object[] deriveArguments(List<E> tokens, Throwable th, Function<E, Object> tokenValueFunct) {
        return Stream.concat(tokens.stream().map(tokenValueFunct),
                             Stream.of(th))
            .toArray(Object[]::new);
    }

    public static <E extends Enum<E>> List<E> extractTokens(Class<E> tokensType, String message) {
        List<E> tokens = new ArrayList<>();
        Matcher matcher = TOKENS_PATTERN.matcher(message);
        
        while (matcher.find()) {
            String token = matcher.group(1).toUpperCase();
            try {
                tokens.add(Enum.valueOf(tokensType, token));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported logging field name: " + token);
            }
        }
        
        return tokens;
    }

    public static String toLogMessage(String message) {
        return message.replaceAll(TOKEN_REGEX, "{}");
    }
}
