package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.huertapp.adaptador.AdaptadorMisHuertos;
import com.example.huertapp.databinding.ActivityMisHuertosBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MisHuertos extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = "MisHuertos Activity";

    ActivityMisHuertosBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    List<Huerto> listaHuertos;
    AdaptadorMisHuertos adaptadorMisHuertos;
    RecyclerView recyclerView;

    String IdUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityMisHuertosBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarMisHuertos);

        recyclerView = findViewById(R.id.rvMisHuertos);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        IdUsuario = firebaseAuth.getCurrentUser().getUid();

        //Inserto en el título el nombre del huerto
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        databaseReference.child("usuarios").child(IdUsuario).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (!task.isSuccessful()) {
//                    Log.e("firebase", "Error getting data", task.getException());
//                }
//                else {
//                    DataSnapshot dataSnapshot = task.getResult();
//                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
//                    binding.toolbarMisHuertos.setTitle(usuario.getNombre());
//                    Log.d("Firebase GET Usuario", String.valueOf(task.getResult().getValue()));
//                }
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        listaHuertos = new ArrayList<>();
        adaptadorMisHuertos = new AdaptadorMisHuertos(getApplicationContext(), listaHuertos);
        adaptadorMisHuertos.setClickListener(this);
        recyclerView.setAdapter(adaptadorMisHuertos);

        databaseReference.child("huertos").orderByChild("fecha").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaHuertos.removeAll(listaHuertos);
                for (DataSnapshot ds :
                        snapshot.getChildren()) {
                    Huerto huerto = ds.getValue(Huerto.class);
                    if (huerto.getidUsuario().equals(firebaseAuth.getUid())) listaHuertos.add(huerto);
                    else if (ds.child("miembros/" + IdUsuario).exists()) {
                        listaHuertos.add(huerto);
                        Log.i(TAG, "Miembro: "+ ds.child("miembros/" + IdUsuario + "").getValue().toString());
                    }
                }
                if (listaHuertos.isEmpty()) {
                    binding.llSinHuertos.setVisibility(View.VISIBLE);
                    binding.scrollViewMisHuertos.setVisibility(View.GONE);
                    binding.btnCrearPrimerHuerto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), CrearHuerto.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    binding.llSinHuertos.setVisibility(View.GONE);
                    binding.scrollViewMisHuertos.setVisibility(View.VISIBLE);
                    adaptadorMisHuertos.notifyDataSetChanged();
                }
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
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                startActivity(intent);
                break;
            }

            case R.id.mnMisHuertosLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MisHuertos.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view, int position) {
        final Huerto huerto = listaHuertos.get(position);
        Intent i = new Intent(this, DetalleHuerto.class);
        Bundle bundle = new Bundle();
        bundle.putString("idHuerto", huerto.getIdHuerto());
        bundle.putSerializable("huerto", huerto);
        i.putExtras(bundle);
        Log.i(TAG, "Nombre del huerto: "+ huerto.getNombre() + " · Descripción: " + huerto.getDescripcion() + " · KEY:" + huerto.getIdHuerto());
        startActivity(i);
    }

}