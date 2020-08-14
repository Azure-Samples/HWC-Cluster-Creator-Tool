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
type: SPARK_AND_LLAP # [Optional], values can be either SPARK_ONLY or LLAP_ONLY. Default is SPARK_AND_LLAP
clusterNamePrefix: Foo # This prefix is used while creating the cluster name. Only first three chars are used as prefix from this string.
resourceGroup: foobar-rg # Resource group where the cluster needs to be created
region: eastus2 # Region name where the cluster needs to be created, should be in smallcase without space 
headNodeVMSize: STANDARD_D13_V2 # Any Standrad VM Size supported for Head Nodes
workerNodeVMSize: STANDARD_D13_V2 # Any Standrad VM Size supported for Worker Nodes
workerNodeSize: 3 # Size of worker nodes
subscription: YOUR_SUBSCRIPTION # Subscription ID

activeDirectory:
  azureEnv: AZURE # Azure Env either AZURE_CHINA/AZURE_GERMANY/AZURE_US_GOVERNMENT , default AZURE
  clientId: YOUR_CLIENT_ID # Client ID of the service principal
  tenantId: YOUR_TENANT_ID # Tenant ID of the service principal
  clientSecret: YOUR_CLIENT_SECRET # Client Secret for the service principal

clusterCredentials:
  clusterLoginUsername: YOU_USER_NAME # Ambari username
  clusterLoginPassword: YOUR_PASSWORD # Ambari password
  sshUsername: SSH_USER # SSH username
  sshPassword: SSH_PASSWORD # SSH password

storage:
  type: WASB # Default is WASB, we can use ADLS_GEN2 as well
  endpoint: <YOUR_STORAGE_ACCOUNT>.blob.core.windows.net or <YOUR_STORAGE_ACCOUNT>.dfs.core.windows.net # Storage account name 
  key: YOUR_STORAGE_KEY # Storage key for WASB, not required for ADLS_GEN2
  resourceGroup: <RESOUCE_GROUP> # Resource group where ADLS Gen2 exist
  managedIdentityName: <IDENTITY_NAME> # Managed Identity Name
  mangedIdentityResourceGroup: <IDENTITY_RESOURCE_GREOUP> # Resource Group name where the Managed Identity exist


network:
  vnetName: YOUR_VNET # VNet Name to be used
  resourceGroup: VNET_RESOURCE_GROUP # Resource group in which the VNet exists
  subnetName: SUBNET_NAME # Subnet name to be used within a VNet 
  create: false # If true, creates new one (resourceGroup here is not required), else configures the existing VNet and Subnet from the resourceGroup mentioned

security:  # This has to be configured only for Secure(ESP Enabled) clusters, for standard clusters this is not required
  ldapUrl: YOUR_LDAP_URL # LDAP URL of the AAD-DS
  domainUserName: YOUR_DOMAIN_USERNAME # eg : foobar@securehadoop.onmicrosoft.com
  aaddsDnsDomainName: YOUR_AADDS_DNS_DOMAIN_NAME # eg: securehadoop.onmicrosoft.com
  clusterAccessGroup: YOUR_ACCESS_GROUP # eg: clusterusers
  aaddsResourceId: YOUR_AADDS_RESOURCE_ID # eg: /subscriptions/YOUR_SUBSCRIPTION_ID/resourceGroups/YOUR_RESOURCE_GROUP/providers/Microsoft.AAD/domainServices/YOUR_AADDS_DNS_DOMAIN_NAME
  msiResourceId: YOUR_MANAEGED_IDENTITY # /subscriptions/YOUR_SUBSCRIPTION/resourceGroups/YOUR_RESOURCE_GROUP/providers/Microsoft.ManagedIdentity/userAssignedIdentities/YOUR_IDENTITY
```


### Running the tool
- Clone the repository
    ```bash
    git clone https://github.com/Sushil-K-S/HWC-Cluster-Creator-Tool.git
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

Once the tool creates the cluster, we can verify wether the Hive Warehouse Connection from HDI Spark to HDI LLAP Cluster is working properly
- Use [ssh command](https://docs.microsoft.com/en-us/azure/hdinsight/hdinsight-hadoop-linux-use-ssh-unix) to connect to your Apache Spark cluster. Edit the command below by replacing CLUSTERNAME with the name of your cluster, and then enter the command.<br/> 
<strong>Note</strong>: If the custom VNet is created from this tool, then we need to manually add the inbound rule inside the NSG for ssh i.e allow connection to port 22 inside Virtual Network from any source. By default this rule is not added.
    ```cmd
    ssh sshuser@CLUSTERNAME-ssh.azurehdinsight.net
    ```
- For Secure Clusters, use kinit before starting the spark-shell or spark-submit. Replace USERNAME with the name of a domain account with permissions to access the cluster, then execute the following command:
    ```bash
    kinit USERNAME
    ```
- From your ssh session, execute the following command to note the hive-warehouse-connector-assembly version:
    ```bash
    ls /usr/hdp/current/hive_warehouse_connector
    ```

- Edit the code below with the hive-warehouse-connector-assembly version identified above. Then execute the command to start the spark shell:
    ```bash
    spark-shell --master yarn \
    --jars /usr/hdp/current/hive_warehouse_connector/hive-warehouse-connector-assembly-<STACK_VERSION>.jar \
    --conf spark.security.credentials.hiveserver2.enabled=false \
    --conf spark.hadoop.hive.llap.daemon.service.hosts=@llap0 \
    --conf spark.sql.hive.hiveserver2.jdbc.url=jdbc:hive2://<LLAPCLUSTERNAME>.azurehdinsight.net:443/;user=admin;password=PWD;ssl=true;transportMode=http;httpPath=/hive2 \
    --conf spark.datasource.hive.warehouse.load.staging.dir=<STORAGE_PATH> \
    --conf spark.datasource.hive.warehouse.metastoreUri=thrift://<HEADNODE-0 HOSTNAME of LLAP Cluster>:9083,thrift://<HEADNODE-1 HOSTNAME of LLAP Cluster>:9083 \
    --conf spark.hadoop.hive.zookeeper.quorum='<ZOOKEEPER_QUORUM>'
    ```
    - <strong>STORAGE_PATH</strong> :
        1. <strong>For WASB</strong> wasbs://<FIRST_3_CHARS_OF_CLUSTER_NAME_PREFIX>-interactivehive@<STORAGE_ACCOUNT_NAME>.blob.core.windows.net/tmp
        2. <strong>For ADLS2</strong> abfs://<FIRST_3_CHARS_OF_CLUSTER_NAME_PREFIX>-interactivehive@<STORAGE_ACCOUNT_NAME>.dfs.core.windows.net/tmp/ 
    - <strong>ZOOKEEPER_QUORUM</strong> :
         \<ZK-HOSTNAME-OF-LLAP-CLUSTER-1>:2181,\<ZK-HOSTNAME-OF-LLAP-CLUSTER-2>:2181,\<ZK-HOSTNAME-OF-LLAP-CLUSTER-3>:2181
- Test read from Hive to Spark. The output of "result.first().getLong(1)" should be 2 if everything worked properly. 
    ```scala
    import com.hortonworks.hwc.HiveWarehouseSession 
    import com.hortonworks.hwc.HiveWarehouseSession._ 
    val hive = HiveWarehouseSession.session(spark).build() 
    hive.createDatabase("hwc_test", false) 
    hive.setDatabase("hwc_test") 
    hive.createTable("test_table").column("fst", "bigint").column("snd", "bigint").create() 
    hive.executeUpdate("insert into table test_table values (1, 2)") 
    val result = hive.executeQuery("select * from test_table")
    result.first().getLong(1)
    ```
- Test write from Spark to Hive (assuming the previous step was executed in the same session). The variable "result_2" should get 1 if everything worked properly. 
    ```scala
    hive.createTable("test_table_2").column("fst", "bigint").column("snd", "bigint").create() 
    result.write.format("com.hortonworks.spark.sql.hive.llap.HiveWarehouseConnector").option("table", "test_table_2").mode("append").save() 
    val result_2 = hive.executeQuery("select * from test_table_2").first().getLong(0)
    ```
- Once you are able to test the HWC Connection from HDI Spark to HDI LLAP cluster, you can [refer these steps](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector#configure-spark-cluster-settings) for setting the spark configs inside Ambari instead of passing them everytime we launch the spark-shell command. 
## Additional Resources


- [Integrate Apache Spark and Apache Hive with Hive Warehouse Connector in Azure HDInsight](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector)
- [Apache Spark operations supported by Hive Warehouse Connector in Azure HDInsight](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector-operations)
- [Integrate Apache Zeppelin with Hive Warehouse Connector in Azure HDInsight](https://docs.microsoft.com/en-us/azure/hdinsight/interactive-query/apache-hive-warehouse-connector-zeppelin)
