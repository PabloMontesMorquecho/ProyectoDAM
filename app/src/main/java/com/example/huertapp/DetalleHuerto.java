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
import com.example.huertapp.adaptador.AdaptadorDetalleHuerto;
import com.example.huertapp.databinding.ActivityDetalleHuertoBinding;
import com.example.huertapp.modelo.Actividad;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class DetalleHuerto extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = "DetalleHuerto Activity";

    ActivityDetalleHuertoBinding binding;
    DatabaseReference databaseReference, dbHuertoActual, dbActividades;
    List<Planta> listaPlantas;
    List<Actividad> listaActividades;
    AdaptadorDetalleHuerto adaptadorDetalleHuerto;
    Huerto huerto;
    String idUsuario, keyHuerto;
    RecyclerView recyclerView;

    private FirebaseStorage storage;
    private ImageView imagenHuerto;

    private ValueEventListener mHuertoListener, mActividadesListener;

    private MenuItem mnItemColaborador, mnItemVerColaboradores, mnItemBorrarHuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "ON CREATE DetalleHuerto");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityDetalleHuertoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarDetalleHuerto);

        recyclerView = findViewById(R.id.rvDetalleHuerto);
        imagenHuerto = findViewById(R.id.imgDetalleHuerto);

        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");

            // Cargo nombre del creador del huerto y lo inserto en la vista
            Log.i(TAG, "ON CREATE DetalleHuerto : Nombre del creador con .get()");
            databaseReference.child("usuarios").child(huerto.getidUsuario()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                 @Override
                 public void onComplete(@NonNull Task<DataSnapshot> task) {
                     if (!task.isSuccessful()) {
                         Log.e("Firebase ERROR", "Error Obteniendo Usuario", task.getException());
                     } else {
//                         Log.d("Firebase SUCCESSFUL", String.valueOf(task.getResult().getValue()));
                         binding.tvHuertoNombreUsuarioCreador.setText(String.valueOf(task.getResult().child("nombre").getValue()));
                     }
                 }
             });

            // Inserto el nombre, la fecha de creación y la descripción del huerto en la vista
            binding.toolbarDetalleHuerto.setTitle(huerto.getNombre());
            binding.tvHuertoFechaDetalleHuerto.setText(huerto.getFecha());
            binding.tvHuertoDescripcionDetalleHuerto.setText(huerto.getDescripcion());

            // Creo una referencia de archivo usando una Google Cloud Storage URI
            StorageReference srReference = storage.getReferenceFromUrl(huerto.getFoto());
            // Inserto la imagen en la vista
            Glide.with(this)
                 .load(srReference)
                 .into(imagenHuerto);
        }

    }

    /**
     * https://developer.android.com/guide/components/activities/activity-lifecycle?hl=es-419#alc
     * User nivigates to the Activity from onStop()
     *
     * Cuándo vuelve a la Actividad desde la siguiente
     * la que la puso en onStop(), Perfil o DetallePlanta
     *
     * */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "ON RESTART DetalleHuerto");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "ON START DetalleHuerto");

        if (huerto.getidUsuario().equals(idUsuario)) {
            binding.tvHuertoNumeroColaboradores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToVerColaboradores();
                }
            });
        }

        // Carga el numero total de miembros y actualiza el texto N Colaboradores;
        dbHuertoActual = FirebaseDatabase.getInstance().getReference().child("huertos").child(huerto.getIdHuerto());
        addHuertoEventListener(dbHuertoActual);

        // Preparo el RecyclerView de Plantas con un adaptador vacío en orden inverso
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        listaPlantas = new ArrayList<>();
        adaptadorDetalleHuerto = new AdaptadorDetalleHuerto(getApplicationContext(), listaPlantas);
        adaptadorDetalleHuerto.setClickListener(this);
        recyclerView.setAdapter(adaptadorDetalleHuerto);

        // Obtengo todas las plantas ordenadas por fecha
        // y actualizo el adaptador del RecyclerView de Plantas
        // Además muestro un layout para crear la primera planta si no hay plantas
        databaseReference.child("plantas").orderByChild("fecha").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPlantas.removeAll(listaPlantas);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Planta planta = ds.getValue(Planta.class);
                    if (planta.getIdHuerto().equals(keyHuerto)) listaPlantas.add(planta);
                }
                if (listaPlantas.isEmpty()) {
                    // Si no hay plantas, escondo el recyclerView y muestro layout para crear la primera
                    binding.llSinPlantas.setVisibility(View.VISIBLE);
//                    binding.scrollViewPlantas.setVisibility(View.GONE);
                    binding.rvDetalleHuerto.setVisibility(View.GONE);
                    binding.btnCrearPrimeraPlanta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("idHuerto", keyHuerto);
                            bundle.putSerializable("huerto", huerto);
                            Intent intent = new Intent(getApplicationContext(), CrearPlanta.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                } else {
                    // Si hay plantas, muestro el recyclerView y actualizao su adaptador
                    binding.llSinPlantas.setVisibility(View.GONE);
//                    binding.scrollViewPlantas.setVisibility(View.VISIBLE);
                    binding.scrollViewDetallePlanta.setBackground(null);
                    binding.rvDetalleHuerto.setVisibility(View.VISIBLE);
                    adaptadorDetalleHuerto.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DatabaseError Obteniendo el Listado de Plantas: " + error.getMessage());
            }
        });

    }

    /**
     * User returns to the Activity from onPause()
     *
     * */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "ON RESUME DetalleHuerto");
        Log.i(TAG, "ON RESUME : Activity Running until other Activity comes into foreground");
    }

    /**
     * Another Activity comes into the foreground
     * De onResume() viene a onPause()
     * mientras muestra la nueva Avtivity
     *
     * */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "ON PAUSE DetalleHuerto");
    }

    /**
     * The Activity is no longer visible
     * De onPause() viene a onStop()
     *
     * */
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "ON STOP DetalleHuerto");
        // Remove huerto y actividades value event listener
        if (mHuertoListener != null) {
            dbHuertoActual.removeEventListener(mHuertoListener);
            Log.i(TAG, "ON STOP DetalleHuerto : Huerto Listener REMOVED!");
        }
        if (mActividadesListener != null) {
            dbActividades.removeEventListener(mActividadesListener);
            Log.i(TAG, "ON STOP DetalleHuerto : Actividades Listener REMOVED!");
        }
    }

    /**
     * The Activity is finishing
     * or being destroyed by the system
     *
     * De onStop() viene a onDestroy()
     *
     * Sólo ocurre cuando va atrás a la Actividad que genera el onCreate
     * cuando va a MisHuertos...que hace el intent a DetalleHuerto
     *
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ON DESTROY DetalleHuerto");
    }

    private void cargaNombreCreadorHuerto() {
        databaseReference.child("usuarios").child(huerto.getidUsuario()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.tvHuertoNombreUsuarioCreador.setText(snapshot.child("nombre").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DatabaseError Obteniendo el Ususario Creador del Huerto: " + error.getMessage());
            }
        });
    }

    private void addHuertoEventListener(DatabaseReference dbHuertoReference) {
        Log.i(TAG, "ONSTART DetalleHuerto : Huerto Listener ATTACHED!");
        // [START huerto_value_event_listener]
        ValueEventListener huertosListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Huerto object and use the values to update the UI
                Huerto huerto = dataSnapshot.getValue(Huerto.class);
                if (huerto == null) return; // Para evitar error al escuchar que borro el huerto y cambian los datos
                if (huerto.miembros.size() != 0) {
                    if (huerto.miembros.size() == 1) {
                        binding.tvHuertoNumeroColaboradores.setText("1 Colaborador");
                    } else {
                        binding.tvHuertoNumeroColaboradores.setText(huerto.miembros.size() + " Colaboradores");
                    }
                } else {
                    binding.tvHuertoNumeroColaboradores.setText("0 Colaboradores");
                }

                if (huerto.getDescripcion().trim().isEmpty()) {
                    binding.tvHuertoDescripcionDetalleHuerto.setVisibility(View.GONE);
                    binding.tvHuertoNumeroColaboradores.setHeight(104);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Huerto failed, log a message
                Log.w(TAG, "loadHuerto:onCancelled", databaseError.toException());
            }
        };
        dbHuertoReference.addValueEventListener(huertosListener);
        // [END huerto_value_event_listener]
        // Keep copy of post listener so we can remove it when app stops
        mHuertoListener = huertosListener;
    }

    private void addActividadesEventListener(DatabaseReference dbActividadesReference) {
        listaActividades = new ArrayList<>();
        //        listaActividades.removeAll(listaActividades);
        Log.i(TAG, "DetalleHuerto : Actividades Listener ATTACHED!");
        // [START actividades_value_event_listener]
        ValueEventListener actividadesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Actividad actividad = ds.getValue(Actividad.class);
                    for (Planta pl : listaPlantas) {
                        if (actividad.getIdPlanta().equals(pl.getIdPlanta())) {
                            databaseReference.child("actividades").child(actividad.getIdActividad()).removeValue();
                            Log.d(TAG, "Actividad del Huerto a Borrar · Actividad ID : " + actividad.getIdActividad());
                            listaActividades.add(actividad);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Huerto failed, log a message
                Log.w(TAG, "loadActividades:onCancelled", databaseError.toException());
            }
        };
        dbActividadesReference.addValueEventListener(actividadesListener);
        // [END huerto_value_event_listener]
        // Keep copy of post listener so we can remove it when app stops
        mActividadesListener = actividadesListener;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_huerto, menu);
        if (!huerto.getidUsuario().equals(idUsuario)) {
            mnItemColaborador = menu.findItem(R.id.mnMisPlantasAddColaborador);
            mnItemColaborador.setVisible(false);
            mnItemVerColaboradores = menu.findItem(R.id.mnMisPlantasVerColaboradores);
            mnItemVerColaboradores.setVisible(false);
            mnItemBorrarHuerto = menu.findItem(R.id.mnMisPlantasBorrarHuerto);
            mnItemBorrarHuerto.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnMisPlantasAddColaborador: {
                    Intent intent = new Intent(getApplicationContext(), CrearColaborador.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("idHuerto", keyHuerto);
                    bundle.putSerializable("huerto", huerto);
                    intent.putExtras(bundle);
                    startActivity(intent);
                break;
            }

            case R.id.mnMisPlantasVerColaboradores: {
                    goToVerColaboradores();
                break;
            }

            case R.id.mnMisPlantasAddPlanta: {
                Intent intent = new Intent(getApplicationContext(), CrearPlanta.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                bundle.putSerializable("huerto", huerto);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.mnMisPlantasBorrarHuerto: {
                confirmarBorrado(keyHuerto);
                break;
            }

            case R.id.mnMisPlantasPerfil: {
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                bundle.putSerializable("huerto", huerto);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }

            case R.id.mnMisPlantasLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(DetalleHuerto.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
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

    private void confirmarBorrado(String keyHuerto) {
        new MaterialAlertDialogBuilder(DetalleHuerto.this, R.style.AlertDialogTheme)
                .setTitle("Atención")
                .setMessage("Si continuas, no podras recuperar los datos borrados.")
                .setPositiveButton("BORRAR HUERTO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        borrarHuerto(keyHuerto);
                    }
                })
                .setNeutralButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void borrarHuerto(String keyHuerto) {
        borrarActividadesDeLasPlantas();
        databaseReference.child("huertos").child(keyHuerto).removeValue();
        Log.d(TAG, "Huerto borrado.");
        Toast.makeText(getApplicationContext(),
                       "Huerto borrado correctamente",
                       Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void borrarPlantasDelHuerto() {
        for (Planta pl: listaPlantas) {
            databaseReference.child("plantas").child(pl.getIdPlanta()).removeValue();
            Log.d(TAG, "Planta del Huerto a Borrar · Planta ID : " + pl.getIdPlanta());
        }
    }

    private void borrarActividadesDeLasPlantas() {
        // Carga todas las actividades de las plantas de este huerto y las borra
        dbActividades = FirebaseDatabase.getInstance().getReference().child("actividades");
        addActividadesEventListener(dbActividades);
        borrarPlantasDelHuerto();
    }

    private void goToVerColaboradores() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("huerto", huerto);
        bundle.putString("nombreCreador", binding.tvHuertoNombreUsuarioCreador.getText().toString().trim());
        Intent intent = new Intent(getApplicationContext(), VerColaboradores.class);
        intent.putExtras(bundle);
        startActivity(intent);
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
        Log.i("Nombre de la planta: ",
              planta.getNombre() + " · Descripción: " + planta.getDescripcion() + " · KEY: " + planta.getIdPlanta());
        startActivity(i);
    }
}