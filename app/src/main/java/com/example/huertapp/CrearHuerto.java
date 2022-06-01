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

import com.example.huertapp.databinding.ActivityCrearHuertoBinding;
import com.example.huertapp.modelos.Huerto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CrearHuerto extends AppCompatActivity {

    private static final String TAG = "CrearHuerto Activity";

    ActivityCrearHuertoBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    String keyHuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCrearHuertoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.btnCrearHuerto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearHuertoCargando.setVisibility(View.INVISIBLE);
                String nombreHuerto = binding.etCrearHuertoNombreHuerto.getText().toString();
                String descripcionHuerto = binding.etCrearHuertoDescripcion.getText().toString();
                String idUsuario = firebaseAuth.getCurrentUser().getUid();
                keyHuerto = databaseReference.child("huertos").push().getKey();
                Huerto huerto = new Huerto(nombreHuerto, descripcionHuerto, "direccionFoto", keyHuerto, idUsuario);

                databaseReference.child("huertos").child(keyHuerto).setValue(huerto).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Huerto creado correctamente", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear el huerto, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Intent intent = new Intent(getApplicationContext(), MisPlantas.class);

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.pbCrearHuertoCargando.setVisibility(View.INVISIBLE);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("idHuerto",keyHuerto);
                        bundle.putSerializable("huerto", huerto);
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

            case R.id.mnCrearHuertoPerfil: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.mnCrearHuertoLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(CrearHuerto.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }
}