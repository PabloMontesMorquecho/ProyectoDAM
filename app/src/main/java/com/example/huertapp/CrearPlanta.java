package com.example.huertapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.huertapp.databinding.ActivityCrearHuertoBinding;
import com.example.huertapp.databinding.ActivityCrearPlantaBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CrearPlanta extends AppCompatActivity {

    ActivityCrearPlantaBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    String keyHuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCrearPlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarCrearPlanta);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            keyHuerto = bundle.getString("idHuerto");
            System.out.println("yeah, keyHUERTO: " + keyHuerto);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.btnCrearPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearPlantaCargando.setVisibility(View.INVISIBLE);
                String nombrePlanta = binding.etCrearPlantaNombreHuerto.getText().toString();
                String descripcionPlanta = binding.etCrearPlantaDescripcion.getText().toString();
                String keyPlanta = databaseReference.child("plantas").push().getKey();
                Planta planta = new Planta(nombrePlanta, descripcionPlanta, "direccionFoto", keyPlanta, keyHuerto);

                databaseReference.child("plantas").child(keyPlanta).setValue(planta).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Planta creada correctamente", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear la planta, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Intent intent = new Intent(getApplicationContext(), MisPlantas.class);

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.pbCrearPlantaCargando.setVisibility(View.INVISIBLE);
                        Bundle bundle = new Bundle();
                        bundle.putString("idHuerto", keyHuerto);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crear_huerto, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.perfilID: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.logOutID: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(CrearPlanta.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }
}