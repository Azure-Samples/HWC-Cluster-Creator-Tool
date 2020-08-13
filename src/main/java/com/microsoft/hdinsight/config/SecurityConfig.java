package com.microsoft.hdinsight.config;

/**
 * Security Configs for ESP enabled HDI Clusters.
 */
public class SecurityConfig {
  private String ldapUrl;
  private String domainUserName;
  private String aaddsDnsDomainName;
  private String clusterAccessGroup;
  private String aaddsResourceId;
  private String msiResourceId;

  public String getLdapUrl() {
    return ldapUrl;
  }

  public void setLdapUrl(String ldapUrl) {
    this.ldapUrl = ldapUrl;
  }

  public String getDomainUserName() {
    return domainUserName;
  }

  public void setDomainUserName(String domainUserName) {
    this.domainUserName = domainUserName;
  }

  public String getAaddsDnsDomainName() {
    return aaddsDnsDomainName;
  }

  public void setAaddsDnsDomainName(String aaddsDnsDomainName) {
    this.aaddsDnsDomainName = aaddsDnsDomainName;
  }

  public String getAaddsResourceId() {
    return aaddsResourceId;
  }

  public String getClusterAccessGroup() {
    return clusterAccessGroup;
  }

  public void setClusterAccessGroup(String clusterAccessGroup) {
    this.clusterAccessGroup = clusterAccessGroup;
  }

  public void setAaddsResourceId(String aaddsResourceId) {
    this.aaddsResourceId = aaddsResourceId;
  }

  public String getMsiResourceId() {
    return msiResourceId;
  }

  public void setMsiResourceId(String msiResourceId) {
    this.msiResourceId = msiResourceId;
  }
}
