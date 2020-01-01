sap.ui.define([
	"com/sap/ESPM-UI/controller/BaseController",
	"sap/ui/core/BusyIndicator"
], function(BaseController, BusyIndicator) {
	"use strict";

	return BaseController.extend("com.sap.ESPM-UI.controller.Home", {
		
		onInit : function () {
		},
		
		approveTilePressed: function(){
			var oCustomerModel = this.getView().getModel('customer');
			var that = this;
			var ctrl = this.getView().byId("container"); 
			BusyIndicator.show();
			oCustomerModel.getSalesOrders()		
    		.then(function () {
    			BusyIndicator.hide();
    			var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
//    			oRouter.navTo("SalesOrder");
    			oRouter.navTo("SalesOrder");		
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
//			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
//			oRouter.navTo("SalesOrder");
		},
		
		stockTilePressed: function(){
			
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.navTo("StockInformation");
		}

	});

});