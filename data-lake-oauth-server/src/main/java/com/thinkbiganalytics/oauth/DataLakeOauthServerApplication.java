package com.thinkbiganalytics.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.security.Principal;

/**
 * Code used from:
 * https://spring.io/blog/2015/02/03/sso-with-oauth2-angular-js-and-spring-security-part-v
 *
 * and from the 1.3 migration:
 * https://spring.io/blog/2015/11/30/migrating-oauth2-apps-from-spring-boot-1-2-to-1-3
 * TODO once implement change the client and secret
 */
@SpringBootApplication
@RestController
@EnableResourceServer
@EnableAuthorizationServer
public class DataLakeOauthServerApplication extends WebMvcConfigurerAdapter {

  public static void main(String[] args) {
    SpringApplication.run(DataLakeOauthServerApplication.class, args);
  }

  @Configuration
  @EnableAuthorizationServer
  protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
      endpoints.authenticationManager(authenticationManager);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
      clients.inMemory()
	  .withClient("acme")
	  .secret("acmesecret")
	  .authorizedGrantTypes("authorization_code", "refresh_token",
				"password").scopes("openid");
    }

  }

  @RequestMapping("/user")
  public Principal user(Principal user) {
    return user;
  }


}
