package co.com.expressdelnorte.expressdelnorte.notix;


import org.json.JSONObject;

public interface AuthListener {
    void onWebSuccessLogin();

    void onWebErrorLogin();

    void onGetData(JSONObject data);
}
