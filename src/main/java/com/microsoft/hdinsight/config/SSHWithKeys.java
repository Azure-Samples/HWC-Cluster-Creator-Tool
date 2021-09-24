package com.microsoft.hdinsight.config;

import java.util.List;

public class SSHWithKeys implements SSHCredentials {
  private List<String> publicKeypaths;

  public List<String> getPublicKeypaths() {
    return publicKeypaths;
  }

  public void setPublicKeypath(List<String> publicKeypath) {
    this.publicKeypaths = publicKeypath;
  }
}
