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
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.huertapp.adaptador.AdaptadorHuerto;
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

    ActivityMisHuertosBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    List<Huerto> listaHuertos;
    AdaptadorHuerto adaptadorHuerto;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMisHuertosBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarMisHuertos);
        binding.toolbarMisHuertos.setTitle("Mis Huertos");
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Preparo el Recycler View de Huertos
        // Con un adaptador vacío
        binding.rvMisHuertos.setLayoutManager(new LinearLayoutManager(this));
        listaHuertos = new ArrayList<>();
        adaptadorHuerto = new AdaptadorHuerto(listaHuertos);
        adaptadorHuerto.setClickListener(this);
        binding.rvMisHuertos.setAdapter(adaptadorHuerto);

        // Recorro FB Realtime DB
        // y actualizo el adaptador
        // del Recycler View de Productos
        databaseReference.child("huertos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaHuertos.removeAll(listaHuertos);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Huerto huerto = ds.getValue(Huerto.class);
//                    huerto.setKey(snapshot.getKey());
                    if (huerto.getidUsuario().equals(firebaseAuth.getUid())) listaHuertos.add(huerto);
                }
                adaptadorHuerto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.crearID: {
                Intent intent = new Intent(getApplicationContext(), CrearHuerto.class);
                startActivity(intent);
                break;
            }

            case R.id.perfilID: {
                Intent intent = new Intent(getApplicationContext(), Perfil.class);
                startActivity(intent);
                break;
            }

            case R.id.logOutID: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MisHuertos.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    /**
     * Escucha el click en el Recycler View Huertos
     * y pasa a la siguiente pantalla
     * con las datos del producto correspondiente
     *
     * @param view
     * @param position
     */
    @Override
    public void onClick(View view, int position) {
        final Huerto huerto = listaHuertos.get(position);
        Intent i = new Intent(this, MisPlantas.class);
        Bundle bundle = new Bundle();
        bundle.putString("idHuerto", huerto.getIdHuerto());
        bundle.putSerializable("huerto", huerto);
        i.putExtras(bundle);
        Log.i("Nombre del huerto: ", huerto.getNombre() + " · Descripción: " + huerto.getDescripcion() + " · KEY:" + huerto.getIdHuerto());
        startActivity(i);
    }
}