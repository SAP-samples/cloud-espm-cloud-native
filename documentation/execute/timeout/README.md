### Resilience Patterns in Action - Timeout
 This pattern is implemented in Sales Service along with Circuit Breaker pattern. It's used to ensure that any request from Sales Service to Tax service does not wait indefinitely but times out after a preconfigured time for 1.2 seconds and a fall back is used for Tax calculation. To see these patterns in action, follow these steps:
* Run Tax Service locally as [Spring Boot Application as mentioned](/tax-service#running-locally-as-spring-boot-application)
* Check if the Tax service URL is configured in application.properties (/sale-service/src/main/resources folder) file as `tax.service=http://localhost:9994/tax.svc/api/v1/calculate/tax?amount=`
* Run the Sale Service
* Create a Sales Order with following data using Postman
  Method : Post
  URL : http://localhost:9993/sale.svc/api/v1/salesOrders
  Header: `Content-Type : application/json`
  Body: `{"customerEmail": "customer1@gmail.com", "productId": "HT-1005",  "productName" :"Notebook Basic 15", "currencyCode": "DLR", "grossAmount": 5000, "quantity": 2 }`

* Check if the request succeeds.
* Check that the response time would be under 1 seconds
* Stop the Tax Services which is running on SAP BTP Neo or on Cloud Foundry.
* Create a Sales Order with following data  
` Method : POST `    
  `URL : http://localhost:9993/sale.svc/api/v1/salesOrders`       
  `Header: Content-Type : application/json`  
  `Body: {"customerEmail": "customer2@gmail.com",  "productName" :"Notebook Basic 15", "productId": "HT-1005", "currencyCode": "DLR", "grossAmount": 5000, "quantity": 2 } `
* The requests succeed, but takes more than 1.2 seconds this is because, since Tax service was down, the Sale Service after sending request for Tax calculation, waited for 1.2 seconds. Due to Time Out configuration and due to the Circuit Breaker implementation, it falls back to default Tax calculation implementation
* Hit get request on Sales Services

  `Method : GET`
  `URL : http://localhost:9993/sale.svc/api/v1/salesOrders`
* In the response it can be seen that the sales order is created with email id `customer1@gmail.com`. The first sales order created while Tax service was up will have a non-zero value for `taxAmount` field, while for the sales order created with email id `customer2@gmail.com` will have `taxAmount` as zero as the fallback `taxAmount` is set to zero
