package com.sap.refapps.espm.listner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import com.rabbitmq.client.Channel;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;

/**
 * This is the spring boot 
 * application class for worker.
 *
 */
@Profile("local")
@Service
public class LocalListner {

	@Autowired
	ApplicationContext appContext;

	private Logger logger = LoggerFactory.getLogger(LocalListner.class);

	@Value("${worker.retry.initial}")
	private Long initialValue;

	@Value("${worker.retry.initial}")
	private Long value = initialValue;

	@Value("${worker.retry.multiplier}")
	private Long multiplier;

	@Value("${worker.retry.maxVal}")
	private Long maxVal;

	
	/**
	 * This method is used to process messages from queue.
	 * 
	 * @param in
	 * @param channel
	 * @param tag
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@RabbitListener(queues = "espm.salesOrders")
	public void recieve(SalesOrder in, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag)
			throws IOException, InterruptedException {

		SalesOrderRepository repo = appContext.getBean(SalesOrderRepository.class);
		try {
			if (!repo.existsById(in.getSalesOrderId())) {
				repo.save(in);
				logger.info(in.getSalesOrderId() + " created");
				channel.basicAck(tag, false);
				value = initialValue;

			} else {
				logger.error(in.getSalesOrderId() + " already Exists, Deleting from Queue");
				channel.basicAck(tag, false);

			}
		} catch (DataIntegrityViolationException e) {
			logger.error(in.getSalesOrderId() + " is an invalid Sales-Order, Deleting from Queue");
			channel.basicNack(tag, false, false);

		} catch (CannotCreateTransactionException ccte) {
			logger.error("Unable to connect to DB");
			logger.error("Backing  for " + value);
			TimeUnit.MILLISECONDS.sleep(value);
			if (value <= maxVal)
				value = value * multiplier;
			channel.basicNack(tag, false, true);

		}

	}

}
