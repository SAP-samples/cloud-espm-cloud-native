jQuery.sap.require("com.sap.espm.retailer.model.format");
sap.ui.define([
	"com/sap/espm/retailer/controller/BaseController",
	"sap/ui/core/mvc/Controller",
	"com/sap/espm/retailer/model/formatter",
	"sap/m/MessageToast",
	"sap/m/MessageBox",
	"sap/ui/core/BusyIndicator"
], function (Controller, formatter, BaseController, MessageToast, MessageBox, BusyIndicator) {
	"use strict";

	return Controller.extend("com.sap.espm.retailer.controller.SalesOrder", {

		formatter: formatter,


		/**
		 * Called when a controller is instantiated and its View controls (if available) are already created.
		 * Can be used to modify the View before it is displayed, to bind event handlers and do other one-time initialization.
		 * @memberOf com.sap.espm.retailer.view.SalesOrder
		 */
		onInit: function () {

		},

		/**
		 * Similar to onAfterRendering, but this hook is invoked before the controller's View is re-rendered
		 * (NOT before the first rendering! onInit() is used for that one!).
		 * @memberOf com.sap.espm.retailer.view.SalesOrder
		 */
		//	onBeforeRendering: function() {
		//
		//	},

		/**
		 * Called when the View has been rendered (so its HTML is part of the document). Post-rendering manipulations of the HTML could be done here.
		 * This hook is the same one that SAPUI5 controls get after being rendered.
		 * @memberOf com.sap.espm.retailer.view.SalesOrder
		 */
		onAfterRendering: function () {

		},

		/**
		 * Called when the Controller is destroyed. Use this one to free resources and finalize activities.
		 * @memberOf com.sap.espm.retailer.view.SalesOrder
		 */
		//	onExit: function() {
		//
		//	}

		listUpdateFinished: function () {

			this.getView().byId("detailPageId").setVisible(false);
		},

		handleListItemPress: function (event) {

			this.getView().byId("detailPageId").setVisible(true);

			var context = event.getSource().getBindingContextPath();
			var salesModel = new sap.ui.model.json.JSONModel(this.getView().getModel("espmRetailerModel").getProperty(context));
			this.byId("detailPageId").setModel(salesModel, "sales");
			var oCustomerModel = this.getView().getModel('espmRetailerModel');
			var customerEmail = oCustomerModel.getProperty(context + '/customerEmail');
			oCustomerModel.loadCustomer(customerEmail);
			var customer = oCustomerModel.getProperty('/customer');
			this.getView().byId("customerForm").bindElement('customer>/customer');

			if (this.getView().getModel("device").oData.isPhone) {
				this.byId("splitContId").to(this.byId("detailPageId"));
			}

		},

		handleSearch: function (evt) { // create model filter 

			var filters = [];
			var query = evt.getParameter("query");
			var model = this.getView().byId("masterPageId").getModel("espmRetailerModel");
			var lifecyclestatus = model.oData.salesorders.data;
			if (query && query.length > 0) {
				var filter = new sap.ui.model.Filter("lifecycleStatusName", sap.ui.model.FilterOperator.Contains, query);
				filters.push(filter);
			} // update list binding 
			var list = this.getView().byId("list");
			var binding = list.getBinding("items");
			binding.filter(filters);

		},
		handleApprove: function (event) {
			var model = this.getView().byId("detailPageId").getModel("sales")
			var salesOrderId = model.oData.salesOrderId;
			var lifecyclestatus = model.oData.lifecycleStatus;
			var bundle = this.getView().getModel("i18n").getResourceBundle();
			var objectAttributes = this.getView().byId("detailObjectStatus");
			var lifecycleStatusName = model.oData.lifecycleStatusName;
			var that = this;
			var val = 0;
			if (lifecyclestatus == 'N' && lifecycleStatusName == "New") {
				MessageBox.confirm(bundle.getText("sales.approveDialogMsg"),
					function (oAction) {
						if (MessageBox.Action.OK === oAction) {
							var oCustomerModel = that.getView().byId("detailPageId").getModel("espmRetailerModel");
							oCustomerModel.updateSalesOrder(salesOrderId, "S", "Sales order Shipped")
								.then(function (status) {
									BusyIndicator.hide();
									console.log(status);
									that.getView().byId("detailPageId").setVisible(true);
									if (status.statusCode === 200) {
										MessageToast.show("Sales Order Shipped Successfully");

									}
									else {

										MessageToast.show("Product is Out of Stock");

									}
								})
								.fail(function (error) {
									BusyIndicator.hide();
									MessageToast.show("Product URL is wrong");
								});
						}
					},
					bundle.getText("sales.approveDialogTitle"));
			} else if (lifecyclestatus == 'R' || lifecyclestatus == 'C') {
				MessageToast.show("Rejected Sales Order cannot be shipped");
			} else {
				MessageToast.show("Only new Sales Order can be shipped");
			}

		},

		handleReject: function () {
			var model = this.getView().byId("detailPageId").getModel("sales")
			var salesOrderId = model.oData.salesOrderId;
			var lifecyclestatus = model.oData.lifecycleStatus;
			var bundle = this.getView().getModel("i18n").getResourceBundle();
			var objectAttributes = this.getView().byId("detailObjectStatus");
			var lifecycleStatusName = model.oData.lifecycleStatusName;
			var that = this;
			if (lifecyclestatus == 'N' && lifecycleStatusName == "New") {
				MessageBox.confirm(bundle.getText("sales.rejectDialogMsg"),
					function (oAction) {
						if (MessageBox.Action.OK === oAction) {
							var oCustomerModel = that.getView().byId("detailPageId").getModel("espmRetailerModel");
							oCustomerModel.updateSalesOrder(salesOrderId, "R", "Sales order Rejected by Retailer")
								.then(function () {
									BusyIndicator.hide();
									that.getView().byId("detailPageId").setVisible(true);
									var objectAttributes = that.getView().byId("detailObjectStatus");
									objectAttributes.setText("Rejected by Retailer");
									MessageToast.show(bundle.getText("sales.rejectDialogSuccessMsg"));
								})
								.fail(function (error) {
									BusyIndicator.hide();
									MessageToast.show(bundle.getText("sales.rejectFailed"));
								});
						}
					},
					bundle.getText("sales.rejectDialogTitle"));
			} else if (lifecyclestatus == 'S') {
				MessageToast.show("Shipped Sales Order cannot be rejected");
			} else {
				MessageToast.show("Only new Sales Order can be rejected");
			}
		},

		onNavBack: function () {
			window.history.go(-1);
		},

		handleNavButtonPress: function () {
			var oSplitCont = this.byId("splitContId");
			var oMaster = oSplitCont.getMasterPages()[0];
			oSplitCont.toMaster(oMaster);
		}


	});

});
