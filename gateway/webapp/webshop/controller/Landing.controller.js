sap.ui.define([
	"com/sap/espm/shop/controller/BaseController",
	"sap/ui/core/BusyIndicator",
	"sap/ui/core/UIComponent",
	"sap/ui/core/routing/History"
], function (BaseController, UIComponent, History, BusyIndicator) {

	"use strict";
	return BaseController.extend("com.sap.espm.shop.controller.Landing", {

		onContinuePressed: function (oEvent) {
			var ctrl = this.getView().byId("existingFormId");
			var email = ctrl._aElements[1].mProperties.value;
			if(!email){
				this.getView().byId("existingEmailId").setValueState("Error");
			}else{
			var that = this;
			this.getView().byId("existingEmailId").setValueState("None");
			var customerModel = this.getView().getModel('customer');

			//			BusyIndicator.show();
			console.log("email:" + email);

			customerModel.loadCustomer(email)
				.then(function () {
					//			BusyIndicator.hide();
					customerModel.loadProducts();
					that.getRouter().navTo("Product", { customerEmail: email });
				})
				.fail(function (error) {
					//				BusyIndicator.hide();
					if (!error || !error.msg) {
						sap.m.MessageToast.show("There was an error");
					} else {
						sap.m.MessageToast.show(error.msg);
					}

					ctrl.setValueState(sap.ui.core.ValueState.Error);
				});

		}
		},

		onRegisterPressed: function (oEvent) {
			var oItem, oCtx;
			oItem = oEvent.getSource();
			oCtx = oItem.getBindingContext();
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.navTo("Register");
		}
	});
});