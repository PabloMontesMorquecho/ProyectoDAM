package com.example.huertapp.adaptador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.huertapp.ItemClickListener;
import com.example.huertapp.R;
import com.example.huertapp.modelo.Planta;

import java.util.List;

public class AdaptadorPlanta extends RecyclerView.Adapter<AdaptadorPlanta.PlantasViewHolder> {

    List<Planta> listaPlantas;
    static ItemClickListener clickListener;

    public AdaptadorPlanta(List<Planta> listaPlantas) {
        this.listaPlantas = listaPlantas;
    }

    @NonNull
    @Override
    public AdaptadorPlanta.PlantasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_planta, parent, false);
        AdaptadorPlanta.PlantasViewHolder holder = new AdaptadorPlanta.PlantasViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorPlanta.PlantasViewHolder holder, int position) {
        Planta planta = listaPlantas.get(position);
        holder.nombrePlanta.setText(planta.getNombre());
        holder.descripcionPlanta.setText(planta.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return listaPlantas.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class PlantasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nombrePlanta, descripcionPlanta;
        public PlantasViewHolder(@NonNull View itemView) {
            super(itemView);
            nombrePlanta = itemView.findViewById(R.id.tvPlantaNombre);
            descripcionPlanta = itemView.findViewById(R.id.tvPlantaDescripcion);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
