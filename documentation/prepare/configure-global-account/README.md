 # Configure Your Account in SAP BTP

In this section, you will set up the subaccount in SAP BTP to deploy the ESPM Application.

**Prerequisite:** You must have an administrator role for SAP BTP.

1. Log in to the SAP BTP cockpit and select your global account.

2. In the **Account Explorer**, choose **Create** &rarr; **Subaccount**.


![create](../images/createSubAccount.png)
   
3. In the **Create Subaccount** wizard, enter the following values:

   *  In the **Display Name** field, enter your subaccount name.
   *  In the **Subdomain** field, enter your subdomain ID.
   *  In the **Region** field, select the Cloud Foundry region of your choice: Amazon Web Service, Google Cloud Platform or Microsoft Azure.
   *  In the **Parent** field, select your global account.


      ![create](../images/createSubaccount2.png)

   * For categorizing your subaccount you can add some labels. Choose **Create**.

4. Add entitlements:

    1. Choose **Entitlements** &rarr; **Configure Entitlements** &rarr; **Add Service Plan**.
   ![entitlement](../images/entitlements.png)

   2. Search for **Cloud Foundry Runtime** and select the respective plan.

   ![entitlement runtime](../images/cf-entitlements.png)

   3. Search for **SAP HANA Cloud** and select the **hana** service plan. 

   ![entitlement hana](../images/ent-hana.png)
   
   4. Do the same for the following services:

      - 'SAP HANA Schemas & HDI container' ->Select Available Plans -> "hdi-shared"
      - 'Event Mesh' -> Select Available Service Plans -> "default"
      
   5. Choose **Add 4 Service Plans**.

   6. Choose **Save**.
   
5. Enable the Cloud Foundry environment:
  
   ![enable cf](../images/cf-enablement.png)

   1. In the **Plan** field, select **standard**.
   2. Choose **Create**.

6. To add additional users to the subaccount, choose **Security** &rarr; **Users** and choose **Create**.

   ![users](../images/User1.png)

7. In the **User Name** field, enter the user ID in the selected identity provider.

   ![users](../images/user2.png)   
   
8. Assign the relevant subaccount roles to the users.

      1. Choose **Security** &rarr; **Users** and **Assign Role Collection**:

         ![role collection](../images/rolecollection1.png)

      2. Select the required Role Collections and click on **Assign Role Collection**

         ![role collection](../images/rolecollection2.png)   
      
