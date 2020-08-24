package com.microsoft.hdinsight.config;

import com.microsoft.azure.AzureEnvironment;

/**
 * Azure Environment where the HDI cluster needs to be created.
 */
public enum AzureEnv {
  AZURE, AZURE_CHINA, AZURE_GERMANY, AZURE_US_GOVERNMENT;

  public AzureEnvironment getEnv() {
    switch (this) {
      case AZURE_CHINA:
        return AzureEnvironment.AZURE_CHINA;
      case AZURE_GERMANY:
        return AzureEnvironment.AZURE_GERMANY;
      case AZURE_US_GOVERNMENT:
        return AzureEnvironment.AZURE_US_GOVERNMENT;
      default:
        return AzureEnvironment.AZURE;
    }
  }
}
