package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.adaptador.AdaptadorVerColaboradores;
import com.example.huertapp.databinding.ActivityVerColaboradoresBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class VerColaboradores extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = "VerColaboradores Class";

    ActivityVerColaboradoresBinding binding;

    ImageView imagenHuerto;
    RecyclerView recyclerView;

    Huerto huerto;
    String nombreCreador;

    FirebaseStorage storage;
    DatabaseReference databaseReference;

    AdaptadorVerColaboradores adaptadorVerColaboradores;
    List<String> listaIdUsuarios;
    List<Usuario> listaUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityVerColaboradoresBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarVerColaboradores);

        imagenHuerto = findViewById(R.id.imgVerColaboradores);
        recyclerView = findViewById(R.id.rvVerColaboradores);

        storage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            nombreCreador = bundle.getString("nombreCreador");

            // Inserto el nombre, la fecha de creación y la descripción del huerto en la vista
            binding.toolbarVerColaboradores.setTitle(huerto.getNombre());
            binding.tvVerColaboradoresNombreUsuarioCreador.setText(nombreCreador);
            binding.tvVerColaboradoresFechaCreacionHuerto.setText(huerto.getFecha());
            binding.tvVerColaboradoresDescripcionHuerto.setText(huerto.getDescripcion());

            // Creo una referencia de archivo usando una Google Cloud Storage URI
            StorageReference srReference = storage.getReferenceFromUrl(huerto.getFoto());
            // Inserto la imagen en la vista
            Glide.with(this)
                 .load(srReference)
                 .into(imagenHuerto);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Preparo el RecyclerView de Colaboradores con un adaptador vacío en orden inverso
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        listaUsuarios = new ArrayList<>();
        listaIdUsuarios = new ArrayList<>();
        adaptadorVerColaboradores = new AdaptadorVerColaboradores(getApplicationContext(), listaUsuarios,
                                                                  huerto.getIdHuerto());
        adaptadorVerColaboradores.setClickListener(this);
        recyclerView.setAdapter(adaptadorVerColaboradores);

        //Obtengo lista id miembros y a continuación sus datos personales
        databaseReference.child("huertos").child(huerto.getIdHuerto()).child("miembros").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaUsuarios.removeAll(listaUsuarios);
                listaIdUsuarios.removeAll(listaIdUsuarios);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String idUsuarioColaborador = String.valueOf(ds.getKey());
                    listaIdUsuarios.add(idUsuarioColaborador);
                }
                for (String id: listaIdUsuarios) {
                    Log.i(TAG, id);
                    FirebaseDatabase.getInstance().getReference().child("usuarios").child(id).get()
                                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("AdpDetHuerto FIREBASE", "Error getting data from user id :" + id,
                                                      task.getException());
                                            } else {
                                                Usuario usuario = new Usuario();
                                                usuario.setIdUsuario(String.valueOf(task.getResult().child("idUsuario").getValue()));
                                                usuario.setEmail(String.valueOf(task.getResult().child("email").getValue()));
                                                usuario.setNombre(String.valueOf(task.getResult().child("nombre").getValue()));
                                                usuario.setPassword(String.valueOf(task.getResult().child("password").getValue()));
                                                listaUsuarios.add(usuario);
                                                adaptadorVerColaboradores.notifyDataSetChanged();
                                            }
                                        }
                                    });
                }
                binding.scrollViewVerColaboradores.setBackground(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DatabaseError Obteniendo el Listado de Colaboradores: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ver_colaboradores, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnVerColaboradoresAddColaborador: {
                Intent intent = new Intent(getApplicationContext(), CrearColaborador.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("huerto", huerto);
                bundle.putString("idHuerto", huerto.getIdHuerto());
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }


            case R.id.mnVerColaboradoresPerfil: {
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("huerto", huerto);
                bundle.putString("idHuerto", huerto.getIdHuerto());
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.mnVerColaboradoresLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(VerColaboradores.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view, int position) {

    }
}