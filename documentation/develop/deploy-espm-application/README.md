## Deploying the ESPM Application Locally

* Download the project from GitHub or Open the Gitbash/cmd (if you have Git installed) and clone the project by using the command
  `git clone https://github.com/SAP-samples/cloud-espm-cloud-native.git`
* The project is built as set of maven modules. Each maven module is Spring Boot application and is packaged as a jar.
* Do a maven build of the application by running command `mvn clean install` from the root folder of the project. If tests need to be skipped, run the command `mvn clean install -Dmaven.test.skip=true`

Follow steps below to run each microservice of ESPM one by one. Please ensure that message server and SQL Database server are running before you start.

#### Customer Service

* Navigate to `/customer-service/src/main/resources` in the project you have cloned
* If required update the `<PORT_NO>`, `<DATABASE_NAME>`, `<USERNAME>`, `<PASSWORD>`  in application.properties file.

  ```
	#Local postgresql DB configuration
	jdbc.sql.driverClassName = org.postgresql.Driver
	jdbc.sql.url = jdbc:postgresql://localhost:<PORT_NO>/<DATABASE_NAME>
	jdbc.sql.username =  <USERNAME>
  jdbc.sql.password =  <PASSWORD>
  ```
  *The default values for the parameters mentioned above are already present in the application.properties file.*

* Navigate to customer-service folder and execute the command to run the application

  `mvn spring-boot:run`

* Ensure that the following log is found in the bottom of the console

  ~~~
  INFO 35816 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 9991 (http)
  INFO 35816 --- [           main] com.sap.refapps.espm.Application         : Started Application in 7.806 seconds (JVM running for 12.842)
  ~~~

* Test the Customer Service by running the url http://localhost:9991/customer.svc/api/v1/customers/viola.gains@itelo.info

#### Product Service

* Navigate to `/product-service/src/main/resources` in the project you have cloned
* If required update the `<PORT_NO>`,`<DATABASE_NAME>`, `<USERNAME>`, `<PASSWORD>`  in application.properties file.

  ```
	#Local postgresql DB configuration
	jdbc.sql.driverClassName = org.postgresql.Driver
	jdbc.sql.url = jdbc:postgresql://localhost:<PORT_NO>/<DATABASE_NAME>
	jdbc.sql.username =  <USERNAME>
	jdbc.sql.password =  <PASSWORD>
  ```
  *The default values for the parameters mentioned above are already present in the application.properties file.*

* Navigate to product-service folder and execute the command to run the application

  `mvn spring-boot:run`

* Ensure that the following log is found in the bottom of the console

  ~~~
  INFO 35816 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 9992 (http)
  INFO 35816 --- [           main] com.sap.refapps.espm.Application         : Started Application in 7.806 seconds (JVM running for 12.842)
  ~~~
* Test the Product Service by running the url http://localhost:9992/product.svc/api/v1/products

#### Worker

* Navigate to `/worker/src/main/resources` in the project you have cloned
* If required update the `<PORT_NO>`,`<DATABASE_NAME>`, `<USERNAME>`, `<PASSWORD>`  in application.properties file.

  ```
	#Local postgresql DB configuration
	jdbc.sql.driverClassName = org.postgresql.Driver
	postgresql.url = jdbc:postgresql://localhost:<PORT_NO>/<DATABASE_NAME>
	jdbc.sql.username =  <USERNAME>
	jdbc.sql.password =  <PASSWORD>
  ```
  *The default values for the parameters mentioned above are already present in the application.properties file.*

* Ensure that [Qpid](#message-server) and [PostgreSQL](#sql-database-server) are running in your system.

* Navigate to worker folder and execute the command to run the application

  `mvn spring-boot:run`

* Ensure that the following log is found in the bottom of the console

  ~~~

  INFO 35816 --- [           main] com.sap.refapps.espm.Application         : Started Application in 7.806 seconds (JVM running for 12.842)
  ~~~

#### Tax Service

[Tax service](/tax-service) is an external service. This service does Tax calculation while a sales order is created. Tax Service can be locally deployed by following [these](/tax-service#running-locally-as-spring-boot-application) steps.

* Test the Tax Service by running the url  http://localhost:9994/tax.svc/api/v1/calculate/tax?amount=1000

#### Sales Service

* Navigate to `/sale-service/src/main/resources` in the project you have cloned
* If required update the `<PORT_NO>`, `<DATABASE_NAME>`, `<USERNAME>`, `<PASSWORD>`  in application.properties file.

  ```
	#Local postgresql DB configuration
	jdbc.sql.driverClassName = org.postgresql.Driver
	jdbc.sql.url = jdbc:postgresql://localhost:<PORT_NO>/<DATABASE_NAME>
	jdbc.sql.username =  <USERNAME>
	jdbc.sql.password =  <PASSWORD>
  ```
  *The default values for the parameters mentioned above are already present in the application.properties file.*

* Update the tax.service parameter with the url where tax service is running locally.

* Navigate to sale-service folder and execute the command to run the application

  `mvn spring-boot:run`

* Ensure that the following log is found in the bottom of the console

  ~~~
  INFO 35816 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 9993 (http)
  INFO 35816 --- [           main] com.sap.refapps.espm.Application         : Started Application in 7.806 seconds (JVM running for 12.842)
  ~~~
  
* Test the Sale Service by running the url http://localhost:9993/sale.svc/api/v1/salesOrders/

| Lifecycle |  Life Cycle Status Name | Note |
|--|--|--|
| N | New |	When the Sales Order is created  |
| I | In Progress | |
| C | Cancelled | When the product is Out Of Stock |
| S | Shipped | When the Sales Order is Shipped |
| R | Rejected | When the Sales Order is Rejected by Retailer |
| D | Delivered | |

#### Gateway

* In the root folder of the project, execute the command to build the Gateway project (Gateway is a [Node.js](https://nodejs.org/en/) project, but it can be built using maven node plugins)

  `mvn clean install -pl gateway`

* Gateway acts as the single-entry point into the ESPM application. It's implemented using SAP HANA XS Advanced [Approuter](https://help.sap.com/viewer/4505d0bdaf4948449b7f7379d24d0f0d/2.0.03/en-US/0117b71251314272bfe904a2600e89c0.html) library.
* Navigate to gateway folder.
* Change the authenticationMethod and authenticationType to `none` in xs-app.json.
* Configure all three microservice end point by specifying the name (destination name) and local url of the microservice in the file default-env.json as shown below
`{
    "name": "customer-service",
    "url": "http://localhost:9991",
	"strictSSL": false
  }`  
* Approuter port can be configured via PORT parameter in default-env.json. By default, the port is set to 9999

* Once all microservices are running and their endpoints are specified in default-env.json, API gateway being a Node.js component can be run via the command from gateway project `node\npm start` to run it locally.
* This will start gateway in the url http://localhost:9999
* Once gateway is started all the microservice are accessible via the gateway url http://localhost:9999
  E.g. Get Customer by Email Address http://localhost:9999/customer.svc/api/v1/customers/{emailAddress}
