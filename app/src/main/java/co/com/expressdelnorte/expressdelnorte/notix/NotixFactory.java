package co.com.expressdelnorte.expressdelnorte.notix;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.com.expressdelnorte.expressdelnorte.models.Pedido;


public class NotixFactory {

    public static ArrayList<Pedido> notifications = new ArrayList<>();

    public static Notix buildNotix(Context context) {
        Notix notix = Notix.getInstance();
        if (!notix.hasUser()) {
            notix.setUser(context);
        }
        return notix;
    }

    public static void buildNotification(Context context, JSONObject notification) {
        /*notifications.add(notification);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_logo);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_logo_outline)
                .setLargeIcon(largeIcon)
                .setContentTitle("Piscix")
                .setContentText(notifications.size() + " notificaciones sin leer");
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("Notificaciones Piscix");
        for (int i = 0; i < 5 && i < notifications.size(); i++) {
            JSONObject event = notifications.get(i);
            try {
                Log.i("html", event.getString("html"));
                inboxStyle.addLine(event.getString("html"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (notifications.size() > 5) {
            inboxStyle.setSummaryText("+" + (notifications.size() - 5) + " mas");
        }

        mBuilder.setStyle(inboxStyle);
        mBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mBuilder.setVibrate(new long[]{0, 800, 100, 800});
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent resultIntent = new Intent(context, NotificationActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(12, mBuilder.build());*/
    }
}
