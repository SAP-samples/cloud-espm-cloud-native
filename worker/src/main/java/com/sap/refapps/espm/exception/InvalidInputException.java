package com.sap.refapps.espm.exception;

public class InvalidInputException extends RuntimeException {

	private static final long serialVersionUID = -7178875961463655244L;

	public InvalidInputException(final String message) {
		super(message);
	}
}
