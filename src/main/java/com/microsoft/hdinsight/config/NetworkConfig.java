package com.microsoft.hdinsight.config;

/**
 * Network config for the HDI to either create a new VNet with
 * limited InBound rules else use a existing VNet already
 * configured.
 */
public class NetworkConfig {
  private String vnetName;
  private String resourceGroup;
  private String subnetName;
  private boolean create;

  public String getVnetName() {
    return vnetName;
  }

  public void setVnetName(String vnetName) {
    this.vnetName = vnetName;
  }

  public String getResourceGroup() {
    return resourceGroup;
  }

  public void setResourceGroup(String resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  public String getSubnetName() {
    return subnetName;
  }

  public void setSubnetName(String subnetName) {
    this.subnetName = subnetName;
  }

  public boolean isCreate() {
    return create;
  }

  public void setCreate(boolean create) {
    this.create = create;
  }
}