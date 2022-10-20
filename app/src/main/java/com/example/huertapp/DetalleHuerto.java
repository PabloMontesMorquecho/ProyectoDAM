package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    DatabaseReference databaseReference;
    List<Planta> listaPlantas;
    AdaptadorDetalleHuerto adaptadorDetalleHuerto;
    Huerto huerto, huertoActual;
    String idUsuario, keyHuerto;
    RecyclerView recyclerView;

    private FirebaseStorage storage;
    private ImageView imagenHuerto;

    String nombreUsuarioCreador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }

    @Override
    protected void onStart() {
        super.onStart();

        cargaNombreCreadorHuerto();

        //
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            System.out.println("yeah, keyHUERTO: " + keyHuerto);
            System.out.println("yeah, idUsuario creador del huerto: " + huerto.getidUsuario());
//            System.out.println("yeah, Nombre creador del huerto: "+databaseReference.child("usuarios/" + huerto
//            .getidUsuario()).child("nombre").get());
            databaseReference.child("usuarios").child(huerto.getidUsuario()).get()
                             .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<DataSnapshot> task) {
                                     if (!task.isSuccessful()) {
                                         Log.e("firebase", "Error getting data", task.getException());
                                     } else {
                                         Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                         Log.d("nombre", String.valueOf(task.getResult().child("nombre").getValue()));
                                         Log.d("email", String.valueOf(task.getResult().child("email").getValue()));
                                     }
                                 }
                             });
            //Inserto en el título el nombre del huerto
            binding.toolbarDetalleHuerto.setTitle(huerto.getNombre());

            binding.tvHuertoFechaDetalleHuerto.setText(huerto.getFecha());
            binding.tvHuertoDescripcionDetalleHuerto.setText(huerto.getDescripcion());

            // Create a reference to a file from a Google Cloud Storage URI
            StorageReference
                    srReference = storage.getReferenceFromUrl(huerto.getFoto());
            Glide.with(this)
                 .load(srReference)
                 .into(imagenHuerto);
        }
        //

        // Preparo el Recycler View de Plantas
        // Con un adaptador vacío
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        listaPlantas = new ArrayList<>();
        adaptadorDetalleHuerto = new AdaptadorDetalleHuerto(getApplicationContext(), listaPlantas);
        adaptadorDetalleHuerto.setClickListener(this);
        recyclerView.setAdapter(adaptadorDetalleHuerto);

        // Recorro FB Realtime DB
        // y actualizo el adaptador
        // del Recycler View de Plantas
        databaseReference.child("plantas").orderByChild("fecha").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPlantas.removeAll(listaPlantas);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Planta planta = ds.getValue(Planta.class);
                    if (planta.getIdHuerto().equals(keyHuerto)) listaPlantas.add(planta);
                }
                if (listaPlantas.isEmpty()) {
                    binding.llSinPlantas.setVisibility(View.VISIBLE);
                    binding.scrollViewPlantas.setVisibility(View.GONE);
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
                    binding.llSinPlantas.setVisibility(View.GONE);
                    binding.scrollViewPlantas.setVisibility(View.VISIBLE);
                    adaptadorDetalleHuerto.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void cargaNombreCreadorHuerto() {
        databaseReference.child("usuarios").orderByChild("idUsuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario usuario = ds.getValue(Usuario.class);
                    if (usuario.getIdUsuario().equals(huerto.getidUsuario())) {
                        nombreUsuarioCreador = ds.child("nombre").getValue().toString();
                        Log.i(TAG, "ID     : "+ ds.child("idUsuario").getValue().toString());
                        Log.i(TAG, "NOMBRE : "+ ds.child("nombre").getValue().toString());
                        Log.i(TAG, "EMAIL  : "+ ds.child("email").getValue().toString());
                        binding.tvHuertoNombreUsuarioCreador.setText(nombreUsuarioCreador);
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
        getMenuInflater().inflate(R.menu.menu_detalle_huerto, menu);
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
                borrarHuerto(keyHuerto);
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
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    private void borrarHuerto(String keyHuerto) {
        databaseReference.child("huertos").child(keyHuerto).removeValue();
        Log.d(TAG, "Huerto borrada.");
        Toast.makeText(getApplicationContext(),
                       "Huerto borrado correctamente",
                       Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
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