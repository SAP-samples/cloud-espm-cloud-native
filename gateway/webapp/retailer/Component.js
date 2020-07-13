sap.ui.define([
	"sap/ui/core/UIComponent",
	"com/sap/espm/retailer/model/DeviceModel",
	"com/sap/espm/retailer/model/espmRetailerModel"
], function (UIComponent, DeviceModel, espmRetailerModel) {
	"use strict";

	return UIComponent.extend("com.sap.espm.retailer.Component", {

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
			var oEspmModel = new espmRetailerModel();
			this.setModel(oDeviceModel, "device");
			this.setModel(oEspmModel, "espmRetailerModel");

			// enable routing
			this.getRouter().initialize();

		}
	});
});