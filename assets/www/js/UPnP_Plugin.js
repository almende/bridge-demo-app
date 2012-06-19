
function UPNP_Plugin() {
}

/**
 * Returns the phone's IMEI number.
 * 
 * @param successCallback
 *            The callback which will be called when the operation completed successfully
 * @param failureCallback
 *            The callback which will be called when an error occurred
 */
UPNP_Plugin.prototype.initUPNP = function(uniqueName, friendlyName) {
    return PhoneGap.exec(null, null, 'UPNP_Plugin', 'initUPNP', [uniqueName,friendlyName]);
};
UPNP_Plugin.prototype.start = function() {
	console.log("Start UPNP server")
    return PhoneGap.exec(null, null, 'UPNP_Plugin', 'start', []);
};
UPNP_Plugin.prototype.stop = function() {
	console.log("Stop UPNP server")
    return PhoneGap.exec(null, null, 'UPNP_Plugin', 'stop', []);
};
UPNP_Plugin.prototype.registerStateVar = function(varName,allowedValues) {
	console.log("registerStateVar");
	return PhoneGap.exec(null,null, 'UPNP_Plugin', 'registerStateVar', [varName, allowedValues]);
}
UPNP_Plugin.prototype.registerAction = function(actionName,direction,argumentNames,returnNames) {
	console.log("registerAction");
	return PhoneGap.exec(null,null, 'UPNP_Plugin', 'registerAction', [actionName, direction, argumentNames, returnNames]);
}
UPNP_Plugin.prototype.doReturn = function(returnValKey,returnValue){
	console.log("doReturn");
	return PhoneGap.exec(null,null, 'UPNP_Plugin', 'doReturn', [returnValKey,returnValue]);
}

UPNP_Plugin.prototype.runDiscovery = function(types){
	console.log("runDiscovery");
	return PhoneGap.exec(null,null, 'UPNP_Plugin', 'runDiscovery', types);
}

UPNP_Plugin.prototype.listDevices = function(callback){
	console.log("listDevices");
	return PhoneGap.exec(callback,null, 'UPNP_Plugin', 'listDevices', []);
}

UPNP_Plugin.prototype.getDescription = function(url,callback){
	console.log("getDescription");
	return PhoneGap.exec(callback,null, 'UPNP_Plugin', 'getDescription', [url]);
}
UPNP_Plugin.prototype.runAction = function(url,action,service,callback){
	console.log("runAction");
	return PhoneGap.exec(callback,null, 'UPNP_Plugin', 'runAction', [url,action,service]);
}

PhoneGap.addConstructor(function() {
    try {
        PhoneGap.addPlugin("UPNP_Plugin", new UPNP_Plugin());
    } catch (e) {
        // do it again to fix a bug in iOS:
        if (!window.plugins)
            window.plugins = {};
        if (!window.plugins.UPNP_Plugin)
            window.plugins.UPNP_Plugin = new UPNP_Plugin();
    }
});
