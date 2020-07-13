sap.ui.define([
	"com/sap/espm/shop/controller/BaseController",
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
], function (BaseController, Dialog, Button, MessageToast, Text, UIComponent, BusyIndicator, JSONModel, Formatter, Fragment, Filter) {
	"use strict";

	return BaseController.extend("com.sap.espm.shop.controller.Customer", {
		formatter: Formatter,
		oCart: null,
		inputId: '',

		handleValueHelp: function (oEvent) {

			var sInputValue = oEvent.getSource().getValue();

			this.inputId = oEvent.getSource().getId();

			var oCustomerModel = this.getView().getModel('customer');

			var that = this;

			oCustomerModel.loadProducts()
				.then(function () {

					// create value help dialog
					if (!that._valueHelpDialog) {
						that._valueHelpDialog = sap.ui.xmlfragment(
							"com.sap.espm.shop.view.Dialog",
							that
						);
						that.getView().addDependent(that._valueHelpDialog);
					}


					// create a filter for the binding
				
					sInputValue = that._valueHelpDialog.getBinding("items").oModel.oData.products.data;

					// open value help dialog filtered by the input value
					that._valueHelpDialog.open(sInputValue);
				})
				.fail(function (error) {
					BusyIndicator.hide();
					MessageToast.show("Cannot Load Products");
				});

		},

		_handleValueHelpClose: function (evt) {
			var oSelectedItem = evt.getParameter("selectedItem");
			var sKey = oSelectedItem.mProperties;
			var descrip = sKey.description;
			var titles = sKey.title;
			sap.ui.getCore().byId("productInput").setSelectedKey(titles);

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

			// if (!this._oCartOperationDialog) {
			this._oCartOperationDialog = sap.ui.xmlfragment("com.sap.espm.shop.view.CartOperations", this);
			//}
			this._oCartOperationDialog.setModel(cartModel, 'cartModel');

			this._oCartOperationDialog.open();
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
		 * This function creates the cart dialog to add a cart.
		 * We use a fragment to isolate the dialog to make it more maintainable.
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
		 * When the user clicks cancel on the CreateCart dialog it
		 * will call this function
		 */
		onCreateCartCancelPressed: function () {
			this._oCreateCartDialog.close();
		},

		/**
		 *@memberOf com.sap.espm.shop.controller.Customer
		 */
		onListItemPress: function (oEvent) {
			alert('inside line item');
			var bindingContext = oEvent.getSource().getBindingContextPath();
			var oRouter = UIComponent.getRouterFor(this);
			oRouter.navTo("SalesOrder", { SalesOrder: bindingContext.substr(1) });
		}
	});
});
