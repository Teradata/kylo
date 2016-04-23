package com.thinkbiganalytics.servicemonitor.rest.client;

/**
 * Client Configuration Bean for the Rest Client
 */
public class RestClientConfig {

  private String username = "USERNAME";
  private String password = "PASSWORD";
  private String serverUrl = "URL";
  private String port = "";

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }
}
