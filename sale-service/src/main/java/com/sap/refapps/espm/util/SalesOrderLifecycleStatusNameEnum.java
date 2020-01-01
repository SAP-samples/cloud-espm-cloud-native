package com.sap.refapps.espm.util;

public enum SalesOrderLifecycleStatusNameEnum {
	
	N ("New"),
	I("In Progress"),
	C ("Cancelled"),
	S ("Shipped"),
	R ("Returned"),
	D ("Delivered");
			
	private final String lifecycleStatusName;

	private SalesOrderLifecycleStatusNameEnum(final String lifecycleStatusName){
		this.lifecycleStatusName = lifecycleStatusName;
	}

	public String toString() {
		return this.lifecycleStatusName;
	}
}
