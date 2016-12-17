package co.com.expressdelnorte.expressdelnorte;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class StartUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationService.startService(context);
    }
}
