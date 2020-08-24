# HDInsight Cluster Creation Tool for Hive Warehouse Connector (HWC)

![N|Solid](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/media/apache-hive-warehouse-connector/hive-warehouse-connector-architecture.png)

This tool helps to spin-up HWC enabled Azure HDInsight clusters on the given customer subscription, storage account and/or custom VNet, with minimal manual steps.

## Features

This tool provides the following features:
* Creates both HDI Spark and HDI LLAP Clusters under the same VNet and allows required Health and Management Inbound Rules.(Does not allow ssh access inside the inbound rule by Default). 
* Creates only Spark Cluster for a given VNet and Storage account if the LLAP cluster is already created and vice versa.
* Supports  WASB and ADLS_GEN2 storage types. However, user should input an existing storage account before running this tool i.e this tool does not create a new storage account if it does not exist.
* Supports creation of secure HDInsight Clusters i.e clusters with Enterprise Security Pack.
* Custom VNet is configured with minimal Inbound Rules, however if the VNet is already present it can be reused.
* This tool requires Azure Active Directory (AAD) credentials for creating the HDI clusters.


## Getting Started

### Prerequisites

- An active Azure Account for creating HDI Clusters.
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) should be installed.
    - Run the below command for creating a new Service Principal or use the one which is already created.
    ```bash 
    az account set -s YOUR_SUBSCRIPTION
    az ad sp create-for-rbac --name YOUR_SERVICE_PRINCIPAL_NAME --sdk-auth
    ```
    - Store the json info returned from the above command into to a file so that the clientID, tenenatID and clientSecret info can be used while configuring the tool.

### Configuring the Tool

User can set configs in a YAML file (as shown below) and pass it to the tool. The below list covers all the configurations supported by this tool. Additionally, the conf folder in this repo has templates for standard and secure cluster config files
 
```yml
type: SPARK_AND_LLAP # Values can be either SPARK_AND_LLAP , SPARK_ONLY or LLAP_ONLY Default is SPARK_AND_LLAP. For example, if the user has an existing LLAP cluster, they can use this Tool to create Spark Cluster by specifying the type as SPARK_ONLY and configure the network with the existing VNet of the LLAP Cluster by setting create field to false.
clusterNamePrefix: Foo # This prefix is used while creating the cluster name. Only first three chars are used as prefix from this string
resourceGroup: <RESOURCE_GROUP> # Resource group where the cluster needs to be created
region: <REGION> # Region name where the cluster needs to be created, should be in small case without space. Eg: eastus2
headNodeVMSize: STANDARD_D13_V2 # Any Standard VM Size supported for Head Nodes in HDInsight, https://docs.microsoft.com/en-us/azure/hdinsight/hdinsight-supported-node-configuration
workerNodeVMSize: STANDARD_D13_V2 # Any Standard VM Size supported for Worker Nodes in HDInsight, https://docs.microsoft.com/en-us/azure/hdinsight/hdinsight-supported-node-configuration
workerNodeSize: 3 # Size of worker nodes
subscription: <YOUR_SUBSCRIPTION> # Subscription ID

activeDirectory:
  azureEnv: AZURE # Azure Env either AZURE_CHINA/AZURE_GERMANY/AZURE_US_GOVERNMENT , default AZURE
  clientId: <YOUR_CLIENT_ID> # Client ID of the service principal
  tenantId: <YOUR_TENANT_ID> # Tenant ID of the service principal
  clientSecret: <YOUR_CLIENT_SECRET> # Client Secret for the service principal

clusterCredentials:
  clusterLoginUsername: <YOU_USER_NAME> # Ambari username
  clusterLoginPassword: <YOUR_PASSWORD> # Ambari password
  sshUsername: <SSH_USER> # SSH username
  sshPassword: <SSH_PASSWORD> # SSH password

storage:
  type: WASB # Default is WASB, we can use ADLS_GEN2 as well
  endpoint: <YOUR_STORAGE_ACCOUNT>.blob.core.windows.net # For WASB <YOUR_STORAGE_ACCOUNT>.blob.core.windows.net and for ADLS_GEN2 <YOUR_STORAGE_ACCOUNT>.dfs.core.windows.net
  key: <YOUR_STORAGE_KEY> #[Required for WASB] Storage key for WASB
  resourceGroup: <RESOURCE_GROUP> #[Required for ADLS_GEN2] Resource group where ADLS_GEN2 exist
  managedIdentityName: <IDENTITY_NAME> #[Required for ADLS_GEN2] Managed Identity Name for ADLS_GEN2
  mangedIdentityResourceGroup: <IDENTITY_RESOURCE_GROUP> #[Required for ADLS_GEN2] Resource Group name where the Managed Identity exist for ADLS_GEN2

network:
  vnetName: <YOUR_VNET> # VNet Name to be used
  resourceGroup: <VNET_RESOURCE_GROUP> # Resource group in which the VNet exists
  subnetName: <SUBNET_NAME> # Subnet name to be used within a VNet
  create: false # If true, creates new one (resourceGroup here is not required), else configures the existing VNet and Subnet from the resourceGroup mentioned

security:  #[Optional] This has to be configured only for Secure(ESP Enabled) clusters, for standard clusters this is not required
  ldapUrl: <YOUR_LDAP_URL> # LDAP URL of the AAD-DS
  domainUserName: <YOUR_DOMAIN_USERNAME> # eg : foobar@securehadoop.onmicrosoft.com
  aaddsDnsDomainName: <YOUR_AADDS_DNS_DOMAIN_NAME> # eg: securehadoop.onmicrosoft.com
  clusterAccessGroup: <YOUR_ACCESS_GROUP> # eg: clusterusers
  aaddsResourceId: <YOUR_AADDS_RESOURCE_ID> # eg: /subscriptions/<YOUR_SUBSCRIPTION_ID>/resourceGroups/<YOUR_RESOURCE_GROUP>/providers/Microsoft.AAD/domainServices/<YOUR_AADDS_DNS_DOMAIN_NAME>
  msiResourceId: <YOUR_MANAGED_IDENTITY> # /subscriptions/<YOUR_SUBSCRIPTION>/resourceGroups/<YOUR_RESOURCE_GROUP>/providers/Microsoft.ManagedIdentity/userAssignedIdentities/<YOUR_IDENTITY>
```


### Running the tool
- Clone the repository
    ```bash
    git clone https://github.com/Azure-Samples/HWC-Cluster-Creator-Tool.git
    ```
- Build the repository
    ```bash
    mvn clean install
    ```

- Launch the tool for creating HWC Cluster
    ```bash
    java -cp target/HWC-ClusterCreator-1.0-SNAPSHOT-jar-with-dependencies.jar com.microsoft.hdinsight.HWCClusterCreator YAML_CONFIG_PATH
    ```

## Verify the HWC Cluster Setup
- [Apache Spark operations supported by Hive Warehouse Connector in Azure HDInsight](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector-operations)
 
## Additional Resources
- [Integrate Apache Spark and Apache Hive with Hive Warehouse Connector in Azure HDInsight](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector)
- [Integrate Apache Zeppelin with Hive Warehouse Connector in Azure HDInsight](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector-zeppelin)
