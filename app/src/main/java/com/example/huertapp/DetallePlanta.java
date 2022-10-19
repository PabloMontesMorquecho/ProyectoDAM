package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.adaptador.AdaptadorDetallePlanta;
import com.example.huertapp.databinding.ActivityDetallePlantaBinding;
import com.example.huertapp.modelo.Actividad;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    RecyclerView recyclerView;

    private FirebaseStorage storage;
    private ImageView imagenPlanta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityDetallePlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarDetallePlanta);

        recyclerView = findViewById(R.id.rvDetallePlanta);
        imagenPlanta = findViewById(R.id.imgDetallePlanta);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        storage = FirebaseStorage.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = bundle.getString("idPlanta");
            System.out.println("yeah, keyHUERTO: "+keyHuerto);
            System.out.println("yeah, keyPLANTA: "+keyPlanta);
        }

        //Inserto en el título el nombre de la planta
        binding.toolbarDetallePlanta.setTitle(planta.getNombre());

        binding.tvPlantaFechaDetallePlanta.setText(planta.getFecha());
        binding.tvPlantaDescripcionDetallePlanta.setText(planta.getDescripcion());

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(planta.getFoto());
        Glide.with(this)
             .load(srReference)
             .into(imagenPlanta);

        // Preparo el Recycler View de Actividades
        // Con un adaptador vacío
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        listaActividades = new ArrayList<>();
        adaptadorDetallePlanta = new AdaptadorDetallePlanta(listaActividades);
        adaptadorDetallePlanta.setClickListener(this);
        recyclerView.setAdapter(adaptadorDetallePlanta);

        // Recorro FB Realtime DB
        // y actualizo el adaptador
        // del Recycler View de Actividades
        databaseReference.child("actividades").orderByChild("fecha").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaActividades.removeAll(listaActividades);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Actividad actividad = ds.getValue(Actividad.class);
                    if (actividad.getIdPlanta().equals(keyPlanta)) listaActividades.add(actividad);
                }
                if (listaActividades.isEmpty()) {
                    binding.llSinDetalles.setVisibility(View.VISIBLE);
                    binding.scrollViewDetallePlanta.setVisibility(View.GONE);
                    binding.btnCrearPrimeraAnotacion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("idHuerto", keyHuerto);
                            bundle.putSerializable("huerto", huerto);
                            bundle.putString("idPlanta", keyPlanta);
                            bundle.putSerializable("planta", planta);
                            Intent intent = new Intent(getApplicationContext(), CrearActividad.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                } else {
                    binding.llSinDetalles.setVisibility(View.GONE);
                    binding.scrollViewDetallePlanta.setVisibility(View.VISIBLE);
                    adaptadorDetallePlanta.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_planta, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

//            case R.id.mnDetallePlantaGoToMisHuertos: {
//                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("idHuerto", keyHuerto);
//                bundle.putSerializable("huerto", huerto);
//                bundle.putString("idPlanta", keyPlanta);
//                bundle.putSerializable("planta", planta);
//                intent.putExtras(bundle);
//                startActivity(intent);
//                break;
//            }
//
//            case R.id.mnDetallePlantaGoToMisPlantas: {
//                Intent intent = new Intent(getApplicationContext(), DetalleHuerto.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("idHuerto", keyHuerto);
//                bundle.putSerializable("huerto", huerto);
//                bundle.putString("idPlanta", keyPlanta);
//                bundle.putSerializable("planta", planta);
//                intent.putExtras(bundle);
//                startActivity(intent);
//                break;
//            }

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
                confirmarBorrado(keyPlanta);
                break;
            }

            case R.id.mnDetallePlantaPerfil: {
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                startActivity(intent);
                break;
            }

            case R.id.mnDetallePlantaLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(DetallePlanta.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
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

    private void confirmarBorrado(String keyPlanta) {
//        new MaterialAlertDialogBuilder(DetallePlanta.this, R.style.AlertDialogTheme)
        new MaterialAlertDialogBuilder(DetallePlanta.this)
                .setTitle("Atención")
                .setMessage("Si continuas, no podras recuperar los datos borrados.")
                .setPositiveButton("BORRAR PLANTA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        borrarPlanta(keyPlanta);
                    }
                })
                .setNeutralButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
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
        Log.i("Nombre de la planta: ", planta.getNombre() + " · Descripción: " + planta.getDescripcion() + " · KEY: " + planta.getIdPlanta());
//        startActivity(i);
    }
}