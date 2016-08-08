package com.thinkbiganalytics.metadata.sla;

import org.apache.commons.lang3.StringUtils;

/**
 * Properties
 */
public class EmailConfiguration {


    private String protocol;
    private String host;
    private int port;
    private boolean smtpAuth;
    private boolean starttls;
    private String from;
    private String username;
    private String password;
    private String smptAuthNtmlDomain;
    private boolean sslEnable;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public void setStarttls(boolean starttls) {
        this.starttls = starttls;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

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

    public String getSmptAuthNtmlDomain() {
        return smptAuthNtmlDomain;
    }

    public void setSmptAuthNtmlDomain(String smptAuthNtmlDomain) {
        this.smptAuthNtmlDomain = smptAuthNtmlDomain;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public boolean isConfigured(){
        return StringUtils.isNotBlank(getHost()) && StringUtils.isNotBlank(getProtocol());
    }
}
