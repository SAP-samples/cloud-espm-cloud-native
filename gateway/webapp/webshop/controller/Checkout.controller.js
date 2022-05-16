jQuery.sap.require("com.sap.espm.shop.model.format");
sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"com/sap/espm/shop/model/Formatter",
	"sap/ui/core/UIComponent",
	"sap/ui/model/odata/ODataModel",
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/routing/History",
	"sap/m/MessageBox"
], function (Controller, Formatter, UIComponent, ODataModel, JSONModel, History, MessageBox) {
	"use strict";
	var oDataModel;
	var customerId = "";
	var firstName = "";
	var lastName = "";
	var birthDate = "";
	var eMail = "";
	var street = "";
	var houseNumber = "";
	var city = "";
	var postalCode = "";
	var country = "";
	var name = "";
	var cardNumber = "";
	var secNumber = "";

	return Controller.extend("com.sap.espm.shop.controller.Checkout", {

		formatter: Formatter,
		cardType: "american",
		onInit: function () {
			var that = this;
			var oCheckoutTemplate = new sap.m.ColumnListItem({
				cells: [
					new sap.m.ObjectIdentifier({
						title: "{name}"
					}),
					new sap.m.Text({
						text: "{quantity}"

					})
				]
			});
			var oComponent = this.getOwnerComponent();
			var model = oComponent.getModel("Cart");

			this._oView = this.getView();
			this._oTotalFooter = this.byId("totalFooter");
			this._oExistingForm = this.byId("existingFormId");
			this._oNewForm = this.byId("newFormId");
			////wizard 
			this._wizard = this.getView().byId("checkoutWizard");
			this._oNavContainer = this.getView().byId("wizardNavContainer");
			this._oWizardReviewPage = sap.ui.xmlfragment("com.sap.espm.shop.view.ReviewPage", this);
			this._oNavContainer.addPage(this._oWizardReviewPage);
			this._oWizardContentPage = this.getView().byId("checkoutContentPage");

			var oreviewTable = sap.ui.getCore().byId("reviewCartTable");
			oreviewTable.setModel(model);
			//		oreviewTable.bindItems("/ShoppingCart",oCheckoutTemplate);
		},
		onAfterRendering: function () {

		},
		onBeforeRendering: function () {

		},
		onNavBack: function () {
			window.history.go(-1);
		},

		_handleMessageBoxOpen: function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that._handleNavigationToStep(0);
						that._wizard.discardProgress(that._wizard.getSteps()[0]);
					}
				}
			});
		},
		_handleNavigationToStep: function (iStepNumber) {
			var that = this;
			function fnAfterNavigate() {
				that._wizard.goToStep(that._wizard.getSteps()[iStepNumber]);
				that._oNavContainer.detachAfterNavigate(fnAfterNavigate);
			}

			this._oNavContainer.attachAfterNavigate(fnAfterNavigate);
			this.backToWizardContent();
		},
		discardProgress: function () {
			this._wizard.discardProgress(this.getView().byId("ProductTypeStep"));

			var clearContent = function (content) {
				for (var i = 0; i < content.length; i++) {
					if (content[i].setValue) {
						content[i].setValue("");
					}

					if (content[i].getContent) {
						clearContent(content[i].getContent());
					}
				}
			};
			clearContent(this._wizard.getSteps());
		},
		wizardCompletedHandler: function () {

			//get the resource bundle
			var oBundle = this.getView().getModel('i18n').getResourceBundle();
			sap.ui.getCore().byId("cardNumber").setText(this.byId("numberId").getValue());

			sap.ui.getCore().byId("cardImg").setSrc("images/" + this.cardType + ".png");
			this._oNavContainer.to(this._oWizardReviewPage);
		},
		handleWizardCancel: function () {

			this._oNavContainer.backToPage(this._oWizardContentPage.getId());
			var oRouter = UIComponent.getRouterFor(this);
			oRouter.navTo("Home", true);
		},
		handleWizardSubmit: function () {
			var that = this;
			// this._oWizardReviewPage.setBusy(true);
			//			this.createSalesOrders();
			var oCustomerModel = this.getView().getModel('customer');
			var numberoforder = oCustomerModel.oData.carts.count;
			while (numberoforder != 0) {
				numberoforder = numberoforder - 1;
				var oCartInputModel = oCustomerModel.oData.carts.data[numberoforder];
				var productId = oCartInputModel.productId;
				var productName = oCartInputModel.name;
				var quantityUnit = oCartInputModel.quantityUnit;
				var sCartId = oCartInputModel.itemId;
				oCustomerModel.createSalesOrder(productId, quantityUnit);
				oCustomerModel.removeCart(sCartId)

			}
			var that = this;
			MessageBox.success("Order Created!", {
				actions: [MessageBox.Action.OK],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.OK) {
						localStorage.setItem("checkedOut",true);
						that._oNavContainer.backToPage(that._oWizardContentPage.getId());
						var oRouter = UIComponent.getRouterFor(that);
						oRouter.navTo("Product", true);
					}
				}
			});
			
			
			// sap.m.MessageToast.show("Orders Placed!!");
			// this._oNavContainer.backToPage(this._oWizardContentPage.getId());
			// var oRouter = UIComponent.getRouterFor(this);
			// oRouter.navTo("Product", true);

		},

		createSalesOrders: function () {
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

		createCustomer: function () {
			var oBundle = this.getView().getModel('i18n').getResourceBundle();
			var that = this;
			var date = this.byId("birthId").getValue();
			var utctime = Date.parse(date);
			date = "/Date(" + utctime + ")/";
			var customer = {
				"EmailAddress": this.byId("newEmailId").getValue().toLowerCase(),
				"LastName": this.byId("lastnameId").getValue(),
				"FirstName": this.byId("firstNameId").getValue(),
				"HouseNumber": this.byId("houseNumberId").getValue(),
				"DateOfBirth": date,
				"PostalCode": this.byId("postalId").getValue(),
				"City": this.byId("cityId").getValue(),
				"Street": this.byId("streetId").getValue(),
				"Country": this.byId("countryListId").getSelectedKey()

			};

			$.ajax({
				type: "POST",
				async: true,
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				url: "customers",
				data: JSON.stringify(customer),
				success: function (responsedata) {
					customerId = responsedata.d.CustomerId;
					that.createSalesOrder();
				},
				error: function () {
					sap.m.MessageToast.show(oBundle.getText("check.customerCreateFailed"));
				}
			});


		},

		backToWizardContent: function () {
			this._oNavContainer.backToPage(this._oWizardContentPage.getId());
		},
		radioButtonSelected: function (oEvent) {

			var buttonId = oEvent.getSource().getSelectedIndex();
			if (buttonId === 0) {
				this._oExistingForm.setVisible(true);
				this._oNewForm.setVisible(false);
				this._wizard.validateStep(this.getView().byId("creditCardStep"));
				this.clearNewForm();
			}
			else {
				this._oExistingForm.setVisible(false);
				this._oNewForm.setVisible(true);
				this._wizard.validateStep(this.getView().byId("creditCardStep"));
				this.clearNewForm();

			}

		},

		getCustomerId: function () {
			var that = this;
			customerId = "";
			var oBundle = this.getView().getModel('i18n').getResourceBundle();
			var buttonIndex = that.getView().byId("radioButtonGroupId").getSelectedIndex();
			var sFunctionImportEmailParam;
			if (buttonIndex === 1) {
				if (that.byId("newEmailId").getValue().length !== 0) {
					sFunctionImportEmailParam = "EmailAddress='" + that.byId("newEmailId").getValue().toLowerCase() + "'";
					var email = this.byId("newEmailId").getValue();

				}
			}
			else {
				if (that.byId("existingEmailId").getValue().length !== 0) {
					sFunctionImportEmailParam = "EmailAddress='" + this.byId("existingEmailId").getValue().toLowerCase() + "'";
					var email = this.byId("existingEmailId").getValue();
				}
			}

			var aParams = [];
			aParams.push(sFunctionImportEmailParam);

			oDataModel = this.getView().getModel("customer");

			oDataModel.loadCustomer(email).then(function (data) {

				if (data.results.length === 0) {
					customerId = "";
				}
				else {
					customerId = data.customerId;

				}
			}, function () {
				sap.m.MessageToast.show(oBundle.getText("soPopup.errorMessage"));

			});

		},

		checkExistingEmailId: function () {

			this.byId("existingEmailId").setValue();


		},
		validateEmail: function (mail) {

			var mailregex = /^\w+[\w-+\.]*\@\w+([-\.]\w+)*\.[a-zA-Z]{2,}$/;
			var oBundle = this.getView().getModel('i18n').getResourceBundle();
			if (typeof (mail) === "object") {
				mail = mail.getSource().getValue();
			}

			if (mail.match(mailregex)) {
				return (true);
			} else {
				sap.m.MessageToast.show(oBundle.getText("soPopup.invalidEmailAddress"));

			}
		},
		validateNumberInputField: function (oEvent) {

			var myInteger = (/^-?\d*(\.\d+)?$/);
			if (!oEvent.getSource().getValue().match(myInteger)) {
				oEvent.getSource().setValueState(sap.ui.core.ValueState.Error);
			}
			else {
				oEvent.getSource().setValueState(sap.ui.core.ValueState.None);
			}

		},
		validateStringInputField: function (oEvent) {

			var myInteger = (/^-?\d*(\.\d+)?$/);
			if (oEvent.getSource().getValue().match(myInteger)) {
				oEvent.getSource().setValueState(sap.ui.core.ValueState.Error);
			}
			else {
				oEvent.getSource().setValueState(sap.ui.core.ValueState.None);
			}


		},

		valueChanged: function () {

			this.byId("nameId").setValue(this.byId("firstNameId").getValue() + " " + this.byId("lastnameId").getValue());
		},

		checkCustomerInformation: function () {

		},

		addMoreProducts: function () {
			var oRouter = UIComponent.getRouterFor(this);
			oRouter.navTo("Product", true);
		},

		formatCustomerLocation: function (location) {
			return formatter.formatCountryName(location);
		},

		clearNewForm: function () {
			this.byId("newEmailId").setValue("");
			this.byId("firstNameId").setValue("");
			this.byId("lastnameId").setValue("");
			this.byId("houseNumberId").setValue("");
			this.byId("birthId").setValue("");
			this.byId("streetId").setValue("");
			this.byId("cityId").setValue("");
			this.byId("countryListId").setSelectedKey("");
			this.byId("postalId").setValue("");
			this.byId("nameId").setValue("");
			this.byId("numberId").setValue("");
			this.byId("securityId").setValue("");
			this.byId("existingEmailId").setValue("");
		},

		validateDateField: function (oEvent) {
			if (!oEvent.getParameter("valid")) {
				oEvent.oSource.setValueState(sap.ui.core.ValueState.Error);
			} else {
				oEvent.oSource.setValueState(sap.ui.core.ValueState.None);
			}
		},

		onRbChange: function (oEvent) {
			this.cardType = oEvent.getSource().data("cardtype");
		}


	});

});
