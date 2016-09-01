package com.example.android.express;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.github.nkzawa.socketio.client.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GPSIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    Thread thread;
    private static boolean isStarted = false;
    private static final String START_GPS = "com.example.android.express.action.START_GPS";
    private static final String STOP_GPS = "com.example.android.express.action.STOP_GPS";

    public GPSIntentService() {
        super("GPSIntentService");
    }


    public static void startGPS(Context context, Socket socket) {
        Intent intent = new Intent(context, GPSIntentService.class);
        intent.setAction(START_GPS);
        context.startService(intent);
    }

    public static void stopGPS(Context context) {
        Intent intent = new Intent(context, GPSIntentService.class);
        intent.setAction(STOP_GPS);
        context.startService(intent);
    }

    private void handleActionGPS() {
        isStarted = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (GPSIntentService.isStarted) {
                    new GPSTask().execute((Void[]) null);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // Restore interrupt status.
                        Thread.currentThread().interrupt();
                    }
                }
                Log.i("GPS service", "Thread died");
            }
        });
        thread.start();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (START_GPS.equals(action) && !isStarted) {
                handleActionGPS();
            } else if (STOP_GPS.equals(action)) {
                isStarted = false;
            } else {
                Log.i("GPS service", "im running");
            }
        }
    }

    private class GPSTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            requestGPS();
            return null;
        }

        protected void onPostExecute(Void param) {
            //Print Toast or open dialog
        }

        private void requestGPS() {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i("GPS", location.getLatitude() + "");
                    Log.i("GPS", location.getLongitude() + "");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };
            if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("GPS", "NO PERMMISSIONS");
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }
}
