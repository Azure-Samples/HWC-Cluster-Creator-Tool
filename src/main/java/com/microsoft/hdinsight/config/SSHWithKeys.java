package com.microsoft.hdinsight.config;

import java.util.List;

public class SSHWithKeys implements SSHCredentials {
  private String sshUsername;
  private List<String> publicKeypaths;

  public String getSshUsername() {
    return sshUsername;
  }

  public void setSshUsername(String sshUsername) {
    this.sshUsername = sshUsername;
  }

  public List<String> getPublicKeypaths() {
    return publicKeypaths;
  }

  public void setPublicKeypath(List<String> publicKeypath) {
    this.publicKeypaths = publicKeypath;
  }
}
