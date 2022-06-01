package com.example.huertapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.huertapp.databinding.ActivityCrearPlantaBinding;
import com.example.huertapp.modelos.Huerto;
import com.example.huertapp.modelos.Planta;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CrearPlanta extends AppCompatActivity {

    private static final String TAG = "CrearPlanta Activity";

    ActivityCrearPlantaBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    Huerto huerto;
    String keyHuerto;
    Planta planta;
    String keyPlanta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCrearPlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

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
                            binding.pbCrearPlantaCargando.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Planta creada correctamente", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(getApplicationContext(), DetallePlanta.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("idHuerto", keyHuerto);
                            bundle.putSerializable("huerto", huerto);
                            bundle.putString("idPlanta", keyPlanta);
                            bundle.putSerializable("planta", planta);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear la planta, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crear_planta, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnCrearPlantaPerfil: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.mnCrearPlantaLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(CrearPlanta.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }
}