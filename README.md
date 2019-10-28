  # Enterprise Sales and Procurement Model (ESPM) Cloud Native

  ## Description

  Enterprise Sales and Procurement Model (ESPM) Cloud Native is reference application to showcase how resilience patterns can be implemented in a Cloud Native application. It is built based on microservices architecture principles. Each microservice is built as a [Spring Boot](https://spring.io/projects/spring-boot) application. The current scope of the application showcases the below resilience patterns
  * Retry
  * Timeout
  * Circuit Breaker
  * Shed Load
  * Unit Isolation

  ## Table of Contents
  <!-- toc -->

- [Architecture](#architecture)
  * [Retry](#retry)
  * [Timeout](#timeout)
  * [Circuit Breaker](#circuit-breaker)
  * [Shed Load](#shed-load)
  * [Unit Isolation](#unit-isolation)
- [REST API](#rest-api)
  * [Swagger API Definition](#swagger-api-definition)
- [Requirements](#requirements)
  * [SAP Cloud Platform Enterprise Messaging](#sap-cloud-platform-enterprise-messaging)
- [Running ESPM Application Locally](#running-the-espm-application-locally)
- [Running the application on Cloud Foundry using MTA](#running-the-espm-application-locally)
- [Running the application on Cloud Foundry](#running-the-application-on-cloud-foundry)
- [Accessing the Cloud Foundry API Endpoints](#accessing-the-cloud-foundry-api-endpoints)
  * [Customer Service](#customer-service-3)
  * [Product Service](#product-service-2)
  * [Sales Service](#sales-service-3)
- [Resilience Patterns in action](#resilience-patterns-in-action)
- [Known issues](#known-issues)
- [Out of Scope](#out-of-scope)
- [Support](#support)
- [License](#license)
<!-- tocstop -->


## Architecture
![Alt text](./documentation/images/ESPM-CN.JPG "Architecture")


The ESPM applications consists of five microservices and one external service.
1. Customer Service - This service process customer and shopping cart information
2. Product Service - This service can be used to process products and stock information
3. Sales Services - Sales Orders are processed by this service. Each time a sales order is created, it’s not directly inserted into the database, but inserted into a queue. A background process called worker picks the message from queue and inserts to the database. The rationale behind this approach is explained later in the document. For read operation on sales order, its directly read from the database.
4. Worker - Background process which picks the Sales Order from the queue and inserts it into the database.
5. Gateway - it’s an optional component and acts as entry point for the complete application. It also acts as a reverse proxy and routes the request to the appropriate microservice. We have added UI for the application to showcase end to end story of a customer. We have specified the destinations in the manifest.yml file as part of the destinations environment variable.
6. External Tax Service - This is a service which is external to the application and used to do tax calculation. This Tax calculation service is provided, to be used along with the implementation of Circuit Breaker, Quarantine pattern.

A [Domain Driven Design](https://en.wikipedia.org/wiki/Domain-driven_design) approach was used to decide the capabilities of each microservices. The Customer and Cart entities are part of the Customer Microservice and Product and Stock entities are part of the Product Service. To keep things simple there is only one entity in Sales Service which is the Sales Order entity. In real world scenarios, Sales Entity might have Sales Order Header and Sales Order Line Items Entity and more. The Product and Customer service has its own database while Sale and worker shares the same database.

Each of the resilience patterns has been fit into architecture of the ESPM Application to showcase how they can make an application resilient during potential failures. These are some of the potential places where the pattern could be applied. There could be more points in the application where the pattern could have been applied to make it more resilient.

#### Retry
In a distributed environment some resources may not be reachable or unavailable due to network latency or network glitches. A simple retry might cause the execution of a task to succeed which would have failed, if no retry was attempted. This pattern is showcased by wrapping the database calls in Product and Customer Service with a retry. This ensures that if the database is not momentarily reachable a retry will ensure that the task succeeds.

#### Timeout
It's usually not possible to predict how long it will take for response while calling an external service.  Defining a timeout ensures that the caller be interrupted and does not wait indefinitely if the no response is received.  The timeout is implemented in the Sales Service while calling the external Tax Service. This ensures that Sales Service is not indefinitely blocked by calls to Tax Service.

#### Circuit Breaker
This pattern addresses the challenge in communicating with an external system. The status of the external system is not known, and it could be under load and not responding.  The circuit breaker tackles these problems by introducing a kind of circuit for each external dependency. If a problem is identified, the circuit on the caller side controls the behavior of the calls in future. The circuit breaker is implemented in the Sale Service of ESPM application for communicating with the external Tax service. The Tax service could be temporarily, unavailable, under load or non-responsive. The Circuit Breaker ensures that if Tax service is not reachable the circuit is opened, and no future calls goes Tax service and a fall back service or mechanism is used for Tax Calculation.

#### Shed load
This pattern focuses on handling the rate at which requests are coming and reject requests before processing, if the system can't handle it. Each request consumes memory. If the system tries to process too many requests than it can handle, it can crash. Shedding the load by rejecting requests which it can't handle as early as possible, ensures that the application remains healthy and does not crash. The system can define a fixed rate for accepting request or be elastic and decide at runtime the current load on resources and decide to accept or reject the request. The Shed Load pattern is implemented in Product and Customer Service to avoid spike in the number of concurrent requests handled by the application. The number of requests which can be processed at a point in time is fixed to specific number and the requests exceeding this number is rejected.

#### Unit Isolation
The focus of this pattern is on the design of the failure unit. A failure unit is the entity of an application that can fail without overall availability of the entire application being affected.  The microservices architecture paradigm itself brings in a level of unit isolation while applying methodology of domain driven design to define the units.

## REST API

### Swagger API Definition

#### Customer Service
https://customer-service.cfapps.eu10.hana.ondemand.com

#### Product Service
https://product-service.cfapps.eu10.hana.ondemand.com

#### Sales Service
https://sales-service.cfapps.eu10.hana.ondemand.com


## Requirements

Before running ESPM application one would need

* Java 8
* [Apache Maven](https://maven.apache.org/)
* SAP Cloud Platform account
* [SAP Cloud Platform Enterprise Messaging](https://help.sap.com/viewer/product/SAP_ENTERPRISE_MESSAGING/Cloud/en-US)
* To deploy the MTAR we need the MTA CF CLI plugin, download the MTA CF CLI Plugin from [here](https://tools.hana.ondemand.com/#cloud)
* The MultiApps CF CLI Plugin is now also available on the CF Community Repository. To install the latest available version of the MultiApps CLI Plugin execute the following:

cf install-plugin multiapps

* If you do not have the community repository in your CF CLI you can add it first by executing:

cf add-plugin-repo CF-Community https://plugins.cloudfoundry.org


* The multi-target application archive builder is a standalone command-line tool that builds a deployment-ready multi-target application (MTA) archive .mtar file from the artifacts of an MTA project according to the project’s MTA development descriptor (mta.yaml file).The archive builder is used on a file system independently of the development environment in which the application project has been created. The build process and the resulting MTA archive depend on the target platform on which the archive is deployed. Download MTA archive builder - jar file from [here](https://tools.hana.ondemand.com/#cloud) and rename it as mta.jar

### SAP Cloud Platform Enterprise Messaging

* Create new enterprise messaging service using the command:

`cf cs enterprise-messaging dev espm-em -c em.json`

Make sure that the emname is a unique name

For more details, check [here](https://help.sap.com/viewer/bf82e6b26456494cbdd197057c09979f/Cloud/en-US/d0483a9e38434f23a4579d6fcc72654b.html)

* Open the dashboard for the service that is created and create a queue.

![Alt text](./documentation/images/EM.png "Enterprise Messaging")


## Running the ESPM application locally

Please refer [link](https://github.com/SAP-samples/cloud-espm-cloud-native/tree/localDeployment#running-the-espm-application-locally).

## Running the application on Cloud Foundry using MTA

From the root folder where mta.yaml is kept run the command: 

	java -jar mta.jar --build-target=CF --mtar=cloud-espm-cf.mtar build
	
This will package your application to be ready for deployment.

As you can see from MTA file, the service names for enterprise-messaging is kept as enterprise-messaging-dev, destination as espm-destination, hana as espm-hana-db. Make sure to rename unique id as your I/C/D number.

To Deploy MTAR, run the command:

	cf deploy cloud-espm-cf.mtar

## Running the application on Cloud Foundry

To run the application on Cloud Foundry you need an account on SAP Cloud Platform Cloud Foundry Environment or signup for a [SAP Cloud Platform Cloud Foundry Environment trial account](https://cloudplatform.sap.com/try.html)
*Please note that in SAP Cloud Platform Cloud Foundry Environment,  for a trial account, there is limited resource and you get a RAM of 2 GB which is not sufficient to run the complete ESPM application.*  
To run the complete ESPM application, one will need around 5.5 GB of RAM. Each of the 5 Spring boot applications (Product Service, Customer Service, Sales Service, Worker and Tax Service) needs 1 GB of RAM and Gateway (based on Node.js) which also contains UI for the application, needs around 512 MB. The optimal way to run application is
* Signup for SAP Cloud Platform Neo trial account by following [these steps](https://cloudplatform.sap.com/try.html)
* Run Tax service in [SAP Cloud Platform Neo Environment](./tax-service#running-on-sap-cloud-platfrom-neo-environment)

* The recommended way to consume the Tax service would be via a [Destination Service](https://help.sap.com/viewer/cca91383641e40ffbe03bdc78f00f681/Cloud/en-US/7e306250e08340f89d6c103e28840f30.html).  

* Run command `cf marketplace` and check the service and plan names of HANA and Enterprise Messaging backing service.

* Create HANA Service instance with `schema` plan.  `cf create-service hana schema espm-hana-db`.

*For simplicity all the microservices are bound to one database instance espm-hana-db. If required three database instances can be created (e.g. esmp-customer, espm-product and espm-sales) and individual microservice can be bound to them*

* Create Enterprise Messaging Service instance as mentioned [above](#sap-cloud-platform-enterprise-messaging). 

* Edit the manifest.yml file and update `<unique_id>` with some unique value for each applications host name

* The TAX SERVICE can be accessed in Cloud Foundary in either of the 2 ways:
   * **Destination Services (Recommended):** <br>
       * Create an instance of the destination service by using the command `cf create-service destination lite espm-destination` <br>
       * From the SCP Cockpit go to your space and open the `espm-destination` service instance in your space.. Create a new destination by clicking `New Destination`
       and filling with the properties as shown below. (URL of tax service running on SAP Cloud Platform Neo or SAP Cloud Platform Cloud Foundry.)
       <br>.

       ![Alt text](./documentation/images/tax-service-properties.png "Adding Destination")<br>
       * The implementation of destination services is in [SalesOrderServiceImpl](./sale-service/src/main/java/com/sap/refapps/espm/service/SalesOrderServiceImpl.java#L214) class.



   * **Environment Variable :** <br>
   If you do not want to configure Destination Services, the alternative approach is to edit the TAX_SERVICE env variable in manifest.yml file under the module espm-sales-svc with the URL of tax service running on SAP Cloud Platform Neo or SAP Cloud Platform Cloud Foundry.<br> 
*Note: This is not a recommended approach since if the tax service url changes the new url must be updated in manifest file for the env TAX_SERVICE and the application must be redeployed. This would mean some downtime for the ESPM application.*

* Do a maven build of complete application from command line by running command `mvn clean install` from the projects root folder.

* Deploy Worker on to Cloud Foundry from the project root folder by running command `cf push <unique_id>-espm-worker` from CLI

* Deploy Sale Service  on to Cloud Foundry from the project root folder by running command `cf push espm-sales-svc` from CLI.

* Learn resilience patterns implemented in Sale and worker services

* Stop Sale and Worker service.

* Deploy Product Service on to Cloud Foundry from the project root folder by running command `cf push espm-product-svc` from CLI.

* Deploy Customer Service on to Cloud Foundry from the project root folder by running command `cf push espm-customer-svc` from CLI.

* Learn resilience patterns implemented in Product and Customer  services

* *[Optional] if one has a non-trial SAP Cloud Platform Cloud Foundry account with 5.5GB of RAM or more, espm-gateway can be deployed via command `cf push espm-gateway` from CLI.*

* When the UI is deployed, you will be presented with a screen where you can enter using the email address provided for a customer. The views themselves are rather simple and use databinding extensively to avoid writing lots of code. You can do the operations like, view details of the customer, display shopping cart, display sales order, create cart, delete cart, create sales order.


### Accessing the Cloud Foundry API Endpoints

The below are the list of local service API endpoints of all the microservices.

#### Customer Service

| |Get Customer by Email ID |
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-customer-svc.cfapps.sap.hana.ondemand.com/customer.svc/api/v1/customers/{emailAddress} 	
| Method       	| `GET`                                                        	|


| |Create Cart |
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-customer-svc.cfapps.sap.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/|
| Header       	| `Content-Type:application/json`                                        |
| Method       	| `POST`                                                                 |
| Body         	| `{"productId": "HT-1000","checkOutStatus": "false","quantityUnit": 3}`     

| |Get Cart by Customer ID |
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-customer-svc.cfapps.sap.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/|
| Method       	| `GET`                                                                	 |


| |Update Cart by Item ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-customer-svc.cfapps.sap.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/{itemId}         
| Header       	| `Content-Type:application/json`                                                         |
| Method       	| `PUT`                                                                                   |
| Body         	| `{"itemId": {itemId},"productId": "HT-1000","quantityUnit": 10,"checkOutStatus": false}`|


| |Delete Cart by Item ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-customer-svc.cfapps.sap.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/{itemId}|
| Method       	| `DELETE`                                                                	 |

#### Product Service


| |Get All Products|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-product-svc.cfapps.sap.hana.ondemand.com/product.svc/api/v1/products		 	|
| Method       	| `GET`                                                                	|

| |Get Product by Product ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-product-svc.cfapps.sap.hana.ondemand.com/product.svc/api/v1/products/{productId}	 	|
| Method       	| `GET`       


| |Get Stock by Product ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-product-svc.cfapps.sap.hana.ondemand.com/product.svc/api/v1/stocks/{productId}	 	|
| Method       	| `GET`       


*The stock is updated by quantity specified. e.g. if the current quantity is 50 and in the body for update stock request quantity is provided as 20 the quantity will be updated to 70*

| |Update Stock by Product ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-product-svc.cfapps.sap.hana.ondemand.com/product.svc/api/v1/stocks/{productId}		 
| Header       	| `Content-Type:application/json`                                        |
| Method       	| `PUT`                                                                 |
| Body        	| `{"productId": "HT-1000","quantity": 20}`     

#### Sales Service

| |Create Sales Order|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-sales-svc.cfapps.sap.hana.ondemand.com/sale.svc/api/v1/salesOrders			 |
| Header       	| `Content-Type:application/json`                                        |
| Method       	| `POST`                                                                 |
| Body         	| `{"customerEmail": "viola.gains@itelo.info","productId": "HT-1000","currencyCode": "EUR", "grossAmount":956,"quantity":4}`     


| |Get Sales Order by Sales Order ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-sales-svc.cfapps.sap.hana.ondemand.com/sale.svc/api/v1/salesOrders/{salesOrderId} 	|
| Method       	| `GET`       

| |Get Sales Order by Customer Email ID|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-sales-svc.cfapps.sap.hana.ondemand.com/sale.svc/api/v1/salesOrders/email/{emailAddress}|
| Method       	| `GET`       

| |Get All Sales Order|
|-|-|
| Endpoint URL 	| https://<unique_id>-espm-sales-svc.cfapps.sap.hana.ondemand.com/sale.svc/api/v1/salesOrders/                    |
| Method       	| `GET`       



## Resilience Patterns in action
You can see the different resilience patterns in action by running the application locally in your system and following the [steps provided](https://github.com/SAP-samples/cloud-espm-cloud-native/tree/localDeployment#resilience-patterns-in-action)

## Out of scope

Currently security aspects like authentication and authorizations have not be considered in this application.

## Known issues

None


## Support

Please use GitHub [issues](https://github.com/SAP-samples/cloud-espm-cloud-native/issues/new) for any bugs to be reported.

## License

Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
This project is licensed under the SAP Sample Code License Agreement except as noted otherwise in the [LICENSE](SAP_Sample_Code_License_Agreementv1.0.docx) file.
