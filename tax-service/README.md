# ESPM Tax Service 

ESPM Tax Service is a tax calculation app. This was developed to supplement the [ESPM Dragonblood](https://github.wdf.sap.corp/refapps/espm-dragonblood) application. It can be deployed locally (as a Spring Boot application), on SAP Cloud Platform Cloud Foundry Environment and on SAP Cloud Platform Neo Environment.  
# Tax Calculation Matrix (Tax Slab)
The tax service calculate the tax based on various slabs as defined below. The requesting application needs to provide a gross amount and tax service will respond with tax calculation
`/api/v1/calculate/tax?amount=?`

| Tax Slabs | Amount (Rs)  | Tax |  
|---|---|---|
| SLAB1 | 0 - 499  |  0.00 %|  
| SLAB2 | 500 - 999  | 5.00 %  |   
| SLAB3 | 1000 - 1999  | 10.00 %  |  
| SLAB4 | 2000 and above  | 15.00 %  |  

# Running locally as Spring Boot Application

1.  Navigate to the tax-service folder and execute command

    `mvn spring-boot:run`

2. Application will be accessible via URL : http://localhost:9994/

3. To test the service execute URL http://localhost:9994/tax.svc/api/v1/calculate/tax?amount=1000
response will be `{"taxAmount":100.0,"taxPercentage":10.0}`

# Running on SAP Cloud Platfrom CloudFoundary Environment

1.  Open the manifest.yml file and for name property replace <unique-id> with your I/D number or any uniqe string
2. Navigate to the tax-service folder and execute command

    `mvn clean package install -P cf`
    
3.  After successful build , login into cloud foundry account and choose `space` where to deploy

4.  Navigate to the tax-service folder execute command

    `cf push`

5. Application will be accessible via URL : https://<unique-id>-taxservice.cfapps.eu10.hana.ondemand.com/
6. To test the service execute URL https://<unique-id>-taxservice.cfapps.eu10.hana.ondemand.com/tax.svc/api/v1/calculate/tax?amount=1000

# Running on SAP Cloud Platfrom Neo Environment

1.  Navigate to the tax-service folder and execute command

    `mvn clean package install -P neo`
    
2.  After successful build, deploy the `target/ROOT.war` file on SAP Cloud Platform Cockpit > Neo > Applications > Java Applications
    
    * Click on "Deploy Application"
    * Browse the WAR file
    * Enter the application name as "taxservice"
    * Select the Runtime name as "Java Web Tomcat 8"
    * Other fields keep it as default.

3. To test the service execute URL : https://taxservicew6e0ed3dd.int.sap.hana.ondemand.com/tax.svc/api/v1/calculate/tax?amount=1000


# API Documentation

## Tax Calculation API
   
   **Overview**
   
   The Tax Service allows you to fetch the tax amount and tax percentage based on amount provided.
   
   **API Reference**
   
   **Request Endpoint**
   
    /tax.svc/api/v1/calculate/tax
   
   Retrives the tax amount and the percentage based on amount
   
    
   **Method**
   
    GET
    
   **Request**
   
   **Query Parameters**
   
   - amount: (BigDecimal)
   
   Example
   
   `1999.00`
   
   **Response**
   
   **HTTP status code**
   
    200
   
   Resource successfully retrieved.
   
   **Body**
   
   - Type: `application/json`
   - schema:
   
   ```
    {
        "taxAmount": 199.9,
        "taxPercentage": 10
    }
   ```
  
  **Example**
  
  **Request Endpoint**
  
    /tax.svc/api/v1/calculate/tax?amount=1999.00
  
  **Response**
  
  - **Http status**
  
        200
  
  - **Body**
  
       ```
        {
            "taxAmount": 199.9,
            "taxPercentage": 10
        }
       ```
 ## Tax Updation API   
 
   **Overview**
   
   This Service allows you to update the tax percentage in tax slabs.
   
   **API Reference**
   
   **Endpoint**
   
    /tax.svc/api/v1/tax/update
    
   Update the tax percentage.
   
   **Method**
   
    POST
    
   **Request**
   
   **Body**
    
   - **Content-Type**  
    
    application/json
    
   - **Payload**

     ```
     {
        "SLAB1": (double),

              ...

        "SLAB4": (double)
     }
     ```
   
  **Response**
    
   **HTTP status code**
   
    200
    
   **Body**
   
    Tax updated successfully..
    
  **Example**
  
  **Request Endpoint**
  
     /tax.svc/api/v1/tax/update
  
   **Method**
  
     POST 
  
   **Body**
    
   - **Content-Type**  
    
    application/json
    
   - **Payload**
   
     ```
     {
        "SLAB1": 5,
        "SLAB2": 10,
        "SLAB3": 15,
        "SLAB4": 20
     }
     ```
  
  **Response**
  
  - **Http status**
  
        200
  
  - **Body**
    
        Tax updated successfully..
      
   
 

   
   
   
  
  
    
   

   
   
