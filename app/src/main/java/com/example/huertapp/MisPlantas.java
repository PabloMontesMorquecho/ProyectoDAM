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

import com.example.huertapp.adaptador.AdaptadorPlanta;
import com.example.huertapp.databinding.ActivityMisPlantasBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MisPlantas extends AppCompatActivity implements ItemClickListener {

    ActivityMisPlantasBinding binding;
    DatabaseReference databaseReference;
    List<Planta> listaPlantas;
    AdaptadorPlanta adaptadorPlanta;
    Huerto huerto;
    String keyHuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMisPlantasBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarMispPlantas);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            System.out.println("yeah, keyHUERTO: "+keyHuerto);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Preparo el Recycler View de Plantas
        // Con un adaptador vacío
        binding.rvMisPlantas.setLayoutManager(new LinearLayoutManager(this));
        listaPlantas = new ArrayList<>();
        adaptadorPlanta = new AdaptadorPlanta(listaPlantas);
        adaptadorPlanta.setClickListener(this);
        binding.rvMisPlantas.setAdapter(adaptadorPlanta);

        // Recorro FB Realtime DB
        // y actualizo el adaptador
        // del Recycler View de Plantas
        databaseReference.child("plantas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPlantas.removeAll(listaPlantas);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Planta planta = ds.getValue(Planta.class);
                    if (planta.getIdHuerto().equals(keyHuerto)) listaPlantas.add(planta);
                }
                adaptadorPlanta.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mis_plantas, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.misHuertos: {
                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
                startActivity(intent);
                break;
            }

            case R.id.anadirPlantaID: {
                Intent intent = new Intent(getApplicationContext(), CrearPlanta.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.borrarHuertoID: {

                break;
            }

            case R.id.perfilID: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.logOutID: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MisPlantas.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    /**
     * Escucha el click en el Recycler View Plantas
     * y pasa a la siguiente pantalla
     * con las datos del producto correspondiente
     *
     * @param view
     * @param position
     */
    @Override
    public void onClick(View view, int position) {
        final Planta planta = listaPlantas.get(position);
        Intent i = new Intent(this, DetallePlanta.class);
        Bundle bundle = new Bundle();
        bundle.putString("idHuerto", huerto.getIdHuerto());
        bundle.putSerializable("huerto", huerto);
        bundle.putString("idPlanta", planta.getIdPlanta());
        bundle.putSerializable("planta", planta);
        i.putExtras(bundle);
        Log.i("Nombre de la planta: ", planta.getNombre() + " · Descripción: " + planta.getDescripcion() + " · KEY: " + planta.getIdPlanta());
        startActivity(i);
    }
}