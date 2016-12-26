package co.com.expressdelnorte.expressdelnorte;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.expressdelnorte.expressdelnorte.models.Pedido;
import co.com.expressdelnorte.expressdelnorte.notix.Notix;
import co.com.expressdelnorte.expressdelnorte.notix.NotixFactory;
import co.com.expressdelnorte.expressdelnorte.notix.onNotixListener;

public class NotificationService extends Service implements onNotixListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Notix notix;
    private GoogleApiClient mGoogleClient;

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
        notix.getTecnoSoat();
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleClient.connect();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("NotificationService", "onStartCommand");
        notix.setNotixListener(this);
        notix.getMessages();
        notix.getTecnoSoat();
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
    public void onTecnoSoat(JSONObject data) {
        NotixFactory.buildNotifSoatTecno(this, data);
    }

    @Override
    public void onCancelations(JSONArray data) {

    }

    @Override
    public void onRequestGPS(JSONObject data) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
        notix.responseGPS(lastLocation.getLatitude(), lastLocation.getLongitude(), data);
    }

    @Override
    public void onDelete() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
