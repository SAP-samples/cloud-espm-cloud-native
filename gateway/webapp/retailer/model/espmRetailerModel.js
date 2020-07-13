sap.ui.define(
    ["sap/ui/model/json/JSONModel", "com/sap/espm/retailer/utils/httpops"],
    function (JSONModel, HTTPOps) {
        "use strict";

        var PRODUCTS_ENDPOINT = "/product.svc/api/v1/products/";
        var STOCKS_ENDPOINT = "/product.svc/api/v1/stocks/";
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

        var emptyStocksModel = {
            count: 0,
            data: []
        };

        return JSONModel.extend("espmRetailerModel", {
            constructor: function () {
                JSONModel.call(this);

                this.setProperty('/products', emptyProductsModel);
                this.setProperty('/carts', emptyCartModel);
                this.setProperty('/salesorders', emptySalesOrderModel);
                this.setProperty('/stocks', emptyStocksModel);
            },

            getSalesOrders: function () {
                var def = jQuery.Deferred();
                var that = this;

                var sUrl = SALESORDER_ENDPOINT;

                HTTPOps.getAsync(sUrl).then(function (salesOrders) {
                    var model = {
                        count: salesOrders.length,
                        data: salesOrders
                    };

                    that.setProperty("/salesorders", model);
                    return that.loadProducts();
                }).then(function () {
                    var oSalesOrder = that.getProperty("/salesorders");
                    def.resolve(oSalesOrder);
                }).fail(function (error) {
                    def.reject(error);
                });
                return def.promise();
            },

            getStocks: function () {
                var def = jQuery.Deferred();
                var that = this;

                var sUrl = STOCKS_ENDPOINT;
                var ssUrl = PRODUCTS_ENDPOINT;
                HTTPOps.getAsync(sUrl).
                    then(function (stocks) {
                    var model = {
                        count: stocks.length,
                        data: stocks
                    };
                    that.setProperty("/stocks", model);
                    return that.loadProducts();
                }).then(function () {
                    var oStock = that.getProperty("/stocks");
                    def.resolve(oStock);
                }).fail(function (error) {
                    def.reject(error);
                });

                HTTPOps.getAsync(ssUrl).
                then(function (products) {
                var model = {
                    count: products.length,
                    data: products
                };
                that.setProperty("/products", model);
                return that.loadProducts();
            }).then(function () {
                var oProduct = that.getProperty("/products");
                def.resolve(oProduct);
            }).fail(function (error) {
                def.reject(error);
            });
                return def.promise();
            },

            loadCustomer: function (sEmail) {
                var def = jQuery.Deferred();
                var that = this;

                var sUrl = CUSTOMERS_ENDPOINT + sEmail;
                HTTPOps.getAsync(sUrl).then(function (oCustomer) {

                    that.setProperty("/customer", oCustomer);
                    def.resolve(oCustomer);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },

            loadProducts: function () {
                var def = jQuery.Deferred();
                var that = this;
                var sUrl = PRODUCTS_ENDPOINT;

                HTTPOps.getAsync(sUrl).then(function (products) {
                    var model = {
                        count: products.length,
                        data: products
                    };

                    that.setProperty("/products", model);
                    def.resolve(model);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },

            loadProductsid: function (id1) {
                var def = jQuery.Deferred();
                var that = this;
                var sUrl = PRODUCTS_ENDPOINT + id1;

                HTTPOps.getAsync(sUrl).then(function (products) {
                    var model = {
                        count: products.length,
                        data: products
                    };

                    that.setProperty("/products", model);
                    def.resolve(model);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },

            loadproductusingid: function (sEmail) {
                var def = jQuery.Deferred();
                var that = this;

                var sUrl = PRODUCTS_ENDPOINT + sEmail;
                HTTPOps.getAsync(sUrl).then(function (oCustomer) {

                    that.setProperty("/products", oCustomer);
                    def.resolve(oCustomer);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },

            loadstockusingid: function (sId) {
                var def = jQuery.Deferred();
                var that = this;

                var sUrl = STOCKS_ENDPOINT + sId;
                HTTPOps.getAsync(sUrl).then(function (oStock) {

                    that.setProperty("/stocks", oStock);
                    def.resolve(oStock);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },


            loadStocks: function () {
                var def = jQuery.Deferred();
                var that = this;
                var sUrl = STOCKS_ENDPOINT;

                HTTPOps.getAsync(sUrl).then(function (stocks) {
                    var model = {
                        count: stocks.length,
                        data: stocks
                    };

                    that.setProperty("/stocks", model);
                    def.resolve(model);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },

            updateStock: function (sProductId, dQuantity) {
                var def = jQuery.Deferred();
                var that = this;
                var sUrl = STOCKS_ENDPOINT;
                var oProducts = this.getProperty("/products/data");
                var dQuantity = this.getView().byId("quantitycard").quantity;
                var oProduct = oProducts.find(
                    function (_oProduct) {
                        return _oProduct.productId === sProductId;
                    });
    
                if (!oProduct) {
                    def.reject("Unable to find product id " + sProductId);
                } else {
                    var oStock = {
                        productId: sProductId,
                        quantity: dQuantity+10
                    };
    
                    HTTPOps.putAsync(sUrl, oStock)
                        .then(function () {
                            return that.loadStocks();
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

            updateSalesOrder: function (salesOrderId, status, note) {
                var def = jQuery.Deferred();
                var that = this;
                var sUrl = SALESORDER_ENDPOINT + "/" + salesOrderId + "/"
                    + status;
                HTTPOps.putAsync(sUrl, note).then(function (val) {
                    console.log(val);
                    that.getSalesOrders();
                    def.resolve(val);
                }).then(function (data) {
                    def.resolve(data);
                }).fail(function (error) {
                    def.reject(error);
                });

                return def.promise();
            },
        });

    });