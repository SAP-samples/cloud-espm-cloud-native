package com.sap.refapps.espm.listner;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;

/**
 * This is the spring boot application class for worker.
 *
 */
@Profile("cloud")
@Service("EMListner")
public class EMListner {

	@Autowired
	ApplicationContext appContext;

	private Logger logger = LoggerFactory.getLogger(EMListner.class);

	@Value("${worker.retry.initial}")
	private Long initialValue;

	@Value("${worker.retry.initial}")
	private Long value = initialValue;

	@Value("${worker.retry.multiplier}")
	private Long multiplier;

	@Value("${worker.retry.maxVal}")
	private Long maxVal;

	@Autowired
	@Qualifier("getMessagingServiceJmsConnectionFactory")
	private MessagingServiceJmsConnectionFactory connectionFactory;

	private Connection connection;
	private Session session;
	private Queue queue;
	private MessageConsumer consumer;

	/**
	 * This method is used to process messages from queue.
	 * 
	 * @param in
	 * @param channel
	 * @param tag
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void receive() throws JMSException {
		final CountDownLatch latch = new CountDownLatch(1);
		connection = connectionFactory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		queue = session.createQueue("queue:" + System.getenv("QUEUE_NAME"));
		consumer = session.createConsumer(queue);

		consumer.setMessageListener(message -> {
			try {
				String messageBody = message.getBody(String.class);
				System.out.println("message: " + messageBody);
				ObjectMapper mapper = new ObjectMapper();
				SalesOrder so = mapper.readValue(messageBody, SalesOrder.class);

				SalesOrderRepository repo = appContext.getBean(SalesOrderRepository.class);
				try {
					if (!repo.existsById(so.getSalesOrderId())) {
						repo.save(so);
						logger.info(so.getSalesOrderId() + " created");

					} else {
						logger.error(so.getSalesOrderId() + " already Exists, Deleting from Queue");
						// message.acknowledge();

					}
					message.acknowledge();
				} catch (DataIntegrityViolationException e) {
					logger.error(so.getSalesOrderId() + " is an invalid Sales-Order, Deleting from Queue");
					// channel.basicNack(tag, false, false);
					message.acknowledge();

				} catch (CannotCreateTransactionException ccte) {
					logger.error("Unable to connect to DB");
					logger.error("Backing  for " + value);
					TimeUnit.MILLISECONDS.sleep(value);
					if (value <= maxVal)
						value = value * multiplier;

				}
			} catch (Exception e) {
				System.out.println("ex: " + e.getMessage());
				latch.countDown();
			}
			try {
				message.acknowledge();
			} catch (JMSException e) {
				logger.error(e.getMessage());
			}
		});

		connection.start();
		System.out.println("Connection started");

		try {

			latch.await();

		} catch (InterruptedException e) {
			Thread.interrupted();
			consumer.close();
			System.out.println("Closed consumer");

			session.close();
			System.out.println("Closed session");

			connection.close();
			System.out.println("Closed connection");
		}

	}

}
