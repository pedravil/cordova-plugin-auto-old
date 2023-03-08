package com.bool.auto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.json.JSONObject;

public class MessageReadReceiver extends BroadcastReceiver {

    public static boolean isRegister = false;

    public static void register(Context context) {
        if(!MessageReadReceiver.isRegister) {
            context.registerReceiver(new MessageReadReceiver(), new IntentFilter(NotifCar.READ_ACTION));
        }
    }

    MessageReadReceiver() {
        MessageReadReceiver.isRegister = true;
    }

    @Override
    protected void finalize() throws Throwable {
        MessageReadReceiver.isRegister = false;
        super.finalize();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v("Bool", "READ onReceive");
        final int conversation_id = intent.getIntExtra(NotifCar.CONVERSATION_ID, -1);
        new Api(context).sendRead(
            conversation_id,
            new Api.CallBackRequest() {
                @Override
                public void response(JSONObject message_req) {
                    new NotifCar(context).cancelNotification(conversation_id);
                }
            },
            new Api.CallBackRequest() {
                @Override
                public void response(JSONObject message_req) {
                    new NotifCar(context).cancelNotification(conversation_id);
                }
            });
    }
}
