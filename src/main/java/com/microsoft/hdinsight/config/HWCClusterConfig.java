package com.microsoft.hdinsight.config;

/**
 * HWC Cluster Config for creating
 * - HDI Spark only cluster on an existing VNet
 * - HDI LLAP only cluster on an existing VNet
 * - Both HDI Spark and LLAP cluster on a custom VNet
 */
public class HWCClusterConfig {
  private CreateType type = CreateType.SPARK_AND_LLAP;
  private String resourceGroup;
  private String headNodeVMSize;
  private String workerNodeVMSize;
  private int workerNodeSize;
  private String clusterNamePrefix;
  private String region;
  private String subscription;
  private ActiveDirectoryConfig activeDirectory;
  private ClusterCredentialsConfig clusterCredentials;
  private StorageConfig storage;
  private NetworkConfig network;
  private SecurityConfig security;

  public CreateType getType() {
    return type;
  }

  public void setType(CreateType type) {
    this.type = type;
  }

  public String getResourceGroup() {
    return resourceGroup;
  }

  public void setResourceGroup(String resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  public String getHeadNodeVMSize() {
    return headNodeVMSize;
  }

  public void setHeadNodeVMSize(String headNodeVMSize) {
    this.headNodeVMSize = headNodeVMSize;
  }

  public String getWorkerNodeVMSize() {
    return workerNodeVMSize;
  }

  public void setWorkerNodeVMSize(String workerNodeVMSize) {
    this.workerNodeVMSize = workerNodeVMSize;
  }

  public int getWorkerNodeSize() {
    return workerNodeSize;
  }

  public void setWorkerNodeSize(int workerNodeSize) {
    this.workerNodeSize = workerNodeSize;
  }

  public String getClusterNamePrefix() {
    return clusterNamePrefix;
  }

  public void setClusterNamePrefix(String clusterNamePrefix) {
    this.clusterNamePrefix = clusterNamePrefix;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getSubscription() {
    return subscription;
  }

  public void setSubscription(String subscription) {
    this.subscription = subscription;
  }

  public ActiveDirectoryConfig getActiveDirectory() {
    return activeDirectory;
  }

  public void setActiveDirectory(ActiveDirectoryConfig activeDirectory) {
    this.activeDirectory = activeDirectory;
  }

  public ClusterCredentialsConfig getClusterCredentials() {
    return clusterCredentials;
  }

  public void setClusterCredentials(ClusterCredentialsConfig clusterCredentials) {
    this.clusterCredentials = clusterCredentials;
  }

  public StorageConfig getStorage() {
    return storage;
  }

  public void setStorage(StorageConfig storage) {
    this.storage = storage;
  }

  public NetworkConfig getNetwork() {
    return network;
  }

  public void setNetwork(NetworkConfig network) {
    this.network = network;
  }

  public SecurityConfig getSecurity() {
    return security;
  }

  public void setSecurity(SecurityConfig security) {
    this.security = security;
  }
}
