package com.example.android.express;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pico on 1/08/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PedidoViewHolder>{

    List<Pedido> pedidos;

    RVAdapter(List<Pedido> persons){
        this.pedidos = persons;
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        LinearLayout lv;
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


        PedidoViewHolder(View itemView) {
            super(itemView);
            lv = (LinearLayout)itemView.findViewById(R.id.linear_layout);
            cliente = (TextView)itemView.findViewById(R.id.cliente);
            direccionSubtitle = (TextView)itemView.findViewById(R.id.direccion_subtitle);
            direccion = (TextView)itemView.findViewById(R.id.direccion);
            telefono = (TextView)itemView.findViewById(R.id.telefono);
            celular = (TextView)itemView.findViewById(R.id.celular);
            tienda = (TextView)itemView.findViewById(R.id.tienda);
            direccionTienda = (TextView)itemView.findViewById(R.id.direccion_tienda);
            total = (TextView)itemView.findViewById(R.id.total);
            negativeButton = (Button) itemView.findViewById(R.id.negative_button);
            positiveButton = (Button) itemView.findViewById(R.id.positive_button);
            dropImage = (ImageView) itemView.findViewById(R.id.drop_image);
        }
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    @Override
    public PedidoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pedido, viewGroup, false);
        PedidoViewHolder pvh = new PedidoViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PedidoViewHolder pedidoViewHolder, int i) {
        Pedido pedido = pedidos.get(i);
        pedidoViewHolder.cliente.setText(pedido.getClienteNombre());
        pedidoViewHolder.direccionSubtitle.setText(pedido.getDireccion());
        pedidoViewHolder.direccion.setText(pedido.getDireccion());
        pedidoViewHolder.telefono.setText(pedido.getTelefono());
        pedidoViewHolder.celular.setText(pedido.getCelular());
        pedidoViewHolder.tienda.setText(pedido.getTienda());
        pedidoViewHolder.direccionTienda.setText(pedido.getDireccionTienda());
        pedidoViewHolder.total.setText(pedido.getTotal());
        if (pedido.getEstado().equals("asignado")){
            pedidoViewHolder.positiveButton.setText("Recogido");
            pedidoViewHolder.negativeButton.setText("Cancelar");
        }
        if(pedido.checked){
            pedidoViewHolder.dropImage.setImageResource(R.drawable.ic_done);
            pedidoViewHolder.dropImage.setVisibility(View.VISIBLE);
        }else{
            pedidoViewHolder.dropImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}