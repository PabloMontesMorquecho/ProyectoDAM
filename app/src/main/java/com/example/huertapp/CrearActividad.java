package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.huertapp.databinding.ActivityCrearActividadBinding;
import com.example.huertapp.modelo.Actividad;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class CrearActividad extends AppCompatActivity {

    private static final String TAG = "CrearActividad Activity";

    ActivityCrearActividadBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    Planta planta;
    String keyPlanta;
    Huerto huerto;
    String keyHuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        binding = ActivityCrearActividadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

//        setSupportActionBar(binding.toolbarCrearActividad);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = bundle.getString("idPlanta");
            System.out.println("yeah, keyHUERTO: "+keyHuerto);
            System.out.println("yeah, keyPLANTA: "+keyPlanta);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.btnAddActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearActividadCargando.setVisibility(View.INVISIBLE);
                String tipoActividad = binding.etCrearActividadTipoActividad.getText().toString();
                String descripcionActividad = binding.etCrearActividadDescripcion.getText().toString();
                String fechaActividad = binding.etCrearActividadFecha.getText().toString();
                String keyActividad = databaseReference.child("actividades").push().getKey();
                Actividad actividad = new Actividad(tipoActividad, descripcionActividad, fechaActividad, keyActividad, keyPlanta);

                databaseReference.child("actividades").child(keyActividad).setValue(actividad).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Actividad creada correctamente", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear la actividad, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Intent intent = new Intent(getApplicationContext(), DetallePlanta.class);

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.pbCrearActividadCargando.setVisibility(View.INVISIBLE);
                        Bundle bundle = new Bundle();
                        bundle.putString("idHuerto", keyHuerto);
                        bundle.putSerializable("huerto", huerto);
                        bundle.putString("idPlanta", keyPlanta);
                        bundle.putSerializable("planta", planta);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }, 1000);
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_crear_actividad, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//
//        switch (menuItem.getItemId()) {
//
//            case R.id.perfilID: {
//                Intent intent = new Intent(getApplicationContext(), Perfil.class);
//                startActivity(intent);
//                break;
//            }
//
//            case R.id.logOutID: {
//                FirebaseAuth.getInstance().signOut();
//                Toast.makeText(CrearPlanta.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), Login.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                break;
//            }
//        }
//        return true;
//    }
}