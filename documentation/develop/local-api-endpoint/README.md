### Accessing the Local API Endpoints
The below are the list of local service API endpoints of all the microservices.

#### Customer Service

Create Customer

    Endpoint URL - http://localhost:9991/customer.svc/api/v1/customers/
    Header - `Content-Type:application/json`
    Method - `POST`
    Body - `{"emailAddress": "<customer-emailId>", "phoneNumber": "0123456789", "firstName": "new", "lastName": "customer", "dateOfBirth": "19900911", "city": "Bang, KR", "postalCode": "112233", "street": "100ft Road", "houseNumber": "123", "country": "IN"}`

Get Customer by Email ID

    Endpoint URL - http://localhost:9991/customer.svc/api/v1/customers/{emailAddress} 	
    Method - `GET`

Create Cart

    Endpoint URL - http://localhost:9991/customer.svc/api/v1/customers/{customerId}/carts/
    Header - `Content-Type:application/json`
    Method - `POST`
    Body - `{"productId": "HT-1000", "name" :"Notebook Basic 15", "checkOutStatus": "false","quantityUnit": 3}`     

Get Cart by Customer ID

    Endpoint URL - http://localhost:9991/customer.svc/api/v1/customers/{customerId}/carts/
    Method - `GET`

Update Cart by Item ID

    Endpoint UR - http://localhost:9991/customer.svc/api/v1/customers/{customerId}/carts/{itemId}
    Header - `Content-Type:application/json`
    Method - `PUT`
    Body - `"productId": "HT-1000", "name" :"Notebook Basic 15", "quantityUnit": 10,"checkOutStatus": false}`

Delete Cart by Item ID

    Endpoint URL - http://localhost:9991/customer.svc/api/v1/customers/{customerId}/carts/{itemId}
    Method - `DELETE`

#### Product Service

Get All Products 

    Endpoint URL - http://localhost:9992/product.svc/api/v1/products
    Method - `GET`

Get Product by Product ID 

    Endpoint URL - http://localhost:9992/product.svc/api/v1/products/{productId}
    Method - `GET`       

Get Stock by Product ID|

    Endpoint URL - http://localhost:9992/product.svc/api/v1/stocks/{productId}
    Method - `GET`       

*The stock is updated by quantity specified in the payload. e.g. if the current quantity is 50 and in payload in the body for update stock request quantity is provided as 20 the quantity will be updated to 70.*

Update Stock by Product ID

    Endpoint URL - http://localhost:9992/product.svc/api/v1/stocks/{productId}
    Header - `Content-Type:application/json`
    Method - `PUT`
    Body - `{"productId": "HT-1000","quantity": 20}`     

#### Sales Service

Create Sales Order

    Endpoint URL - http://localhost:9993/sale.svc/api/v1/salesOrders
    Header - `Content-Type:application/json`
    Method - `POST`
    Body - `{"customerEmail": "viola.gains@itelo.info","productId": "HT-1000","productName" :"Notebook Basic 15" , "currencyCode": "EUR", "grossAmount":956,"quantity":4}`     

Get Sales Order by Sales Order ID

    Endpoint URL - http://localhost:9993/sale.svc/api/v1/salesOrders/{salesOrderId}
    Method - `GET`       

Get Sales Order by Customer Email ID

    Endpoint URL - http://localhost:9993/sale.svc/api/v1/salesOrders/email/{emailAddress}
    Method - `GET`       


Get All Sales Order|

    Endpoint URL - http://localhost:9993/sale.svc/api/v1/salesOrders/
    Method - `GET`       

#### Tax Service (External Service)

Get Tax Amount

Endpoint URL - http://localhost:9994/tax.svc/api/v1/calculate/tax?amount=1000
Method - `GET`       

#### Test the ESPM Application Locally
To test the ESPM application, [Postman REST Client](https://www.getpostman.com/apps) can be used. A Postman collection which is provided [here](../../postman-collections/ESPM-LOCAL.postman_collection.json) has all the request URLs and sample request body payloads (in case of a POST request).
