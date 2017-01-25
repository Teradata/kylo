/**
 * 
 */
package com.thinkbiganalytics.security.action;

/*-
 * #%L
 * thinkbig-security-api
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

import java.util.Arrays;
import java.util.List;

/**
 * Identifies an action that that may be checked if permitted. 
 *
 * @author Sean Felten
 */
public interface Action {
    
    String getSystemName();
    
    String getTitle();
    
    String getDescription();
    
    List<Action> getHierarchy();

    
    default boolean implies(Action action) {
        return getHierarchy().stream().anyMatch(a -> a.equals(action));
    }
    
    static Action create(String name, String title, String descr, Action... parents) {
        return new ImmutableAction(name, title, descr, Arrays.asList(parents));
    }
    
    static Action create(String name, Action... parents) {
        return new ImmutableAction(name, name, "", Arrays.asList(parents));
    }
    
    default Action subAction(String name, String title, String descr) {
        return create(name, title, descr, this);
    }
    
    default Action subAction(String name) {
        return create(name, name, "", this);
    }
}
