/**
 *
 */
package com.thinkbiganalytics.jpa;

/*-
 * #%L
 * thinkbig-commons-jpa
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

import java.lang.reflect.Constructor;
import java.security.Principal;

import javax.persistence.AttributeConverter;

/**
 * Converts a principal to a string in the form: "<principal class name>:<principal name>".
 * By default it assumes the principal class has a single-argument constructor rhat accepts
 * the principal name.  Subclass may override this assumption.
 */
public class PrincipalConverter implements AttributeConverter<Principal, String> {

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(Principal principal) {
        String className = principal.getClass().getName();
        return className + ":" + principal.getName();
    }

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public Principal convertToEntityAttribute(String dbData) {
        try {
            int div = dbData.indexOf(':');
            String className = dbData.substring(0, div);
            String name = dbData.substring(div + 1, dbData.length());
            @SuppressWarnings("unchecked")
            Class<Principal> principalClass = (Class<Principal>) Class.forName(className);
            Constructor<Principal> constructor = principalClass.getConstructor(String.class);
            return constructor.newInstance(name);
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new IllegalStateException("Failed to instantiate principal from: " + dbData, x);
        }
    }

}
