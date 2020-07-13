package com.sap.refapps.espm.exception;

public class ConflictException extends RuntimeException{
	
	private static final long serialVersionUID = 2590741844251861472L;

	public ConflictException(final String message) {
		super(message);
	}

}
