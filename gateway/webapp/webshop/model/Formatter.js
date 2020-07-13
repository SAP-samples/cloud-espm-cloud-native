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
			sap.m.MessageToast.show(productContext.Name +" " + " added to the cart");
			
			for(var i=0; i<aData.length; i++){
					var prodId = aData[i];
					if(prodId.ProductId === productContext.ProductId){
						prodId.Quantity += 1;//prodId.Quantity++;
						prodId.Total = prodId.Quantity * prodId.Price;
						oModel.setData({ShoppingCart : aData});
						return;
					}
				}
				productContext.Quantity = 1;
				productContext.Total = productContext.Quantity * productContext.Price;
				aData.push(productContext);
				oModel.setData({ShoppingCart : aData});	

		},
		onAddCountToCart: function(oModel){
			
				var totalQuantity = 0;
				
				var data = oModel.getProperty("/carts");
				for(var i=0; i<data.length; i++){
						var prodId = data[i];
						totalQuantity += prodId.Quantity;
					}
			return totalQuantity;		
		}
	};
});
