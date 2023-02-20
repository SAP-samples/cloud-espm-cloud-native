## Explore the Database

ESPM Application uses Spring MVC with JPA. JPA (Java Persistence API) is an API that works as a bridge between Java application and Relational database. It is the standard way of persisting Java objects into relational databases. It is a set of classes and interfaces that are used to perform database operations efficiently.

This project uses the database server based on the environment it is deployed.

* PostgreSQL is used as the SQL Database server for local deployment. [PostgreSql JDBC Driver](https://jdbc.postgresql.org/documentation) provides connectivity to postgreSql database in java Application. PostgresSql Jdbc connection is provided in [application.properties](https://github.com/SAP-samples/cloud-espm-cloud-native/blob/main/customer-service/src/main/resources/application.properties) of each microservice.

* SAP HANA Cloud is used as the Database server for cloud deployment. We use SAP HANA Schema on SAP HANA Cloud database for persistence.
[SAP HANA JDBC Driver](https://help.sap.com/docs/SAP_HANA_CLIENT/f1b440ded6144a54ada97ff95dac7adf/434e2962074540e18c802fd478de86d6.html?version=latest) provides connectivity to HANA Databases in Java applications.

## How Does It Work?

Lets take produce service as an example.
* [Product.java](/product-service/src/main/java/com/sap/refapps/espm/model/Product.java) is our entity class that represents the product table structure in the database.
We used @Entity annotation to mark it as an Entity. The Spring will create a product table into the database.

* [ProductRepository.java](/product-service/src/main/java/com/sap/refapps/espm/repository/ProductRepository.java) is our DAO(Data Access Object) implementation class that performs database operations. 
It extends [CrudRepository](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html), a Spring Data interface for generic CRUD operations on a repository of a specific type. It provides several methods out of the box for interacting with a database.

* [ProductController.java](/product-service/src/main/java/com/sap/refapps/espm/controller/ProductController.java#L43) is our controller class which is responsible for handling all endpoints.

Database Tables That Are Used ESPM Application's persistence layer has the following tables: 

* Product
* Stock
* Customer
* Cart
* salesOrder

### Product
The Product table is used to store the details of the Products.

The following fields are part of this table:

* PRODUCT_ID	
* NAME	
* SHORT_DESCRIPTION	
* CATEGORY	
* WEIGHT	
* WEIGHT_UNIT	
* PRICE	NUMERIC		
* CURRENCY_CODE		
* DIMENSION_WIDTH			
* DIMENSION_DEPTH			
* DIMENSION_HEIGHT	
* DIMENSION_UNIT		
* PICTURE_URL		
	
### Stock	
The Stock table is used to store the details of the Quantity of products.

The following fields are part of this table:

* PRODUCT_ID
* QUANTITY
	
### Customer
The Customer table is used to store the details of the Customers.

The following fields are part of this table:	

* EMAIL_ADDRESS	
* PHONE_NUMBER			
* FIRST_NAME			
* LAST_NAME			
* DATE_OF_BIRTH		
* CITY			
* POSTAL_CODE			
* STREET				
* HOUSE_NUMBER			
* COUNTRY			

### Cart
The Cart table is used to store the details of products in the cart.

The following fields are part of this table:

* ITEM_ID		
* CUSTOMER_ID			
* PRODUCT_ID			
* QUANTITY			
* CHECK_OUT_STATUS	
	
### SalesOrder
The salesOrder table is used to store the details of the orders.

The following fields are part of this table:	

* SALES_ORDER_ID		
* CUSTOMER_EMAIL			
* CURRENCY_CODE			
* GROSS_AMOUNT			
* NET_AMOUNT			
* TAX_AMOUNT			
* LIFE_CYCLE_STATUS	
* LIFE_CYCLE_STATUS_NAME	
* CREATED_AT			
* DELIVERY DATE			
* PRODUCT_ID			
* QUANTITY			
* QUANTITY_UNIT			


	
	
