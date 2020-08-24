package com.microsoft.hdinsight.config;

/**
 * Azure Active Directory configuration for authenticating the HDI Java SDK.
 */
public class ActiveDirectoryConfig {
  private AzureEnv azureEnv;
  private String clientId;
  private String tenantId;
  private String clientSecret;

  public AzureEnv getAzureEnv() {
    return azureEnv;
  }

  public void setAzureEnv(AzureEnv azureEnv) {
    this.azureEnv = azureEnv;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }
}
