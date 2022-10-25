package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.databinding.ActivityActualizarPlantaBinding;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.example.huertapp.ui.dialog.DatePickerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ActualizarPlanta extends AppCompatActivity {

    private static final String TAG = "ActualizarPlanta.Class";
    ActivityActualizarPlantaBinding binding;
    DatabaseReference dbPlanta;
    Huerto huerto;
    Planta planta;
    String _dbFoto, _dbFecha, _dbNombre, _dbDescripcion;
    ImageView imagenPlanta;
    public Uri imageUri;
    private boolean imagenElegida = false;
    String nombreImagenPlanta, direccionFoto;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityActualizarPlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
        }

        imagenPlanta = findViewById(R.id.imgActualizarPlanta);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dbPlanta = FirebaseDatabase.getInstance().getReference("plantas").child(planta.getIdPlanta());
        StorageReference srReference = FirebaseStorage.getInstance().getReferenceFromUrl(planta.getFoto());
        Glide.with(this)
             .load(srReference)
             .into(imagenPlanta);

    }

    @Override
    protected void onStart() {
        super.onStart();

        dbPlanta.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    Planta planta = dataSnapshot.getValue(Planta.class);
                    _dbNombre = planta.getNombre();
                    binding.etActualizarPlantaNombre.setText(_dbNombre);
                    _dbDescripcion = planta.getDescripcion();
                    binding.etActualizarPlantaDescripcion.setText(_dbDescripcion);
                    _dbFecha = planta.getFecha();
                    binding.etActualizarPlantaFecha.setText(_dbFecha);
                    _dbFoto = planta.getFoto();
                    Log.d("Firebase GET Planta", String.valueOf(task.getResult().getValue()));
                }
            }
        });

        imagenPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });

        binding.etActualizarPlantaFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnActualizarPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbActualizarPlantaCargando.setVisibility(View.VISIBLE);
                boolean camposValidos = validarCampos();
                if (camposValidos) {
                    actualizarDatosPlanta();
//                    Intent intent = new Intent(getApplicationContext(), DetallePlanta.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("nuevoNombrePlanta", binding.etActualizarPlantaNombre.getText().toString());
//                    bundle.putString("nuevaDescripcionPlanta",
//                                     binding.etActualizarPlantaDescripcion.getText().toString());
//                    bundle.putString("nuevaFechaPlanta", binding.etActualizarPlantaFecha.getText().toString());
//                    bundle.putSerializable("huerto", huerto);
//                    bundle.putString("idPlanta", planta.getIdPlanta());
//                    bundle.putSerializable("planta", planta);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    finish();
                }
                binding.pbActualizarPlantaCargando.setVisibility(View.INVISIBLE);
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

    private void subirFoto(Uri uri) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Subiendo imagen...");
        pd.show();
        nombreImagenPlanta = planta.getIdPlanta() + "." + getFileExtension(uri);
        StorageReference plantaFotoRef = storageReference.child("fotos/planta/" + nombreImagenPlanta);
        plantaFotoRef.putFile(imageUri)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             pd.dismiss();
                             Snackbar.make(findViewById(android.R.id.content), "Imagen subida correctamente", Snackbar.LENGTH_LONG).show();
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             pd.dismiss();
                             Toast.makeText(getApplicationContext(), "Error en la subida, inténtelo de nuevo más tarde", Toast.LENGTH_LONG).show();
                         }
                     })
                     .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                             double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                             pd.setMessage("Porcentaje: " + (int) progressPercent + "%");
                         }
                     });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = twoDigits(year) + " / " + twoDigits(month+1) + " / " + twoDigits(day);
                binding.etActualizarPlantaFecha.setText(selectedDate);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean validarCampos() {
        if (imagenElegida || isNameChanged() || isDetailsChanged() || isDateChanged()) {
            return true;
        } else {
            Toast.makeText(this, "No hay datos que modificar", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private boolean isDateChanged() {
        if (_dbFecha.equals(binding.etActualizarPlantaFecha.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isNameChanged() {
        if (_dbNombre.equals(binding.etActualizarPlantaNombre.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isDetailsChanged() {
        if (_dbDescripcion.equals(binding.etActualizarPlantaDescripcion.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }

    private void actualizarDatosPlanta() {
        if (imagenElegida) {
            subirFoto(imageUri);
            direccionFoto = "gs://huertapp-db.appspot.com/fotos/planta/" + nombreImagenPlanta;
            dbPlanta.child("foto").setValue(direccionFoto);
            Log.d(TAG, "Foto de la planta actualizada. : " + direccionFoto);
        }

        dbPlanta.child("fecha").setValue(binding.etActualizarPlantaFecha.getText().toString());
        _dbFecha = binding.etActualizarPlantaFecha.getText().toString();
        Log.d(TAG, "Fecha de la planta actualizada. : " + _dbFecha);

        dbPlanta.child("nombre").setValue(binding.etActualizarPlantaNombre.getText().toString());
        _dbNombre = binding.etActualizarPlantaNombre.getText().toString();
        Log.d(TAG, "Nombre de la planta actualizado. : " + _dbNombre);

        dbPlanta.child("descripcion").setValue(binding.etActualizarPlantaDescripcion.getText().toString());
        _dbDescripcion = binding.etActualizarPlantaDescripcion.getText().toString();
        Log.d(TAG, "Detalles de la planta actualizados. : " + _dbDescripcion);

        Toast.makeText(this, "PLANTA Actualizada correctamente", Toast.LENGTH_LONG).show();
    }

}