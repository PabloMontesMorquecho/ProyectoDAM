package com.example.huertapp.adaptador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.huertapp.ItemClickListener;
import com.example.huertapp.R;
import com.example.huertapp.modelo.Planta;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdaptadorDetalleHuerto extends RecyclerView.Adapter<AdaptadorDetalleHuerto.PlantasViewHolder> {

    List<Planta> listaPlantas;
    private Context context;
    private FirebaseStorage storage;
    static ItemClickListener clickListener;

    public AdaptadorDetalleHuerto(Context context, List<Planta> listaPlantas) {
        this.context = context;
        this.listaPlantas = listaPlantas;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public AdaptadorDetalleHuerto.PlantasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_planta_card, parent, false);
        AdaptadorDetalleHuerto.PlantasViewHolder holder = new AdaptadorDetalleHuerto.PlantasViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorDetalleHuerto.PlantasViewHolder holder, int position) {
        Planta planta = listaPlantas.get(position);
        holder.nombrePlanta.setText(planta.getNombre().trim());
        if (planta.getDescripcion().trim().isEmpty()) {
            holder.descripcionPlanta.setHeight(0);
            holder.descripcionPlanta.setVisibility(View.GONE);
        }
        holder.descripcionPlanta.setText(planta.getDescripcion().trim());
        holder.fechaPlanta.setText(planta.getFecha());
        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(planta.getFoto());
        Glide.with(context)
             .load(srReference)
             .into(holder.imagenPlanta);
    }

    @Override
    public int getItemCount() {
        return listaPlantas.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class PlantasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imagenPlanta;
        TextView nombrePlanta, descripcionPlanta, fechaPlanta;
        public PlantasViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPlanta = itemView.findViewById(R.id.imgPlanta);
            nombrePlanta = itemView.findViewById(R.id.tvPlantaNombre);
            descripcionPlanta = itemView.findViewById(R.id.tvPlantaDescripcion);
            fechaPlanta = itemView.findViewById(R.id.tvPlantaFechaCreacion);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
