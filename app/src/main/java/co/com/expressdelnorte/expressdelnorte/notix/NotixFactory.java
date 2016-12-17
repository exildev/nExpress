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

import java.util.ArrayList;

import co.com.expressdelnorte.expressdelnorte.HomeActivity;
import co.com.expressdelnorte.expressdelnorte.R;
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

    public static void buildNotification(Context context) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_motorcycle_24dp)
                .setLargeIcon(largeIcon)
                .setContentTitle("Express del norte")
                .setContentText("Tienes domicilios pendientes por entregar");

        mBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mBuilder.setVibrate(new long[]{0, 800, 100, 800});
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent resultIntent = new Intent(context, HomeActivity.class);

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
        mNotificationManager.notify(12, mBuilder.build());
    }
}
