package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.huertapp.databinding.ActivityActualizarPerfilBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActualizarPerfil extends AppCompatActivity {

    private static final String TAG = "ActualizarPerfil.Class";

    ActivityActualizarPerfilBinding binding;
    DatabaseReference dbUsuarios;

    String _dbNombre, _dbEmail, _dbPassword;
    String numeroHuertos, numeroPlantas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityActualizarPerfilBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        dbUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            numeroHuertos = bundle.getString("nHuertos");
            binding.tvActualizarPerfilCantidadHuertos.setText(numeroHuertos);
            numeroPlantas = bundle.getString("nPlantas");
            binding.tvActualizarPerfilCantidadPlantas.setText(numeroPlantas);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        loadUserData();

        binding.btnActualizarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbActualizarPerfilCargando.setVisibility(View.VISIBLE);
                boolean camposValidos = validarCampos();
                if (camposValidos) {
                    actualizarDatosUsuario();
//                    Intent intent = new Intent(getApplicationContext(), UserProfile.class);
//                    startActivity(intent);
//                    finish();
                }
                binding.pbActualizarPerfilCargando.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loadUserData() {
        dbUsuarios.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    _dbNombre = usuario.getNombre();
                    binding.etActualizarPerfilNombre.setText(_dbNombre);
                    binding.tvActualizarPerfilNombre.setText(_dbNombre);
                    _dbEmail = usuario.getEmail();
                    binding.etActualizarPerfilEmail.setText(_dbEmail);
                    binding.tvActualizarPerfilEmail.setText(_dbEmail);
                    _dbPassword = usuario.getPassword();
                    binding.etActualizarPerfilPassword.setText(_dbPassword);
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

    private boolean validarCampos() {
        if (isNameChanged() || isPasswordChanged() || isEmailChanged()) {
            return true;
        } else {
            Toast.makeText(this, "No hay datos que modificar", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private boolean isEmailChanged() {
        if (!_dbEmail.equals(binding.etActualizarPerfilEmail.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isNameChanged() {
        if (!_dbNombre.equals(binding.etActualizarPerfilNombre.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isPasswordChanged() {
        if (!_dbPassword.equals(binding.etActualizarPerfilPassword.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }

    private void actualizarDatosUsuario() {
        dbUsuarios.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("nombre").setValue(binding.etActualizarPerfilNombre.getText().toString());
        _dbNombre = binding.etActualizarPerfilNombre.getText().toString();
        binding.tvActualizarPerfilNombre.setText(_dbNombre);
        Log.d(TAG, "User name updated.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (isEmailChanged()) {
            user.updateEmail("user@example.com")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dbUsuarios.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(binding.etActualizarPerfilEmail.getText().toString());
                            _dbEmail = binding.etActualizarPerfilEmail.getText().toString();
                            binding.tvActualizarPerfilEmail.setText(_dbEmail);
                            Log.d(TAG, "User email address updated.");
                        }
                    }
                });
        }
        if (isPasswordChanged()) {
            user.updatePassword(binding.etActualizarPerfilPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dbUsuarios.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("password")
                                      .setValue(binding.etActualizarPerfilPassword.getText().toString());
                            Log.d(TAG,
                                  "User password updated. NEW PASSWORD : " +
                                  binding.etActualizarPerfilPassword.getText().toString());
                            _dbPassword = binding.etActualizarPerfilPassword.getText().toString();
                        }
                    }
                });
        }
        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
    }
}