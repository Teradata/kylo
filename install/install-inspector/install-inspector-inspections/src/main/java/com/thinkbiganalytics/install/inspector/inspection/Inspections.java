package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector-inspections
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.FileSystemResource;

public class Inspections {

    public Object inspect(String path, String inspectionsPath, String isDevMode, String projectVersion) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        Configuration config = new DefaultConfiguration(path, inspectionsPath, isDevMode, projectVersion);

        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setLocation(new FileSystemResource(config.getServicesConfigLocation()));
        ppc.setIgnoreUnresolvablePlaceholders(true);
        ppc.setIgnoreResourceNotFound(false);
        ppc.postProcessBeanFactory(ctx.getBeanFactory());
        ppc.setEnvironment(ctx.getEnvironment());

        PropertySources sources = ppc.getAppliedPropertySources();
        sources.forEach(source -> ctx.getEnvironment().getPropertySources().addLast(source));

        ctx.scan("com.thinkbiganalytics.install.inspector.inspection"
            , "com.thinkbiganalytics.hive.config"
                 // , "com.thinkbiganalytics.server" - this will load the whole kylo-services app
                 // , "com.thinkbiganalytics.kerberos" - this too will scan 'com.thinkbiganalytics'
                 // , "com.thinkbiganalytics.nifi.rest.config"
        );
        ctx.refresh();

        InspectionService service = ctx.getBean(InspectionService.class);
        return service.inspect(config);
    }
}
