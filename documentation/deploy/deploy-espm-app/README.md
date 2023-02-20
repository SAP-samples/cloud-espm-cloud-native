## Deploying the ESPM Application on Cloud Foundry

To run the application on Cloud Foundry you need an account on SAP Business Technology Platform Productive account.
*Please note that in SAP Business Technology Platform Cloud Foundry Environment,  for a trial account, there is limited resource and you get a RAM of 2 GB which is not sufficient to run the complete ESPM application.*  

Check if the Cloud Foundry Space you will be deploying the application has the required [entitlements](/documentation/prepare/mission-prerequisites/README.md#entitlements):

The ESPM application has a dependency to Tax Service Application which is a mock external service and needs to be separately deployed. Tax service is bound to its own instance of the Authorization and Trust Management service(XSUAA).

*Please note that the ESPM application and Tax Service application should be deployed on the same CF space.*

### Configuring SAP Event Mesh

* Open em-default.json file and update `"namespace": "<yourorgname>/<any_messageclientname>/<uniqueID>"`
  e.g `"namespace": "myorg/espm/1"`
 For more details, check [here](https://help.sap.com/viewer/bf82e6b26456494cbdd197057c09979f/Cloud/en-US/d0483a9e38434f23a4579d6fcc72654b.html)

### Tax Service Application Deployment

*Please note that the ESPM application and Tax Service application should be deployed on the same CF space.

From the tax-service folder where mta.yaml is kept for tax-service application run the command:

	  mbt build -p=cf

This will package your application to be ready for deployment.


To Deploy MTAR, run the command:

	cf deploy mta_archives/cloud-espm-cloud-native-tax_1.3.2.mtar


### Build and Deploy ESPM Application

* Navigate to gateway folder from root folder of your project.

* Change the authenticationMethod to `route` and authenticationType to `xsuaa` in xs-app.json.

* In mta.yml update `QUEUE_NAME` parameter for modules  espm-sales-svc and espm-worker with value
  `"<yourorgname>/<any_messageclientname>/<uniqueID>/salesorderqueue"`
  e.g `myorg/espm/1/salesorderqueue`
  
  > In case you are using a different name for the HANA instance, please update the [hana configuration file](/commons/src/main/java/com/sap/refapps/espm/config/HanaDataSourceConfig.java#L23), mta file as well with the same name. <br>
    If there are multiple instances of SAP HANA cloud in the space where you plan to deploy this application, please modify the mta.yaml as shown below. Replace <database_guid> with the [id of the database](https://help.sap.com/viewer/cc53ad464a57404b8d453bbadbc81ceb/Cloud/en-US/93cdbb1bd50d49fe872e7b648a4d9677.html?q=guid) you would like to bind the application with :
 ```
 # Hana Schema
    - name: espm-hana-db
    type: com.sap.xs.hana-schema
    parameters:
      service: hana
      service-plan: schema
      config:
        database_id: <database_guid>
```

* From the root folder where mta.yaml is kept run the command:

	  mbt build -p=cf

This will package your application to be ready for deployment.

* To Deploy MTAR, run the command:

	cf deploy mta_archives/cloud-espm-cloud-native_1.3.2.mtar


