package com.example.huertapp.adaptador;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.huertapp.ItemClickListener;
import com.example.huertapp.R;
import com.example.huertapp.modelo.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdaptadorVerColaboradores extends RecyclerView.Adapter<AdaptadorVerColaboradores.ColaboradoresViewHolder> {

    List<Usuario> listaUsuarios;
    private Context context;
    static ItemClickListener clickListener;
    private DatabaseReference databaseReference;
    String idHuerto;

    public AdaptadorVerColaboradores(Context context, List<Usuario> listaUsuarios, String idHuerto) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
        this.idHuerto = idHuerto;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public AdaptadorVerColaboradores.ColaboradoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_colaborador_card, parent, false);
        AdaptadorVerColaboradores.ColaboradoresViewHolder holder =
                new AdaptadorVerColaboradores.ColaboradoresViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorVerColaboradores.ColaboradoresViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.nombreUsuario.setText(usuario.getNombre());
        holder.emailUsuario.setText(usuario.getEmail());
        holder.btnBorrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i("AdpVerColaboradores", "BORRAR USUARIO " + usuario.getIdUsuario());
                databaseReference.child("huertos").child(idHuerto).child("miembros").child(usuario.getIdUsuario()).removeValue();
            }
        });
    }

    public static class ColaboradoresViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nombreUsuario, emailUsuario;
        Button btnBorrarUsuario;
        public ColaboradoresViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreUsuario = itemView.findViewById(R.id.tvColaboradorNombre);
            emailUsuario = itemView.findViewById(R.id.tvColaboradorEmail);
            btnBorrarUsuario = itemView.findViewById(R.id.btnEliminarColaborador);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() { return listaUsuarios.size(); }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}
