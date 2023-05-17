## Prerequisites and Required Systems
This section contains the prerequisites that you have to fulfill before you get started. Make sure that the prerequisites are fulfilled and all required systems, services, and tools are available. 

* SAPMachineJDK11
* [Apache Maven](https://maven.apache.org/)
* To build the multi target application, we need the [Cloud MTA Build tool](https://sap.github.io/cloud-mta-build-tool/), download the tool from [here](https://sap.github.io/cloud-mta-build-tool/download/)
* For Windows system, install 'MAKE' from https://sap.github.io/cloud-mta-build-tool/makefile/
>Note: Please set the npm registry for @sap libraries using the command :  
`npm set @sap:registry= https://registry.npmjs.org/`
* Install the following:
	1. grunt 	- `npm install -g grunt-cli`
	2. cds	 	- `npm install -g @sap/cds`
	3. [multiapps plugin](https://github.com/cloudfoundry-incubator/multiapps-cli-plugin) - `cf install-plugin multiapps`  
	4. mbt         -  `npm install -g mbt`

For Running locally:
* Message Server
  [Apache Qpid](https://qpid.apache.org/) will be used as message server for local development and testing (steps on installing QPID can be found below. Qpid was chosen was local development as it's easy to install and setup. (Ensure JDK 11 is present)
* Database Server
  [PostgreSQL](https://www.postgresql.org/) would be used as the SQL Database server for local development.


### Message server

* Download Qpid Broker-J 8.0 from [its repository](https://qpid.apache.org/releases/qpid-broker-j-8.0.6/)

>Note: Qpid Broker-J 7.0 is incompatible with JDK 11.`  
* Extract the zip and navigate to the bin directory
* To run Qpid server
     - Windows - Run the qpid-server.bat
     - Linux/Mac - Run ./qpid-server

* On the first run a qpid-broker a default config.json will be generated in your user directory
  * On windows C:\users\<username>\Appdata\roaming\Qpid\config.json
  * On Linux/Mac /Users/<username>/config.json
	
>Note: If you're facing issues in starting the qpid server, please delete the already existing Qpid\config.json file and then re-start the server again.
		
* add the property "secureOnlyMechanisms": [], in the config.json file to disable SSL, as indicated in [sample file](https://github.com/SAP-samples/cloud-espm-cloud-native/blob/master/documentation/config.json#L9). Please do not use the sample file but update your own config.json file with this property.
* Stop Qpid server and start it again
* The default Qpid user is <b>guest</b> and password is also <b>guest</b>

### SQL Database Server

* Download PostgreSQL Database Server from [hits repository](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) by selecting the version and OS.
* Run the downloaded installer and follow the instructions to install and run the PostgreSQL.

	*<b>make a note of the password and port number</b>*
	

For Cloud:

* SAP Business Technology Platform account with [SAP Event Mesh](https://help.sap.com/viewer/product/SAP_ENTERPRISE_MESSAGING/Cloud/en-US) service. The 'default' plan for SAP Event Mesh service is required.
* To deploy the MTAR we need the MTA CF CLI plugin, download the MTA CF CLI Plugin from [here](https://tools.hana.ondemand.com/#cloud)
* The MultiApps CF CLI Plugin is now also available on the CF Community Repository. To install the latest available version of the MultiApps CLI Plugin execute the following:

cf install-plugin multiapps

* If you do not have the community repository in your CF CLI you can add it first by executing:

cf add-plugin-repo CF-Community https://plugins.cloudfoundry.org
	
### Entitlements
	
| Service                                  | Plan       | Number of Instances |
|------------------------------------------|------------|:-------------------:|
| SAP Event Mesh                           | default    |          1          |
| SAP HANA Schemas & HDI Containers        | schema     |          1          |
| SAP HANA cloud                           | hana       |          1          |
| Cloud Foundry Runtime                    |            |          7          |		
