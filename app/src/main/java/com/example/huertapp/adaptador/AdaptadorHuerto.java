package com.example.huertapp.adaptador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.huertapp.ItemClickListener;
import com.example.huertapp.R;
import com.example.huertapp.modelo.Huerto;

import java.util.List;

public class AdaptadorHuerto extends RecyclerView.Adapter<AdaptadorHuerto.HuertosViewHolder> {

    List<Huerto> listaHuertos;
    static ItemClickListener clickListener;

    public AdaptadorHuerto(List<Huerto> listaHuertos) {
        this.listaHuertos = listaHuertos;
    }

    @NonNull
    @Override
    public AdaptadorHuerto.HuertosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_huerto, parent, false);
        HuertosViewHolder holder = new HuertosViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorHuerto.HuertosViewHolder holder, int position) {
        Huerto huerto = listaHuertos.get(position);
        holder.nombreHuerto.setText(huerto.getNombre());

    }

    @Override
    public int getItemCount() {
        return listaHuertos.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class HuertosViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nombreHuerto, descripcionHuerto;
        public HuertosViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreHuerto = itemView.findViewById(R.id.tvHuertoNombre);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
