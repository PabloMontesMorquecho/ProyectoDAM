package com.example.huertapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.huertapp.databinding.ActivityPerfilBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Perfil extends AppCompatActivity {

    private static final String TAG = "Perfil Activity";

    ActivityPerfilBinding binding;

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser usuarioActual;
    String IdUsuario;

    Huerto huerto;
    String keyHuerto;
    Planta planta;
    String keyPlanta;

    ActivityResultLauncher<String> mgetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarProfile);

        firebaseAuth = FirebaseAuth.getInstance();
        IdUsuario = firebaseAuth.getCurrentUser().getUid();
        usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = bundle.getString("idPlanta");
            System.out.println("yeah, keyPLANTA: "+keyPlanta);
            System.out.println("yeah, keyHUERTO: "+keyHuerto);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("usuarios").child(IdUsuario).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    binding.tvProfileNombre.setText(usuario.getNombre());
                    binding.tvProfileEmail.setText(usuario.getEmail());
                    Log.d("Firebase GET Usuario", String.valueOf(task.getResult().getValue()));
                }
            }
        });

        binding.btnEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usuarioActual.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    databaseReference.child("usuarios").child(IdUsuario).removeValue();
                                    Log.d(TAG, "Cuenta de usuario borrada.");
                                    Toast.makeText(getApplicationContext(),
                                            "Cuenta de usuario borrada correctamente",
                                            Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.d(TAG, "No se puede borrar la cuenta de usuario.");
                                    Toast.makeText(getApplicationContext(),
                                            "No se puedo eliminar su cuenta. Por favor, inténtelo de nuevo más tarde.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_perfil, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnPerfilSubirFoto: {
//                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("idHuerto", keyHuerto);
//                bundle.putSerializable("huerto", huerto);
//                intent.putExtras(bundle);
//                startActivity(intent);
//                mgetContent.launch("image/*");
//                mgetContent=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
//                    @Override
//                    public void onActivityResult(Uri result) {
//                        mImage
//                    }
//                })
                break;
            }

            case R.id.mnPerfilGoToMisHuertos: {
                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
                Bundle bundle = new Bundle();
                bundle.putString("idHuerto", keyHuerto);
                bundle.putSerializable("huerto", huerto);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }


            case R.id.mnPerfilLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(Perfil.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }
}