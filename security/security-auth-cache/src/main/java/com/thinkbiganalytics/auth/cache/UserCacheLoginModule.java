package com.thinkbiganalytics.auth.cache;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.CredentialNotFoundException;

import com.google.common.cache.Cache;

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;

/**
 *
 */
public class UserCacheLoginModule extends AbstractLoginModule {
    
    public static final String CACHE_OPTION = "cache";
    public static final String MODE_OPTION = "mode";

    public enum Mode { AUTHENTICATE, CACHE }
    
    private Mode mode;
    private Cache<Object, Set<Principal>> principalCache;
    
    private Object cacheKey;
    
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        
        this.mode = (Mode) getOption(MODE_OPTION).orElseThrow(() -> new IllegalArgumentException("The \"mode\" option is required"));
        this.principalCache = (Cache<Object, Set<Principal>>) getOption(CACHE_OPTION).orElseThrow(() -> new IllegalArgumentException("The \"cache\" option is required"));
    }

    @Override
    protected boolean doLogin() throws Exception {
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        handle(nameCallback, passwordCallback);
        
        this.cacheKey = createCacheKey(nameCallback, passwordCallback);

        if (this.cacheKey == null) {
            return false;
        } else {
            if (this.mode == Mode.AUTHENTICATE) {
                Set<Principal> principals = this.principalCache.getIfPresent(this.cacheKey);
                
                if (principals == null) {
                    throw new CredentialNotFoundException("No cached entry found for user");
                } else {
                    addAllPrincipals(principals);
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    @Override
    protected boolean doCommit() throws Exception {
        if (this.mode == Mode.AUTHENTICATE) {
            getSubject().getPrincipals().addAll(getAllPrincipals());
        } else {
            Set<Principal> set = getSubject().getPrincipals();
            this.principalCache.put(this.cacheKey, set);
        }
        
        return true;
    }


    @Override
    protected boolean doAbort() throws Exception {
        return doLogout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        if (this.mode == Mode.AUTHENTICATE) {
            getSubject().getPrincipals().removeAll(getAllPrincipals());
        } else if (this.cacheKey != null) {
            this.principalCache.invalidate(this.cacheKey);
        }
        
        return true;
    }

    protected Object createCacheKey(NameCallback nameCallback, PasswordCallback passwordCallback) {
        if (nameCallback.getName() == null) {
            return null;
        } else {
            StringBuilder chars = new StringBuilder(32);
            
            chars.append(nameCallback.getName());
            if (passwordCallback.getPassword() != null) {
                chars.append(passwordCallback.getPassword());
            }
            
            try {
                byte[] hashed = MessageDigest.getInstance("SHA").digest(chars.toString().getBytes());
                return Base64.getEncoder().encodeToString(hashed);
            } catch (NoSuchAlgorithmException e) {
                return chars.toString();
            }
        }
    }

}
