/*
 * PhoneGap plugin for actions that are specific to the Paige Execution Environment (PEE).<br /> 
 * <br />
 * You can access the functions for this plugin via window.plugins.pee.xxxx().
 * 
 * @author Steven
 */

function PeePlugin() {

}

/**
 * Returns the phone's IMEI number.
 * 
 * @param successCallback
 *            The callback which will be called when the operation completed successfully
 * @param failureCallback
 *            The callback which will be called when an error occurred
 */
PeePlugin.prototype.getImei = function(successCallback, failureCallback) {
    return PhoneGap.exec(successCallback, failureCallback, 'PeePlugin', 'getImei', []);
};

/**
 * Starts the Android service that periodically checks the ASK App Service if there are new messages
 * for the user.
 * 
 * @param successCallback
 *            The callback which will be called when the operation completed successfully
 * @param failureCallback
 *            The callback which will be called when an error occurred
 * @param host The App Service host URL to use for checking messages
 * @param sessionId
 *            The Paige session ID to use with the request to App Services
 * @param interval
 *            Time between updates, in milliseconds
 */
PeePlugin.prototype.startMsgService = function(successCallback, failureCallback, url, sessionId,
        interval) {
    return PhoneGap.exec(successCallback, failureCallback, 'PeePlugin', 'startMsgService', [ url,
            sessionId, interval ]);
};

/**
 * Stops the Android service that periodically checks the ASK App Service if there are new messages
 * for the user.
 * 
 * @param successCallback
 *            The callback which will be called when the operation completed successfully
 * @param failureCallback
 *            The callback which will be called when an error occurred
 */
PeePlugin.prototype.stopMsgService = function(successCallback, failureCallback) {
    return PhoneGap.exec(successCallback, failureCallback, 'PeePlugin', 'stopMsgService', [ ]);
};

PeePlugin.prototype.setCallbackC2DM = function(callback) {
    return PhoneGap.exec(null, null, 'PeePlugin', 'setCallbackC2DM', [ callback ]);
};

PeePlugin.prototype.registerC2DM = function() {
    return PhoneGap.exec(null, null, 'PeePlugin', 'registerC2DM', [ "android@ask-cs.com" ]);
};

PeePlugin.prototype.unregisterC2DM = function(successCallback, failureCallback) {
    return PhoneGap.exec(successCallback, failureCallback, 'PeePlugin', 'unregisterC2DM', null);
};

PeePlugin.prototype.notify = function(prioCount,unreadCount) {
    return PhoneGap.exec(null, null, 'PeePlugin', 'notify', [ prioCount, unreadCount ]);
};

PhoneGap.addConstructor(function() {
    try {
        PhoneGap.addPlugin("pee", new PeePlugin());
    } catch (e) {
        // do it again to fix a bug in iOS:
        if (!window.plugins)
            window.plugins = {};
        if (!window.plugins.pee)
            window.plugins.pee = new PeePlugin();
    }
});
