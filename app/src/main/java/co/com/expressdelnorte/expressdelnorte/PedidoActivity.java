package co.com.expressdelnorte.expressdelnorte;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class PedidoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        try {
            setData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void setData() throws JSONException {
        if (getIntent().hasExtra("pedido")) {
            JSONObject message = new JSONObject(getIntent().getStringExtra("pedido"));
            JSONObject cliente;
            if (message.has("cliente")) {
                cliente = ((JSONArray) message.get("cliente")).getJSONObject(0);
            } else {
                cliente = message.getJSONObject("info").getJSONObject("cliente");
            }

            JSONObject tienda = ((JSONArray) message.get("tienda")).getJSONObject(0);
            JSONArray info = message.getJSONArray("info");

            String nombre = cliente.getString("nombre");
            String apellidos = cliente.getString("apellidos");
            String direccion = cliente.getString("direccion");
            String telefono = "";
            if (cliente.has("fijo")) {
                telefono = cliente.getString("fijo");
            }
            String celular = cliente.getString("celular");
            String tiendaNombre = tienda.getString("referencia");
            String direccionTienda = tienda.getString("direccion");
            String telefonoTienda = tienda.getString("celular");

            TextView client_name = (TextView) findViewById(R.id.client_name);
            client_name.setText(nombre + " " + apellidos);
            TextView client_address = (TextView) findViewById(R.id.client_address);
            client_address.setText(direccion);
            TextView client_phone = (TextView) findViewById(R.id.client_phone);
            if (telefono.equals("")) {
                client_phone.setText(celular);
            } else {
                client_phone.setText(telefono);
            }

            TextView store_name = (TextView) findViewById(R.id.store_name);
            store_name.setText(tiendaNombre);
            TextView store_address = (TextView) findViewById(R.id.store_address);
            store_address.setText(direccionTienda);
            TextView store_phone = (TextView) findViewById(R.id.store_phone);
            store_phone.setText(telefonoTienda);

            LinearLayout items_container = (LinearLayout) findViewById(R.id.items_container);
            LayoutInflater inflater = LayoutInflater.from(items_container.getContext());
            for (int i = 0; i < info.length(); i++) {
                JSONObject item = info.getJSONObject(i);
                LinearLayout detail = (LinearLayout) inflater.inflate(R.layout.item, items_container, false);
                TextView producto = (TextView) detail.getChildAt(0);
                TextView cantidad = (TextView) detail.getChildAt(1);
                TextView precio = (TextView) detail.getChildAt(2);
                producto.setText(item.getString("nombre"));
                cantidad.setText(item.getString("cantidad"));
                precio.setText(NumberFormat.getCurrencyInstance().format(item.get("valor")));
                items_container.addView(detail);
            }
        } else {
            finish();
        }
    }

    public void expandCollapse(View view) {
        View container = findViewById(R.id.items_container);
        View title = findViewById(R.id.items_title);
        int v = container.getVisibility();
        container.setVisibility(title.getVisibility());
        title.setVisibility(v);
    }

}
