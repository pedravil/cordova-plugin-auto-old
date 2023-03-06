package com.bool.auto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.firebase.messaging.RemoteMessage;

import org.apache.cordova.firebase.FirebasePluginMessageReceiver;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class BoolFirebase extends FirebasePluginMessageReceiver {

    private NotifCar notifCar;

    public BoolFirebase(Context context) {
        super(context);

        MessageReadReceiver.register(context);
        MessageReplyReceiver.register(context);

        this.notifCar = new NotifCar(context);
    }

    @Override
    public boolean onMessageReceived(RemoteMessage remoteMessage) {
        if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("inBackground", true)) {
            return false;
        }

        try {
            Map<String, String> data = remoteMessage.getData();
            JSONObject js = new JSONObject(data.get("data"));
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            InputStream in = new java.net.URL("https://api.tagncar.com/data/" + js.getString("u_avatar") +"-200x200").openStream();
            Bitmap u_avatar = getCroppedBitmap(BitmapFactory.decodeStream(in));

            Bundle bundle = new Bundle();
            for (String key : data.keySet()) {
                bundle.putString(key, data.get(key));
            }

            int icon_id = context.getResources().getIdentifier("fcm_push_icon", "drawable", context.getApplicationContext().getPackageName());

            notifCar.sendNotification(
                    js.getString("u_name"),
                    icon_id,
                    u_avatar,
                    js.getString("message"),
                    js.getInt("conversation_id"),
                    df.parse(js.getString("created_date")).getTime(),
                    js.getString("title"),
                    js.getString("body"),
                    bundle, new String[]{});

        } catch (JSONException | ParseException | IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle((float) (bitmap.getWidth() / 2.0), (float) (bitmap.getHeight() / 2.0), (float) (bitmap.getWidth() / 2.0), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
