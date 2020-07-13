sap.ui.define([
	"sap/ui/core/UIComponent",
	"com/sap/espm/shop/model/DeviceModel",
	"com/sap/espm/shop/model/CustomerModel"
], function (UIComponent, DeviceModel, CustomerModel) {
	"use strict";

	return UIComponent.extend("com.sap.espm.shop.Component", {

		metadata: {
			manifest: "json"
		},

		/**
		 * The component is initialized by UI5 automatically during the startup of the app and calls the init method once.
		 * @public
		 * @override
		 */
		init: function () {
			// call the base component's init function
			UIComponent.prototype.init.apply(this, arguments);

			var oDeviceModel = new DeviceModel();
			var oCustomerModel = new CustomerModel();

			this.setModel(oDeviceModel, "device");
			this.setModel(oCustomerModel, "customer");
			var oData ={
				ShoppingCart:[]
			};
			var oModel = new sap.ui.model.json.JSONModel(oData);
			this.setModel(oModel,"Cart");
			
			// enable routing
			this.getRouter().initialize();

		}
	});
});