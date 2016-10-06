Security
==========

### Overview

The submodules of this project provide support for application security: access control, pluggable authentication, and some pre-canned authentication implementations.

## Submodules

| Module | Description |
|---------------------------------|------|
| [security-api](security-api)   | The API defining common user and group principals, and the frameworks for defining actions that may be permitted through access control lists
| [security-auth](security-auth) | The pluggable authentication framework
| [security-kylo](security-auth-kylo) | A configuration for authentication against the Kylo user/group store; typically used in conjunction with one of the other authentication configurations
| [security-ldap](security-auth-ldap) | A configuration for authenticating users using an LDAP or Active Directory server
| [security-simple](security-auth-simple) | The default authentication configuration that allows a single user/password defined in appicaiton properties (for development only)
| [security-file](security-auth-file) | A configuration for authenticating users/groups stored in property files (typically used only in development)


