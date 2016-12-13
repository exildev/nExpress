package co.com.expressdelnorte.expressdelnorte;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import co.com.expressdelnorte.expressdelnorte.notix.AuthListener;
import co.com.expressdelnorte.expressdelnorte.notix.Notix;
import co.com.expressdelnorte.expressdelnorte.notix.NotixFactory;

public class LoginActivity extends AppCompatActivity implements AuthListener {

    Notix notix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteAll();
                return true;
            }
        });

        notix = NotixFactory.buildNotix(this);
        notix.setAuthListener(this);
        notix.getData();

        autoLogin();
    }

    private void autoLogin() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String password = prefs.getString("password", null);
        if (password != null) {
            hideKeyboard();
            bigAvatar();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notix.webLogin(password);
                }
            }, 400);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                notix.sendQR(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    public void login(View view) {
        TextView passwordView = (TextView) findViewById(R.id.password);
        final String password = passwordView.getText().toString();
        hideKeyboard();
        bigAvatar();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notix.webLogin(password);
            }
        }, 400);
    }

    public void deleteAll() {
        TextView passwordEditText = (TextView) findViewById(R.id.password);
        passwordEditText.setText("");
    }

    public void qrScan(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private void updateData(String newName, String newFoto) {
        TextView name = (TextView) findViewById(R.id.name);
        ImageView foto = (ImageView) findViewById(R.id.foto);
        if (!newFoto.equals("")) {
            Picasso.with(LoginActivity.this)
                    .load(newFoto)
                    .placeholder(R.drawable.avatar)
                    .into(foto);
        }
        name.setText(newName);
    }

    private void initHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void ononWebSuccessLogin() {
        TextView passwordView = (TextView) findViewById(R.id.password);
        final String password = passwordView.getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("password", password).apply();
        initHome();
    }

    @Override
    public void onWebErrorLogin() {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessage("Error al iniciar sesión");
                showKeyboard();
                normalAvatar();
            }
        });
    }

    @Override
    public void onGetData(JSONObject data) {
        final JSONObject d = data;
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("data", d.toString());
                try {

                    String nombre = d.getString("nombre");
                    String appellidos = d.getString("apellidos");
                    String foto = "";
                    if (d.has("foto")) {
                        foto = d.getString("foto");
                    }
                    updateData(nombre + " " + appellidos, foto);
                } catch (JSONException e) {
                    showMessage("No se pudo mostrar el mensaje");
                }
            }
        });
    }
}
