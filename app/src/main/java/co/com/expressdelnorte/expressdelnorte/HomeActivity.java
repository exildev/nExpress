package co.com.expressdelnorte.expressdelnorte;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    private Notix notix;
    private String nPedidos;
    private InfiniteListView infiniteListView;
    private GoogleApiClient mGoogleClient;

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
                    holder.button_expand = (CardView) convertView.findViewById(R.id.button_expand);
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
                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Pedido pedido = NotixFactory.notifications.get(position);

                if (pedido != null) {
                    holder.cliente.setText(pedido.getClienteNombre());
                    holder.direccionSubtitle.setText(pedido.getDireccion());
                    holder.direccion.setText(pedido.getDireccion());
                    holder.telefono.setText(pedido.getTelefono());
                    holder.celular.setText(pedido.getCelular());
                    holder.tienda.setText(pedido.getTienda());
                    holder.direccionTienda.setText(pedido.getDireccionTienda());
                    holder.total.setText(pedido.getTotal());
                    if (pedido.getEstado().equals("asignado")) {
                        holder.positiveButton.setText(R.string.recogido);
                        holder.negativeButton.setText(R.string.cancelar);
                    } else {
                        holder.dropImage.setVisibility(View.INVISIBLE);
                    }
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
    }

    private Pedido formatPedido(JSONObject message) throws JSONException {
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
        String tipo = String.valueOf(message.getInt("tipo"));
        String estado = message.getString("estado");
        int id = message.getInt("id");

        Pedido pedido = new Pedido(total, direccionTienda, tiendaNombre, celular, telefono, apellidos, nombre, direccion);
        pedido.setId(id);
        pedido.setTipo(tipo);
        pedido.setEstado(estado);
        return pedido;
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

    protected void createLocationRequest() {
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
            Snackbar.make(findViewById(R.id.fab), "Has entregado " + nPedidos + " pedidos esta quincena", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAsignarPedido(JSONObject data) {
        final JSONObject message = data;
        Log.i("pedido", data.toString());
        HomeActivity.this.runOnUiThread(new Runnable() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            Log.i("GPS", "" + (resultCode == Activity.RESULT_OK));
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    static class ViewHolder {
        CardView button_expand;
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
    }
}
