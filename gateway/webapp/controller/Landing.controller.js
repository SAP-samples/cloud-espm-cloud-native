sap.ui.define([
	"com/sap/ESPM-UI/controller/BaseController",
	"sap/ui/core/BusyIndicator"
],function (BaseController, BusyIndicator) {
	
	"use strict";
	return BaseController.extend("com.sap.ESPM-UI.controller.Landing", {
		/**
		 * Called when a controller is instantiated and its View controls (if available) are already created.
		 * Can be used to modify the View before it is displayed, to bind event handlers and do other one-time initialization.
		 * @memberOf com.sap.ESPM-UI.view.landing
		 */ //	onInit: function() {
		//
		//	},
		/**
		 * Similar to onAfterRendering, but this hook is invoked before the controller's View is re-rendered
		 * (NOT before the first rendering! onInit() is used for that one!).
		 * @memberOf com.sap.ESPM-UI.view.landing
		 */ //	onBeforeRendering: function() {
		//
		//	},
		/**
		 * Called when the View has been rendered (so its HTML is part of the document). Post-rendering manipulations of the HTML could be done here.
		 * This hook is the same one that SAPUI5 controls get after being rendered.
		 * @memberOf com.sap.ESPM-UI.view.landing
		 */ //	onAfterRendering: function() {
		//
		//	},
		/**
		 * Called when the Controller is destroyed. Use this one to free resources and finalize activities.
		 * @memberOf com.sap.ESPM-UI.view.landing
		 */ //	onExit: function() {
		//
		//	}
		/**
		 *@memberOf com.sap.ESPM-UI.controller.Landing
		 */
		onContinuePressed: function (oEvent) {
			var ctrl = this.getView().byId("txtEmailAddress"); 
			var email = ctrl._oItemNavigation.oDomRef.firstElementChild.innerText;
			var that = this;
			var customerModel = this.getView().getModel('customer');
			
			BusyIndicator.show();
			
			customerModel.loadCustomer(email)    		
    		.then(function () {
    			BusyIndicator.hide();
    			that.getRouter().navTo("customer", {customerEmail: email});		
    		})
    		.fail(function(error) {
    			BusyIndicator.hide();
    			if (!error || !error.msg) {
    				sap.m.MessageToast.show("There was an error");
    			} else {
    				sap.m.MessageToast.show(error.msg);	
    			}
    			
				ctrl.setValueState(sap.ui.core.ValueState.Error);
    		});
		}
	});
});