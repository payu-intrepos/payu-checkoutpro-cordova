package com.payu.cordova;

import static com.payu.checkoutpro.parser.constants.PayUHybridValues.Error.GENERIC_ERROR_CODE;

import android.webkit.WebView;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.payu.base.models.ErrorResponse;
import com.payu.checkoutpro.parser.CheckoutProCallbackToJSONParser;
import com.payu.checkoutpro.parser.constants.PayUHybridKeys;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;

import com.payu.checkoutpro.PayUCheckoutPro;

public class PayUCheckoutProCordova extends CordovaPlugin {
    private CheckoutProCallbackToJSONParser responseTransformer = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            if (action.equals(PayUHybridKeys.Others.hashGenerated) && args != null && args.length() > 0) {
                JSONObject object = args.getJSONObject(0);
                this.hashGenerated(object, callbackContext);
                return true;
            }
            if (action.equals(PayUHybridKeys.Others.openCheckoutScreen) && args != null && args.length() > 0) {
                JSONObject object = args.getJSONObject(0);
                this.openCheckoutScreen(object, callbackContext);
                return true;
            }
        } catch (JSONException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(GENERIC_ERROR_CODE);
            errorResponse.setErrorMessage(e.getMessage());
            onErrorCallback(errorResponse, callbackContext);
        }
        return false;
    }

    private void hashGenerated(JSONObject hash, CallbackContext callbackContext) throws JSONException {
        responseTransformer.hashGenerated(cordova.getActivity(), toMap(hash), error -> {
            onErrorCallback(error, callbackContext);
            return null;
        });
    }

    public void openCheckoutScreen(JSONObject paymentParams, CallbackContext callbackContext) throws JSONException {
        responseTransformer = new CheckoutProCallbackToJSONParser();
        PayUCheckoutPro.open(this.cordova.getActivity(), toMap(paymentParams), new PayUCheckoutProListener() {
            @Override
            public void onPaymentSuccess(@NonNull Object o) {
                sendResultBack(responseTransformer.onPaymentSuccess(o), PayUHybridKeys.Others.onPaymentSuccess, callbackContext);
            }

            @Override
            public void onPaymentFailure(@NonNull Object o) {
                sendResultBack(responseTransformer.onPaymentFailure(o), PayUHybridKeys.Others.onPaymentFailure, callbackContext);
            }

            @Override
            public void onPaymentCancel(boolean isTxnInitiated) {
                sendResultBack(responseTransformer.onPaymentCancel(isTxnInitiated), PayUHybridKeys.Others.onPaymentCancel, callbackContext);
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                sendResultBack(responseTransformer.onError(errorResponse), PayUHybridKeys.Others.onError, callbackContext);
            }

            @Override
            public void generateHash(@NonNull HashMap<String, String> hashMap, @NonNull PayUHashGenerationListener payUHashGenerationListener) {

                JSONObject reply = new JSONObject();
                try {
                    reply.put(PayUHybridKeys.Others.generateHash, new JSONObject(responseTransformer.generateHash(hashMap, payUHashGenerationListener)));
                } catch (JSONException e) {
                }
                PluginResult resultA = new PluginResult(PluginResult.Status.OK, reply);
                resultA.setKeepCallback(true);
                callbackContext.sendPluginResult(resultA);
            }

            @Override
            public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {

            }
        });
    }

    private void onErrorCallback(@NonNull ErrorResponse errorResponse, CallbackContext callbackContext) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (errorResponse != null){
                jsonObject.put(PayUHybridKeys.Others.errorCode, errorResponse.getErrorCode());
                jsonObject.put(PayUHybridKeys.Others.errorMsg, errorResponse.getErrorMessage());
            }
        }catch (Exception e) {
        }
        sendResultBack(jsonObject, PayUHybridKeys.Others.onError, callbackContext);
    }

    private void sendResultBack(Object result, String statusCode, CallbackContext callbackContext) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(statusCode, result.toString());
        } catch (Exception e) {
        }
        callbackContext.success(jsonObject);
    }


    public static HashMap<String, Object> toMap(JSONObject jsonobj) throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
