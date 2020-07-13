jQuery.sap.require("com.sap.espm.shop.model.format");
sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/m/Dialog",
	"sap/m/Button",
	"sap/m/MessageToast",
	"sap/m/Text",
	"sap/ui/core/UIComponent",
	"sap/ui/core/BusyIndicator",
	"sap/ui/model/json/JSONModel",
	"com/sap/espm/shop/model/Formatter",
	"sap/ui/core/Fragment",
	"sap/ui/model/Filter"
], function (Controller, Dialog, Button, MessageToast, Text, UIComponent, BusyIndicator, JSONModel, Formatter, Fragment, Filter) {
	"use strict";

	var customerId;

	return Controller.extend("com.sap.espm.shop.controller.Shoppingcart", {

		formatter: Formatter,
		oCart: null,
		onInit: function () {
			this.getView().byId("shoppingCartPage").setVisible(true);
		},

		onAfterRendering: function () {
		},

		onBeforeRendering: function () {
		},

		onLineItemPressed: function () {
		},

		onNavBack: function () {
			window.history.go(-1);
		},

		/***
		 * 
		 * This code creates a sales order
		 * 
		 * We close the dialog and open a busy indicator
		 * 
		 * This create the sales order, delete the cart and
		 * reload the carts and sales orders
		 * 
		 */
		onCartOperationsCreateSalesOrderPressed: function () {
			this._oCartOperationDialog.close();

			var oCartInputModel = this._oCartOperationDialog.oModels.cartModel.oData;
			var productId = oCartInputModel.productId;
			var quantityUnit = oCartInputModel.quantityUnit;
			var sCartId = oCartInputModel.itemId;

			BusyIndicator.show();

			var oCustomerModel = this.getView().getModel('customer');
			oCustomerModel.createSalesOrder(productId, quantityUnit)
				.then(function () {
					BusyIndicator.hide();
					MessageToast.show("Sales order Successfully created");
					oCustomerModel.removeCart(sCartId)
				})
				.fail(function (error) {
					BusyIndicator.hide();
					MessageToast.show("Sales order creation canceled");
				});
		},

		/***
		 * 
		 * This function creates a cart
		 * 
		 * we use a model for the fragment then read the values from
		 * the fragment.
		 * 
		 * Once we have a cart we then ask the cart model to create
		 * the cart and reload the carts.
		 * 
		 * We need the customer model to get the id of the current customer
		 */
		onCreateCartSaveCartPressed: function () {
			this._oCreateCartDialog.close();

			var oCartInputModel = this._oCreateCartDialog.getModel();
			var productId = sap.ui.getCore().byId("productInput").getSelectedKey();
			var quantityUnit = oCartInputModel.getProperty("/quantityUnit");

			BusyIndicator.show();

			var oCustomerModel = this.getView().getModel('customer');
			oCustomerModel.createCart(productId, quantityUnit)
				.then(function () {
					BusyIndicator.hide();
					MessageToast.show("Successfully created the cart");
				})
				.fail(function (error) {
					BusyIndicator.hide();
					MessageToast.show("Failed to create cart");
				});
		},

		/***
		 * This function deletes a cart
		 * 
		 * We take the currently selected cart and using that delete
		 * the cart through the model
		 */
		onCartOperationsDeleteCartPressed: function () {


			var oCartInputModel = this._oCartOperationDialog.getModel("cartModel");
			var sCartId = oCartInputModel.getProperty("/itemId");

			BusyIndicator.show();

			var oCustomerModel = this.getView().getModel("customer");
			this._oCartOperationDialog.destroy();
			oCustomerModel.removeCart(sCartId)
				.then(function () {
					BusyIndicator.hide();
					MessageToast.show("Deleted Cart");

				})
				.fail(function (error) {
					BusyIndicator.hide();
					MessageToast.show("Failed to delete cart");
				});
		},

		/***
		 * The user clicked a cart in the list box
		 * 
		 * This will open a dialog allowing the user to delete
		 * the cart or convert the cart into a sales order
		 * 
		 * Note the use of data binding
		 * 
		 */
		onCartSelected: function (oEvent) {
			var oItem = oEvent.getSource();
			var oContext = oItem.getBindingContext("customer");
			var oCart = oContext.getObject();
			var cartModel = new JSONModel(oCart);

			this._oCartOperationDialog = sap.ui.xmlfragment("com.sap.espm.shop.view.CartOperations", this);

			this._oCartOperationDialog.setModel(cartModel, 'cartModel');

			this._oCartOperationDialog.open();
		},

		/**
		 * Open the cart operations dialog allowing you to delete or convert a cart
		 * to a sales order
		 */
		_getCreateCartDialog: function () {
			if (!this._oCreateCartDialog) {
				this._oCreateCartDialog = sap.ui.xmlfragment("com.sap.espm.shop.view.CreateCart", this);

				var defaultCart = {
					productId: "",
					quantityUnit: 1
				};

				var cartModel = new JSONModel(defaultCart);
				this._oCreateCartDialog.setModel(cartModel);

				this.getView().addDependent(this._oCreateCartDialog);
			}

			return this._oCreateCartDialog;
		},

		/***
		 * This function is called when the cart operations dialog is closed using
		 * the cancel button
		 */
		onCartOperationsCancelPressed: function () {
			this._oCartOperationDialog.close();
		},

		/***
		 * This function creates a cart 
		 */
		onCreateCartRequestPressed: function (oEvent) {
			this._getCreateCartDialog().open();
		},

		/***
		 * This function helps the product selecting help to convert the
		 * selected item to a key for the value in the input control
		 */
		suggestionItemSelected: function (oEvent) {
			var oItem = oEvent.getParameter('selectedItem');
			var sKey = oItem.getKey();
			this._oCreateCartDialog.getModel().setProperty('/productId', sKey);
		},

		/***
		 * When the user clicks cancel on the CreateCart dialog it
		 * will call this function
		 */
		onCreateCartCancelPressed: function () {
			this._oCreateCartDialog.close();
		},

		
		onListItemPress: function (oEvent) {
			alert('inside line item');
			var bindingContext = oEvent.getSource().getBindingContextPath();
			var oRouter = UIComponent.getRouterFor(this);
			oRouter.navTo("SalesOrder", { SalesOrder: bindingContext.substr(1) });
		}

	});

});