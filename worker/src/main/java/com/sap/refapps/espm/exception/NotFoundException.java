package com.sap.refapps.espm.exception;

public class NotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 8852035257640697806L;

	public NotFoundException(final String message) {
		super(message);
	}

}
