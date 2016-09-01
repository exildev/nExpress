package com.example.android.express;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    public static final String SOCKET_USERNAME = "user1";
    public static final String SOCKET_PASSWORD = "123456";
    private Socket mSocket;
    private String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteAll(view);
                return true;
            }
        });

        try {
            mSocket = IO.socket("http://104.236.33.228:4000");
            //mSocket.on("get-data", onGetData);
            mSocket.on("web-success-login", onWebSuccessLogin);
            mSocket.on("web-error-login", onWebErrorLogin);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.obtainImei();
    }

    public void clickButton(View view) {
        Button button = (Button) view;
        TextView passwordEditText = (TextView) findViewById(R.id.password);
        String newPassword = passwordEditText.getText().toString() + button.getText().toString();
        passwordEditText.setText(newPassword);
    }

    public void delete(View view) {
        TextView passwordEditText = (TextView) findViewById(R.id.password);
        String oldPassword = passwordEditText.getText().toString();
        if (oldPassword.length() > 0) {
            String newPassword = oldPassword.substring(0, oldPassword.length() - 1);
            passwordEditText.setText(newPassword);
        }
    }

    public void deleteAll(View view) {
        TextView passwordEditText = (TextView) findViewById(R.id.password);
        passwordEditText.setText("");
    }

    public void qrScan(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    public void login(View view) {
        TextView passwordView = (TextView) findViewById(R.id.password);
        final String password = passwordView.getText().toString();
        hideKeyboard();
        bigAvatar();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*try {
                    JSONObject message = new JSONObject();
                    message.put("django_id", imei);
                    message.put("usertype", "CELL");
                    message.put("web_password", password);
                    message.put("password", SOCKET_PASSWORD);
                    message.put("username", SOCKET_USERNAME);
                    mSocket.emit("web-login", message);
                } catch (JSONException e) {
                    showMessage("no se pudo enviar el inicio de sesion");
                    normalAvatar();
                    showKeyboard();
                    e.printStackTrace();
                }*/
                initHome();
            }
        }, 1200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                JSONObject message = new JSONObject();
                try {
                    message.put("web_id", result.getContents());
                    message.put("cell_id", imei);
                    mSocket.emit("ionic-qr", message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setImei();
                    getData();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    imei = "";
                    showMessage("Sin permisos el app no puede funcionar");
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showKeyboard();
        normalAvatar();
        TextView passwordEditText = (TextView) findViewById(R.id.password);
        passwordEditText.setText("");
    }

    private void showKeyboard() {
        LinearLayout keyboard = (LinearLayout) findViewById(R.id.keyboard);
        keyboard.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        keyboard.requestLayout();
    }

    private void hideKeyboard() {
        final LinearLayout keyboard = (LinearLayout) findViewById(R.id.keyboard);

        ValueAnimator va = ValueAnimator.ofInt(keyboard.getHeight(), 0);

        va.setDuration(400);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                keyboard.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                keyboard.requestLayout();
            }
        });
        va.start();
    }

    private void normalAvatar() {

        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        final ImageView foto = (ImageView) findViewById(R.id.foto);
        final CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);

        foto.getLayoutParams().height = (int) (96 * scale + 0.5f);
        foto.getLayoutParams().width = (int) (96 * scale + 0.5f);
        foto.requestLayout();

        progressView.getLayoutParams().height = (int) (106 * scale + 0.5f);
        progressView.getLayoutParams().width = (int) (106 * scale + 0.5f);
        progressView.requestLayout();
        progressView.setVisibility(View.INVISIBLE);
        progressView.stopAnimation();
    }

    private void bigAvatar() {

        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        final ImageView foto = (ImageView) findViewById(R.id.foto);
        final CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);

        progressView.setVisibility(View.VISIBLE);
        progressView.startAnimation();

        ValueAnimator va = ValueAnimator.ofInt(0, (int) (10 * scale + 0.5f));

        va.setDuration(400);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                foto.getLayoutParams().height = foto.getHeight() + value;
                foto.getLayoutParams().width = foto.getWidth() + value;
                foto.requestLayout();

                progressView.getLayoutParams().height = progressView.getHeight() + value;
                progressView.getLayoutParams().width = progressView.getWidth() + value;
                progressView.requestLayout();
            }
        });
        va.start();
    }

    private void obtainImei() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            setImei();
            getData();
        }/*else if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
            showMessage("aqui te voy a pedir permiso :D");
        }*/ else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    private void setImei() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = manager.getDeviceId();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void getData() {
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", imei);
            mSocket.emit("get-data", message);
        } catch (JSONException e) {
            showMessage("Hubo un error al crear el mensaje");
        }
    }

    private void updateData(String newName, String newFoto) {
        TextView name = (TextView) findViewById(R.id.name);
        ImageView foto = (ImageView) findViewById(R.id.foto);
        if (!newFoto.equals("")) {
            new DownloadImageTask(foto)
                    .execute(newFoto);
        }
        name.setText(newName);
    }

    private void initHome(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private MainActivity getActivity() {
        return this;
    }

    private Emitter.Listener onGetData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("data", data.toString());
                    try {

                        String nombre = data.getString("nombre");
                        String appellidos = data.getString("apellidos");
                        String foto = "";
                        if (data.has("foto")){
                            foto = data.getString("foto");
                        }
                        updateData(nombre + " " + appellidos, foto);
                    } catch (JSONException e) {
                        showMessage("No se pudo mostrar el mensaje");
                    }
                }
            });
        }
    };

    private Emitter.Listener onWebSuccessLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initHome();
                }
            });
        }
    };

    private Emitter.Listener onWebErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessage("Error al iniciar sesión");
                    showKeyboard();
                    normalAvatar();
                }
            });
        }
    };

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}


