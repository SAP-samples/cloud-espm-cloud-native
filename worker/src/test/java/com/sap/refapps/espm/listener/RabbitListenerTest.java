package com.sap.refapps.espm.listener;

import static org.assertj.core.api.Assertions.assertThat;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Collection;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.sap.refapps.espm.EmbeddedQpidBrokerConfig;
import com.sap.refapps.espm.listner.LocalListner;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * This is RabbitListner Test class
 *
 */
@SpringBootTest
@ActiveProfiles(profiles = { "test", "local" })
@ExtendWith(SpringExtension.class)
@ComponentScan({ "com.sap.refapps.espm" })
@TestInstance(value = Lifecycle.PER_CLASS)
public class RabbitListenerTest {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(RabbitListenerTest.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SalesOrderRepository salesOrderRepo;

    @Autowired
    private RabbitListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedQpidBrokerConfig broker;

    private static final SalesOrder salesOrder = new SalesOrder();

    @BeforeAll
    public void setUp() throws Exception {
        broker.startQpidBroker();
        Collection<MessageListenerContainer> messageListnerContainer = registry.getListenerContainers();
        for (MessageListenerContainer container : messageListnerContainer) {
            container.start();
        }
        salesOrder.setSalesOrderId("cd4e89d8-60f2-4af2-8f15-548e96729a3f");
        salesOrder.setCustomerEmail("test@gmail.com");
        salesOrder.setProductId("HT-1000");
        salesOrder.setCurrencyCode("EUR");
        salesOrder.setGrossAmount(BigDecimal.valueOf(1500.0));
        salesOrder.setNetAmount(BigDecimal.valueOf(1650.0));
        salesOrder.setTaxAmount(BigDecimal.valueOf(150.0));
        salesOrder.setLifecycleStatus("N");
        salesOrder.setLifecycleStatusName("New");
        salesOrder.setQuantity(BigDecimal.valueOf(2.0));
        salesOrder.setQuantityUnit("EA");
        salesOrder.setDeliveryDate("2022-09-01");
        salesOrder.setCreatedAt("2022-09-09");
        salesOrder.setNote("");
        salesOrder.setProductName("Notebook Basic 15");
        rabbitTemplate.convertAndSend("espm.salesOrders", salesOrder);

    }

    @AfterAll
    public void tearDown() {
        Collection<MessageListenerContainer> messageListnerContainer = registry.getListenerContainers();
        for (MessageListenerContainer container : messageListnerContainer) {
            container.stop();
        }
        broker.after();
    }

    /**
     * It is used to test the receive().
     * 
     * @throws Exception
     */
    @Test
    public void listnerTest() throws Exception {
        assertEquals(true, salesOrderRepo.existsById("cd4e89d8-60f2-4af2-8f15-548e96729a3f"));
    }

    /**
     * It is used to test the duplicate salesOrder.
     */
    @Test
    public void duplicateSalesOrderTest() {
        Logger ListnerLogger = (Logger) LoggerFactory.getLogger(LocalListner.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        ListnerLogger.addAppender(listAppender);

        rabbitTemplate.convertAndSend("espm.salesOrders", salesOrder);

        Assertions.assertThat(listAppender.list).extracting(ILoggingEvent::getMessage)
                .contains("cd4e89d8-60f2-4af2-8f15-548e96729a3f already Exists, Deleting from Queue");

    }

}