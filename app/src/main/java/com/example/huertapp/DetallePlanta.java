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

import com.example.huertapp.adaptador.AdaptadorDetallePlanta;
import com.example.huertapp.databinding.ActivityDetallePlantaBinding;
import com.example.huertapp.modelo.Actividad;
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

public class DetallePlanta extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = "DetallePlanta Activity";

    ActivityDetallePlantaBinding binding;
    DatabaseReference databaseReference;
    List<Actividad> listaActividades;
    AdaptadorDetallePlanta adaptadorDetallePlanta;
    Huerto huerto;
    String keyHuerto;
    Planta planta;
    String keyPlanta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetallePlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarDetallePlanta);
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

        // Preparo el Recycler View de Actividades
        // Con un adaptador vac??o
        binding.rvDetallePlanta.setLayoutManager(new LinearLayoutManager(this));
        listaActividades = new ArrayList<>();
        adaptadorDetallePlanta = new AdaptadorDetallePlanta(listaActividades);
        adaptadorDetallePlanta.setClickListener(this);
        binding.rvDetallePlanta.setAdapter(adaptadorDetallePlanta);

        // Recorro FB Realtime DB
        // y actualizo el adaptador
        // del Recycler View de Actividades
        databaseReference.child("actividades").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaActividades.removeAll(listaActividades);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Actividad actividad = ds.getValue(Actividad.class);
                    if (actividad.getIdPlanta().equals(keyPlanta)) listaActividades.add(actividad);
                }
                adaptadorDetallePlanta.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_planta, menu);
        //binding.toolbarDetallePlanta.setTitle("Detalle Planta");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnDetallePlantaGoToMisHuertos: {
                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                bundle.putSerializable("huerto", huerto);
                bundle.putString("idPlanta", keyPlanta);
                bundle.putSerializable("planta", planta);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.mnDetallePlantaGoToMisPlantas: {
                Intent intent = new Intent(getApplicationContext(), DetalleHuerto.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                bundle.putSerializable("huerto", huerto);
                bundle.putString("idPlanta", keyPlanta);
                bundle.putSerializable("planta", planta);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.mnDetallePlantaCrearActividad: {
                Intent intent = new Intent(getApplicationContext(), CrearActividad.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                bundle.putSerializable("huerto", huerto);
                bundle.putString("idPlanta", keyPlanta);
                bundle.putSerializable("planta", planta);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.mnDetallePlantaBorrarPlanta: {
                borrarPlanta(keyPlanta);
                break;
            }

            case R.id.mnDetallePlantaPerfil: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.mnDetallePlantaLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(DetallePlanta.this, "Sesi??n finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    private void borrarPlanta(String keyPlanta) {
        databaseReference.child("plantas").child(keyPlanta).removeValue();
        Log.d(TAG, "Planta borrada.");
        Toast.makeText(getApplicationContext(),
                "Planta borrado correctamente",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), DetalleHuerto.class);
        Bundle bundle = new Bundle();
        bundle.putString("idHuerto", keyHuerto);
        bundle.putSerializable("huerto", huerto);
        startActivity(intent);
        finish();
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
//        final Actividad actividad = listaActividades.get(position);
//        Intent i = new Intent(this, DetallePlanta.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("idPlanta", planta.getIdPlanta());
//        bundle.putSerializable("planta", planta);
//        i.putExtras(bundle);
        Log.i("Nombre de la planta: ", planta.getNombre() + " ?? Descripci??n: " + planta.getDescripcion() + " ?? KEY: " + planta.getIdPlanta());
//        startActivity(i);
    }
}