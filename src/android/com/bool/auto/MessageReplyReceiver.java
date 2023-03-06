package com.bool.auto;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageReplyReceiver extends BroadcastReceiver {

    public static boolean isRegister = false;

    public static void register(Context context) {
        if(!MessageReplyReceiver.isRegister) {
            context.registerReceiver(new MessageReplyReceiver(), new IntentFilter(NotifCar.REPLY_ACTION));
        }
    }

    MessageReplyReceiver() {
        MessageReplyReceiver.isRegister = true;
    }

    @Override
    protected void finalize() throws Throwable {
        MessageReplyReceiver.isRegister = false;
        super.finalize();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v("TNC", "REPLY onReceive");
        if (NotifCar.REPLY_ACTION.equals(intent.getAction())) {
            final Bundle params = intent.getExtras();
            if(params != null) {
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                final int conversation_id = params.getInt("conversation_id");
                if (remoteInput != null) {
                    String mreply = (String) remoteInput.getCharSequence(NotifCar.EXTRA_REPLY);
                    String[] message_reply = params.getStringArray("message_reply");
                    if (message_reply != null) {
                        ArrayList<String> myList = new ArrayList<>(Arrays.asList(message_reply));
                        myList.add(mreply);
                        message_reply = myList.toArray(new String[]{});
                        if (conversation_id != -1) {
                            final String[] message_reply_final = message_reply;
                            new Api(context).sendReply(
                                mreply,
                                conversation_id,
                                new Api.CallBackRequest() {
                                    @Override
                                    public void response(JSONObject message_req) {
                                        new NotifCar(context).sendNotification(
                                        params.getString("u_name", ""),
                                        params.getInt("small_icon"),
                                        (Bitmap) params.getParcelable("u_avatar"),
                                        params.getString("message"),
                                        conversation_id,
                                        params.getLong("time"),
                                        params.getString("title", ""),
                                        params.getString("body"),
                                        params.getBundle("data"),
                                        message_reply_final);
                                    }
                                },
                                new Api.CallBackRequest() {
                                    @Override
                                    public void response(JSONObject message_req) {
                                        new NotifCar(context).cancelNotification(conversation_id);
                                    }
                                });
                        } else {
                            new NotifCar(context).cancelNotification(conversation_id);
                        }
                    } else {
                        new NotifCar(context).cancelNotification(conversation_id);
                    }
                } else {
                    new NotifCar(context).cancelNotification(conversation_id);
                }
            }
        }
    }

}