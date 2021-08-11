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
		},
		onAddToCart: function(oModel, productContext){
			
			var aData = oModel.getProperty("/carts");
			sap.m.MessageToast.show(productContext.name +" " + " added to the cart");
			
			for(var i=0; i<aData.length; i++){
					var prodId = aData[i];
					if(prodId.productId === productContext.productId){
						prodId.quantity += 1;//prodId.Quantity++;
						prodId.total = prodId.quantity * prodId.price;
						oModel.setData({ShoppingCart : aData});
						return;
					}
				}
				productContext.quantity = 1;
				productContext.total = productContext.quantity * productContext.price;
				aData.push(productContext);
				oModel.setData({ShoppingCart : aData});	

		},
		onAddCountToCart: function(oModel){
			
				var totalQuantity = 0;
				
				var data = oModel.getProperty("/carts");
				if(data){
						for(var i=0; i<data.length; i++){
						var prodId = data[i];
						totalQuantity += prodId.quantity;
					}
				}
				
			return totalQuantity;		
		}
	};
});
