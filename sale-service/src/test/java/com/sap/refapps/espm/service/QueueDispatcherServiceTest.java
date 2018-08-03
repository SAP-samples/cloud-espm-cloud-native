package com.sap.refapps.espm.service;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.sap.refapps.espm.model.SalesOrder;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ComponentScan({ "com.sap.refapps.espm" })
@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:beforeTestRun.sql" })
public class QueueDispatcherServiceTest {

	@Autowired
	private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;
	
	@Autowired
	private QueueDispatcherService queueDispatcherService;

	Appender mockAppender;



	private String queueName;
	private final SalesOrder salesOrder = new SalesOrder();
	private final String correlationId = "d1235291-06d1-4c39-a091-36f4f6361273";
	private int flag=1;



    @Before
    public void setup(){

        try {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            mockAppender = mock(Appender.class);
            when(mockAppender.getName()).thenReturn("MOCK");
            root.addAppender(mockAppender);
            Queue dlQueue = QueueBuilder.durable("espm.salesOrders").build();
            amqpAdmin.declareQueue(dlQueue);
        }catch(AmqpException e){
            System.out.println("No Broker, Skip Test");
            flag=0;
        }


    }

    @After
    public void teardown() {
      if(flag !=0){
          ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
          root.detachAppender("MOCK");
          amqpAdmin.deleteQueue("espm.salesOrders");
      }
    }

    @Test
    public void messageToValidQueue(){

       if(flag !=0){
           SalesOrder so = new SalesOrder();
           so.setSalesOrderId("SalesID");
           so.setCustomerEmail("as@a.com");
           assertTrue(queueDispatcherService.dispatch(so,""));
       }

    }

    @Test
    public void messageToInvalidQueue(){



       if(flag !=0){
           amqpAdmin.deleteQueue("espm.salesOrders");
           SalesOrder so = new SalesOrder();
           so.setSalesOrderId("as");
           so.setCustomerEmail("as@a.com");
           assertFalse(queueDispatcherService.dispatch(so,""));
           verifyLogs("AMQP Acknowledgement Error");
       }

    }



    private void verifyLogs(final String expectedMessage){
        verify(mockAppender).doAppend(argThat(new ArgumentMatcher() {
            @Override
            public boolean matches(final Object argument) {
                return ((LoggingEvent)argument).getFormattedMessage().contains(expectedMessage);
            }
        }));
    }


	

	
}
