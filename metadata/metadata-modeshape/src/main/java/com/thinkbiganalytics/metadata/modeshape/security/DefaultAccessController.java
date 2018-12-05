/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security;

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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.AccessControlled;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.context.SecurityContextUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class DefaultAccessController implements AccessController {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultAccessController.class);

    
    public enum LogFields { PERM, ENTITY, RESULT, USER, GROUPS, IP_ADDRESS };
    
    private static final Pattern LOG_FIELD_PATTERN = Pattern.compile("\\s*\\{\\s*(\\w+)\\s*\\}\\s*", Pattern.CASE_INSENSITIVE);
    
    @org.springframework.beans.factory.annotation.Value("${security.entity.access.controlled:false}")
    private volatile boolean entityAccessControlled;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.auth:false}")
    private volatile boolean logAuthentication;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.access:false}")
    private volatile boolean logAccessCheck;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.access.level:DEBUG}")
    private volatile String accessLogLevel;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.access.ignore.users:service}")
    private volatile String ignoreUsersCsv;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.access.ignore.groups:}")
    private volatile String ignoreGroupsCsv;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.access.format:Permission check entity: {ENTITY}, permission: {PERM}, result: {RESULT} - user: {USER}}")
    private volatile String accessLogFormat;
    
    @Inject
    private MetadataAccess metadata;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    
    private List<LogFields> formatFields;
    private Set<UsernamePrincipal> ignoreUsers;
    private Set<GroupPrincipal> ignoreGroups;
    private String logMessage;
    private BiConsumer<String, Object[]> loggingConsumer;

    public DefaultAccessController() {
    }
    
    @PostConstruct
    public void init() {
        this.formatFields = generateFormatFields();
        this.logMessage = generateLogMessage(this.formatFields);
        this.loggingConsumer = generateLoggingConsumer();
        
        if (StringUtils.isBlank(ignoreUsersCsv)) {
            this.ignoreUsers = Collections.emptySet();
        } else {
            this.ignoreUsers = Arrays.stream(this.ignoreUsersCsv.split(","))
                .map(String::trim)
                .map(UsernamePrincipal::new)
                .collect(Collectors.toSet());
        }
        
        if (StringUtils.isBlank(ignoreGroupsCsv)) {
            this.ignoreGroups = Collections.emptySet();
        } else {
            this.ignoreGroups = Arrays.stream(this.ignoreGroupsCsv.split(","))
                .map(String::trim)
                .map(GroupPrincipal::new)
                .collect(Collectors.toSet());
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#checkPermission(java.lang.String, com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public void checkPermission(String moduleName, Action action, Action... others) {
        checkPermission(moduleName, Stream.concat(Stream.of(action),
                                                  Arrays.stream(others)).collect(Collectors.toSet()));
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#checkPermission(java.lang.String, java.util.Set)
     */
    @Override
    public void checkPermission(String moduleName, Set<Action> actions) {
        this.metadata.read(() -> {
            AllowedActions allowed = this.actionsProvider.getAllowedActions(moduleName)
                .orElseThrow(() -> new AccessControlException("No actions are defined for the module named: " + moduleName));
            
            try {
                allowed.checkPermission(actions);
                logAccessCheck(moduleName, actions, "success");
            } catch (AccessControlException e) {
                logAccessCheck(moduleName, actions, "failure");
                throw e;
            }
        });
    }

    @Override
    public boolean hasPermission(String moduleName, Action action, Action... others) {
        try {
            checkPermission(moduleName, action, others);
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }
    
    @Override
    public boolean hasPermission(String moduleName, Set<Action> actions) {
        try {
            checkPermission(moduleName, actions);
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }
    
    @Override
    public void checkPermission(AccessControlled accessControlled, Action action, Action... others) {
        checkPermission(accessControlled, Stream.concat(Stream.of(action), 
                                                        Arrays.stream(others)).collect(Collectors.toSet()));   
    }
    
    @Override
    public void checkPermission(AccessControlled accessControlled, Set<Action> actions) {
        if (isEntityAccessControlled()) {
            try {
                accessControlled.getAllowedActions().checkPermission(actions);
                logAccessCheck(accessControlled.getAuditId(), actions, "success");
            } catch (AccessControlException e) {
                logAccessCheck(accessControlled.getAuditId(), actions, "failure");
                throw e;
            }
        }
    }
    
    @Override
    public boolean hasPermission(AccessControlled accessControlled, Action action, Action... others) {
        if (isEntityAccessControlled()) {
            return hasPermission(accessControlled, Stream.concat(Stream.of(action), 
                                                                 Arrays.stream(others)).collect(Collectors.toSet()));   
        } else {
            return true;
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#hasPermission(com.thinkbiganalytics.security.AccessControlled, java.util.Set)
     */
    @Override
    public boolean hasPermission(AccessControlled accessControlled, Set<Action> actions) {
        if (isEntityAccessControlled()) {
            return this.metadata.read(() -> {
                boolean success = accessControlled.getAllowedActions().hasPermission(actions);
                logAccessCheck(accessControlled.getAuditId(), actions, success ? "success" : "failure");
                return success;
            });
        } else {
            return true;
        }
    }

    @Override
    public boolean isEntityAccessControlled() {
        return entityAccessControlled;
    }

    public void setEntityAccessControlled(boolean entityAccessControlled) {
        this.entityAccessControlled = entityAccessControlled;
    }
    
    protected void logAccessCheck(String entity, Set<Action> actions, String result) {
        if (this.logAccessCheck) {
//            Subject subject = Subject.getSubject(java.security.AccessController.getContext());
            
            Principal user = SecurityContextUtil.getCurrentUserPrincipal();
            Set<Principal> groups = SecurityContextUtil.getCurrentPrincipals().stream().filter(GroupPrincipal.class::isInstance).collect(Collectors.toSet());
            Set<UsernamePrincipal> ignoredUsers = getIgnoreUsers();
            boolean ignore = ignoredUsers.contains(user);
            ignore |= ignoreGroups.stream().anyMatch(groups::contains);
            
            if (! ignore) {
                String[] logArgs = getFormatFields().stream()
                    .map(field -> {
                        switch (field) {
                            case PERM:
                                return actions.stream().map(action -> action.getSystemName()).collect(Collectors.joining(", "));
                            case ENTITY:
                                return entity;
                            case RESULT:
                                return result;
                            case USER:
                                return user.getName();
                            case GROUPS:
                                return groups.stream().map(Principal::getName).collect(Collectors.joining(", "));
                            case IP_ADDRESS:
                                return "";
                            default:
                                return "";
                        }
                    })
                    .toArray(String[]::new);
                
                getLoggingConsumer().accept(this.logMessage, logArgs);
            }
        }
    }
    
    protected String getAccessLogFormat() {
        return accessLogFormat;
    }

    protected List<LogFields> getFormatFields() {
        return formatFields;
    }
    
    protected String getLogMessage() {
        return logMessage;
    }
    
    protected Set<UsernamePrincipal> getIgnoreUsers() {
        return ignoreUsers;
    }
    
    protected BiConsumer<String, Object[]> getLoggingConsumer() {
        return loggingConsumer;
    }
    
    protected BiConsumer<String, Object[]> generateLoggingConsumer() {
        if (this.accessLogLevel.equalsIgnoreCase("error")) return (msg, args) -> log.error(msg, args);
        if (this.accessLogLevel.equalsIgnoreCase("warn")) return (msg, args) -> log.warn(msg, args);
        if (this.accessLogLevel.equalsIgnoreCase("info")) return (msg, args) -> log.info(msg, args);
        if (this.accessLogLevel.equalsIgnoreCase("trace")) return (msg, args) -> log.trace(msg, args);
        return (msg, args) -> log.debug(msg, args);
    }

    protected List<LogFields> generateFormatFields() {
        Matcher matcher = LOG_FIELD_PATTERN.matcher(this.accessLogFormat);
        ArrayList<LogFields> fields = new ArrayList<>();
        
        while (matcher.find()) {
            String fieldStr = matcher.group(1).toUpperCase();
            try {
                fields.add(LogFields.valueOf(fieldStr));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported security logging field name: " + fieldStr);
            }
        }
        
        return fields;
    }

    protected String generateLogMessage(List<LogFields> formatFields) {
        return getAccessLogFormat().replaceAll("\\{\\s*\\w+\\s*\\}", "{}");
    }
}
