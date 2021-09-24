package com.microsoft.hdinsight;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.HDInsightManager;
import com.microsoft.hdinsight.config.ClusterCredentialsConfig;
import com.microsoft.hdinsight.config.ClusterType;
import com.microsoft.hdinsight.config.HWCClusterConfig;
import com.microsoft.hdinsight.config.SSHWithKeys;
import com.microsoft.hdinsight.config.SSHWithPassword;
import com.microsoft.hdinsight.utils.AzureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Driver class for launching the HWC Cluster Creation Tool.
 * The YAML config should be passed as an argument.
 */
public class HWCClusterCreator {
  private static final Logger LOG =
      LoggerFactory.getLogger(HWCClusterCreator.class.getName());

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());


  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length < 1) {
        System.out.println("Usage : java -jar " +
            "HWC-ClusterCreator-1.0-SNAPSHOT-jar-with-dependencies.jar " +
            "YOUR_CONFIG.YAML");
        System.exit(1);
    }
    final HWCClusterConfig clusterConfig = OBJECT_MAPPER.readValue(new File(args[0]), HWCClusterConfig.class);
    LOG.info("Successfully read the config yaml from path : {}", args[0]);
    final Azure azure = AzureUtils.authenticate(clusterConfig.getActiveDirectory(), clusterConfig.getSubscription());
    final HDInsightManager manager = HDInsightManager.authenticate(AzureUtils.getAppTokenCred(
        clusterConfig.getActiveDirectory()), clusterConfig.getSubscription());

    switch (clusterConfig.getType()) {
      case LLAP_ONLY:
        AzureUtils.createHDICluster(clusterConfig, azure, manager, ClusterType.LLAP);
        break;
      case SPARK_ONLY:
        AzureUtils.createHDICluster(clusterConfig, azure, manager, ClusterType.SPARK);
        break;
      default:
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        try {
          //Create both Spark and LLAP cluster
          executor.submit(new Runnable() {
            public void run() {
              AzureUtils.createHDICluster(clusterConfig, azure, manager, ClusterType.LLAP);
              countDownLatch.countDown();
            }
          });
          executor.submit(new Runnable() {
            public void run() {
              AzureUtils.createHDICluster(clusterConfig, azure, manager, ClusterType.SPARK);
              countDownLatch.countDown();
            }
          });
        } finally {
          countDownLatch.await();
          executor.shutdownNow();
        }
    }
  }
}