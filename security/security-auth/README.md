Security-Auth
==========

### Overview

This module defines the pluggable authentication framework.  While the framework is integrated with Spring security, it provides its pluggable capability by delegating to [JAAS LoginModules](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jaas/JAASRefGuide.html#LoginModule) to authenticate requests.  JAAS allows multiple LoginModules to be configured in order to participate in an authentication attempt.

### Pre-Configured Pluggable Modules

Kylo compes with some pre-configured authentication plugins that may be activated adding the approperiate Spring profile to the configuration properties.

| Login Method      | Spring Profile    | Description |
| ------------------| ----------------- | ----------- |
| Kylo User         | auth-kylo         | Authenticates users against the Kylo user/group store (Kylo services only) |
| LDAP              | auth-ldap         | Authenticates users stored in LDAP |
| Active Directory  | auth-ad           | Authenticates users stored in Active Directory |
| Users file        | auth-file         | Authenticates users in a file users.properties (typically used in development only) |
| Simple            | auth-simple       | Allows only one admin user defined in the configuration properties (development only) |

### JAAS Configuration

Currently, there are two applications for which LoginModules may be configured for authentication: the Kylo UI and services REST API.  This module provides an API that allows plugins to integrate custom login modules into the authentication process.

##### Creating a Custom Authentication Plugin

The first step is to create a [LoginModule](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jaas/JAASLMDevGuide.html) that performs whatever authentication that is necessary, and to add any user name/group principals upon successful authentication.  This module will be added to whatever other LoginModules that have been associated with the target application.

The service-auth framework provides an API to make it easy to integrate a new LoginModule into the authentication of the Kylo UI or services REST API.  The easiest way to integrate your custom LoginModule is to create a Spring configuration class that uses the framework-provided LoginConfigurationBuilder to incorporate your module into the authentication process.  The following is an example of a configuration class that adds a new module to the authentication of the Kylo UI:

```java
@Configuration
public class MyCustomAuthConfig {
    @Bean(name = "uiMyLoginConfiguration")
    public LoginConfiguration uiLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(MyCustomLoginModule.class)
                    .controlFlag("REQUIRED")
                    .option("myCustomOptionKey", "customValue")
                    .add()
                .build();
    }
}
```

As with any Kylo plugin, to deploy this configuration you would create a jar file containing the above configuration class, your custom login module class, and a `plugin/component-context.xml` file to bootstrap your plugin configuration.  Dropping this jar into the Kylo plugin directory would allow your custom LoginModule to participate in the UI login process.

