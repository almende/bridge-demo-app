/**
 * Phonegap WebIntent plugin
 * Copyright (c) Boris Smus 2010
 *
 */
var WebIntent = function() { 

};

WebIntent.ACTION_SEND = "android.intent.action.SEND";
WebIntent.ACTION_VIEW= "android.intent.action.VIEW";
WebIntent.EXTRA_TEXT = "android.intent.extra.TEXT";
WebIntent.EXTRA_SUBJECT = "android.intent.extra.SUBJECT";
WebIntent.ACTION_SENSE = "nl.sense-os.app"

WebIntent.ACTION_NEW_MSG = "nl.sense_os.app.MsgHandler.NEW_MSG";
WebIntent.ACTION_NEW_FILE = "nl.sense_os.app.MsgHandler.NEW_FILE";
WebIntent.ACTION_SEND_DATA = "nl.sense_os.app.MsgHandler.SEND_DATA";
WebIntent.KEY_DATA_TYPE = "data_type";
WebIntent.KEY_SENSOR_DEVICE = "sensor_device";
WebIntent.KEY_SENSOR_NAME = "sensor_name";
WebIntent.KEY_TIMESTAMP = "timestamp";
WebIntent.KEY_VALUE = "value";

WebIntent.SENSOR_DATA_TYPE_BOOL = "bool";
WebIntent.SENSOR_DATA_TYPE_FLOAT = "float";
WebIntent.SENSOR_DATA_TYPE_INT = "int";
WebIntent.SENSOR_DATA_TYPE_JSON = "json";
WebIntent.SENSOR_DATA_TYPE_STRING = "string";
WebIntent.SENSOR_DATA_TYPE_FILE = "file";


WebIntent.prototype.startActivity = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'startActivity', [params]);
};

WebIntent.prototype.hasExtra = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'hasExtra', [params]);
};

WebIntent.prototype.getExtra = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'getExtra', [params]);
};

WebIntent.prototype.getDataString = function(success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'getDataString', []);
};

WebIntent.prototype.sendToSense = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'sendToSense', [params]);
};

PhoneGap.addConstructor(function() {
	if (typeof(window.plugins.webintent) == "undefined") {
		console.log('registering webintent()');
		PhoneGap.addPlugin('webintent', new WebIntent());
//		navigator.app.addService("WebIntent","nl.ask.paige.plugin.WebIntent");
	}
});