package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.huertapp.adaptador.AdaptadorMisHuertos;
import com.example.huertapp.databinding.ActivityMisHuertosBinding;
import com.example.huertapp.modelo.Huerto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MisHuertos extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = "MisHuertos Activity";

    ActivityMisHuertosBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    List<Huerto> listaHuertos;
    AdaptadorMisHuertos adaptadorMisHuertos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMisHuertosBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarMisHuertos);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.rvMisHuertos.setLayoutManager(new LinearLayoutManager(this));
        listaHuertos = new ArrayList<>();
        adaptadorMisHuertos = new AdaptadorMisHuertos(listaHuertos);
        adaptadorMisHuertos.setClickListener(this);
        binding.rvMisHuertos.setAdapter(adaptadorMisHuertos);

        databaseReference.child("huertos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaHuertos.removeAll(listaHuertos);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Huerto huerto = ds.getValue(Huerto.class);
                    if (huerto.getidUsuario().equals(firebaseAuth.getUid())) listaHuertos.add(huerto);
                }
                adaptadorMisHuertos.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mis_huertos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnMisHuertosCrearHuerto: {
                Intent intent = new Intent(getApplicationContext(), CrearHuerto.class);
                startActivity(intent);
                break;
            }

            case R.id.mnMisHuertosPerfil: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.mnMisHuertosLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MisHuertos.this, "Sesi??n finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view, int position) {
        final Huerto huerto = listaHuertos.get(position);
        Intent i = new Intent(this, DetalleHuerto.class);
        Bundle bundle = new Bundle();
        bundle.putString("idHuerto", huerto.getIdHuerto());
        bundle.putSerializable("huerto", huerto);
        i.putExtras(bundle);
        Log.i(TAG, "Nombre del huerto: "+ huerto.getNombre() + " ?? Descripci??n: " + huerto.getDescripcion() + " ?? KEY:" + huerto.getIdHuerto());
        startActivity(i);
    }
}