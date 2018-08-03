package com.sap.refapps.espm.service;

import com.sap.refapps.espm.model.SalesOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This is the service class for Queue
 *
 */
@Service
public class QueueDispatcherService {


    private final String queueName;
    private final RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private CountDownLatch statusLatch = new CountDownLatch(1);


    /**
     * @param queueName
     * @param rabbitTemplate
     */
    public QueueDispatcherService(@Value("${rabbit.queueName}") final String queueName, final RabbitTemplate rabbitTemplate){
        this.queueName = queueName;
        this.rabbitTemplate = rabbitTemplate;
        setupCallbacks();

    }

    /**
     * This method is used to setup callbacks that
     * will be used for publisher confirms.
     */
    private void setupCallbacks(){
        rabbitTemplate.setConfirmCallback((correlationData, acknowledgement, reason) -> {
            if(!acknowledgement){
                logger.warn("The message with coorelation ID {} was not acknowledged by the broker. Reason: {}", correlationData.getId(), reason);
            }
            else{
                logger.info("The message with coorelation ID {} was acknowledged by the broker.", correlationData.getId());
                statusLatch.countDown();
            }


        });

    }


    /**
     * This is method sends sales order to the queue.
     * 
     * @param salesOrder
     * @param correlationId
     * @return true if success
     */
    public boolean dispatch(final SalesOrder salesOrder, final String correlationId) {
        logger.info("Sending message with correlation-ID {} to the exchange: {}, queue: {}", correlationId, "", queueName);
        try{
        rabbitTemplate.convertAndSend(queueName,salesOrder,new CorrelationData(salesOrder.getSalesOrderId()));

            if(statusLatch.await(100, TimeUnit.MILLISECONDS)){
                return true;
            }
            else{
                logger.error("AMQP Acknowledgement Error");
                return false;

            }

        }catch(InterruptedException e){
            //The alternative is https://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            throw new AssertionError("Unexpected Interruption",e);

        }catch(AmqpException e){
            logger.error("Rabbit Connectioin Error");
            return false;
        }

    }

}
