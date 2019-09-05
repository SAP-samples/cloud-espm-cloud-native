 sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"sap/ui/Device"
], function(JSONModel, Device) {
	"use strict";

	return JSONModel.extend("ProductsModel", {
		constructor: function() {
			JSONModel.call(this);
			this.setData(Device);
		}
	});
});
