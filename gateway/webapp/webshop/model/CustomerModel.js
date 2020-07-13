sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"com/sap/espm/shop/utils/httpops"
], function (JSONModel, HTTPOps) {
	"use strict";

	var PRODUCTS_ENDPOINT = "/product.svc/api/v1/products/";
	var CUSTOMERS_ENDPOINT = "/customer.svc/api/v1/customers/";
	var SALESORDER_ENDPOINT = "/sale.svc/api/v1/salesOrders";

	var emptyCartModel = {
		count: 0,
		data: []
	};

	var emptySalesOrderModel = {
		count: 0,
		data: []
	};

	var emptyProductsModel = {
		count: 0,
		data: []
	};

	return JSONModel.extend("CustomerModel", {
		constructor: function () {
			JSONModel.call(this);

			this.setProperty('/products', emptyProductsModel);
			this.setProperty('/carts', emptyCartModel);
			this.setProperty('/salesorders', emptySalesOrderModel);
		},

		getProducts: function () {
			var def = jQuery.Deferred();
			var that = this;

			var sUrl = PRODUCTS_ENDPOINT;

			HTTPOps.getAsync(sUrl).then(function (products) {
				var model = {
					count: products.length,
					data: products
				};

				that.setProperty("/products", model);
				return that.loadProducts();
			}).then(function () {
				var oSalesOrder = that.getProperty("/products");
				def.resolve(oSalesOrder);
			}).fail(function (error) {
				def.reject(error);
			});
			return def.promise();
		},


		loadProducts: function () {
			var def = jQuery.Deferred();
			var that = this;
			var sUrl = PRODUCTS_ENDPOINT;

			HTTPOps.getAsync(sUrl)
				.then(function (products) {
					var model = {
						count: products.length,
						data: products
					};

					that.setProperty("/products", model);
					def.resolve(model);
				})
				.fail(function (error) {
					def.reject(error);
				});

			return def.promise();
		},

		loadCustomer: function (sEmail) {
			var def = jQuery.Deferred();
			var that = this;

			var sUrl = CUSTOMERS_ENDPOINT + sEmail;
			HTTPOps.getAsync(sUrl)
				.then(function (oCustomer) {

					that.setProperty("/customer", oCustomer);

					return that.loadProducts();
				})
				.then(function () {
					return that.loadCarts();
				})
				.then(function () {
					return that.loadSalesOrders();
				})
				.then(function () {
					var oCustomer = that.getProperty("/customer");
					def.resolve(oCustomer);
				})
				.fail(function (error) {
					def.reject(error);
				});

			return def.promise();
		},

		loadCarts: function () {
			var def = jQuery.Deferred();
			var that = this;

			var customerId = this.getProperty("/customer/customerId");
			var sUrl = CUSTOMERS_ENDPOINT + customerId + "/carts/";

			HTTPOps.getAsync(sUrl)
				.then(function (cart) {
					var model = {
						count: cart.length,
						data: cart
					};

					that.setProperty("/carts", model);
					def.resolve(model);
				})
				.fail(function (error) {
					def.reject(error);
				});

			return def.promise();
		},

		createCustomer: function (sEmailAddress, sPhoneNumber, sFirstName, sLastName, sDob, sCity, sPostalCode, sStreet , sHouseNumber, sCountry  ) {
			var def = jQuery.Deferred();
			var that = this;
			var sUrl = CUSTOMERS_ENDPOINT;

			var oCustomer = {
				emailAddress: sEmailAddress,
				phoneNumber: sPhoneNumber,
				firstName: sFirstName,
				lastName: sLastName,
				dateOfBirth: sDob,
				city: sCity,
				postalCode: sPostalCode,
				street: sStreet,
				houseNumber: sHouseNumber,
				country: sCountry
			};

			HTTPOps.postAsync(sUrl, oCustomer)
				.then(function () {
					return that.loadCustomer(sEmailAddress);
				})
				.then(function () {
					def.resolve();
				})
				.fail(function (error) {
					def.reject(error);
				});

			return def.promise();
		},

		createCart: function (sProductId, sProductName, dQuantity) {
			var def = jQuery.Deferred();
			var that = this;

			var sCustomerId = this.getProperty("/customer/customerId");
			var sUrl = CUSTOMERS_ENDPOINT + sCustomerId + "/carts/";

			var oCart = {
				productId: sProductId,
				quantityUnit: dQuantity,
				name: sProductName,
				checkOutStatus: false
			};

			HTTPOps.postAsync(sUrl, oCart)
				.then(function () {
					return that.loadCarts();
				})
				.then(function () {
					def.resolve();
				})
				.fail(function (error) {
					def.reject(error);
				});

			return def.promise();
		},

		removeCart: function (sCartId) {
			var def = jQuery.Deferred();
			var that = this;
			var sCustomerId = this.getProperty('/customer/customerId');
			var sUrl = CUSTOMERS_ENDPOINT + sCustomerId + "/carts/" + sCartId;

			HTTPOps.deleteAsync(sUrl)
				.then(function () {
					return that.loadCarts();
				})
				.then(function () {
					def.resolve();
				})
				.fail(function (error) {
					def.reject(error);
				});

			return def.promise();
		},

		loadSalesOrders: function () {
			var def = jQuery.Deferred();
			var that = this;
			var sCustomerEmail = this.getProperty("/customer/emailAddress");
			var sUrl = SALESORDER_ENDPOINT + "/email/" + sCustomerEmail;

			HTTPOps.getAsync(sUrl)
				.then(function (salesOrders) {
					var model = {
						count: salesOrders.length,
						data: salesOrders
					};

					that.setProperty("/salesorders", model);
					def.resolve(model);
				})
				.fail(function (error) {
					// If the customer does not have any sales orders
					// we get a 404. Since we have already loaded a
					// customer we know the customer exists so this means
					// the error was raised because there are no sales orders
					// so we return an empty array
					if (error.status === 404) {
						that.setProperty("/salesorders", emptySalesOrderModel);
						def.resolve(emptySalesOrderModel);
					} else {
						def.reject(error);
					}

				});

			return def.promise();

		},

		createSalesOrder: function (sProductId, dQuantity) {
			var def = jQuery.Deferred();
			var that = this;
			var sUrl = SALESORDER_ENDPOINT;
			var oProducts = this.getProperty("/products/data");
			var oProduct = oProducts.find(
				function (_oProduct) {
					return _oProduct.productId === sProductId;
				});

			if (!oProduct) {
				def.reject("Unable to find product id " + sProductId);
			} else {
				var oSalesOrder = {
					customerEmail: this.getProperty("/customer/emailAddress"),
					productId: sProductId,
					productName: oProduct.name,
					currencyCode: oProduct.currencyCode,
					grossAmount: dQuantity * oProduct.price,
					quantity: dQuantity
				};

				HTTPOps.postAsync(sUrl, oSalesOrder)
					.then(function () {
						return that.loadSalesOrders();
					})
					.then(function () {
						def.resolve();
					})
					.fail(function (error) {
						def.reject(error);
					});

			}

			return def.promise();
		},


	});

});