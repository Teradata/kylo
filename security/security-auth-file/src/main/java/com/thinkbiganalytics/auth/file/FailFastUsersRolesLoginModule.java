package com.thinkbiganalytics.auth.file;

import org.jboss.security.auth.spi.UsersRolesLoginModule;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

/**
 * FailFastUsersRolesLoginModule will fail fast if user and group configuration files are not found.
 * This class extends original UsersRolesLoginModule so that it can re-use its configuration file loading strategy.
 */
public class FailFastUsersRolesLoginModule extends UsersRolesLoginModule {


    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        checkConfig(options.get("usersProperties"), options.get("rolesProperties"));
    }

    void checkConfig(Object usersResource, Object rolesResource) {
        try {
            this.loadUsers();
        } catch (IOException e) {
            String msg = String.format("auth-file is configured but no users resource is found "
                         + "- please verify the config property security.auth.file.users=%s", usersResource);
            throw new IllegalStateException(msg);
        }
        try {
            this.loadRoles();
        } catch (IOException e) {
            String msg = String.format("auth-file is configured but no roles resource is found "
                         + "- please verify the config property security.auth.file.roles=%s", rolesResource);
            throw new IllegalStateException(msg);
        }
    }
}
