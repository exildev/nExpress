package co.com.expressdelnorte.expressdelnorte.notix;

import org.json.JSONArray;
import org.json.JSONObject;


public interface onNotixListener {

    void onNotix(JSONObject data);

    void onNumeroPedido(JSONObject data);

    void onSetPassword(JSONObject data);

    void onAsignarPedido(JSONObject data);

    void onVisited(JSONObject data);

    void onGetData(JSONObject data);

    void onCancelations(JSONArray data);

    void onDelete();
}
