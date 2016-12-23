package co.com.expressdelnorte.expressdelnorte.notix;


import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import co.com.expressdelnorte.expressdelnorte.models.Pedido;


public class Notix {

    private static final String SOCKET_USERNAME = "user1";
    private static final String SOCKET_PASSWORD = "123456";
    private static Notix instance;
    private Socket mSocket;
    private ArrayList<Message> messages;
    private JSONObject forCancelling;
    private JSONObject forConfirming;
    private String django_id;
    private String username;
    private String type;
    private onNotixListener notixListener;
    private AuthListener authListener;
    private Context context;
    private Emitter.Listener onIdentify = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("onIdentify", "triggered");
            final JSONObject message = (JSONObject) args[0];
            Log.i("onIdentify", message.toString());
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
            Log.i("onSuccesLogin", "triggered");
            sendMessages();
        }
    };
    private Emitter.Listener onErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("onErrorLogin", "triggered");
            Log.i("Error", "Hubo un error en el servidor");
        }
    };
    private Emitter.Listener onWebSuccessLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("weblogin", "success");
            authListener.onWebSuccessLogin();
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
            if (authListener != null) {
                authListener.onGetData(data);
            }
            if (notixListener != null) {
                notixListener.onGetData(data);
            }
        }
    };
    private Emitter.Listener onTecnoSoat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            Log.i("onGetData", data.toString());
            if (notixListener != null) {
                notixListener.onTecnoSoat(data);
            }
        }
    };
    private Emitter.Listener onMotivoCancelar = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONArray data = (JSONArray) args[0];
            Log.i("onMotivoCancelar", data.toString());
            if (notixListener != null) {
                notixListener.onCancelations(data);
            }
        }
    };
    private Emitter.Listener onNumeroPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            notixListener.onNumeroPedido(message);
        }
    };

    private Emitter.Listener onConfirmarPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            deleteMessage(message);
            Pedido pedido = new Pedido();
            try {
                pedido.setId(message.getInt("pedido_id"));
                pedido.setTipo(message.getInt("tipo"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int i = NotixFactory.notifications.indexOf(pedido);
            if (i < 0) {
                return;
            }
            pedido = NotixFactory.notifications.get(i);
            try {
                JSONObject msg = new JSONObject();
                msg.put("message_id", pedido.getMessage_id());
                Log.i("deleting", msg.toString());
                emitMessage("delete-message", msg);
                NotixFactory.notifications.remove(pedido);
                notixListener.onDelete();
                getNumeroPedido();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onSetPassword = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            notixListener.onSetPassword(message);
        }
    };
    private Emitter.Listener onAsignarPedido = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            notixListener.onAsignarPedido(message);
        }
    };
    private Emitter.Listener onRequestGPS = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject message = (JSONObject) args[0];
            notixListener.onRequestGPS(message);
        }
    };

    private Notix() {
        initSocket();
    }

    static Notix getInstance() {
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
            mSocket.on("tecno-soat", onTecnoSoat);
            mSocket.on("motivo-cancelar", onMotivoCancelar);
            mSocket.on("notix", onNotix);
            mSocket.on("visited", onVisited);
            mSocket.on("numero-pedido", onNumeroPedido);
            mSocket.on("set-password", onSetPassword);
            mSocket.on("asignar-pedido", onAsignarPedido);
            mSocket.on("modificar-pedido", onAsignarPedido);
            mSocket.on("confirmar-pedido", onConfirmarPedido);
            mSocket.on("cancelar-pedido", onConfirmarPedido);
            mSocket.on("request-gps", onRequestGPS);
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
            msg.put("usertype", "CELL");
            msg.put("webuser", username);
            msg.put("password", SOCKET_PASSWORD);
            msg.put("username", SOCKET_USERNAME);
            Log.i("login", msg.toString());
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

    public void getTecnoSoat() {
        Log.i("getData", "init");
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", django_id);
            Log.i("getTecnoSoat", message.toString());
            mSocket.emit("tecno-soat", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getCancelations() {
        Log.i("getCancelations", "init");
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", django_id);
            Log.i("getCancelations", message.toString());
            mSocket.emit("motivo-cancelar", message);
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
                msg.put("django_id", django_id);
                msg.put("usertype", "CELL");
                mSocket.emit(message.getEmit(), msg);
                Log.i("sendMessage", message.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (forCancelling != null) {
            cancelar(forCancelling);
        }
        if (forConfirming != null) {
            entrega(forConfirming);
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

    public void stopGPS() {
        try {
            JSONObject message = new JSONObject();
            message.put("cell_id", django_id);
            emitMessage("stop-gps", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void responseGPS(double lat, double lng, JSONObject pedido) {
        try {
            JSONObject message = new JSONObject();
            message.put("lat", lat);
            message.put("lng", lng);
            message.put("pedido", pedido);
            emitMessage("send-gps", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void recojer(Pedido pedido) {
        try {
            JSONObject message = new JSONObject();
            message.put("pedido_id", pedido.getId());
            message.put("tipo", pedido.getTipo());
            message.put("cell_id", django_id);
            emitMessage("recojer-pedido", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void entregar(Pedido pedido) {
        try {
            JSONObject message = new JSONObject();
            message.put("pedido_id", pedido.getId());
            message.put("tipo", pedido.getTipo());
            message.put("cell_id", django_id);
            emitMessage("confirmar-pedido", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void entregar(final Pedido pedido, String photo, Context context) {
        forConfirming = new JSONObject();
        try {
            forConfirming.put("pedido", pedido.getId());
            forConfirming.put("tipo", pedido.getTipo());
            forConfirming.put("confirmacion", photo);
            forConfirming.put("message_id", pedido.getMessage_id());
            this.context = context;
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "CELL");
            mSocket.emit("identify", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void entrega(final JSONObject forConfirming) {
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                .setTitle("Subiendo solucion")
                .setInProgressMessage("Subiendo solucion a [[UPLOAD_RATE]] ([[PROGRESS]])")
                .setErrorMessage("Hubo un error al subir la solucion")
                .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                .setAutoClearOnSuccess(true);
        String url = "http://104.236.33.228:4000/upload";
        try {
            new MultipartUploadRequest(context, url)
                    .setNotificationConfig(notificationConfig)
                    .setAutoDeleteFilesAfterSuccessfulUpload(true)
                    .setMaxRetries(1)
                    .addParameter("django_id", django_id)
                    .addParameter("usertype", "CELL")
                    .addParameter("pedido", forConfirming.getInt("pedido") + "")
                    .addParameter("tipo", forConfirming.getInt("tipo") + "")
                    .addFileToUpload(forConfirming.getString("confirmacion"), "confirmacion")
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(UploadInfo uploadInfo, Exception exception) {
                        }

                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Log.i("upload", serverResponse.getBodyAsString());
                            try {
                                JSONObject message = new JSONObject();
                                message.put("message_id", forConfirming.getString("message_id"));
                                Log.i("deleting", message.toString());
                                emitMessage("delete-message", message);
                                Pedido pedido = new Pedido();
                                pedido.setId(forConfirming.getInt("pedido"));
                                pedido.setTipo(forConfirming.getInt("tipo"));
                                NotixFactory.notifications.remove(pedido);
                                notixListener.onDelete();
                                getNumeroPedido();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {

                        }
                    })
                    .startUpload();
            this.forConfirming = null;
        } catch (MalformedURLException | FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancelar(Pedido pedido, int motivo, String observacion) {
        try {
            JSONObject message = new JSONObject();
            message.put("pedido_id", pedido.getId());
            message.put("tipo", pedido.getTipo());
            message.put("cell_id", django_id);
            message.put("motivo", motivo);
            message.put("observacion", observacion);
            emitMessage("cancelar-pedido", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancelar(final Pedido pedido, int motivo, String observacion, String photo, Context context) {
        forCancelling = new JSONObject();
        try {
            forCancelling.put("pedido", pedido.getId());
            forCancelling.put("tipo", pedido.getTipo());
            forCancelling.put("motivo", motivo);
            forCancelling.put("observacion", observacion);
            forCancelling.put("confirmacion", photo);
            forCancelling.put("message_id", pedido.getMessage_id());
            this.context = context;
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "CELL");
            mSocket.emit("identify", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cancelar(final JSONObject forCancelling) {
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                .setTitle("Subiendo solucion")
                .setInProgressMessage("Subiendo solucion a [[UPLOAD_RATE]] ([[PROGRESS]])")
                .setErrorMessage("Hubo un error al subir la solucion")
                .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                .setAutoClearOnSuccess(true);
        String url = "http://104.236.33.228:4000/cancel";
        try {
            new MultipartUploadRequest(context, url)
                    .setNotificationConfig(notificationConfig)
                    .setAutoDeleteFilesAfterSuccessfulUpload(true)
                    .setMaxRetries(1)
                    .addParameter("django_id", django_id)
                    .addParameter("usertype", "CELL")
                    .addParameter("pedido", forCancelling.getInt("pedido") + "")
                    .addParameter("tipo", forCancelling.getInt("tipo") + "")
                    .addParameter("motivo", forCancelling.getInt("motivo") + "")
                    .addParameter("observacion", forCancelling.getString("observacion"))
                    .addFileToUpload(forCancelling.getString("confirmacion"), "confirmacion")
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(UploadInfo uploadInfo, Exception exception) {
                        }

                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Log.i("upload", serverResponse.getBodyAsString());
                            try {
                                JSONObject message = new JSONObject();
                                message.put("message_id", forCancelling.getString("message_id"));
                                Log.i("deleting", message.toString());
                                emitMessage("delete-message", message);
                                Pedido pedido = new Pedido();
                                pedido.setId(forCancelling.getInt("pedido"));
                                pedido.setTipo(forCancelling.getInt("tipo"));
                                NotixFactory.notifications.remove(pedido);
                                notixListener.onDelete();
                                getNumeroPedido();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {

                        }
                    })
                    .startUpload();
            this.forCancelling = null;
        } catch (MalformedURLException | FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPassword(String password) {
        try {
            JSONObject message = new JSONObject();
            message.put("password", password);
            emitMessage("set-password", message);
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
}
