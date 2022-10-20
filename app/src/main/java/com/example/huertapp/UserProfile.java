package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.huertapp.databinding.ActivityUserProfileBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends AppCompatActivity {

    private static final String TAG = "UserProfile Activity";

    ActivityUserProfileBinding binding;

    String IdUsuario;
    List<Huerto> listaHuertos;
    List<Planta> listaPlantas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarUserProfile);
        binding.toolbarUserProfile.getOverflowIcon().setColorFilter(Color.WHITE , PorterDuff.Mode.SRC_ATOP);

        listaHuertos = new ArrayList<>();
        listaPlantas = new ArrayList<>();

    }

    @Override
    protected void onStart() {
        super.onStart();

        loadUserData();

        binding.btnModificarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActualizarPerfil.class);
                startActivity(intent);
            }
        });

        binding.btnEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarCuenta();
            }
        });

    }

    private void loadUserData() {
        IdUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        dbUsuarios.child(IdUsuario).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    binding.tvUserProfileNombre.setText(usuario.getNombre());
                    binding.tvUserProfileEmail.setText(usuario.getEmail());
                    Log.d("Firebase GET Usuario", String.valueOf(task.getResult().getValue()));
                }
            }
        });

//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference srReference = storage.getReferenceFromUrl("gs://huertapp-db.appspot.com/userFoto/kWxYBikbD0aANJ6lt11HrvHRIHe2.jpg");
//        Glide.with(this)
//                .load(srReference)
//                .into(profilePic);

        DatabaseReference dbHuertos = FirebaseDatabase.getInstance().getReference("huertos");
        dbHuertos.orderByChild("fecha").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaHuertos.removeAll(listaHuertos);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Huerto huerto = ds.getValue(Huerto.class);
                    if (huerto.getidUsuario().equals(IdUsuario)) listaHuertos.add(huerto);
                    else if (ds.child("miembros/" + IdUsuario).exists()) {
                        listaHuertos.add(huerto);
                    }
                }
                int nHuertos = listaHuertos.size();
                Log.d(TAG, "HUERTOS : " + nHuertos);
                binding.tvUserProfileCantidadHuertos.setText(String.valueOf(nHuertos));

                DatabaseReference dbPlantas = FirebaseDatabase.getInstance().getReference("plantas");
                listaPlantas.removeAll(listaPlantas);
                    dbPlantas.orderByChild("fecha").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (Huerto miHuerto : listaHuertos) {
                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {
                                    Planta planta = ds.getValue(Planta.class);
                                    if (planta.getIdHuerto().equals(miHuerto.getIdHuerto())) listaPlantas.add(planta);
                                    Log.d(TAG, "ID HUERTO : " + miHuerto.getIdHuerto());
                                }
                            }
                            int nPlantas = listaPlantas.size();
                            Log.d(TAG, "PLANTAS : " + nPlantas);
                            binding.tvUserProfileCantidadPlantas.setText(String.valueOf(nPlantas));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void eliminarCuenta() {
        // Esto la elimina de Firebase Authentication
        // FALTA eliminar usuario de Firebase Realtime Database
        // Y Recolectar basura
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User account deleted.");
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(UserProfile.this, "Cuenta de usuario borrada", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_perfil, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnPerfilGoToMisHuertos: {
                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
                startActivity(intent);
                break;
            }

            case R.id.mnPerfilLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(UserProfile.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

}