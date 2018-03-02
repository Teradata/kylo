package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
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

/**
 * The purpose of this annotation is to separate configuration of InspectorApp from
 * configuration of Kylo-Services, Kylo-UI and any other applications this InspectorApp is
 * going to inspect. <br>
 *
 * Marking configuration classes annotated with @Configuration with this @IgnoredByInspectorApp annotation
 * allows to have configuration for multiple applications on InspectorApp classpath. <br>
 *
 * In other words, this annotation is expected to be added to classes also annotated with @Configuration.
 * Configuration classes with this annotation will be excluded from applying their configuration to
 * InspectorApp. <br>
 */
public @interface IgnoredByInspectorApp {

}
