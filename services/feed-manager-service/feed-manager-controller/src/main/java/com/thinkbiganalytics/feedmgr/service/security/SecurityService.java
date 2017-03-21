/**
 * 
 */
package com.thinkbiganalytics.feedmgr.service.security;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

/**
 *
 */
public interface SecurityService {

    Optional<ActionGroup> getAvailableFeedActions(String id);
    
    Optional<ActionGroup> getAllowedFeedActions(String id, Set<Principal> principals);
    
    Optional<ActionGroup> changeFeedPermissions(String id, PermissionsChange change);
    
    Optional<ActionGroup> getAvailableCategoryActions(String id);
    
    Optional<ActionGroup> getAllowedCategoryActions(String id, Set<Principal> principals);
    
    Optional<ActionGroup> changeCategoryPermissions(String id, PermissionsChange change);
    
    Optional<ActionGroup> getAvailableTemplateActions(String id);
    
    Optional<ActionGroup> getAllowedTemplateActions(String id, Set<Principal> principals);
    
    Optional<ActionGroup> changeTemplatePermissions(String id, PermissionsChange change);
    
    Optional<PermissionsChange> createFeedPermissionChange(String id, ChangeType changeType, Set<Principal> members);
    
    Optional<PermissionsChange> createCategoryPermissionChange(String id, ChangeType changeType, Set<Principal> members);
    
    Optional<PermissionsChange> createTemplatePermissionChange(String id, ChangeType changeType, Set<Principal> members);
}
