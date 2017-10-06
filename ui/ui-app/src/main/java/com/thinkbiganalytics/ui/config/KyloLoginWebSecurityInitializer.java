package com.thinkbiganalytics.ui.config;

/*-
 * #%L
 * kylo-ui-app
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

import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.SessionTrackingMode;

/**
 * Prevent the JSessionId from being put in the url
 */
@Configuration
public class KyloLoginWebSecurityInitializer extends AbstractSecurityWebApplicationInitializer {
 // Disables URL-based sessions (no more 'jsessionid' in the URL using Tomcat)
    @Override
    protected Set<SessionTrackingMode> getSessionTrackingModes() {
        return EnumSet.of(SessionTrackingMode.COOKIE);
    }
}
