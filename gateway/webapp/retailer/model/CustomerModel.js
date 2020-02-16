sap.ui.define(
		[ "sap/ui/model/json/JSONModel", "com/sap/ESPM-UI/utils/httpops" ],
		function(JSONModel, HTTPOps) {
			"use strict";

			var PRODUCTS_ENDPOINT = "/product.svc/api/v1/products/";
			var CUSTOMERS_ENDPOINT = "/customer.svc/api/v1/customers/";
			var SALESORDER_ENDPOINT = "/sale.svc/api/v1/salesOrders";

			var emptyCartModel = {
				count : 0,
				data : []
			};

			var emptySalesOrderModel = {
				count : 0,
				data : []
			};

			var emptyProductsModel = {
				count : 0,
				data : []
			};

			return JSONModel.extend("CustomerModel", {
				constructor : function() {
					JSONModel.call(this);

					this.setProperty('/products', emptyProductsModel);
					this.setProperty('/carts', emptyCartModel);
					this.setProperty('/salesorders', emptySalesOrderModel);
				},

				getSalesOrders : function() {
					var def = jQuery.Deferred();
					var that = this;

					var sUrl = SALESORDER_ENDPOINT;

					HTTPOps.getAsync(sUrl).then(function(salesOrders) {
						var model = {
							count : salesOrders.length,
							data : salesOrders
						};

						that.setProperty("/salesorders", model);
						return that.loadProducts();
					}).then(function() {
						var oSalesOrder = that.getProperty("/salesorders");
						def.resolve(oSalesOrder);
					}).fail(function(error) {
						def.reject(error);
					});
					return def.promise();
				},

				loadCustomer : function(sEmail) {
					var def = jQuery.Deferred();
					var that = this;

					var sUrl = CUSTOMERS_ENDPOINT + sEmail;
					HTTPOps.getAsync(sUrl).then(function(oCustomer) {

						that.setProperty("/customer", oCustomer);
						def.resolve(oCustomer);
					}).fail(function(error) {
						def.reject(error);
					});

					return def.promise();
				},

				loadProducts : function() {
					var def = jQuery.Deferred();
					var that = this;
					var sUrl = PRODUCTS_ENDPOINT;

					HTTPOps.getAsync(sUrl).then(function(products) {
						var model = {
							count : products.length,
							data : products
						};

						that.setProperty("/products", model);
						def.resolve(model);
					}).fail(function(error) {
						def.reject(error);
					});

					return def.promise();
				},

				updateSalesOrder : function(salesOrderId, status, note) {
					var def = jQuery.Deferred();
					var that = this;
					var sUrl = SALESORDER_ENDPOINT + "/" + salesOrderId + "/"
							+ status;
					HTTPOps.putAsync(sUrl, note).then(function() {
						return that.getSalesOrders();
					}).then(function() {
						def.resolve();
					}).fail(function(error) {
						def.reject(error);
					});

					return def.promise();
				},
			});

		});