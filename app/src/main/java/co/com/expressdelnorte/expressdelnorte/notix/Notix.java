package co.com.expressdelnorte.expressdelnorte.notix;


import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import co.com.expressdelnorte.expressdelnorte.VolleySingleton;


public class Notix {

    private static final String SOCKET_USERNAME = "user1";
    private static final String SOCKET_PASSWORD = "123456";

    private Socket mSocket;
    private ArrayList<Message> messages;
    private String django_id;
    private String username;
    private String type;
    private static Notix instance;
    private onNotixListener notixListener;
    private AuthListener authListener;

    private Notix() {
        initSocket();
    }

    public static Notix getInstance() {
        if (instance == null) {
            instance = new Notix();
        }

        return instance;
    }

    public void setNotixListener(onNotixListener notixListener) {
        this.notixListener = notixListener;
    }

    public void setAuthListener(AuthListener authListener) {
        this.authListener = authListener;
    }

    public boolean isConnected() {
        return mSocket.connected();
    }

    private void initSocket() {
        messages = new ArrayList<>();
        try {
            mSocket = IO.socket("http://104.236.33.228:4000");
            mSocket.on("identify", onIdentify);
            mSocket.on("success-login", onSuccesLogin);
            mSocket.on("error-login", onErrorLogin);
            mSocket.on("web-success-login", onWebSuccessLogin);
            mSocket.on("web-error-login", onWebErrorLogin);
            mSocket.on("get-data", onGetData);
            mSocket.on("notix", onNotix);
            mSocket.on("visited", onVisited);
            mSocket.on("numero-pedido", onNumeroPedido);
            mSocket.on("asignar-pedido", onAsignarPedido);
            mSocket.on("modificar-pedido", onAsignarPedido);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void setUser(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        django_id = manager.getDeviceId();
        username = manager.getDeviceId();
        type = "Motorizado";
    }

    boolean hasUser() {
        return !(username == null || type == null || django_id == null);
    }

    public void getMessages() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("cell_id", django_id);
            emitMessage("get-messages", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void visitMessage(JSONObject message) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("message_id", message.get("message_id"));
            msg.put("emit", message.get("emit"));
            emitMessage("visit-message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void visitMessages(ArrayList<String> messages) {
        JSONArray messages_id = new JSONArray(messages);
        Log.i("visit", messages_id.toString());
        try {
            JSONObject msg = new JSONObject();
            msg.put("webuser", username);
            msg.put("type", type);
            msg.put("messages_id", messages_id);
            emitMessage("visited", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getNumeroPedido() {
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", django_id);
            emitMessage("numero-pedido", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(JSONObject message) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("message_id", message.get("message_id"));
            emitMessage("delete-message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void webLogin(String webPassword) {
        try {
            JSONObject message = new JSONObject();
            message.put("django_id", django_id);
            message.put("usertype", "CELL");
            message.put("web_password", webPassword);
            message.put("password", SOCKET_PASSWORD);
            message.put("username", SOCKET_USERNAME);
            mSocket.emit("web-login", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "WEB");
            msg.put("webuser", username);
            msg.put("password", SOCKET_PASSWORD);
            msg.put("username", SOCKET_USERNAME);
            mSocket.emit("login", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendQR(String qr) {
        try {
            JSONObject message = new JSONObject();
            message.put("web_id", qr);
            message.put("cell_id", django_id);
            mSocket.emit("ionic-qr", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getData() {
        Log.i("getData", "init");
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", django_id);
            Log.i("getData", message.toString());
            mSocket.emit("get-data", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessages() {
        Log.i("sendMessages", messages.size() + "");
        if (messages == null) {
            messages = new ArrayList<>();
            return;
        }
        for (Message message : messages) {
            try {
                JSONObject msg = message.getMessage();
                msg.put("django_id", django_id);
                msg.put("usertype", "CELL");
                mSocket.emit(message.getEmit(), msg);
                Log.i("sendMessage", message.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        messages = new ArrayList<>();
    }

    public void sendGPS(double lat, double lng) {
        try {
            JSONObject message = new JSONObject();
            message.put("lat", lat);
            message.put("lng", lng);
            emitMessage("send-gps", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitMessage(String emit, JSONObject message) {
        messages.add(new Message(emit, message));
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "CELL");
            mSocket.emit("identify", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onIdentify = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            if (!message.has("ID")) {
                login();
            } else {
                sendMessages();
            }
        }
    };

    private Emitter.Listener onNotix = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[0];
                String id = message.getString("_id");
                JSONObject data = message.getJSONObject("data");
                data.put("_id", id);
                if (data.has("data")) {
                    if (notixListener != null) {
                        notixListener.onNotix(data);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onVisited = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("visited", "tiggered");
            try {
                JSONObject message = (JSONObject) args[0];
                notixListener.onVisited(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onSuccesLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendMessages();
        }
    };

    private Emitter.Listener onErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("Error", "Hubo un error en el servidor");
        }
    };

    private Emitter.Listener onWebSuccessLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("weblogin", "success");
            authListener.ononWebSuccessLogin();
        }
    };

    private Emitter.Listener onWebErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("weblogin", "error");
            authListener.onWebErrorLogin();
        }
    };

    private Emitter.Listener onGetData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            Log.i("onGetData", data.toString());
            authListener.onGetData(data);
        }
    };

    private Emitter.Listener onNumeroPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            notixListener.onNumeroPedido(message);
        }
    };

    private Emitter.Listener onAsignarPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            notixListener.onAsignarPedido(message);
        }
    };
}
