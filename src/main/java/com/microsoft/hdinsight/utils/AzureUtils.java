package com.microsoft.hdinsight.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterCreateParametersExtended;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterCreateProperties;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterDefinition;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterIdentity;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterIdentityUserAssignedIdentitiesValue;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ComputeProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.DirectoryType;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.HardwareProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.LinuxOperatingSystemProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.OSType;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.OsProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ResourceIdentityType;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.Role;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.SecurityProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.SshProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.SshPublicKey;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.StorageAccount;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.StorageProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.Tier;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.VirtualNetworkProfile;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.HDInsightManager;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.hdinsight.config.ActiveDirectoryConfig;
import com.microsoft.hdinsight.config.ClusterType;
import com.microsoft.hdinsight.config.HWCClusterConfig;
import com.microsoft.hdinsight.config.NetworkConfig;
import com.microsoft.hdinsight.config.SSHWithKeys;
import com.microsoft.hdinsight.config.SSHWithPassword;
import com.microsoft.hdinsight.config.StorageConfig;
import com.microsoft.hdinsight.config.StorageType;
import com.microsoft.rest.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Util class hosting a set of helper methods accessing
 * HDI and Azure SDK's for configuring and creating HDI Cluster.
 */
public class AzureUtils {

  private static final Logger LOG =
      LoggerFactory.getLogger(AzureUtils.class.getName());

  private static final Map<String, String> healthAndMgmtRules = new HashMap<String, String>() {{
    put("Health_and_Management_1", "168.61.49.99");
    put("Health_and_Management_2", "23.99.5.239");
    put("Health_and_Management_3", "168.61.48.131");
    put("Health_and_Management_4", "138.91.141.162");
    put("Health_and_Management_5", "52.164.210.96");
    put("Health_and_Management_6", "13.74.153.132");
  }};
  private static final String MANAGED_IDENTITIES = "/subscriptions/%s/resourceGroups/%s/providers" +
      "/Microsoft.ManagedIdentity/userAssignedIdentities/%s";
  private static final String STORAGE_ACCOUNT = "/subscriptions/%s/resourceGroups/%s" +
          "/providers/Microsoft.Storage/storageAccounts/%s";

  public static Azure authenticate(ActiveDirectoryConfig activeDirectoryConfig, String subscription) {
    ApplicationTokenCredentials credentials = getAppTokenCred(activeDirectoryConfig);

    LOG.info("Authenticating the app credential token for the subscription  :{}",
        subscription);
    return Azure
        .configure()
        .withLogLevel(LogLevel.BODY_AND_HEADERS)
        .authenticate(credentials)
        .withSubscription(subscription);
  }

  public static ApplicationTokenCredentials getAppTokenCred(ActiveDirectoryConfig activeDirectoryConfig) {
    LOG.info("Creating app credential token for AAD Config...");
    return new ApplicationTokenCredentials(activeDirectoryConfig.getClientId(),
        activeDirectoryConfig.getTenantId(), activeDirectoryConfig.getClientSecret(),
        activeDirectoryConfig.getAzureEnv().getEnv());
  }

  private static Network createVNet(Azure azure, NetworkConfig networkConfig, String region, String resourceGroup) {
    LOG.info("Creating NSG with limited InBound rules...");
    NetworkSecurityGroup backEndSubnetNsg = azure.networkSecurityGroups()
        .define(networkConfig.getVnetName() + "-HWC-NSG")
        .withRegion(Region.fromName(region)).withExistingResourceGroup(resourceGroup)
        .defineRule("HDInsight_ServiceTag").allowInbound().fromAddress("HDInsight").fromAnyPort()
        .toAnyAddress().toPort(443).withAnyProtocol().withPriority(108)
        .attach()
        .defineRule("Allow_Ambari_UI").allowInbound().fromAnyAddress().fromAnyPort()
        .toAddress("VirtualNetwork").toPort(443).withAnyProtocol().withPriority(109)
        .attach()
        .create();

    LOG.info("Adding HDI health and management rules for inbound connection...");
    int priority = 111;
    for (Map.Entry<String, String> healthAndMgmtRule : healthAndMgmtRules.entrySet()) {
      backEndSubnetNsg.update()
          .defineRule(healthAndMgmtRule.getKey()).allowInbound().fromAddress(healthAndMgmtRule.getValue())
          .fromAnyPort().toAddress("VirtualNetwork").toPort(443).withAnyProtocol().withPriority(priority++)
          .attach()
          .apply();
    }

    LOG.info("Creating VNet : {} and Subnet : {}", networkConfig.getVnetName(),
        networkConfig.getSubnetName());
    return azure.networks().define(networkConfig.getVnetName())
        .withRegion(Region.fromName(region))
        .withExistingResourceGroup(resourceGroup)
        .withAddressSpace("192.168.0.0/16")
        .defineSubnet(networkConfig.getSubnetName())
        .withAddressPrefix("192.168.1.0/24")
        .withExistingNetworkSecurityGroup(backEndSubnetNsg)
        .attach()
        .create();
  }

  private static Map<String, String> getComponenVersion(HWCClusterConfig clusterConfig) {
    if (clusterConfig.isEnableSpark3()) {
      return ImmutableMap.of("spark", "3.0");
    }

    return ImmutableMap.of();
  }

  private static ClusterCreateParametersExtended getClusterCreateParameters(
      HWCClusterConfig clusterConfig, Azure azure, ClusterType clusterType) {
    ClusterCreateParametersExtended createParametersExtended = new ClusterCreateParametersExtended()
        .withLocation(Region.fromName(clusterConfig.getRegion()).toString())
        .withProperties(new ClusterCreateProperties()
            .withClusterVersion("4.0")
            .withOsType(OSType.LINUX)
            .withTier(Tier.STANDARD)
            .withClusterDefinition(new ClusterDefinition()
                .withKind(clusterType.getType())
                .withComponentVersion(getComponenVersion(clusterConfig))
                .withConfigurations(ImmutableMap.of(
                    "gateway", ImmutableMap.of(
                        "restAuthCredential.isEnabled", "true",
                        "restAuthCredential.username", clusterConfig.getClusterCredentials().getClusterLoginUsername(),
                        "restAuthCredential.password", clusterConfig.getClusterCredentials().getClusterLoginPassword()
                    )))
            )
            .withComputeProfile(new ComputeProfile()
                .withRoles(ImmutableList.of(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile()
                            .withVmSize(clusterConfig.getHeadNodeVMSize()))
                        .withOsProfile(addSSHCredentials(clusterConfig))
                        .withVirtualNetworkProfile(getVirtualNetworkProfile(clusterConfig, azure)),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(clusterConfig.getWorkerNodeSize())
                        .withHardwareProfile(new HardwareProfile()
                            .withVmSize(clusterConfig.getWorkerNodeVMSize()))
                        .withOsProfile(addSSHCredentials(clusterConfig))))));

    createParametersExtended.properties()
        .withStorageProfile(new StorageProfile()
            .withStorageaccounts(ImmutableList.of(
                fetchStorageAccount(clusterConfig, clusterType, createParametersExtended)
            )));
    if (clusterConfig.getSecurity() != null) {
      LOG.info("Configuring security profile for the ESP cluster...");
      createParametersExtended.properties().withTier(Tier.PREMIUM);
      configureSecurityProfile(clusterConfig, createParametersExtended);
    }
    return createParametersExtended;
  }

  private static StorageAccount fetchStorageAccount(HWCClusterConfig clusterConfig,
      ClusterType clusterType, ClusterCreateParametersExtended createParametersExtended) {
    final StorageConfig storageConfig = clusterConfig.getStorage();
    switch (storageConfig.getType()) {
      case WASB:
        return new StorageAccount()
            .withName(storageConfig.getEndpoint())
            .withKey(storageConfig.getKey())
            .withContainer(clusterConfig.getClusterNamePrefix() + "-" + clusterType.getType().toLowerCase())
            .withIsDefault(true);
      case ADLS_GEN2:
        addUserIdentitiesForADLS2(clusterConfig, createParametersExtended,
            new HashMap<>());
        return new StorageAccount()
            .withName(storageConfig.getEndpoint())
            .withIsDefault(true)
            .withFileSystem(clusterConfig.getClusterNamePrefix() + "-" + clusterType.getType().toLowerCase())
            .withResourceId(String.format(STORAGE_ACCOUNT,
                clusterConfig.getSubscription(), storageConfig
                    .getResourceGroup(), storageConfig.getEndpoint().split("\\.")[0]))
            .withMsiResourceId
                (String.format(MANAGED_IDENTITIES,
                    clusterConfig.getSubscription(), storageConfig
                        .getMangedIdentityResourceGroup(), storageConfig
                        .getManagedIdentityName()));
      default:
        throw  new IllegalArgumentException(String.format("Unable to to " +
          "initialize storage " +
            "account %s , please check if the storage configs are correct. " +
            "Supported storage accounts : %s", storageConfig
            .getType(), Arrays.toString(StorageType.values())));
    }

  }

  private static OsProfile addSSHCredentials(HWCClusterConfig clusterConfig) {
    LOG.info("Setting up SSH credentials...");
    if (clusterConfig.getClusterCredentials().getSshCredentials() instanceof SSHWithPassword) {
      return addSSHWithPasswordCredentials((SSHWithPassword) clusterConfig.getClusterCredentials().getSshCredentials());
    } else {
      return addSSHWithKeysCredentials((SSHWithKeys) clusterConfig.getClusterCredentials().getSshCredentials());
    }
  }

  private static OsProfile addSSHWithKeysCredentials(SSHWithKeys sshWithKeys) {
    List<SshPublicKey> sshPublicKeys = new ArrayList<>();
    for (String path : sshWithKeys.getPublicKeypaths()) {
      final StringBuilder contentBuilder = new StringBuilder();
      try (Stream<String> stream = Files.lines( Paths.get(path), StandardCharsets.UTF_8)) {
        stream.forEach(s -> contentBuilder.append(s));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }


      SshPublicKey sshPublicKey = new SshPublicKey();
      sshPublicKey.withCertificateData(contentBuilder.toString());
      sshPublicKeys.add(sshPublicKey);
      contentBuilder.setLength(0);
    }

    SshProfile profile = new SshProfile();
    profile.withPublicKeys(sshPublicKeys);

    return new OsProfile()
      .withLinuxOperatingSystemProfile(
        new LinuxOperatingSystemProfile().withSshProfile(profile).withUsername(sshWithKeys.getSshUsername())
      );
  }

  private static OsProfile addSSHWithPasswordCredentials(SSHWithPassword sshWithPassword) {
    return new OsProfile()
      .withLinuxOperatingSystemProfile(
        new LinuxOperatingSystemProfile()
                .withUsername(sshWithPassword.getSshUsername())
                .withPassword(sshWithPassword.getSshPassword())
      );
  }

  private static VirtualNetworkProfile getVirtualNetworkProfile(HWCClusterConfig clusterConfig, Azure azure) {
    if (clusterConfig.getNetwork().isCreate()) {
      createVNet(azure, clusterConfig.getNetwork(), clusterConfig.getRegion(),
          clusterConfig.getResourceGroup());
      return new VirtualNetworkProfile()
          .withId(azure.networks().getByResourceGroup(clusterConfig.getResourceGroup(),
              clusterConfig.getNetwork().getVnetName()).id())
          .withSubnet(String.format("%s/subnets/%s", azure.networks().getByResourceGroup(
              clusterConfig.getResourceGroup(), clusterConfig.getNetwork().getVnetName()).id(),
              clusterConfig.getNetwork().getSubnetName()));
    }
    final NetworkConfig networkConfig = clusterConfig.getNetwork();
    LOG.info("Using VNet : {} and Subnet : {} from Resource Group : {}",
        networkConfig.getVnetName(), networkConfig.getSubnetName(), networkConfig.getResourceGroup());
    return new VirtualNetworkProfile()
        .withId(azure.networks().getByResourceGroup(networkConfig.getResourceGroup(),
            networkConfig.getVnetName()).id())
        .withSubnet(String.format("%s/subnets/%s", azure.networks().getByResourceGroup(
            networkConfig.getResourceGroup(), networkConfig.getVnetName()).id(),
            networkConfig.getSubnetName()));
  }

  private static void configureSecurityProfile(HWCClusterConfig clusterConfig,
      ClusterCreateParametersExtended createParams) {
    createParams.properties().withSecurityProfile(new SecurityProfile()
        .withDirectoryType(DirectoryType.ACTIVE_DIRECTORY)
        .withLdapsUrls(Collections.singletonList(clusterConfig.getSecurity().getLdapUrl()))
        .withDomainUsername(clusterConfig.getSecurity().getDomainUserName())
        .withDomain(clusterConfig.getSecurity().getAaddsDnsDomainName())
        .withClusterUsersGroupDNs(Collections.singletonList(clusterConfig.getSecurity().getClusterAccessGroup()))
        .withAaddsResourceId(clusterConfig.getSecurity().getAaddsResourceId())
        .withMsiResourceId(clusterConfig.getSecurity().getMsiResourceId())
    );
    Map<String, ClusterIdentityUserAssignedIdentitiesValue>
        userAssignedIdentities = new HashMap<>();
    userAssignedIdentities.put(clusterConfig.getSecurity()
        .getMsiResourceId(), new ClusterIdentityUserAssignedIdentitiesValue());

    addUserIdentitiesForADLS2(clusterConfig, createParams, userAssignedIdentities);
  }

  private static void addUserIdentitiesForADLS2(HWCClusterConfig clusterConfig,
      ClusterCreateParametersExtended createParams,
      Map<String, ClusterIdentityUserAssignedIdentitiesValue> userAssignedIdentities) {
    if (clusterConfig.getStorage().getType() == StorageType.ADLS_GEN2) {
      userAssignedIdentities.putIfAbsent(String.format(MANAGED_IDENTITIES,
          clusterConfig.getSubscription(), clusterConfig.getStorage()
              .getMangedIdentityResourceGroup(), clusterConfig.getStorage()
              .getManagedIdentityName()),
          new ClusterIdentityUserAssignedIdentitiesValue());
    }
    createParams.withIdentity(new ClusterIdentity()
        .withType(ResourceIdentityType.USER_ASSIGNED)
        .withUserAssignedIdentities(userAssignedIdentities));
  }

  public static void createHDICluster(HWCClusterConfig clusterConfig,
      Azure azure, HDInsightManager manager, ClusterType clusterType) {
    ClusterCreateParametersExtended createParams =
        getClusterCreateParameters(clusterConfig, azure, clusterType);

    final String clusterName = clusterConfig.getClusterNamePrefix()
        .substring(0,3) + "-" + clusterType.toString().toLowerCase();
    LOG.info("Submitting {} cluster creation with name {}",
        clusterType.getType(), clusterName);

    try {
      manager.clusters().inner()
          .create(clusterConfig.getResourceGroup(),
              clusterName, createParams);
      LOG.info("Successfully created {} cluster with name : {} ",
          clusterType, clusterName);
    } catch (Exception e) {
      LOG.error("Unable to complete {} cluster creation with name : {}",
          clusterType, clusterName, e);
    }
  }
}