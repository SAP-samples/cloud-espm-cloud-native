sap.ui.define(function() {
	"use strict";

	return {
		checkoutStatus :  function (sStatus) {
			return sStatus ? "Success" : "Error";
		},
		
		checkoutStatusText: function(sStatus) {
			return sStatus? "Checked Out" : "Pending";
		},
		
		lifecycleStatus: function(sLifecycleStatus) {
			return sLifecycleStatus === 'N' ? "Success" : "None";
		}
	};
});
