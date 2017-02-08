/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.user;

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
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@SpringApplicationConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class})
public class JcrUserProviderTest extends AbstractTestNGSpringContextTests {

    @Inject
    private JcrMetadataAccess metadata;

    @Inject
    private UserProvider provider;

    @Test
    public void testCreateUsers() throws Exception {
        User.ID id = metadata.commit(() -> {
            User user1 = this.provider.createUser("user1");

            assertThat(user1).isNotNull();

            User user2 = this.provider.createUser("user2");
            user2.setDisplayName("Mr. User Two");

            assertThat(user2).isNotNull();

            User user3 = this.provider.createUser("user3");
            user3.setEnabled(false);

            assertThat(user2).isNotNull();

            return user2.getId();
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Optional<User> optional = this.provider.findUserById(id);

            assertThat(optional.isPresent()).isTrue();
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateUsers")
    public void testFindUserByName() {
        metadata.read(() -> {
            Optional<User> optional = provider.findUserBySystemName("user2");

            assertThat(optional.isPresent()).isTrue();

            User user2 = optional.get();

            assertThat(user2).extracting(User::getSystemName,
                                         User::getDisplayName,
                                         User::isEnabled).containsExactly("user2", "Mr. User Two", true);
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Optional<User> optional = provider.findUserBySystemName("bogus");

            assertThat(optional.isPresent()).isFalse();
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateUsers")
    public void testUserExists() {
        metadata.read(() -> {
            assertThat(this.provider.userExists("user1")).isTrue();
            assertThat(this.provider.userExists("bogus")).isFalse();
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateUsers")
    public void testFindUsers() {
        metadata.read(() -> {
            List<User> users = StreamSupport.stream(provider.findUsers().spliterator(), false).collect(Collectors.toList());

            assertThat(users).hasSize(3).extracting(User::getSystemName).contains("user1", "user2", "user3");
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testFindUsers")
    public void testCreateGroup() {
        UserGroup.ID id = metadata.commit(() -> {
            UserGroup groupA = this.provider.createGroup("groupA");

            assertThat(groupA).isNotNull();

            return groupA.getId();
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Optional<UserGroup> optional = this.provider.findGroupById(id);

            assertThat(optional.isPresent()).isTrue();
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateGroup")
    public void testFindGroupByName() {
        metadata.read(() -> {
            Optional<UserGroup> optional = provider.findGroupByName("groupA");

            assertThat(optional.isPresent()).isTrue();

            UserGroup groupA = optional.get();

            assertThat(groupA).extracting(UserGroup::getSystemName).containsExactly("groupA");
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateGroup")
    public void testCreateMemberGroups() {
        metadata.commit(() -> {
            UserGroup groupA = this.provider.findGroupByName("groupA").get();

            UserGroup groupB = this.provider.createGroup("groupB");
            UserGroup groupC = this.provider.createGroup("groupC");
            UserGroup groupD = this.provider.createGroup("groupD");

            assertThat(groupA.addGroup(groupB)).isTrue();
            assertThat(groupA.addGroup(groupC)).isTrue();
            assertThat(groupC.addGroup(groupD)).isTrue();
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateMemberGroups")
    public void testAddUsersMembers() {
        metadata.commit(() -> {
            User user1 = this.provider.findUserBySystemName("user1").get();
            User user2 = this.provider.findUserBySystemName("user2").get();
            User user3 = this.provider.findUserBySystemName("user3").get();
            UserGroup groupA = this.provider.findGroupByName("groupA").get();
            UserGroup groupB = this.provider.findGroupByName("groupB").get();
            UserGroup groupD = this.provider.findGroupByName("groupD").get();

            assertThat(groupA.addUser(user1)).isTrue();
            assertThat(groupB.addUser(user2)).isTrue();
            assertThat(groupD.addUser(user3)).isTrue();
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testAddUsersMembers")
    public void testGetGroups() {
        metadata.read(() -> {
            UserGroup groupA = this.provider.findGroupByName("groupA").get();
            UserGroup groupC = this.provider.findGroupByName("groupC").get();

            assertThat(groupA.getGroups()).hasSize(2).extracting(g -> g.getSystemName()).contains("groupB", "groupC");
            assertThat(groupC.getGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupD");
        });
    }

    @Test(dependsOnMethods = "testAddUsersMembers")
    public void testGetUsers() {
        metadata.read(() -> {
            UserGroup groupA = this.provider.findGroupByName("groupA").get();
            UserGroup groupB = this.provider.findGroupByName("groupB").get();
            UserGroup groupC = this.provider.findGroupByName("groupC").get();
            UserGroup groupD = this.provider.findGroupByName("groupD").get();

            assertThat(groupA.getUsers()).extracting(User::getSystemName).containsExactly("user1");
            assertThat(groupB.getUsers()).extracting(User::getSystemName).containsExactly("user2");
            assertThat(groupD.getUsers()).extracting(User::getSystemName).containsExactly("user3");
            assertThat(groupC.getUsers()).hasSize(0);
        });
    }

    @Test(dependsOnMethods = "testCreateMemberGroups")
    public void testGroupGetContainingGroups() {
        metadata.read(() -> {
            UserGroup groupA = this.provider.findGroupByName("groupA").get();
            UserGroup groupB = this.provider.findGroupByName("groupB").get();
            UserGroup groupC = this.provider.findGroupByName("groupC").get();
            UserGroup groupD = this.provider.findGroupByName("groupD").get();

            assertThat(groupA.getContainingGroups()).isEmpty();
            assertThat(groupB.getContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupA");
            assertThat(groupC.getContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupA");
            assertThat(groupD.getContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupC");
        });
    }

    @Test(dependsOnMethods = "testCreateMemberGroups")
    public void testGroupGetAllContainingGroups() {
        metadata.read(() -> {
            UserGroup groupA = this.provider.findGroupByName("groupA").get();
            UserGroup groupB = this.provider.findGroupByName("groupB").get();
            UserGroup groupC = this.provider.findGroupByName("groupC").get();
            UserGroup groupD = this.provider.findGroupByName("groupD").get();

            assertThat(groupA.getAllContainingGroups()).isEmpty();
            assertThat(groupB.getAllContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupA");
            assertThat(groupC.getAllContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupA");
            assertThat(groupD.getAllContainingGroups()).hasSize(2).extracting(g -> g.getSystemName()).contains("groupC", "groupA");
        });
    }

    @Test(dependsOnMethods = "testAddUsersMembers")
    public void testUserGetContainingGroups() {
        metadata.read(() -> {
            User user1 = this.provider.findUserBySystemName("user1").get();
            User user2 = this.provider.findUserBySystemName("user2").get();
            User user3 = this.provider.findUserBySystemName("user3").get();

            assertThat(user1.getContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupA");
            assertThat(user2.getContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupB");
            assertThat(user3.getContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupD");
        });
    }

    @Test(dependsOnMethods = "testAddUsersMembers")
    public void testUserGetAllContainingGroups() {
        metadata.read(() -> {
            User user1 = this.provider.findUserBySystemName("user1").get();
            User user2 = this.provider.findUserBySystemName("user2").get();
            User user3 = this.provider.findUserBySystemName("user3").get();

            assertThat(user1.getAllContainingGroups()).hasSize(1).extracting(g -> g.getSystemName()).contains("groupA");
            assertThat(user2.getAllContainingGroups()).hasSize(2).extracting(g -> g.getSystemName()).contains("groupB", "groupA");
            assertThat(user3.getAllContainingGroups()).hasSize(3).extracting(g -> g.getSystemName()).contains("groupD", "groupC", "groupA");
        });
    }

    @Test(dependsOnMethods = "testAddUsersMembers")
    public void testUserGetPrincipals() {
        metadata.read(() -> {
            User user1 = this.provider.findUserBySystemName("user1").get();
            User user2 = this.provider.findUserBySystemName("user2").get();
            User user3 = this.provider.findUserBySystemName("user3").get();

            assertThat(user1.getPrincipal()).isEqualTo(new UsernamePrincipal("user1"));
            assertThat(user2.getPrincipal()).isEqualTo(new UsernamePrincipal("user2"));
            assertThat(user3.getPrincipal()).isEqualTo(new UsernamePrincipal("user3"));
        });
    }

    @Test(dependsOnMethods = "testAddUsersMembers")
    public void testUserGetGroupPrincipals() {
        metadata.read(() -> {
            User user1 = this.provider.findUserBySystemName("user1").get();
            User user2 = this.provider.findUserBySystemName("user2").get();
            User user3 = this.provider.findUserBySystemName("user3").get();

            assertThat(user1.getAllGroupPrincipals()).hasSize(1).contains(new GroupPrincipal("groupA"));
            assertThat(user2.getAllGroupPrincipals()).hasSize(2).contains(new GroupPrincipal("groupA"), new GroupPrincipal("groupB"));
            assertThat(user3.getAllGroupPrincipals()).hasSize(3).contains(new GroupPrincipal("groupA"), new GroupPrincipal("groupC"), new GroupPrincipal("groupD"));
        });
    }
}
