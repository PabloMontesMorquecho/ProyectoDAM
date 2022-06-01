package com.example.huertapp.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.huertapp.ItemClickListener;
import com.example.huertapp.R;
import com.example.huertapp.modelos.Actividad;

import java.util.List;

public class AdaptadorActividad extends RecyclerView.Adapter<AdaptadorActividad.ActividadesViewHolder> {

    List<Actividad> listaActividades;
    static ItemClickListener clickListener;

    public AdaptadorActividad(List<Actividad> listaActividades) {
        this.listaActividades = listaActividades;
    }

    @NonNull
    @Override
    public AdaptadorActividad.ActividadesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_actividad, parent, false);
        AdaptadorActividad.ActividadesViewHolder holder = new AdaptadorActividad.ActividadesViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorActividad.ActividadesViewHolder holder, int position) {
        Actividad actividad = listaActividades.get(position);
        holder.nombreActividad.setText(actividad.getTipo());
        holder.fechaActividad.setText(actividad.getFecha().toString());
        holder.detalleActividad.setText(actividad.getObservaciones());
    }

    @Override
    public int getItemCount() {
        return listaActividades.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class ActividadesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nombreActividad, fechaActividad, detalleActividad;
        public ActividadesViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreActividad = itemView.findViewById(R.id.tvActividadNombre);
            fechaActividad = itemView.findViewById(R.id.tvActividadFecha);
            detalleActividad = itemView.findViewById(R.id.tvActividadDescripcion);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
