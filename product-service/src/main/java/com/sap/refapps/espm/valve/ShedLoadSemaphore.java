package com.sap.refapps.espm.valve;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * This class is the semaphore implementation for 
 * checking the number of concurrent requests.
 *
 */
@Component
public class ShedLoadSemaphore {

    private final Semaphore semaphore;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    

    /**
     * It is used to configure the semaphore.
     * 
     * @param maxRequests
     * @param maxWaitForSemaphore
     */
    public ShedLoadSemaphore(@Value("${max.requests}") final int maxRequests) {
     
        this.semaphore = new Semaphore(maxRequests, true);
        logger.info("Maximum parallel requests set to : {}", maxRequests);
    }


    public static class LoadExceedException extends Exception {
    }

    public class GateKeeper implements AutoCloseable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        //private final int permits;

      
        @Override
        public void close() throws LoadExceedException{
            if (!closed.compareAndSet(false, true)) {
                throw new IllegalStateException("GateKeeper has already been closed");
            }
            semaphore.release();
        }

    }

    /**
     *  It used to check if a request can be permitted
     *  
     * @param permits
     * @return GateKeeper
     * @throws LoadExceedException
     * @throws InterruptedException
     */
    public GateKeeper tryRequest() throws LoadExceedException, InterruptedException {
        if (semaphore.tryAcquire()) {
            return new GateKeeper();
        }
        throw new LoadExceedException();
    }
}