package com.thinkbiganalytics.classnameregistry;

/*-
 * #%L
 * thinkbig-classname-change-api
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes that are stored in the Kylo metadata may change their package name or class name over time. If these objects, especially those stored as JSON objects in our metadata store change their
 * signature overtime, the framework needs to know of these changes and create the correct object if the old class doesnt exist Any classes that are persisted and change their names over time need to
 * register the old names with the framework so it knows how to load them.
 *
 * Example: Suppose the class named "MyClass" changed its package structure and/or class name to "MyNewClass".  The following would be annotated on the "MyNewClass"
 *
 * package com.thinkbiganalytics.data
 *
 * @ClassNameChange(classNames = {"com.thinkbiganalytics.old.pkg.MyClass"}) public class MyNewClas { ...}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClassNameChange {

    String[] classNames();
}
