Security-LDAP
==========

###  Overview

This module is a pluggable implementation of [security-auth](../security-auth) that authenticates the username and password against an LDAP or Active Directory server.  This module is usually configured to work in conjunction with the [Kylo authentication module](../security-auth-kylo).  LDAP or AD authentication may be activated by adding either the `auth-ldap` or the `auth-ad` profile to the configuration properties, for example:

```
spring.profiles.active=auth-kylo,auth-ldap
```
or
```
spring.profiles.active=auth-kylo,auth-ad
```

### LDAP Configuration

If the `auth-ldap` profile is activated then the following properties should also be added to the configuration:

| Property  | Required   | Example Value  | Description  |
|-----------|------------|----------------|--------------|
| security.auth.ldap.server.uri | Yes   | `ldap://localhost:52389/dc=example,dc=com`	| The URI to the LDAP server and root context |
| security.auth.ldap.authenticator.userDnPatterns | Yes     | `uid={0},ou=people`	| The DN filter patterns, minus the root context portion, that identifies the entry for the user. The username is substitued for the `{0}` tag.  If more than one pattern is supplied they should be separated by "|". |
| security.auth.ldap.user.enableGroups | No	| `true` or `false`	| Activates user group loading; default: `false` |
| security.auth.ldap.user.groupsBase | No	| `ou=groups`	| The filter pattern that identifies group entries |
| security.auth.ldap.user.groupNameAttr | No	| `ou`	| The attribute of the group entry containing the group name |
| security.auth.ldap.server.authDn | No	| `uid=admin,ou=people,dc=example,dc=com`	| The LDAP account with the privileges necessary to access user or group entries; usually only needed (if at all) when group loading is activated |
| security.auth.ldap.server.password | No	| `admin's password`	| The password for the account with the privileges necessary to access user or group entries |

### Active Directory Configuration

If the `auth-ad` profile is activated then the following properties should also be added to the configuration:

| Property  | Required   | Example Value  | Description  |
|-----------|------------|----------------|--------------|
| security.auth.ad.server.uri | Yes | ldap://example.com/ | The URI to the AD server |
| security.auth.ad.server.domain | Yes | test.example.com | The AD domain of the users to authenticate |
| security.auth.ad.user.enableGroups | No | `true` or `false` | Activates user group loading; default: `false` |

### User Groups

Kylo access control is governed by permissions assigned to user groups, so upon successful authentication any groups to which the user belongs must be loaded and associated with the current authenticated request being processed.  Both the LDAP and AD implementations support loading a user's groups from LDAP/AD and associating them with the user upon successful authentication.  This feature is deactivated by default.  

As stated above, this module is typically used in conjunction with the [Kylo authentication module](../security-auth-kylo); which is responsible for, after validating the user exists in the user store, loading the groups associated with the user.  In this configuration (recommended) a user will only be successfully authenticated when that user exists both in LDAP/AD and the Kylo user/group store.

In cases when it is not feasible to have all Kylo users exist in the Kylo user/group store, this module may be configured to load the user's groups directly from LDAP/AD.  In this configuration you would disable Kylo authentication by removing the `auth-kylo` profile from the set of active profiles.  When this is configured, even though the users would no longer be required to exist in the Kylo user/group store, the groups are still required to exist in the store.  Additionally, the names of the groups as they exist in LDAP/AD must match exactly with the system names of the groups in the Kylo store.

