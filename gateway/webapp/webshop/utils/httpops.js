sap.ui.define(['jquery.sap.global'], function (jQuery) {
	"use strict";

	var HTTPServiceCallClass = {
		getAsync: function (url) {
			var def = jQuery.Deferred();

			jQuery.ajax({
				url: url,
				cache: true,
				async: true,
				method: "GET",
				dataType: "json",
				success: function (data, status, jqXHR) {
					def.resolve(data);
				},
				error: function (jqXHR, status, error) {
					def.reject({
						status: jqXHR.status,
						msg: jqXHR.responseText || status || error.toString()
					});
				}
			});

			return def.promise();
		},
		postAsync: function (url, body) {
			var def = jQuery.Deferred();

			jQuery.ajax({
				url: url,
				async: true,
				contentType: "application/json",
				data: JSON.stringify(body),
				method: "POST",
				dataType: "text",
				success: function (data, status, jqXHR) {
					def.resolve(data);
				},
				error: function (jqXHR, status, error) {
					def.reject({
						status: status,
						msg: error.toString()
					});
				}
			});

			return def.promise();
		},
		deleteAsync: function (url) {
			var def = jQuery.Deferred();

			jQuery.ajax({
				url: url,
				async: true,
				method: "DELETE",
				success: function (data, status, jqXHR) {
					def.resolve(data);
				},
				error: function (jqXHR, status, error) {
					def.reject({
						status: status,
						msg: error.toString()
					});
				}
			});

			return def.promise();
		}
	};

	return HTTPServiceCallClass;
}, true);