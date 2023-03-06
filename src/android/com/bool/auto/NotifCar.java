package com.bool.auto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import org.apache.cordova.firebase.OnNotificationOpenReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotifCar {

    public static final String REPLY_ACTION = "com.tnc.app.ACTION_MESSAGE_REPLY";
    public static final String READ_ACTION = "com.tnc.app.ACTION_MESSAGE_READ";
    public static final String EXTRA_REPLY = "extra_remote_reply";
    public static final String CONVERSATION_ID = "conversation_id";

    protected Context context;

    public NotifCar(Context context) {
        this.context = context;
    }

    /**
     * Send Notification
     */
    public void sendNotification(String u_name, int small_icon, Bitmap u_avatar, String message, int conversation_id, long time, String title, String body, Bundle data, String message_reply[]) {

        //SI REPONSE
        Bundle params = new Bundle();
        params.putString("u_name", u_name);
        params.putInt("small_icon", small_icon);
        params.putParcelable("u_avatar", u_avatar);
        params.putString("message", message);
        params.putInt("conversation_id", conversation_id);
        params.putLong("time", time);
        params.putString("title", title);
        params.putString("body", body);
        params.putBundle("data", data);
        params.putStringArray("message_reply",message_reply);

        //Envoie la Réponse sur L'intent
        PendingIntent replyIntent = PendingIntent.getBroadcast(context,
                conversation_id,
                new Intent()
                        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        .setAction(REPLY_ACTION)
                        .putExtra(CONVERSATION_ID, conversation_id)
                        .putExtras(params),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Envoie de Read sur L'intent
        PendingIntent readIntent = PendingIntent.getBroadcast(context,
                conversation_id,
                new Intent()
                        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        .setAction(READ_ACTION)
                        .putExtra(CONVERSATION_ID, conversation_id),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //La input de réponse
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REPLY)
                .setLabel("Répondre")
                .build();

        //Build an Android N compatible Remote Input enabled action.
        NotificationCompat.Action actionReplyByRemoteInput = (new NotificationCompat.Action.Builder(
                small_icon, "Répondre", replyIntent))
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();

        //On doit créer un channel pour certaine version d'android
        String channel_name = "C1";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channel_description = "Notification par votre plaque d'immatriculation";
            NotificationChannel mChannel = new NotificationChannel(channel_name, channel_description, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotManager != null) {
                mNotManager.createNotificationChannel(mChannel);
            }
        }

        //Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, OnNotificationOpenReceiver.class);
        intent.putExtras(data);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(message_reply.length > 0) {
            List<String> list = Arrays.asList(message_reply);
            Collections.reverse(list);
            message_reply = list.toArray(new String[]{});
        }

        //Message qui va etre lu sur android auto
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConvBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(u_name)
                        .setLatestTimestamp(time)
                        .addMessage(message)
                        .setReplyAction(replyIntent, remoteInput)
                        .setReadPendingIntent(readIntent);

        //Ici je recois sur le teléphone. notification de conversation
        NotificationCompat.Builder builderc = new NotificationCompat.Builder(context, channel_name)
                .setSmallIcon(small_icon)
                .setLargeIcon(u_avatar)
                .setContentText(body)
                .setWhen(time)
                .setColor(Color.parseColor("#476b66"))
                .setColorized(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setGroup("G"+conversation_id)
                .setGroupSummary(true)
                .setRemoteInputHistory(message_reply)
                //.setContentInfo("Content Info")
                //.setSubText("Nouveau message") // fb affiche le nombre de messge nom lu
                .extend(new NotificationCompat.CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setLargeIcon(u_avatar)
                        .setColor(Color.parseColor("#476b66")))
                .addAction(actionReplyByRemoteInput);

        NotificationManagerCompat.from(context).notify(conversation_id, builderc.build());
    }

    public void cancelNotification(int conversation_id) {
        NotificationManagerCompat.from(context).cancel(conversation_id);
    }

}