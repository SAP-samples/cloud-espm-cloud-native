sap.ui.define([
	"com/sap/espm/retailer/controller/BaseController",
	"sap/ui/core/BusyIndicator"
], function (BaseController, BusyIndicator) {
	"use strict";

	return BaseController.extend("com.sap.espm.retailer.controller.Home", {

		onInit: function () {
		},

		approveTilePressed: function () {
			var oCustomerModel = this.getView().getModel('espmRetailerModel');
			var that = this;
			var ctrl = this.getView().byId("container");
			BusyIndicator.show();
			oCustomerModel.getSalesOrders()
				.then(function () {
					BusyIndicator.hide();
					var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
					oRouter.navTo("SalesOrder");
				})
				.fail(function (error) {
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

		stockTilePressed: function () {

			var oCustomerModel = this.getView().getModel('espmRetailerModel');
			var that = this;
			var ctrl = this.getView().byId("container");
			BusyIndicator.show();
			oCustomerModel.loadStocks()
				.then(function () {
					BusyIndicator.hide();
					var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
					oRouter.navTo("StockInformation");
				})
				.fail(function (error) {
					BusyIndicator.hide();
					if (!error || !error.msg) {
						sap.m.MessageToast.show("There was an error");
					} else {
						sap.m.MessageToast.show(error.msg);
					}

					ctrl.setValueState(sap.ui.core.ValueState.Error);
				});
			//	var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			//	oRouter.navTo("StockInformation");
		}

	});

});