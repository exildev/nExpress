package com.example.android.express;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private Socket mSocket;
    private String imei;
    private ArrayList<Message> messages;
    private ArrayList<Pedido> pedidos;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private boolean GPSStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        messages = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeData();
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RVAdapter(pedidos);
        mRecyclerView.setAdapter(mAdapter);

        setImei();

        try {
            mSocket = IO.socket("http://104.236.33.228:4000");
            mSocket.on("identify", onIdentify);
            mSocket.on("success-login", onSuccesLogin);
            mSocket.on("error-login", onErrorLogin);
            mSocket.on("numero-pedido", onNumeroPedido);
            mSocket.on("asignar-pedido", onAsignarPedido);
            mSocket.on("modificar-pedido", onAsignarPedido);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            JSONObject msg = new JSONObject();
            msg.put("cell_id", imei);
            //emitMessage("get-messages", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showPedidosNumber(View view) {
        /*if(!GPSStarted){
            GPSService.startService(this, imei);
        }else{
            GPSService.stopService(this);
        }
        GPSStarted = !GPSStarted;
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", imei);
            emitMessage("numero-pedido", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        initMap();
    }

    private void initMap(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void initForm(){
        Intent intent = new Intent(this, FormActivity.class);
        startActivity(intent);
    }

    public void showData(View view) {
        ViewGroup row = (ViewGroup) view.getParent();
        /*final RelativeLayout container = (RelativeLayout) row.findViewById(R.id.container);
        TextView subtitle = (TextView) row.findViewById(R.id.direccion_subtitle);
        TextView title = (TextView) row.findViewById(R.id.cliente);
        CardView icon = (CardView) row.findViewById(R.id.pedido_icon_card);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int close_height = (int) (72 * scale + 0.5f);*/
        final ImageView imageDrop = (ImageView) row.findViewById(R.id.drop_image);

        /*if (container.getHeight() == close_height) {
            subtitle.setVisibility(View.GONE);
            title.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            icon.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            expand(container, imageDrop);
        }else {
            title.setTextColor(Color.parseColor("#000000"));
            icon.setCardBackgroundColor(Color.parseColor("#757575"));
            subtitle.setVisibility(View.VISIBLE);
            collapse(container, imageDrop);
        }*/
        if(imageDrop.getVisibility() != View.VISIBLE){
            initForm();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageDrop.setVisibility(View.VISIBLE);
                }
            }, 500);
        }else{
            showSnackbar("Este equipo ya fue revisado");
        }
    }

    private void expand(final View container, final ImageView icon) {
        container.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = container.getMeasuredHeight();

        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_icon);

        icon.startAnimation(rotate);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                container.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
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

    private void collapse(final View container, final ImageView icon){
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

    private void emitMessage(String emit, JSONObject message) {
        messages.add(new Message(emit, message));
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", imei);
            msg.put("usertype", "CELL");
            mSocket.emit("identify", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", imei);
            msg.put("usertype", "CELL");
            msg.put("password", MainActivity.SOCKET_PASSWORD);
            msg.put("username", MainActivity.SOCKET_USERNAME);
            mSocket.emit("login", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
            return;
        }
        for (Message message : messages) {
            try {
                JSONObject msg = message.getMessage();
                msg.put("django_id", imei);
                msg.put("usertype", "CELL");
                mSocket.emit(message.getEmit(), msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        messages = new ArrayList<>();
    }

    private void initializeData() {
        pedidos = new ArrayList<>();

        for (int i = 0; i< 10; i++){
            Pedido pedido = new Pedido("","","","","","","Maquina "+ i,"Descripcion corta");
            if(i < 4){
                pedido.checked = true;
            }
            pedido.setId(i);
            pedido.setTipo("");
            pedido.setEstado("asignado");
            pedidos.add(pedido);
        }

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

        Pedido pedido = new Pedido(total,direccionTienda,tiendaNombre,celular,telefono,apellidos,nombre,direccion);
        pedido.setId(id);
        pedido.setTipo(tipo);
        pedido.setEstado(estado);
        return pedido;
    }

    private void addData(Pedido pedido){
        pedidos.add(pedido);
        mAdapter = new RVAdapter(pedidos);
        mRecyclerView.swapAdapter(mAdapter, false);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.app_bar), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public HomeActivity getActivity() {
        return this;
    }

    private void setImei() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = manager.getDeviceId();
    }

    private Emitter.Listener onIdentify = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (message.has("ID")) {
                        login();
                    } else {
                        sendMessages();
                    }
                }
            });
        }
    };

    private Emitter.Listener onSuccesLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendMessages();
                }
            });
        }
    };

    private Emitter.Listener onErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSnackbar("Error al intentar iniciar sesión");
                }
            });
        }
    };

    private Emitter.Listener onNumeroPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject msg = new JSONObject();
                        msg.put("message_id", message.get("message_id"));
                        emitMessage("delete-message", msg);
                        showSnackbar("Has entregado " + message.get("numero_pedidos") + " pedidos esta quincena");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onAsignarPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject msg = new JSONObject();
                        msg.put("message_id", message.get("message_id"));
                        msg.put("emit", message.get("emit"));
                        emitMessage("visit-message", msg);

                        Pedido pedido = formatPedido(message);
                        int pedidoIndex = pedidos.indexOf(pedido);
                        if (pedidoIndex < 0){
                            addData(pedido);
                        }else{
                            pedidos.set(pedidoIndex, pedido);
                            mAdapter = new RVAdapter(pedidos);
                            mRecyclerView.swapAdapter(mAdapter, false);
                        }

                        Log.i("pedido", message.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

}
