package com.bool.auto;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.firebase.FirebasePluginMessageReceiverManager;
import org.json.JSONArray;

public class MainCar extends CordovaPlugin {

    protected String message;
    private SharedPreferences prefs;

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        prefs.edit().putBoolean("inBackground", false).apply();
    }

    @Override
    public void onPause(boolean multitasking) {
        prefs.edit().putBoolean("inBackground", true).apply();
        super.onPause(multitasking);
    }

    @Override
    public void onStart() {
        super.onStart();
        prefs.edit().putBoolean("inBackground", false).apply();
    }

    @Override
    public void onStop() {
        prefs.edit().putBoolean("inBackground", true).apply();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        prefs.edit().putBoolean("inBackground", true).apply();
        super.onDestroy();
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        prefs = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity());
        prefs.edit().putBoolean("inBackground", false).apply();
        FirebasePluginMessageReceiverManager.register(TncFirebase.class.getCanonicalName(), cordova.getActivity());
        super.initialize(cordova, webView);
    }

    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) {

        if (action.equals("init")) {
            callbackContext.success("Plugin." + action + " found");
        } else {
            callbackContext.error("AlertPlugin." + action + " not found !");

            return false;
        }

        return true;
    }
}
