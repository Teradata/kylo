package com.thinkbiganalytics.kylo.spark.client.jersey;

/*-
 * #%L
 * kylo-spark-livy-server
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


import org.apache.http.impl.cookie.*;

public class LivyCookieSpec extends RFC6265CookieSpec {

    // NOTE: to use this cookie spec edit the LivyCookieSpecProvider
    public LivyCookieSpec() {
        super(PublicSuffixDomainFilter.decorate(
                new BasicDomainHandler(), null),
                new LaxMaxAgeHandler(),
                new BasicSecureHandler(),
                new LaxExpiresHandler(),
                new BasicExpiresHandler(new String[]{DateUtils.PATTERN_RFC1123}));
    }
} // end class
