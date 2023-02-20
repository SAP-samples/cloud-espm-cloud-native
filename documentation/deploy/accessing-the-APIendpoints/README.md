### Accessing the application API Endpoints

The below are the list of local service API endpoints of all the microservices.

#### Customer Service

Create Customer

    Endpoint URL - https://<unique_id>-espm-customer-svc.cfapps.eu10.hana.ondemand.com/customer.svc/api/v1/customers/
    Header - `Content-Type:application/json`
    Method - `POST`
    Body - `{"emailAddress": "new_customer@test.com", "phoneNumber": "0123456789", "firstName": "new", "lastName": "customer", "dateOfBirth": "19900911", "city": "Bang, KR", "postalCode": "112233", "street": "100ft Road", "houseNumber": "123", "country": "IN"}`

Get Customer by Email ID

    Endpoint URL - https://<unique_id>-espm-customer-svc.cfapps.eu10.hana.ondemand.com/customer.svc/api/v1/customers/{emailAddress} 	
    Method - `GET`


Create Cart

    Endpoint URL - https://<unique_id>-espm-customer-svc.cfapps.eu10.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/
    Header - `Content-Type:application/json`
    Method - `POST`
    Body - `{"productId": "HT-1000", "name" :"Notebook Basic 15", "checkOutStatus": "false","quantityUnit": 3}`     

Get Cart by Customer ID

    Endpoint URL - https://<unique_id>-espm-customer-svc.cfapps.eu10.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/
    Method - `GET`

Update Cart by Item ID

    Endpoint URL - https://<unique_id>-espm-customer-svc.cfapps.eu10.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/{itemId}         
    Header - `Content-Type:application/json`
    Method - `PUT`
    Body - `{"itemId": {itemId},"productId": "HT-1000",  "name" :"Notebook Basic 15", "quantityUnit": 10,"checkOutStatus": false}`

Delete Cart by Item ID

    Endpoint URL - https://<unique_id>-espm-customer-svc.cfapps.eu10.hana.ondemand.com/customer.svc/api/v1/customers/{customerId}/carts/{itemId}
    Method - `DELETE`

#### Product Service

Get All Products

    Endpoint URL - https://<unique_id>-espm-product-svc.cfapps.eu10.hana.ondemand.com/product.svc/api/v1/products
    Method - `GET`

Get Product by Product ID   

    Endpoint URL - https://<unique_id>-espm-product-svc.cfapps.eu10.hana.ondemand.com/product.svc/api/v1/products/{productId}
    Method - `GET`  

In order to access the below endpoint, the user needs retailer role and token has to be passed in the header.

Execute the below command and make note of `xsuaa` service instance url, clientid, clientsecret.

`cf env <unique_id>-espm-product-svc`

Get New access Token

    Access token URL - `<url>/oauth/token`
    Client ID - `<clientid>`
    Client Secret - `<clientsecret>`
    Grant Type - `Client Credentials`          


Get Stock by Product ID

    Endpoint URL - https://<unique_id>-espm-product-svc.cfapps.eu10.hana.ondemand.com/product.svc/api/v1/stocks/{productId}
    Method - `GET`       
    Header - `Content-Type:application/json` , `Authorization:Bearer <Get New Access Token>`                                |

*The stock is updated by quantity specified. e.g. if the current quantity is 50 and in the body for update stock request quantity is provided as 20 the quantity will be updated to 70*

Below URL requires the retailer role to be added to user and hence if you are executing the same from postman, make sure you have the role, and inorder to get the Access token with scopes of `Retailer` role execute the following request from postman.

Access token with scopes of `Retailer` role

    Endpoint URL - Access token URL 
    Header - `Content-Type:application/x-www-form-urlencoded`
    Method - `POST`
    Body - `x-www-form-urlencoded`

The payload of the request needs to have following form-url-encoded values:

*grant_type*: set to password to define that the client and user credentials method has to be used for the token determination

*username*: set user name of authorized user

*password*: password of the authorized user

*client_id*: the client id determined for the application

*client_secret*: the client secret determined for the application

*response_type*: set to token to indicate than an access token is requested   

Update Stock by Product ID

    Endpoint URL - https://<unique_id>-espm-product-svc.cfapps.eu10.hana.ondemand.com/product.svc/api/v1/stocks/{productId}		 
    Header - `Content-Type:application/json`   , `Authorization:Bearer <Access token with scopes of Retailer role>`
    Method - `PUT`
    Body - `{"productId": "HT-1000", "quantity": 20}`     

#### Sales Service

In order to access the below endpoint, the user needs retailer role and token has to be passed in the header.

Execute the below command and make note `xsuaa` service instance of url, clientid, clientsecret.

`cf env <unique_id>-espm-sales-svc`

Get New access Token

    Access token URL - `<url>/oauth/token`
    Client ID - `<clientid>`
    Client Secret: `<clientsecret>`
    Grant Type - `Client Credentials`

Create Sales Order

    Endpoint URL - https://<unique_id>-espm-sales-svc.cfapps.eu10.hana.ondemand.com/sale.svc/api/v1/salesOrders|
    Header - `Content-Type:application/json`
    Method - `POST`
    Body - `{"customerEmail": "viola.gains@itelo.info",  "productName" :"Notebook Basic 15", "productId": "HT-1000","currencyCode": "EUR", "grossAmount":956,"quantity":4}`     

Get Sales Order by Sales Order ID

    Endpoint URL - https://<unique_id>-espm-sales-svc.cfapps.eu10.hana.ondemand.com/sale.svc/api/v1/salesOrders/{salesOrderId}
    Method  - `GET`       
    Header - `Content-Type:application/json` , `Authorization:Bearer <Get New Access Token>`

Get Sales Order by Customer Email ID

    Endpoint URL - https://<unique_id>-espm-sales-svc.cfapps.eu10.hana.ondemand.com/sale.svc/api/v1/salesOrders/email/{emailAddress}    
    Method - `GET`       
    Header - `Content-Type:application/json` , `Authorization:Bearer <Get New Access Token>`

Get All Sales Order

    Endpoint URL - https://<unique_id>-espm-sales-svc.cfapps.eu10.hana.ondemand.com/sale.svc/api/v1/salesOrders/
    Method - `GET`       
    Header - `Content-Type:application/json` , `Authorization:Bearer <Get New Access Token>`
