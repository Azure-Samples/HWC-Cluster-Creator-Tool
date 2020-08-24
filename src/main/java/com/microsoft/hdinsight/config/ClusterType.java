package com.microsoft.hdinsight.config;

/**
 * Type of HDI cluster to be created.
 */
public enum ClusterType {
  LLAP("INTERACTIVEHIVE"), SPARK("SPARK");

  private final String type;
  ClusterType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}