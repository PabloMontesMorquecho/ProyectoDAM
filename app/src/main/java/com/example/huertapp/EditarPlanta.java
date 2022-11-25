package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.databinding.ActivityEditarPlantaBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.example.huertapp.ui.dialog.DatePickerFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditarPlanta extends AppCompatActivity {

    private static final String TAG = "EditarPlanta.Class";
    ActivityEditarPlantaBinding binding;

    Planta planta;
    String _dbFechaPlanta, _dbNombrePlanta, _dbDescripcionPlanta, _dbFotoPlanta;

    DatabaseReference databaseReference;
    FirebaseStorage storage;

    ImageView imagenPlanta;

    StorageReference storageReference;

    Uri imageUri;
    private boolean imagenElegida = false;
    String nombreImagenNueva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityEditarPlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            planta = (Planta) getIntent().getSerializableExtra("planta");
            _dbFechaPlanta = planta.getFecha();
            _dbNombrePlanta = planta.getNombre();
            _dbDescripcionPlanta = planta.getDescripcion();
            _dbFotoPlanta = planta.getFoto();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        imagenPlanta = findViewById(R.id.imgEditarPlanta);

        StorageReference srReference = storage.getReferenceFromUrl(_dbFotoPlanta);
        Glide.with(this)
             .load(srReference)
             .into(imagenPlanta);

    }

    @Override
    protected void onStart() {
        super.onStart();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        binding.tvEditarPlantaFechaCreacion.setText(planta.getFecha());
        binding.etEditarPlantaFecha.setText(planta.getFecha());
        binding.etEditarPlantaNombre.setText(planta.getNombre());
        binding.etEditarPlantaDescripcion.setText(planta.getDescripcion());

        cargarNombreCreadorPlanta();

        imagenPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });

        binding.etEditarPlantaFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnActualizarPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbEditarPlantaCargando.setVisibility(View.VISIBLE);
                if (validarCampos()) {
                    actualizarDatosPlanta();
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.pbEditarPlantaCargando.setVisibility(View.INVISIBLE);
                            finish();
                        }
                    }, 5000);
                }
                binding.pbEditarPlantaCargando.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void cargarNombreCreadorPlanta() {
        databaseReference.child("usuarios").orderByChild("idUsuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario usuario = ds.getValue(Usuario.class);
                    if (usuario.getIdUsuario().equals(planta.getIdUsuario())) {
                        String nombreUsuarioCreador = ds.child("nombre").getValue().toString();
                        binding.tvEditarPlantaNombreUsuarioCreador.setText(nombreUsuarioCreador);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void elegirFoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data.getData() != null) {
            imageUri = data.getData();
            imagenPlanta.setImageURI(imageUri);
            imagenElegida = true;
        }
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = twoDigits(year) + " / " + twoDigits(month+1) + " / " + twoDigits(day);
                binding.etEditarPlantaFecha.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean validarCampos() {
        if (imagenElegida || isTypeChanged() || isDetailsChanged() || isDateChanged()) {
            return true;
        } else {
            Toast.makeText(this, "No hay datos que modificar", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private boolean isDateChanged() {
        if (_dbFechaPlanta.equals(binding.etEditarPlantaFecha.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isTypeChanged() {
        if (_dbNombrePlanta.equals(binding.etEditarPlantaNombre.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isDetailsChanged() {
        if (_dbDescripcionPlanta.equals(binding.etEditarPlantaDescripcion.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }

    private void actualizarDatosPlanta() {
        if (imagenElegida) {
            // Actualizar foto en F.Storage y Realtime Database
            subirFoto(imageUri);
            String direccionFoto = "gs://huertapp-db.appspot.com/fotos/planta/" + nombreImagenNueva;
            databaseReference.child("plantas").child(planta.getIdPlanta()).child("foto").setValue(direccionFoto);
            Log.d(TAG, "Foto de la planta actualizado : " + direccionFoto);
        }
        if (isDateChanged()) {
            databaseReference.child("plantas").child(planta.getIdPlanta()).child("fecha").setValue(binding.etEditarPlantaFecha.getText().toString());
            _dbFechaPlanta = binding.etEditarPlantaFecha.getText().toString();
            binding.tvEditarPlantaFechaCreacion.setText(binding.etEditarPlantaFecha.getText().toString());
            Log.d(TAG, "Fecha creación de la planta actualizada : " + _dbFechaPlanta);
        }
        if (isTypeChanged()) {
            databaseReference.child("plantas").child(planta.getIdPlanta()).child("nombre").setValue(binding.etEditarPlantaNombre.getText().toString());
            _dbNombrePlanta = binding.etEditarPlantaNombre.getText().toString();
            Log.d(TAG, "Nombre planta actualizado : " + _dbNombrePlanta);
        }
        if (isDetailsChanged()) {
            databaseReference.child("plantas").child(planta.getIdPlanta()).child("descripcion").setValue(binding.etEditarPlantaDescripcion.getText().toString());
            _dbDescripcionPlanta = binding.etEditarPlantaDescripcion.getText().toString();
            Log.d(TAG, "Descripción de la planta actualizada : " + _dbDescripcionPlanta);
        }
        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
    }

    private void subirFoto(Uri uri) {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        nombreImagenNueva = planta.getIdPlanta() + ts + "." + getFileExtension(uri);
        StorageReference plantaFotoRef = storageReference.child("fotos/planta/" + nombreImagenNueva);
        plantaFotoRef.putFile(uri)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Toast.makeText(getApplicationContext(), "Error en la actualización, inténtelo de nuevo " +
                                                                     "más tarde", Toast.LENGTH_LONG).show();
                         }
                     })
                     .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                         }
                     });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

}