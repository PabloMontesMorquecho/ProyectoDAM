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
import com.example.huertapp.databinding.ActivityEditarHuertoBinding;
import com.example.huertapp.modelo.Huerto;
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

public class EditarHuerto extends AppCompatActivity {

    private static final String TAG = "EditarHuerto.Class";
    ActivityEditarHuertoBinding binding;

    Huerto huerto;
    String _dbFechaHuerto, _dbNombreHuerto, _dbDescripcionHuerto, _dbFotoHuerto;

    DatabaseReference databaseReference;
    FirebaseStorage storage;

    ImageView imagenHuerto;

    StorageReference storageReference;

    Uri imageUri;
    private boolean imagenElegida = false;
    String nombreImagenNueva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityEditarHuertoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            _dbFechaHuerto = huerto.getFecha();
            _dbNombreHuerto = huerto.getNombre();
            _dbDescripcionHuerto = huerto.getDescripcion();
            _dbFotoHuerto = huerto.getFoto();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        imagenHuerto = findViewById(R.id.imgEditarHuerto);

        StorageReference srReference = storage.getReferenceFromUrl(_dbFotoHuerto);
        Glide.with(this)
             .load(srReference)
             .into(imagenHuerto);

    }

    @Override
    protected void onStart() {
        super.onStart();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        binding.tvEditarHuertoFechaCreacion.setText(huerto.getFecha());
        binding.etEditarHuertoFecha.setText(huerto.getFecha());
        binding.etEditarHuertoNombre.setText(huerto.getNombre());
        binding.etEditarHuertoDescripcion.setText(huerto.getDescripcion());

        cargarNombreCreadorHuerto();

        imagenHuerto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });

        binding.etEditarHuertoFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnActualizarHuerto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbEditarHuertoCargando.setVisibility(View.VISIBLE);
                if (validarCampos()) {
                    actualizarDatosHuerto();
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.pbEditarHuertoCargando.setVisibility(View.INVISIBLE);
                            finish();
                        }
                    }, 3600);
                }
                binding.pbEditarHuertoCargando.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void cargarNombreCreadorHuerto() {
        databaseReference.child("usuarios").orderByChild("idUsuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario usuario = ds.getValue(Usuario.class);
                    if (usuario.getIdUsuario().equals(huerto.getidUsuario())) {
                        String nombreUsuarioCreador = ds.child("nombre").getValue().toString();
                        binding.tvEditarHuertoNombreUsuarioCreador.setText(nombreUsuarioCreador);
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
            imagenHuerto.setImageURI(imageUri);
            imagenElegida = true;
        }
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = twoDigits(year) + " / " + twoDigits(month+1) + " / " + twoDigits(day);
                binding.etEditarHuertoFecha.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean validarCampos() {
        binding.pbEditarHuertoCargando.setVisibility(View.VISIBLE);
        if (imagenElegida || isTypeChanged() || isDetailsChanged() || isDateChanged()) {
            return true;
        } else {
            Toast.makeText(this, "No hay datos que modificar", Toast.LENGTH_LONG).show();
            binding.pbEditarHuertoCargando.setVisibility(View.INVISIBLE);
            return false;
        }
    }
    private boolean isDateChanged() {
        if (_dbFechaHuerto.equals(binding.etEditarHuertoFecha.getText().toString())) {
            binding.pbEditarHuertoCargando.setVisibility(View.INVISIBLE);
            return false;
        } else {
            return true;
        }
    }
    private boolean isTypeChanged() {
        if (_dbNombreHuerto.equals(binding.etEditarHuertoNombre.getText().toString())) {
            binding.pbEditarHuertoCargando.setVisibility(View.INVISIBLE);
            return false;
        } else {
            return true;
        }
    }
    private boolean isDetailsChanged() {
        if (_dbDescripcionHuerto.equals(binding.etEditarHuertoDescripcion.getText().toString())) {
            binding.pbEditarHuertoCargando.setVisibility(View.INVISIBLE);
            return false;
        } else {
            return true;
        }
    }

    private void actualizarDatosHuerto() {
        if (imagenElegida) {
            // Actualizar foto en F.Storage y Realtime Database
            subirFoto(imageUri);
            String direccionFoto = "gs://huertapp-db.appspot.com/fotos/huerto/" + nombreImagenNueva;
            databaseReference.child("huertos").child(huerto.getIdHuerto()).child("foto").setValue(direccionFoto);
            Log.d(TAG, "Foto del huerto actualizado : " + direccionFoto);
        }
        if (isDateChanged()) {
            databaseReference.child("huertos").child(huerto.getIdHuerto()).child("fecha").setValue(binding.etEditarHuertoFecha.getText().toString());
            _dbFechaHuerto = binding.etEditarHuertoFecha.getText().toString();
            binding.tvEditarHuertoFechaCreacion.setText(binding.etEditarHuertoFecha.getText().toString());
            Log.d(TAG, "Fecha creación del huerto actualizado : " + _dbFechaHuerto);
        }
        if (isTypeChanged()) {
            databaseReference.child("huertos").child(huerto.getIdHuerto()).child("nombre").setValue(binding.etEditarHuertoNombre.getText().toString());
            _dbNombreHuerto = binding.etEditarHuertoNombre.getText().toString();
            Log.d(TAG, "Nombre huerto actualizado : " + _dbNombreHuerto);
        }
        if (isDetailsChanged()) {
            databaseReference.child("huertos").child(huerto.getIdHuerto()).child("descripcion").setValue(binding.etEditarHuertoDescripcion.getText().toString());
            _dbDescripcionHuerto = binding.etEditarHuertoDescripcion.getText().toString();
            Log.d(TAG, "Descripción del huerto actualizado : " + _dbDescripcionHuerto);
        }
        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
    }

    private void subirFoto(Uri uri) {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        nombreImagenNueva = huerto.getIdHuerto() + ts + "." + getFileExtension(uri);
        StorageReference plantaFotoRef = storageReference.child("fotos/huerto/" + nombreImagenNueva);
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