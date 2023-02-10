## Explore the Different Miroservices

The ESPM application consists of five microservices and one external service.

1. **Customer Service** - This service process Customer and Shopping cart information

2. **Product Service** - This service can be used to process products and stock information

3. **Sales Service** - Sales Orders are processed by this service. Each time a sales order is created, it’s not directly inserted into the database, but inserted into a queue. A background process called worker picks the message from queue and inserts to the database. The rationale behind this approach is explained later in the document. For read operation on sales order, its directly read from the database.

4. **Worker** - Background process which picks the Sales Order from the queue and inserts it into the database.

5. **Gateway** - It’s an optional component and acts as entry point for the complete application. It also acts as a reverse proxy and routes the request to the appropriate microservice. The UI for the application is integrated into the Gateway module. Then UI of the application consists of two parts


   *Webshop*: An application where an authenticated Customer can buy products by creating Sales Order

   *Retailer*: An application where an authenticated and authorized Sales Manager known as Retailer can approve/reject sales orders. Only a user with retailer role will be able to access the end point.

6. **External Tax Service** - This is a service which is external to the application and used to do tax calculation. This Tax calculation service is provided, to be used along with the implementation of Circuit Breaker, Quarantine pattern. This service is also used in showcasing the app-to-app communication between two microservices deployed in the same subaccount, but bounded to two different Authorization and Trust Management services. For more information, see [referencing the application](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/517895a9612241259d6941dbf9ad81cb.html#loio517895a9612241259d6941dbf9ad81cb__section_fm2_wsk_pdb) in the documentation for SAP Business Technology Platform.
