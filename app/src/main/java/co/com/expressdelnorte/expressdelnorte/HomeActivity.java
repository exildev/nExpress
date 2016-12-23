package co.com.expressdelnorte.expressdelnorte;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import co.com.expressdelnorte.expressdelnorte.models.Pedido;
import co.com.expressdelnorte.expressdelnorte.notix.Notix;
import co.com.expressdelnorte.expressdelnorte.notix.NotixFactory;
import co.com.expressdelnorte.expressdelnorte.notix.onNotixListener;

public class HomeActivity extends AppCompatActivity implements onNotixListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    private static final int REQUEST_LOCATION_SETTINGS = 12;
    private static final int REQUEST_PCITURE = 6;

    private Notix notix;
    private String nPedidos;
    private InfiniteListView infiniteListView;
    private GoogleApiClient mGoogleClient;
    private JSONObject configurations;
    private JSONArray cancelations;

    private Uri mPhotoUri;
    private Pedido delivering = null;
    private boolean isCancelling;
    private int reasonSelected = -1;

    private MaterialDialog loading;

    static Pedido formatPedido(JSONObject message) throws JSONException {
        JSONObject cliente = ((JSONArray) message.get("cliente")).getJSONObject(0);
        JSONObject tienda = ((JSONArray) message.get("tienda")).getJSONObject(0);

        String nombre = cliente.getString("nombre");
        String apellidos = cliente.getString("apellidos");
        String direccion = cliente.getString("direccion");
        String telefono = cliente.getString("fijo");
        String celular = cliente.getString("celular");
        String tiendaNombre = tienda.getString("referencia");
        String direccionTienda = tienda.getString("direccion");
        String total = NumberFormat.getCurrencyInstance().format(message.get("total"));
        String message_id = message.getString("message_id");
        int tipo = message.getInt("tipo");
        String estado = "asignado";
        if (message.has("estado")) {
            estado = message.getString("estado");
        }

        int id = message.getInt("id");

        Pedido pedido = new Pedido(total, direccionTienda, tiendaNombre, celular, telefono, apellidos, nombre, direccion);
        pedido.setId(id);
        pedido.setTipo(tipo);
        pedido.setEstado(estado);
        pedido.setMessage_id(message_id);
        return pedido;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Has entregado " + nPedidos + " pedidos esta quincena", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setInfiniteList();

        notix = NotixFactory.buildNotix(this);
        notix.setNotixListener(this);
        notix.getNumeroPedido();
        notix.getData();
        notix.getTecnoSoat();
        notix.getCancelations();
        notix.getMessages();

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleClient.connect();
    }

    synchronized void setInfiniteList() {
        infiniteListView = (InfiniteListView) findViewById(R.id.content_home);

        final InfiniteListAdapter adapter = new InfiniteListAdapter<Pedido>(this, R.layout.pedido, NotixFactory.notifications) {
            @Override
            public void onNewLoadRequired() {
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                notix.getMessages();
            }

            @Override
            public void onItemClick(int i) {
            }

            @Override
            public void onItemLongClick(int i) {
            }

            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.pedido, parent, false);

                    holder = new ViewHolder();
                    holder.button_expand = convertView.findViewById(R.id.button_expand);
                    holder.cliente = (TextView) convertView.findViewById(R.id.cliente);
                    holder.direccionSubtitle = (TextView) convertView.findViewById(R.id.direccion_subtitle);
                    holder.direccion = (TextView) convertView.findViewById(R.id.direccion);
                    holder.telefono = (TextView) convertView.findViewById(R.id.telefono);
                    holder.celular = (TextView) convertView.findViewById(R.id.celular);
                    holder.tienda = (TextView) convertView.findViewById(R.id.tienda);
                    holder.direccionTienda = (TextView) convertView.findViewById(R.id.direccion_tienda);
                    holder.total = (TextView) convertView.findViewById(R.id.total);
                    holder.negativeButton = (Button) convertView.findViewById(R.id.negative_button);
                    holder.positiveButton = (Button) convertView.findViewById(R.id.positive_button);
                    holder.dropImage = (ImageView) convertView.findViewById(R.id.drop_image);
                    holder.total_container = (LinearLayout) convertView.findViewById(R.id.total_container);
                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final Pedido pedido = NotixFactory.notifications.get(position);

                if (pedido != null) {

                    int tirilla = 0;
                    int cerrar = 0;
                    int cancelar = 0;
                    try {
                        tirilla = configurations.getInt("tirilla");
                        cerrar = configurations.getInt("cerrar");
                        cancelar = configurations.getInt("cancelar");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    holder.cliente.setText(pedido.getClienteNombre());
                    holder.direccionSubtitle.setText(pedido.getDireccion());
                    holder.direccion.setText(pedido.getDireccion());
                    holder.telefono.setText(pedido.getTelefono());
                    holder.celular.setText(pedido.getCelular());
                    holder.tienda.setText(pedido.getTienda());
                    holder.direccionTienda.setText(pedido.getDireccionTienda());

                    switch (tirilla) {
                        case 1:
                            holder.total.setText(pedido.getTotal());
                            holder.total_container.setVisibility(View.VISIBLE);
                            break;
                        default:
                            holder.total_container.setVisibility(View.GONE);
                            break;
                    }

                    final int finalCancelar = cancelar;
                    holder.negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (finalCancelar) {
                                case 1:
                                    delivering = pedido;
                                    isCancelling = true;
                                    mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            new ContentValues());
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                                    startActivityForResult(intent, REQUEST_PCITURE);
                                    break;
                                default:
                                    cancelar(pedido, null);
                                    break;
                            }
                        }
                    });
                    switch (pedido.getEstado()) {
                        case "asignado":
                        case "aceptado":
                            holder.positiveButton.setText(R.string.recogido);
                            holder.negativeButton.setText(R.string.cancelar);
                            holder.positiveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    notix.recojer(pedido);
                                }
                            });
                            break;
                        case "recogido":
                            holder.positiveButton.setText(R.string.entregado);
                            holder.negativeButton.setText(R.string.cancelar);
                            final int finalCerrar = cerrar;
                            holder.positiveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    switch (finalCerrar) {
                                        case 1:
                                            delivering = pedido;
                                            isCancelling = false;
                                            mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                    new ContentValues());
                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                                            startActivityForResult(intent, REQUEST_PCITURE);
                                            break;
                                        default:
                                            loading = new MaterialDialog.Builder(HomeActivity.this)
                                                    .title(R.string.confirm_delivery)
                                                    .content(R.string.plase_wait)
                                                    .progress(true, 0)
                                                    .show();
                                            notix.entregar(pedido);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            holder.positiveButton.setText(R.string.aceptar);
                            holder.negativeButton.setText(R.string.rechazar);
                            break;
                    }
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
    }

    public void action(View view) {
        ViewGroup row = (ViewGroup) view.getParent();
        final RelativeLayout container = (RelativeLayout) row.findViewById(R.id.container);
        TextView subtitle = (TextView) row.findViewById(R.id.direccion_subtitle);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int close_height = (int) (72 * scale + 0.5f);
        final ImageView imageDrop = (ImageView) row.findViewById(R.id.drop_image);

        if (container.getHeight() == close_height) {
            subtitle.setVisibility(View.GONE);
            expand(container, imageDrop);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            collapse(container, imageDrop);
        }
    }

    private void expand(final View container, final ImageView icon) {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        final int close_height = (int) (72 * scale + 0.5f);
        container.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = container.getMeasuredHeight() - close_height;

        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_icon);

        icon.startAnimation(rotate);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                container.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (close_height + (int) (targetHeight * interpolatedTime));
                container.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(200);
        container.startAnimation(a);
    }

    private void collapse(final View container, final ImageView icon) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        final int actualHeight = container.getMeasuredHeight();
        int targetHeight = (int) (72 * scale + 0.5f);
        ValueAnimator va = ValueAnimator.ofInt(actualHeight, targetHeight);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_icon_back);

        icon.startAnimation(rotate);

        va.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                container.getLayoutParams().height = value;
                container.getLayoutParams().width = value;
                container.requestLayout();
            }
        });
        va.start();
    }

    private void validPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                createLocationRequest();
            }
        } else {
            createLocationRequest();
        }
    }

    private void send(MaterialDialog dialog) {

        View view = dialog.getCustomView();

        assert view != null;
        TextView password = (TextView) view.findViewById(R.id.password);
        TextView password2 = (TextView) view.findViewById(R.id.password2);

        if (password.getText().toString().equals(password2.getText().toString()) && !password.getText().toString().equals("")) {
            dialog.dismiss();
            notix.setPassword(password.getText().toString());
        } else if (password.getText().toString().equals("")) {
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.password_container);
            til.setErrorEnabled(true);
            til.setError(getString(R.string.empty_field));
        } else {
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.password_container);
            til.setErrorEnabled(false);
            til = (TextInputLayout) view.findViewById(R.id.password2_container);
            til.setErrorEnabled(true);
            til.setError(getString(R.string.password_match));
        }
    }

    private void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                        builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.i("settings", "si tal");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(HomeActivity.this, REQUEST_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.i("settings", "no tal");
                        break;
                }
            }
        });
    }

    private void cancelar(final Pedido pedido, final String foto) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.cancel_delivery)
                .customView(R.layout.cancelar, true)
                .positiveText(R.string.aceptar)
                .negativeText(R.string.cancelar)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View custom = dialog.getCustomView();
                        assert custom != null;
                        TextInputEditText observacion = (TextInputEditText) custom.findViewById(R.id.observacion);
                        String obs = observacion.getText().toString();
                        if (reasonSelected != -1 && !obs.equals("")) {
                            if (foto == null) {
                                notix.cancelar(pedido, reasonSelected, obs);
                            } else {
                                notix.cancelar(pedido, reasonSelected, obs, foto, HomeActivity.this);
                            }
                            dialog.dismiss();
                            loading = new MaterialDialog.Builder(HomeActivity.this)
                                    .title(R.string.confirm_delivery)
                                    .content(R.string.plase_wait)
                                    .progress(true, 0)
                                    .show();
                        } else if (reasonSelected == -1) {
                            RadioGroup radioGroup = (RadioGroup) custom.findViewById(R.id.radio_group);
                            RadioButton last = (RadioButton) radioGroup.getChildAt(radioGroup.getChildCount() - 1);
                            last.setError(getString(R.string.choose_reason));
                        } else {
                            observacion.setError(getString(R.string.empty_field));
                        }
                    }
                })
                .show();
        View custom = dialog.getCustomView();
        assert custom != null;
        RadioGroup radioGroup = (RadioGroup) custom.findViewById(R.id.radio_group);
        LayoutInflater inflater = LayoutInflater.from(radioGroup.getContext());
        for (int i = 0; i < cancelations.length(); i++) {
            try {
                final JSONObject item = cancelations.getJSONObject(i);
                RadioButton radio = (RadioButton) inflater.inflate(R.layout.motivo, radioGroup, false);
                radio.setText(item.getString("nombre"));
                radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        try {
                            if (b) {
                                reasonSelected = item.getInt("id");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                radioGroup.addView(radio);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPassword(MenuItem item) {
        new MaterialDialog.Builder(this)
                .title(R.string.set_password)
                .customView(R.layout.set_passoword, true)
                .positiveText("Guardar")
                .negativeText("Cerrar")
                .autoDismiss(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        send(dialog);
                    }
                })
                .show();
    }

    public void signOut(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove("password").apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        Log.i("HomeActivity", "onPause");
        NotificationService.startService(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        notix.setNotixListener(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i("HomeActivity", "onDestroy");
        NotificationService.startService(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION || requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                validPermissions();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.gps_permissions_message)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                validPermissions();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onNotix(JSONObject data) {

    }

    @Override
    public void onNumeroPedido(JSONObject data) {
        try {
            notix.deleteMessage(data);
            nPedidos = data.get("numero_pedidos").toString();
            Log.i("pedido", nPedidos);
            Snackbar.make(findViewById(R.id.fab), "Has entregado " + nPedidos + " pedidos esta quincena", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("pedido", nPedidos);
        }
    }

    @Override
    public void onSetPassword(JSONObject data) {
        notix.deleteMessage(data);
        int status = 0;
        try {
            status = data.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (status == 200 || status == 201) {
            Snackbar.make(findViewById(R.id.fab), R.string.password_success, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        Log.i("pedido", data.toString());
    }

    @Override
    public void onAsignarPedido(JSONObject data) {
        final JSONObject message = data;
        Log.i("pedido", data.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    notix.visitMessage(message);
                    Pedido pedido = formatPedido(message);
                    int pedidoIndex = NotixFactory.notifications.indexOf(pedido);
                    if (pedidoIndex < 0) {
                        infiniteListView.addNewItem(pedido);
                        GPSService.startService(HomeActivity.this);
                    } else {
                        NotixFactory.notifications.set(pedidoIndex, pedido);
                        setInfiniteList();
                    }
                    Log.i("pedido", message.toString());

                    findViewById(R.id.no_items).setVisibility(View.GONE);
                    infiniteListView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onVisited(JSONObject data) {

    }

    @Override
    public void onGetData(final JSONObject data) {
        configurations = data;
    }

    @Override
    public void onTecnoSoat(JSONObject data) {
        NotixFactory.buildNotifSoatTecno(this, data);
    }

    @Override
    public void onCancelations(JSONArray data) {
        cancelations = data;
    }

    @Override
    public void onRequestGPS(JSONObject data) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions();
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
        notix.responseGPS(lastLocation.getLatitude(), lastLocation.getLongitude(), data);
    }

    @Override
    public void onDelete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setInfiniteList();
                loading.dismiss();
                if (NotixFactory.notifications.size() < 1) {
                    findViewById(R.id.no_items).setVisibility(View.VISIBLE);
                    infiniteListView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            Log.i("GPS", "" + (resultCode == RESULT_OK));
            switch (resultCode) {
                case Activity.RESULT_OK:
                    break;
                default:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.activate_gps_message)
                            .setCancelable(false)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    createLocationRequest();
                                }
                            })
                            .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    break;
            }
        } else if (requestCode == REQUEST_PCITURE && resultCode == RESULT_OK) {
            if (isCancelling) {
                cancelar(delivering, getRealPathFromURI(mPhotoUri));
            } else {
                notix.entregar(delivering, getRealPathFromURI(mPhotoUri), this);
                loading = new MaterialDialog.Builder(this)
                        .title(R.string.confirm_delivery)
                        .content(R.string.plase_wait)
                        .progress(true, 0)
                        .show();
            }
            Log.i("isCancelling", isCancelling + "");
            Log.i("picture", getRealPathFromURI(mPhotoUri));
            Log.i("pedido", delivering.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    static class ViewHolder {
        View button_expand;
        TextView cliente;
        TextView direccionSubtitle;
        TextView direccion;
        TextView telefono;
        TextView celular;
        TextView tienda;
        TextView direccionTienda;
        TextView total;
        Button negativeButton;
        Button positiveButton;
        ImageView dropImage;
        LinearLayout total_container;
    }
}
