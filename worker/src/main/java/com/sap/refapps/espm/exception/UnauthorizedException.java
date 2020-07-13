package com.sap.refapps.espm.exception;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = -938408570191353414L;

	public UnauthorizedException(final String message) {
		super(message);
	}
}
