package com.sap.refapps.espm.valve;

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is the tomcat valve implementation
 * for ShedLoad pattern implementation.
 *
 */
public class ProductShedLoadSemaphoreValve extends ValveBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ShedLoadSemaphore shedLoad;

    /**
     * @param shedLoad
     */
    public ProductShedLoadSemaphoreValve(ShedLoadSemaphore shedLoad) {
        super(true);
        this.shedLoad = shedLoad;
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.Valve#invoke(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response)
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        try (ShedLoadSemaphore.GateKeeper ignored = shedLoad.tryRequest()) {
            logger.debug("Accepted: {}", request.getRequestURL());
            getNext().invoke(request, response);
        } catch (final ShedLoadSemaphore.LoadExceedException e) {
            logger.warn("Rejected, too many requests: {}", request.getRequestURL());
            response.setStatus(SC_SERVICE_UNAVAILABLE);
            response.setHeader("RATE_LIMIT_SET_AT", "valve");
        } catch (InterruptedException e) {
            logger.warn("Rejected due to interrupt: {}", request.getRequestURL());
            response.setStatus(SC_SERVICE_UNAVAILABLE);
        }
    }


}
