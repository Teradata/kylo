package com.thinkbiganalytics.install.inspector.config;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * %% Licensed under the Apache License, Version 2.0 (the "License");
 * %% you may not use this file except in compliance with the License.
 * %% You may obtain a copy of the License at
 * %%
 * %%     http://www.apache.org/licenses/LICENSE-2.0
 * %%
 * %% Unless required by applicable law or agreed to in writing, software
 * %% distributed under the License is distributed on an "AS IS" BASIS,
 * %% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * %% See the License for the specific language governing permissions and
 * %% limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.validation.ConstraintViolationProblemModule;

@Configuration
public class JacksonConfiguration {

    /*
     * Support for Hibernate types in Jackson.
     */
    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    /*
     * Jackson Afterburner module to speed up serialization/deserialization.
     */
    @Bean
    public AfterburnerModule afterburnerModule() {
        return new AfterburnerModule();
    }

    /*
     * Module for serialization/deserialization of RFC7807 Problem.
     */
    @Bean
    ProblemModule problemModule() {
        return new ProblemModule();
    }

    /*
     * Module for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

}
