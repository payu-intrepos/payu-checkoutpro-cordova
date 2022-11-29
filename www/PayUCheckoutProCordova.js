function payUCheckoutProCordova() {}

payUCheckoutProCordova.prototype.hashGenerated = function(params) {
    return cordova.exec(
        null,
        null,
        'PayUCheckoutProCordova',
        'hashGenerated',
        [params]
    );
};


payUCheckoutProCordova.prototype.openCheckoutScreen = function(successCallback, params) {
    try {
        var payUPaymentParamsExists = "payUPaymentParams" in params;
        if (payUPaymentParamsExists === true) {
            var additionalParamsExists = "additionalParam" in params["payUPaymentParams"];
            if (additionalParamsExists === true &&
                typeof params["payUPaymentParams"]["additionalParam"] === 'object' &&
                !Array.isArray(params["payUPaymentParams"]["additionalParam"])) {

            } else {
                params["payUPaymentParams"]["additionalParam"] = {};
            }

            params["payUPaymentParams"]["additionalParam"].analyticsData = analyticsString();
        }
    } catch (err) {
        console.log(err);
    }
    return cordova.exec(
        successCallback,
        null,
        'PayUCheckoutProCordova',
        'openCheckoutScreen',
        [params]
    );
};

function analyticsString() {
    var platform = "cordova";
    if (window.cordova.platformId == "ios") {
        platform = "iOS";
    } else if (window.cordova.platformId == "android") {
        platform = "android";
    }

    var analyticsDict = {
        "platform": platform,
        "name": "Cordova_CheckoutPro",
        "version": "1.0.0",
    };

    return JSON.stringify(analyticsDict);
}

module.exports = new payUCheckoutProCordova();
