package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.databinding.ActivityCrearColaboradorBinding;
import com.example.huertapp.modelo.Actividad;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CrearColaborador extends AppCompatActivity {

    private static final String TAG = "CrearColaborador Class";


    ActivityCrearColaboradorBinding binding;

    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    Usuario usuarioColaborador;
    Huerto huerto;
    String keyHuerto, idUsuario, idColaborador, idUsuarioColaborador, idGetKey;

    private ImageView imagenHuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCrearColaboradorBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarCrearColaborador);

        imagenHuerto = findViewById(R.id.imgCrearColaborador);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void basicQueryValueListener(String emailColaborador) {
        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query usuarioConEmail = databaseReference.child("usuarios").child(idUsuario)
                                                 .orderByChild("email");

        // [START basic_query_value_listener]
        // My top posts by number of stars
        usuarioConEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot usuarioSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
//                    Usuario usuario = usuarioSnapshot.getValue(Usuario.class);
                    Log.d(TAG, "    ID DE USUARIO : " + usuarioSnapshot.getValue());
                    Log.d(TAG, " EMAIL DE USUARIO : " + usuarioSnapshot.child("email").getValue());
                    Log.d(TAG, "NOMBRE DE USUARIO : " + usuarioSnapshot.child("nombre").getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        // [END basic_query_value_listener]
    }

    @Override
    protected void onStart() {
        super.onStart();

//        basicQueryValueListener("pablo@garcia.es");

        firebaseAuth = FirebaseAuth.getInstance();
        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        Bundle bundle = getIntent().getExtras();
        huerto = (Huerto) getIntent().getSerializableExtra("huerto");
        keyHuerto = bundle.getString("idHuerto");

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(huerto.getFoto());
        Glide.with(this)
             .load(srReference)
             .into(imagenHuerto);

        binding.btnCrearColaborador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearColaboradorCargando.setVisibility(View.VISIBLE);
                String emailColaborador = binding.etCrearColaboradorEmail.getText().toString().trim();

                // Si el email existe en la lista de usuarios
                // Obtener idUsuario con emailColaborador
                databaseReference.child("usuarios").orderByChild("email").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Usuario usuario = ds.getValue(Usuario.class);
                            if (usuario.getEmail().equals(emailColaborador)) {
                                usuarioColaborador = usuario;
                                idColaborador = ds.child("idUsuario").getValue().toString();
                                Log.i(TAG, "ID     : "+ ds.child("idUsuario").getValue().toString());
                                Log.i(TAG, "NOMBRE : "+ ds.child("nombre").getValue().toString());
                                Log.i(TAG, "EMAIL  : "+ ds.child("email").getValue().toString());
                            }
                        }

                        if (usuarioColaborador != null) {
                            // Añadir el id de usuario de miembros de este huerto
                            databaseReference.child("huertos").child(keyHuerto).child("miembros").child(idColaborador).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Colaborador asociado correctamente",
                                                       Toast.LENGTH_LONG).show();

                                        Intent intent = new Intent(getApplicationContext(), DetalleHuerto.class);

                                        final Handler handler = new Handler(Looper.getMainLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.pbCrearColaboradorCargando.setVisibility(View.INVISIBLE);
                                                Bundle bundle = new Bundle();
                                                bundle.putString("idHuerto", keyHuerto);
                                                bundle.putSerializable("huerto", huerto);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 1000);

                                    } else {
                                        Toast.makeText(getApplicationContext(), "!!! No se ha podido asociar este colaborador, " +
                                                                                "intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            binding.pbCrearColaboradorCargando.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "No existe ningún usuario registrado con esa " +
                                                                    "dirección email",
                                           Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

//                databaseReference.child("usuarios").child(huerto.getidUsuario()).get()
//                                 .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                     @Override
//                                     public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                         if (!task.isSuccessful()) {
//                                             Log.e("firebase", "Error getting data", task.getException());
//                                         } else {
//                                             Log.d("firebase", String.valueOf(task.getResult().getValue()));
//                                             Log.d("nombre", String.valueOf(task.getResult().child("nombre").getValue()));
//                                             Log.d("email", String.valueOf(task.getResult().child("email").getValue()));
//                                         }
//                                     }
//                                 });
//
//                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("usuarios");
//                Query userQuery = rootRef.orderByChild("email").equalTo(emailColaborador);
//
//                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for(DataSnapshot datas: dataSnapshot.getChildren()){
//                            idGetKey = datas.getKey();
//                            Log.d(TAG, "Id Usuario obtenido por getKey() " + idGetKey);
//                            idUsuarioColaborador = datas.child("idUsuario").getValue().toString();
//                            Log.d(TAG, "Id Usuario obtenido por getValue() " + idUsuarioColaborador);
//                            String nombre = datas.child("nombre").getValue().toString();
//                            Log.d(TAG, "Nombre de Usuario obtenido por getValue() " + nombre);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                    }
//                });

            }
        });
    }
}