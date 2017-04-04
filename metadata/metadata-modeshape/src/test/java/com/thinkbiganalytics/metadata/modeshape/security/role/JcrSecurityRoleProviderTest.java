/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionTreeBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.ImmutableAllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class, JcrSecurityRoleProviderTestConfig.class })
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class JcrSecurityRoleProviderTest {
    

    @Inject
    private MetadataAccess metadata;

    @Inject
    private SecurityRoleProvider provider;
    
    private AllowedActions testActions;
    
    @Before
    public void setup() {
        this.testActions = metadata.read(() -> { 
            Node temp = JcrUtil.createNode(JcrMetadataAccess.getActiveSession().getRootNode(), "temp", "tba:allowedActions");
            JcrActionTreeBuilder<?> bldr = new JcrActionTreeBuilder<>(temp, null);
            bldr
                .action(FeedAccessControl.EDIT_DETAILS)
                .action(FeedAccessControl.ENABLE_DISABLE)
                .action(FeedAccessControl.EXPORT)
                .action(FeedAccessControl.ENABLE_DISABLE)
                .add();
            JcrAllowedActions actions = JcrUtil.createJcrObject(temp, JcrAllowedActions.class);
            return new ImmutableAllowedActions(actions);
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testCreateRole() {
        String name = metadata.commit(() -> {
            SecurityRole role = createRole("feedEditor", "Editor", "Can edit feeds", FeedAccessControl.EDIT_DETAILS, FeedAccessControl.ENABLE_DISABLE, FeedAccessControl.EXPORT);
            
            assertThat(role).isNotNull().extracting("systemName", "title", "description").contains("feedEditor", "Editor", "Can edit feeds");
            assertThat(role.getAllowedActions().getAvailableActions().stream().flatMap(action -> action.stream()))
                .extracting("systemName")
                .contains(FeedAccessControl.ACCESS_DETAILS.getSystemName(), FeedAccessControl.EDIT_DETAILS.getSystemName(), FeedAccessControl.ENABLE_DISABLE.getSystemName(), FeedAccessControl.EXPORT.getSystemName());
            
            return role.getSystemName();
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testFindRole() {
        metadata.commit(() -> {
            createRole("feedEditor", "Editor", "Can edit feeds", FeedAccessControl.EDIT_DETAILS, FeedAccessControl.ENABLE_DISABLE, FeedAccessControl.EXPORT);
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> { 
            Optional<SecurityRole> option = this.provider.getRole(SecurityRole.FEED, "feedEditor");
            
            assertThat(option).isNotNull();
            assertThat(option.isPresent()).isTrue();
            assertThat(option.get()).isNotNull().extracting("systemName", "title", "description").contains("feedEditor", "Editor", "Can edit feeds");
            assertThat(option.get().getAllowedActions().getAvailableActions().stream().flatMap(action -> action.stream()))
                .extracting("systemName")
                .contains(FeedAccessControl.ACCESS_DETAILS.getSystemName(), 
                          FeedAccessControl.EDIT_DETAILS.getSystemName(), 
                          FeedAccessControl.ENABLE_DISABLE.getSystemName(), 
                          FeedAccessControl.EXPORT.getSystemName());
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> { 
            Optional<SecurityRole> option = this.provider.getRole(SecurityRole.FEED, "bogus");
            
            assertThat(option).isNotNull();
            assertThat(option.isPresent()).isFalse();
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testFindRoles() {
        metadata.commit(() -> {
            createRole("feedEditor", "Editor", "Can edit feeds", FeedAccessControl.EDIT_DETAILS, FeedAccessControl.ENABLE_DISABLE, FeedAccessControl.EXPORT);
            createRole("feedViewer", "Viewer", "Can view feeds only", FeedAccessControl.ACCESS_DETAILS);
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> { 
            List<SecurityRole> list = this.provider.getEntityRoles(SecurityRole.FEED);
            
            assertThat(list).isNotNull().hasSize(2);
            
            assertThat(list.get(0)).isNotNull().extracting("systemName", "title", "description").contains("feedEditor", "Editor", "Can edit feeds");
            assertThat(list.get(0).getAllowedActions().getAvailableActions().stream().flatMap(action -> action.stream()))
                .extracting("systemName")
                .contains(FeedAccessControl.ACCESS_DETAILS.getSystemName(), 
                          FeedAccessControl.EDIT_DETAILS.getSystemName(), 
                          FeedAccessControl.ENABLE_DISABLE.getSystemName(), 
                          FeedAccessControl.EXPORT.getSystemName());
            
            assertThat(list.get(1)).isNotNull().extracting("systemName", "title", "description").contains("feedViewer", "Viewer", "Can view feeds only");
            assertThat(list.get(1).getAllowedActions().getAvailableActions().stream().flatMap(action -> action.stream()))
                .extracting("systemName")
                .contains(FeedAccessControl.ACCESS_DETAILS.getSystemName())
                .doesNotContain(FeedAccessControl.EDIT_DETAILS.getSystemName());
        }, MetadataAccess.SERVICE);
    }
    
    
    @Test
    public void testRemoveRole() {
        metadata.commit(() -> {
            createRole("feedEditor", "Editor", "Can edit feeds", FeedAccessControl.EDIT_DETAILS, FeedAccessControl.ENABLE_DISABLE, FeedAccessControl.EXPORT);
        }, MetadataAccess.SERVICE);
        
        boolean deleted = metadata.commit(() -> {
            return this.provider.removeRole(SecurityRole.FEED, "feedEditor");
        }, MetadataAccess.SERVICE);
        
        assertThat(deleted).isTrue();
        
        metadata.read(() -> { 
            Optional<SecurityRole> option = this.provider.getRole(SecurityRole.FEED, "feedEditor");
            
            assertThat(option).isNotNull();
            assertThat(option.isPresent()).isFalse();
        }, MetadataAccess.SERVICE);
        
        deleted = metadata.commit(() -> {
            return this.provider.removeRole(SecurityRole.FEED, "feedEditor");
        }, MetadataAccess.SERVICE);
        
        assertThat(deleted).isFalse();
    }
    
    @Test
    public void testSetPermissions() {
        metadata.commit(() -> {
            createRole("feedEditor", "Editor", "Can edit feeds", FeedAccessControl.ACCESS_FEED);
        }, MetadataAccess.SERVICE);

        metadata.commit(() -> {
            Optional<SecurityRole> option = this.provider.getRole(SecurityRole.FEED, "feedEditor");
            
            assertThat(option.get().getAllowedActions().getAvailableActions().stream().flatMap(action -> action.stream()))
                .extracting("systemName")
                .contains(FeedAccessControl.ACCESS_FEED.getSystemName())
                .doesNotContain(FeedAccessControl.EDIT_DETAILS.getSystemName());
            
            this.provider.setPermissions(SecurityRole.FEED, "feedEditor", FeedAccessControl.EDIT_DETAILS, FeedAccessControl.EXPORT);
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Optional<SecurityRole> option = this.provider.getRole(SecurityRole.FEED, "feedEditor");
            
            assertThat(option.get().getAllowedActions().getAvailableActions().stream().flatMap(action -> action.stream()))
                .extracting("systemName")
                .contains(FeedAccessControl.ACCESS_FEED.getSystemName(),
                          FeedAccessControl.ACCESS_DETAILS.getSystemName(), 
                          FeedAccessControl.EDIT_DETAILS.getSystemName(), 
                          FeedAccessControl.EXPORT.getSystemName())
                .doesNotContain(FeedAccessControl.ENABLE_DISABLE.getSystemName());
        }, MetadataAccess.SERVICE);
        
    }
    
    private SecurityRole createRole(String sysName, String title, String descr, Action... perms) {
        SecurityRole role = this.provider.createRole(SecurityRole.FEED, sysName, title, descr);
        role.setPermissions(perms);
        return role;
    }

}
