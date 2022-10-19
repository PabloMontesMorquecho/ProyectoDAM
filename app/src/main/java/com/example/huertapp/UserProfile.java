package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.databinding.ActivityUserProfileBinding;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserProfile extends AppCompatActivity {
    ActivityUserProfileBinding binding;

    String IdUsuario;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarUserProfile);
        binding.toolbarUserProfile.getOverflowIcon().setColorFilter(Color.WHITE , PorterDuff.Mode.SRC_ATOP);

        profilePic = findViewById(R.id.circular_image_view);
        loadUserData();
    }

    private void loadUserData() {
        IdUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        dbUsuarios.child(IdUsuario).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    binding.userProfileNombre.setText(usuario.getNombre());
                    binding.tvUserProfileNombre.setText(usuario.getNombre());
                    binding.userProfileEmail.setText(usuario.getEmail());
                    binding.tvUserProfileEmail.setText(usuario.getEmail());
                    Log.d("Firebase GET Usuario", String.valueOf(task.getResult().getValue()));
                }
            }
        });

//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference srReference = storage.getReferenceFromUrl("gs://huertapp-db.appspot.com/userFoto/kWxYBikbD0aANJ6lt11HrvHRIHe2.jpg");
//        Glide.with(this)
//                .load(srReference)
//                .into(profilePic);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_perfil, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.mnPerfilGoToMisHuertos: {
                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
                startActivity(intent);
                break;
            }

            case R.id.mnMisHuertosLogout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(UserProfile.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

}