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
import com.example.huertapp.modelo.Actividad;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdaptadorDetallePlanta extends RecyclerView.Adapter<AdaptadorDetallePlanta.ActividadesViewHolder> {

    List<Actividad> listaActividades;
    static ItemClickListener clickListener;
    private Context context;
    private FirebaseStorage storage;

    public AdaptadorDetallePlanta(Context context, List<Actividad> listaActividades) {
        this.listaActividades = listaActividades;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public AdaptadorDetallePlanta.ActividadesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_actividad, parent, false);
        AdaptadorDetallePlanta.ActividadesViewHolder holder = new AdaptadorDetallePlanta.ActividadesViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorDetallePlanta.ActividadesViewHolder holder, int position) {
        Actividad actividad = listaActividades.get(position);
        holder.tipoActividad.setText(actividad.getTipo());
        holder.fechaActividad.setText(actividad.getFecha());
        if (actividad.getObservaciones().trim().isEmpty()) {
            holder.detalleActividad.setVisibility(View.GONE);
        } else {
            holder.detalleActividad.setText(actividad.getObservaciones().trim());
        }
        FirebaseDatabase.getInstance().getReference().child("usuarios").child(actividad.getIdUsuario()).get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                             @Override
                             public void onComplete(@NonNull Task<DataSnapshot> task) {
                                 if (!task.isSuccessful()) {
                                     Log.e("firebase", "Error getting data", task.getException());
                                 } else {
//                                     Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                     holder.creadorActividad.setText(String.valueOf(task.getResult().child("nombre").getValue()));
                                 }
                             }
                         });
        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(actividad.getFoto());
        Glide.with(context)
             .load(srReference)
             .into(holder.fotoActividad);
    }

    @Override
    public int getItemCount() {
        return listaActividades.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class ActividadesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tipoActividad, fechaActividad, detalleActividad, creadorActividad;
        ImageView fotoActividad;
        public ActividadesViewHolder(@NonNull View itemView) {
            super(itemView);
            tipoActividad = itemView.findViewById(R.id.tvActividadTipo);
            fechaActividad = itemView.findViewById(R.id.tvActividadFechaCreacion);
            detalleActividad = itemView.findViewById(R.id.tvActividadDescripcion);
            creadorActividad = itemView.findViewById(R.id.tvActividadUsuarioCreacion);
            fotoActividad = itemView.findViewById(R.id.imgActividad);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
