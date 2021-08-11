sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "com/sap/espm/shop/model/Formatter",
    "sap/ui/core/UIComponent",
    "sap/m/MessageBox"
], function (Controller, Formatter, UIComponent, MessageBox) {
    "use strict";
    var countvar = 0;
    var customerid;
    var email;
    return Controller.extend("com.sap.espm.shop.controller.Product", {

        formatter: Formatter,

        onInit: function () {
            this.getView().byId("productListPage").setVisible(true);
            localStorage.setItem("checkedOut", false);
            var productsModel = new sap.ui.model.json.JSONModel(this.getView().getModel("customer"));
            this.byId("productListPage").setModel(productsModel, "products");
            var oRouter = UIComponent.getRouterFor(this);
            oRouter
            .getRoute("Product")
            .attachPatternMatched(this._onObjectMatched, this);
        },
        _onObjectMatched: function (oEvent) {
            if(localStorage.getItem("checkedOut") == "true"){
                this.getView().byId("btnProductListHeader").setText(0);
            }
            
          },
        onAfterRendering: function () {

        },
        onBeforeRendering: function () {

        },
        onLineItemPressed: function (event) {
            var bindingContext = event.getSource().getBindingContextPath();
            var oRouter = UIComponent.getRouterFor(this);
            oRouter.navTo("ProductDetail", { Productdetails: bindingContext.split("/")[3] });
        },
        onAddToCartHomePressed: function (oEvent) {
            var productId = oEvent.getSource().getBindingContext('customer').getObject().productId;
            var name = oEvent.getSource().getBindingContext('customer').getObject().name;
            var quantityUnit = oEvent.oSource.oParent.mAggregations.cells[4].mProperties.value;
            if(quantityUnit === "")
            {
                sap.m.MessageToast.show("Minimum quantity 1 is taken");
                quantityUnit = 1;
            }
            var oCustomerModel = this.getView().getModel('customer');
            var ctrl = this.getView().byId("txtEmailAddress");
            email = ctrl.mProperties.text;
            var customerModel = this.getView().getModel('customer');
            customerid = customerModel.loadCustomer(email);
            customerModel.createCart(productId, name, quantityUnit);
            countvar++;
            this.getView().byId("btnProductListHeader").setText(countvar);
            if(countvar>5)
            {   
                sap.m.MessageToast.show("Please try to restrict the Products for fast checkout..");

            }
        },
        onShoppingCartPressed: function () {
            countvar = 0;
            var ctrl = this.getView().byId("txtEmailAddress");
            email = ctrl.mProperties.text;
            var that = this;
            var customerModel = this.getView().getModel('customer');
            customerid = customerModel.loadCustomer(email);
            customerModel.loadCarts()
                .then(function () {
                    var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
                    oRouter.navTo("Checkout");
                })
                .fail(function (error) {
                    if (!error || !error.msg) {
                        sap.m.MessageToast.show("There was an error");
                    } else {
                        sap.m.MessageToast.show(error.msg);
                    }
                });

        },

        onNavBack: function () {
			window.history.go(-1);
		},

        onOrdersButtonPressed: function () {
            var ctrl = this.getView().byId("txtEmailAddress");
            email = ctrl.mProperties.text;
            var that = this;
            var customerModel = this.getView().getModel('customer');
            customerid = customerModel.loadCustomer(email);
            customerModel.loadSalesOrders()
                .then(function () {
                    var oRouter = sap.ui.core.UIComponent.getRouterFor(that);
                    oRouter.navTo("Shoppingcart");
                })
                .fail(function (error) {
                    if (!error || !error.msg) {
                        sap.m.MessageToast.show("There was an error");
                    } else {
                        sap.m.MessageToast.show(error.msg);
                    }
                });
        },

        onPress: function (evt) {
            var ctrl = this.getView().byId("txtEmailAddress");
            email = ctrl.mProperties.text;
            var that = this;
            var customerModel = this.getView().getModel('customer');
            customerid = customerModel.loadCustomer(email);
        }

    });

});