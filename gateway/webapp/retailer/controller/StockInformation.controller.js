sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"com/sap/espm/retailer/model/formatter",
	"sap/ui/model/json/JSONModel",
], function(Controller,formatter, JSONModel) {
	"use strict";	
	return Controller.extend("com.sap.espm.retailer.controller.StockInformation", {

		formatter: formatter,		
			onInit: function() {
				this.getView().byId("stocklistpage").setVisible(true);
				var productsModel = new sap.ui.model.json.JSONModel(this.getView().getModel("espmRetailerModel"));
				this.byId("stocklistpage").setModel(productsModel, "stocks"); 
			},

		/***
		 * We can use This function to create the stock card dialog to add a card.
		 * We use a fragment to isolate the dialog to make it more maintainable.
		
		_initializeStockCard: function () {
			if (!this._oStockCard) {
				this._oStockCard = sap.ui.xmlfragment("com.sap.espm.retailer.view.StockCard",this.getView());
				var stockModel = new JSONModel(this.getView().getModel("espmRetailerModel"));
				this._oStockCard.setModel(stockModel);
				this.getView().addDependent(this._oStockCard);
			}
		},

		onLineItemPressed: function(event){
			
			var context = event.getSource().getBindingContextPath();
			var oLink = event.getSource();
			var stocksModel = new sap.ui.model.json.JSONModel(this.getView().getModel("espmRetailerModel"));
			var oCustomerModel = this.getView().getModel('espmRetailerModel');
			var productid = oCustomerModel.getProperty(context + '/productId');
			oCustomerModel.loadstockusingid(productid);
			var stocks = oCustomerModel.getProperty('/stocks');
			this._initializeStockCard();		
			this._oStockCard.openBy(oLink.getCells()[2]);	
			var oStockInputModel = this._oStockCard.getModel(stocksModel);
		},
*/
		onNavBack: function(){
			window.history.go(-1);
		}
	});

});