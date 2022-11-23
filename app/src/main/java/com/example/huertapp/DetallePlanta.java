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
import com.example.huertapp.modelo.Usuario;
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

    String nombreUsuarioCreador;
    String idUsuario;

    private MenuItem mnItemEditarPlanta, mnItemBorrarPlanta;

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

        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        storage = FirebaseStorage.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = huerto.getIdHuerto();
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = planta.getIdPlanta();
        }

        cargaNombreCreadorPlanta();

        //Inserto en el título el nombre de la planta
        binding.toolbarDetallePlanta.setTitle(planta.getNombre());

        binding.tvPlantaFechaDetallePlanta.setText(planta.getFecha());
        if (planta.getDescripcion().isEmpty()) {
            binding.tvPlantaDescripcionDetallePlanta.setHeight(0);
        }
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
        adaptadorDetallePlanta = new AdaptadorDetallePlanta(getApplicationContext(), listaActividades);
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

    private void cargaNombreCreadorPlanta() {
        databaseReference.child("usuarios").orderByChild("idUsuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario usuario = ds.getValue(Usuario.class);
                    if (usuario.getIdUsuario().equals(planta.getIdUsuario())) {
                        nombreUsuarioCreador = ds.child("nombre").getValue().toString();
                        Log.i(TAG, "ID     : "+ ds.child("idUsuario").getValue().toString());
                        Log.i(TAG, "NOMBRE : "+ ds.child("nombre").getValue().toString());
                        Log.i(TAG, "EMAIL  : "+ ds.child("email").getValue().toString());
                        binding.tvPlantaNombreUsuarioCreador.setText(nombreUsuarioCreador);
                    }
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
        if (!planta.getIdUsuario().equals(idUsuario)) {
            mnItemBorrarPlanta = menu.findItem(R.id.mnDetallePlantaBorrarPlanta);
            mnItemBorrarPlanta.setVisible(false);
            mnItemEditarPlanta = menu.findItem(R.id.mnDetallePlantaEditarPlanta);
            mnItemEditarPlanta.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void borrarActividadesDeLasPlantas() {
        for (Actividad actividad : listaActividades) {
            databaseReference.child("actividades").child(actividad.getIdActividad()).removeValue();
            Log.d(TAG, "Actividad a Borrar · ID : " + actividad.getIdActividad());
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

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

            case R.id.mnDetallePlantaEditarPlanta: {
                Intent intent = new Intent(getApplicationContext(), EditarPlanta.class);
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
            }
        }
        return true;
    }

    private void borrarPlanta(String keyPlanta) {
        borrarActividadesDeLasPlantas();
        databaseReference.child("plantas").child(keyPlanta).removeValue();
        Log.d(TAG, "Planta borrada.");
        Toast.makeText(getApplicationContext(),
                "Planta borrada correctamente",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), DetalleHuerto.class);
        Bundle bundle = new Bundle();
        bundle.putString("idHuerto", keyHuerto);
        bundle.putSerializable("huerto", huerto);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmarBorrado(String keyPlanta) {
        new MaterialAlertDialogBuilder(DetallePlanta.this, R.style.AlertDialogTheme)
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
        final Actividad actividad = listaActividades.get(position);
        if (actividad.getIdUsuario().equals(idUsuario)) {
            Log.i(TAG, "Usuario activo es creador");
            Intent i = new Intent(this, DetalleActividad.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("huerto", huerto);
            bundle.putSerializable("planta", planta);
            bundle.putSerializable("actividad", actividad);
            i.putExtras(bundle);
            startActivity(i);
        } else {
            Log.i(TAG, "Usuario activo no es el creador");
            Toast.makeText(this, "Sólo el usuario creador puede modificar los datos", Toast.LENGTH_LONG).show();
        }
    }
}