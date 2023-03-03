package com.sap.refapps.espm.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class EmbeddedQpidBrokerConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedQpidBrokerConfig.class);

    public static final String INITIAL_CONFIG_PATH = "qpid-config.json";

    private final SystemLauncher broker = new SystemLauncher();

    private static String initialConfigUrl;

    public void after() {
        broker.shutdown();
    }

    public void startQpidBroker() throws Exception {

        Map<String, Object> attributes = new HashMap<>();
        try {
            initialConfigUrl = EmbeddedQpidBrokerConfig.class.getClassLoader().getResource(INITIAL_CONFIG_PATH)
                    .toExternalForm();
        } catch (Exception e) {
            logger.error("Exception occured reading the configuration file " + INITIAL_CONFIG_PATH, e);
        }
        if (!initialConfigUrl.isEmpty()) {
            attributes.put("type", "Memory");
            attributes.put("initialConfigurationLocation", initialConfigUrl);
            broker.startup(attributes);
        }
    }
}