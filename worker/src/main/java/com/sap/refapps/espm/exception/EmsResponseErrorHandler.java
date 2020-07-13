package com.sap.refapps.espm.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

public class EmsResponseErrorHandler extends DefaultResponseErrorHandler {

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		try {
			super.handleError(response);
		} catch (HttpClientErrorException clientException) {
			if (HttpStatus.NOT_FOUND.equals(clientException.getStatusCode())) {
				throw new NotFoundException(constructMessage(clientException));
			} else if (HttpStatus.UNAUTHORIZED.equals(clientException.getStatusCode()) || HttpStatus.FORBIDDEN.equals(clientException.getStatusCode())) {
				throw new UnauthorizedException(constructMessage(clientException));
			} else if (HttpStatus.CONFLICT.equals(clientException.getStatusCode())) {
				throw new ConflictException(constructMessage(clientException));
			} else {
				throw new InvalidInputException(constructMessage(clientException));
			}
		}
	}

	/**
	 * construct message with reason for exceptions
	 
	 * @param clientException
	 * @return constructed error message
	 */
	public String constructMessage(HttpClientErrorException clientException) {
		StringBuilder stringBuilder = new StringBuilder("Received response ");
		stringBuilder.append(clientException.getRawStatusCode()).append(" - ").append(clientException.getStatusText());
		stringBuilder.append(" from Enterprise Messaging Service.");

		if (clientException.getResponseBodyAsString() != null && !"".equals(clientException.getResponseBodyAsString())) {
			stringBuilder.append(" Reason: ").append(clientException.getResponseBodyAsString());
		}

		return stringBuilder.toString();
	}
}
