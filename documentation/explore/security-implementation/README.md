## Security Implementation

The security implementation in the ESPM application is based on [Spring Security](https://spring.io/projects/spring-security-oauth). Spring applications using the Spring-security libraries can integrate with the SAP Business Technology Platform Authorization and Trust Management service as described [here](https://help.sap.com/docs/SAP_HANA_PLATFORM/4505d0bdaf4948449b7f7379d24d0f0d/cc45f1833e364d348b5057a60d0b8aed.html#configure-spring-security-for-spring-boot-applications%0A(spring-xsuaa)). ESPM Application implements app-to-app communication so that two microservices can securely communicate with each other. This application showcases how to implement a secure communication using two  different ways:

- Propagating a Business User
- Using a Technical User

#### Propagating a Business User

In this approach, the business user is authenticated and his authorizations are used to call another microservice. The user is therefore known to the microservice that he is calling.

#### Using a Technical User

In this approach, a technical user is used to access data from another microservice. The called microservice grants the calling application the necessary rights without identifying a user.


Both methods have their use cases, depending on whether or not you need to identify the business user and grant access based on his authorizations or using a technical user is sufficient.


#### Implementing Authentication and Authorization

The steps below describe how authentication and authorization is implemented in the ESPM application.

##### Business User Implementation

As a pre prerequisite, the sale-service and product-service should be bound to same xsuaa instance.

 1. Add the [application security descriptor](https://help.sap.com/docs/CP_AUTHORIZ_TRUST_MNG/42d3f70108eb4439953bfe47f4f90809/381b77b9e77448fd9a2036da1ec39729.html?state=DRAFT&q=application%20security%20descriptor%20file) file (xs-security.json) to the project.
    > This file can be found in the root folder of the project.
 
 2. Define a role **Retailer** within the application security descriptor.
    
    Only a person assigned the **Retailer** role will be able to access the retailer UI of the ESPM application to process the sales orders.

 3. Scope checks are validated in sale-service and product-service by using the incoming jwt token forwarded by the gateway application via the approuter.
 
 4. Implement app-to-app communication for the business user in the createSalesOrder method of class `com.sap.refapps.espm.controller.SalesOrderController ` in the sale-service microservice 
 
 5. Implement app-to-app communication for the business user in the UpdateStockbyProductID method in the `com.sap.refapps.espm.controller.ProductController` class of the product-service microservice.
 
 When a Retailer logs in to accept a sales order created by a customer, the business user is propagated from the sale-service to product-service for a stock check before accepting a sales order. This ensures that enough stock is available before a sales order is accepted and only a user with the **Retailer** role has the permission to do a stock check.
 
 ##### Technical User Implementation
 
 App-to-app communication for the technical user is implemented between the sale-service and the tax-service using **client-credential flow**. The sale-service and the tax-service are bound to different XSUAA instances.
 
 1. The sale-service is bound to the instance **espm-xsuaa**(which uses xs-security.json).
 
 2. The tax-service is bound to the instance **espm-xsuaa-tax**(which uses xs-security-tax.json).
 
 3. The tax-service grants a scope to the sales-service using the property **"grant-as-authority-to-apps"** in the xs-security-tax.json. This property has the value **["$XSAPPNAME(application,espm-cloud-native-uaa)"]** where espm-cloud-native-uaa is the xs-appname of the espm-xsuaa service.
 
 4. The sales-service accepts the granted authorities. This is achieved by the property **"$ACCEPT_GRANTED_AUTHORITIES"** in the xs-security.json. This ensures that the tax-service trusts the sale-service and hence technical user communication between the two services is achieved using client credentials flow.
 
 For more information, refer to section [referencing the application](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/517895a9612241259d6941dbf9ad81cb.html#loio517895a9612241259d6941dbf9ad81cb__section_fm2_wsk_pdb) in the documentation for SAP Business Technology Platform.
