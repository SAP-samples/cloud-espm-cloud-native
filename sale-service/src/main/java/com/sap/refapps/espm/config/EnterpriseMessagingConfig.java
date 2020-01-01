package com.sap.refapps.espm.config;

import com.sap.cloud.servicesdk.xbem.core.MessagingService;
import com.sap.cloud.servicesdk.xbem.core.MessagingServiceFactory;
import com.sap.cloud.servicesdk.xbem.core.exception.MessagingException;
import com.sap.cloud.servicesdk.xbem.core.impl.MessagingServiceFactoryCreator;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsSettings;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;


import org.springframework.cloud.service.ServiceConnectorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cloud")
public class EnterpriseMessagingConfig {

    @Bean
    public MessagingServiceFactory getMessagingServiceFactory() {
        ServiceConnectorConfig config = null; // currently there are no configurations for the MessagingService supported
        Cloud cloud = new CloudFactory().getCloud();
        // get the MessagingService via the service connector
        MessagingService messagingService = cloud.getSingletonServiceConnector(MessagingService.class, config);
        if (messagingService == null) {
            throw new IllegalStateException("Unable to create the MessagingService.");
        }
        return MessagingServiceFactoryCreator.createFactory(messagingService);
    }

    @Bean
    public MessagingServiceJmsConnectionFactory getMessagingServiceJmsConnectionFactory(MessagingServiceFactory messagingServiceFactory) {
        try {
            /*
             * The settings object is preset with default values (see JavaDoc)
             * and can be adjusted. The settings aren't required and depend on
             * the use-case. Note: a connection will be closed after an idle
             * time of 5 minutes.
             */

            MessagingServiceJmsSettings settings = new MessagingServiceJmsSettings();
            settings.setMaxReconnectAttempts(2); // use -1 for unlimited attempts
            settings.setInitialReconnectDelay(3000);
            settings.setReconnectDelay(3000);
            return messagingServiceFactory.createConnectionFactory(MessagingServiceJmsConnectionFactory.class, null);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create the Connection Factory", e);
        }
    }





}
