sap.ui.define([
	"com/sap/espm/shop/controller/BaseController",
	"sap/ui/core/BusyIndicator"
], function (BaseController, BusyIndicator) {
	"use strict";

	return BaseController.extend("com.sap.espm.shop.controller.Home", {

		onInit: function () {
		},

		viewTilePressed: function () {
			var oCustomerModel = this.getView().getModel('customer');
			var that = this;
			var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
			oRouter.navTo("landing");
		},

		prodTilePressed: function () {

			var oCustomerModel = this.getView().getModel('customer');
			var that = this;
			var ctrl = this.getView().byId("container");
			BusyIndicator.show();
			oCustomerModel.loadProducts()
				.then(function () {
					BusyIndicator.hide();
					var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
					oRouter.navTo("Product");
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
		}

	});

});