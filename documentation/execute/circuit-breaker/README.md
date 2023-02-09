### Resilience Patterns in Action 
#### Circuit Breaker
In ESPM this pattern is showcased via sale service. This service needs to compute the tax amount for a Sales Order. This is done by hitting an external [Tax Service](/tax-service). If the Tax Service is unreachable, instead of throwing an error, a fallback mechanism executes the logic and default tax value is returned. Resilience4j library is used to implement Circuit breaker patterns.
To see the pattern in action follow these steps-
* Navigate to tax-service folder
* Run the application [Locally as Spring Boot Application](/tax-service/README.md#running-locally-as-spring-boot-application)
*  Hit the Sales Service by running the url `http://localhost:9993/sale.svc/api/v1/salesOrders/` and POST the sales data.
	  For e.g.:
	  `{
	    "customerEmail": "customer@gmail.com",
	    "productId": "HT-1006",
	     "productName" :"Notebook Basic 15",
	    "currencyCode": "DLR",
	    "grossAmount": 1000,
	    "quantity": 2
	  }`
* Now hit the Sales Service by running the url `http://localhost:9993/sale.svc/api/v1/salesOrders/email/customer@gmail.com` and check whether it returns the data, which includes 2 additional attributes `netAmount` & `taxAmount`.
* Now Stop the Tax Service which is running locally, which means that the Tax Service endpoint will be unreachable.
* Again, POST some data using `http://localhost:9993/sale.svc/api/v1/salesOrders/`.
* Normally, this POST method should fail as the endpoint of Tax Service is unreachable but as Circuit Breaker pattern is implemented, instead of throwing error, a fallback mechanism is executed which in turn gives default tax value when the Tax Service is down.
* Now when you start your Tax Service, endpoint becomes reachable and normal flow is resumed.

#### Unit Isolation
ESPM has a microservice-based architecture, where all the services are independent of each other  and have been isolated against each other here by bringing in Unit Isolation.
