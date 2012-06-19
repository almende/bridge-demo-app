var Toasti = function() {
};

Toasti.prototype.show_long = function(message, win, fail) {
  PhoneGap.exec(win, fail, "Toasti", "show_long", [message]);
};

Toasti.prototype.show_short = function(message, win, fail) {
  PhoneGap.exec(win, fail, "Toasti", "show_short", [message]);
};

/**
 * <ul>
 * <li>Register the ToastPlugin Javascript plugin.</li>
 * <li>Also register native call which will be called when this plugin runs</li>
 * </ul>
 */
PhoneGap.addConstructor(function() { 
  // Register the javascript plugin with PhoneGap
  console.log("registering Toasti()");
  PhoneGap.addPlugin('Toasti', new Toasti());

  // Register the native class of plugin with PhoneGap
  //navigator.app.addService("Toasti", "nl.ask.paige.plugin.Toasti"); 
});