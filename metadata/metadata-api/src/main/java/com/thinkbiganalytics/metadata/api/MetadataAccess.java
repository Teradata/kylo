/**
 *
 */
package com.thinkbiganalytics.metadata.api;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.security.ServiceAdminPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import java.security.Principal;

/**
 * Simple facade over whatever database/transaction mechanism is deployed in the environment where
 * the metadata API is used.  Applications may inject/obtain and use instances of this type to pass commands
 * that read and manipulate metadata entities via the providers.
 */
public interface MetadataAccess {

    /**
     * A principal representing an anonymous user
     */
    public static final Principal ANONYMOUS = new UsernamePrincipal("anonymous");
    /**
     * A principal representing a service account
     */
    public static final Principal SERVICE = new UsernamePrincipal("service");
    /**
     * A principal representing an the admin group
     */
    public static final Principal ADMIN = new ServiceAdminPrincipal();

    /**
     * Executes the {@link MetadataCommand} and commits any changes using credentials derived by the provided principals.
     * If no principals are provided then the commmand will execute using credentials derived from the current
     * security context.
     *
     * @param cmd        the command to execute
     * @param principals one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R commit(MetadataCommand<R> cmd, Principal... principals);

    /**
     * Executes the {@link MetadataCommand} and commits any changes using credentials derived by the provided principals. If no principals are provided then the commmand will execute using credentials
     * derived from the current security context.
     *
     * @param cmd         the command to execute
     * @param rollbackCmd the command to execute if an exception occurs and the transaction is rolled back
     * @param principals  one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals);

    /**
     * Executes the {@link Runnable} and commits any changes using credentials derived by the provided principals.
     * If no principals are provided then the runnable will execute using credentials derived from the current
     * security context.
     *
     * @param action     the command to execute
     * @param principals one or more principals, or none to use the current security context
     */
    void commit(MetadataAction action, Principal... principals);


    /**
     * Executes the {@link Runnable} and commits any changes using credentials derived by the provided principals. If no principals are provided then the runnable will execute using credentials
     * derived from the current security context. the rollbackAction will be called if an execption is thrown and the transaction is rolledback.  This is intended for users that need to do some
     * additiona work when rolling back
     *
     * @param action     the command to execute
     * @param principals one or more principals, or none to use the current security context
     */
    void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals);


    /**
     * Executes the {@link MetadataCommand} in a read-only context using credentials derived by the provided principals.
     * If no principals are provided then the commmand will execute using credentials derived from the current
     * security context.
     *
     * @param cmd        the command to execute
     * @param principals one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R read(MetadataCommand<R> cmd, Principal... principals);

    /**
     * Executes the {@link Runnable} in a read-only context using credentials derived by the provided principals.
     * If no principals are provided then the runnable will execute using credentials derived from the current
     * security context.
     *
     * @param cmd        the command to execute
     * @param principals one or more principals, or none to use the current security context
     */
    void read(MetadataAction cmd, Principal... principals);
}
