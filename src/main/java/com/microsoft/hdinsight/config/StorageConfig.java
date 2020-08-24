package com.microsoft.hdinsight.config;

/**
 * Storage Config for adding the default storage account
 * for the HDI Cluster. The Storage account configured
 * should exist.
 */
public class StorageConfig {
  private StorageType type = StorageType.WASB;
  private String endpoint;
  private String key;
  private String resourceGroup;
  private String managedIdentityName;
  private String mangedIdentityResourceGroup;

  public StorageType getType() {
    return type;
  }

  public void setType(StorageType type) {
    this.type = type;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getManagedIdentityName() {
    return managedIdentityName;
  }

  public void setManagedIdentityName(String managedIdentityName) {
    this.managedIdentityName = managedIdentityName;
  }

  public String getResourceGroup() {
    return resourceGroup;
  }

  public void setResourceGroup(String resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  public String getMangedIdentityResourceGroup() {
    return mangedIdentityResourceGroup;
  }

  public void setMangedIdentityResourceGroup(String mangedIdentityResourceGroup) {
    this.mangedIdentityResourceGroup = mangedIdentityResourceGroup;
  }
}
