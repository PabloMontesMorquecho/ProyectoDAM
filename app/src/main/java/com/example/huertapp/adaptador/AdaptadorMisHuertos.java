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
import com.example.huertapp.modelo.Huerto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdaptadorMisHuertos extends RecyclerView.Adapter<AdaptadorMisHuertos.HuertosViewHolder> {

    private List<Huerto> listaHuertos;
    private Context context;
    private FirebaseStorage storage;
    static ItemClickListener clickListener;

    public AdaptadorMisHuertos(Context context, List<Huerto> listaHuertos) {
        this.context = context;
        this.listaHuertos = listaHuertos;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public AdaptadorMisHuertos.HuertosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_huerto_card, parent, false);
        HuertosViewHolder holder = new HuertosViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorMisHuertos.HuertosViewHolder holder, int position) {
        Huerto huerto = listaHuertos.get(position);
        holder.nombreHuerto.setText(huerto.getNombre().trim());
        if (huerto.getDescripcion().trim().isEmpty()) {
            holder.descripcionHuerto.setVisibility(View.GONE);
        } else {
            holder.descripcionHuerto.setText(huerto.getDescripcion().trim());
        }
        FirebaseDatabase.getInstance().getReference().child("usuarios").child(huerto.getidUsuario()).get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("AdpMisHuertos FIREBASE",
                                          "Error getting data from user id :" + huerto.getidUsuario(),
                                          task.getException());
                                } else {
//                                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                    holder.creadorHuerto.setText(String.valueOf(task.getResult().child("nombre").getValue()));
                                }
                            }
                        });
        holder.fechaHuerto.setText(huerto.getFecha());
        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(huerto.getFoto());
        Glide.with(context)
             .load(srReference)
             .into(holder.imagenHuerto);
//        Glide.with(context).load(listaHuertos.get(position).getFoto()).into(holder.imagenHuerto);
    }

    @Override
    public int getItemCount() {
        return listaHuertos.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class HuertosViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imagenHuerto;
        TextView nombreHuerto, descripcionHuerto, fechaHuerto, creadorHuerto;
        public HuertosViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenHuerto = itemView.findViewById(R.id.imgHuerto);
            nombreHuerto = itemView.findViewById(R.id.tvHuertoNombre);
            descripcionHuerto = itemView.findViewById(R.id.tvHuertoDescripcion);
            fechaHuerto = itemView.findViewById(R.id.tvHuertoFechaCreacion);
            creadorHuerto = itemView.findViewById(R.id.tvHuertoUsuarioCreacion);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

}
