package com.example.huertapp.adaptador;

import android.content.Context;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
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
        FirebaseDatabase.getInstance().getReference().child("usuarios").child(planta.getIdUsuario()).get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("AdpDetHuerto FIREBASE", "Error getting data from user id :" + planta.getIdUsuario(),
                                          task.getException());
                                } else {
//                                    Log.i("AdpDetHuerto FIREBASE",
//                                          "Datos del usuario obtenidos mediante su Id" + String.valueOf(task.getResult().getValue()));
                                    holder.creadorPlanta.setText(String.valueOf(task.getResult().child("nombre").getValue()));
                                }
                            }
                        });
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
        TextView nombrePlanta, descripcionPlanta, fechaPlanta, creadorPlanta;
        public PlantasViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPlanta = itemView.findViewById(R.id.imgPlanta);
            nombrePlanta = itemView.findViewById(R.id.tvPlantaNombre);
            descripcionPlanta = itemView.findViewById(R.id.tvPlantaDescripcion);
            fechaPlanta = itemView.findViewById(R.id.tvPlantaFechaCreacion);
            creadorPlanta = itemView.findViewById(R.id.tvPlantaUsuarioCreacion);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
