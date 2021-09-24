package com.microsoft.hdinsight.config;

public class SSHWithPassword implements SSHCredentials {
  private String sshUsername;
  private String sshPassword;

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
