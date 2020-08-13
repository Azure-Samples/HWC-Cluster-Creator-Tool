package com.microsoft.hdinsight.config;

/**
 * HDI Custer credentials for Amabri and SSH Login.
 */
public class ClusterCredentialsConfig {
  private String clusterLoginUsername;
  private String clusterLoginPassword;
  private String sshUsername;
  private String sshPassword;

  public String getClusterLoginUsername() {
    return clusterLoginUsername;
  }

  public void setClusterLoginUsername(String clusterLoginUsername) {
    this.clusterLoginUsername = clusterLoginUsername;
  }

  public String getClusterLoginPassword() {
    return clusterLoginPassword;
  }

  public void setClusterLoginPassword(String clusterLoginPassword) {
    this.clusterLoginPassword = clusterLoginPassword;
  }

  public String getSshUsername() {
    return sshUsername;
  }

  public void setSshUsername(String sshUsername) {
    this.sshUsername = sshUsername;
  }

  public String getSshPassword() {
    return sshPassword;
  }

  public void setSshPassword(String sshPassword) {
    this.sshPassword = sshPassword;
  }
}
