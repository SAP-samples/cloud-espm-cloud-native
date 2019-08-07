package com.sap.refapps.espm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;



import com.sap.refapps.espm.config.WorkerContextInitializer;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.jms.*;

/**
 * This is the spring boot 
 * application class for worker.
 *
 */
@SpringBootApplication
public class Worker implements CommandLineRunner {

	@Autowired
	ApplicationContext appContext;

	private Logger logger = LoggerFactory.getLogger(Worker.class);

	@Value("${worker.retry.initial}")
	private Long initialValue;

	@Value("${worker.retry.initial}")
	private Long value = initialValue;

	@Value("${worker.retry.multiplier}")
	private Long multiplier;

	@Value("${worker.retry.maxVal}")
	private Long maxVal;

    @Autowired
    private MessagingServiceJmsConnectionFactory connectionFactory;

    private Connection connection;
    private Session session;
    private Queue queue;
    private MessageConsumer consumer;

	public static void main(String[] args) {
         SpringApplication.run(Worker.class, args);

    }

	/**
	 * This method is used to process messages from queue.
	 * 

	 * @throws IOException
	 * @throws InterruptedException
	 */

	@Override
	public void run(String... args) throws Exception {
		receive();
	}
	public void receive() throws JMSException {
		final CountDownLatch latch = new CountDownLatch(1);
		 connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        queue = session.createQueue("queue:EmSampleInQueue");
        consumer = session.createConsumer(queue);

        /*
		connection.setExceptionListener(e -> {
			logger.error("Exception Occured");


		});
		*/





		consumer.setMessageListener((message) -> {
			try {
				final byte[] body = message.getBody(byte[].class);
				final String content = new String(body, StandardCharsets.UTF_8);
				System.out.println("Received message: " + content);
				try {
					String newString = content.substring(8);
					System.out.println(newString);
					ObjectMapper mapper = new ObjectMapper();
					SalesOrder so = mapper.readValue(newString, SalesOrder.class);
					System.out.println(so.getCustomerEmail());

					SalesOrderRepository repo = appContext.getBean(SalesOrderRepository.class);
					try {
						if (!repo.existsById(so.getSalesOrderId())) {
							repo.save(so);
							logger.info(so.getSalesOrderId() + " created");


						} else {
							logger.error(so.getSalesOrderId() + " already Exists, Deleting from Queue");
							//message.acknowledge();


						}
						message.acknowledge();
					} catch (DataIntegrityViolationException e) {
						logger.error(so.getSalesOrderId() + " is an invalid Sales-Order, Deleting from Queue");
						//channel.basicNack(tag, false, false);
						message.acknowledge();

					} catch (CannotCreateTransactionException ccte) {
						logger.error("Unable to connect to DB");
						logger.error("Backing  for " + value);
						TimeUnit.MILLISECONDS.sleep(value);
						if (value <= maxVal)
							value = value * multiplier;

					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					latch.countDown();
				}
				message.acknowledge();
			} catch (JMSException e) {
				throw new RuntimeException("Unexpected exception during message receive: " + e.getMessage(), e);
			}
		});

		connection.start();
		System.out.println("Connection started");

		System.out.println("Start receiving messages...");

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
