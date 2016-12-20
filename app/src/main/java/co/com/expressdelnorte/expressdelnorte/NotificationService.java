package co.com.expressdelnorte.expressdelnorte;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import co.com.expressdelnorte.expressdelnorte.models.Pedido;
import co.com.expressdelnorte.expressdelnorte.notix.Notix;
import co.com.expressdelnorte.expressdelnorte.notix.NotixFactory;
import co.com.expressdelnorte.expressdelnorte.notix.onNotixListener;

public class NotificationService extends Service implements onNotixListener {

    Notix notix;

    public NotificationService() {
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        Log.i("NotificationService", "OnCreate");
        notix = NotixFactory.buildNotix(this);
        notix.setNotixListener(this);
        notix.getMessages();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("NotificationService", "onStartCommand");
        notix.getMessages();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onNotix(JSONObject data) {

    }

    @Override
    public void onNumeroPedido(JSONObject data) {

    }

    @Override
    public void onSetPassword(JSONObject data) {

    }

    @Override
    public void onAsignarPedido(JSONObject data) {
        NotixFactory.buildNotification(this);
        notix.visitMessage(data);
        try {
            Pedido pedido = HomeActivity.formatPedido(data);
            int pedidoIndex = NotixFactory.notifications.indexOf(pedido);
            if (pedidoIndex < 0) {
                NotixFactory.notifications.add(pedido);
                GPSService.startService(this);
            } else {
                NotixFactory.notifications.set(pedidoIndex, pedido);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVisited(JSONObject data) {

    }

    @Override
    public void onGetData(JSONObject data) {

    }

    @Override
    public void onDelete() {

    }
}
