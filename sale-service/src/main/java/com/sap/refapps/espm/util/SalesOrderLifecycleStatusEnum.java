package com.sap.refapps.espm.util;

public enum SalesOrderLifecycleStatusEnum {
	
	N ("N"),
	I ("I"),
	C ("C"),
	S ("S"),
	R ("R"),
	D ("D");
			
	private final String lifecycleStatus;

	private SalesOrderLifecycleStatusEnum(final String lifecycleStatus){
		this.lifecycleStatus = lifecycleStatus;
	}

	public String toString() {
		return this.lifecycleStatus;
	}
}
