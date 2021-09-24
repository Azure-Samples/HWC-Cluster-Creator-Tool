package com.microsoft.hdinsight.config;

/**
 * HDI Cluster credentials for Ambari and SSH Login.
 */
public class ClusterCredentialsConfig {
  private String clusterLoginUsername;
  private String clusterLoginPassword;
  private SSHCredentials sshCredentials;

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

  public SSHCredentials getSshCredentials() {
    return sshCredentials;
  }

  public void setSshCredentials(SSHCredentials sshCredentials) {
    this.sshCredentials = sshCredentials;
  }
}
