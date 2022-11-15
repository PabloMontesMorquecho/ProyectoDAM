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
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.databinding.ActivityCrearActividadBinding;
import com.example.huertapp.modelo.Actividad;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.ui.dialog.DatePickerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class CrearActividad extends AppCompatActivity {

    private static final String TAG = "CrearActividad Activity";

    ActivityCrearActividadBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    Planta planta;
    String keyPlanta;
    Huerto huerto;
    String keyHuerto;
    String fecha, idUsuario, keyActividad;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imagenPlanta;
    public Uri imageUri;

    private boolean imagenElegida = false;
    private String imageName, direccionFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityCrearActividadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarCrearActividad);

        imagenPlanta = findViewById(R.id.imgCrearActividad);

    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth = FirebaseAuth.getInstance();
        idUsuario = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        keyActividad = databaseReference.child("actividades").push().getKey();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = bundle.getString("idPlanta");
        }

        imagenPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });
        // Create a reference to a file from a Google Cloud Storage URI
//        StorageReference
//                srReference = storage.getReferenceFromUrl(planta.getFoto());
//        Glide.with(this)
//             .load(srReference)
//             .into(imagenPlanta);

        binding.etCrearActividadFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnCrearActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearActividadCargando.setVisibility(View.VISIBLE);
                if (imagenElegida) {
                    subirFoto(imageUri);
                    direccionFoto = "gs://huertapp-db.appspot.com/fotos/actividad/" + imageName;
                } else {
                    direccionFoto = "gs://huertapp-db.appspot.com/fotos/actividad/placeholder.jpg";
                }
                String tipoActividad = binding.etCrearActividadTipoActividad.getText().toString().trim();
                String descripcionActividad = binding.etCrearActividadDescripcion.getText().toString().trim();
                String fechaActividad = binding.etCrearActividadFecha.getText().toString();
                Actividad actividad = new Actividad(tipoActividad, descripcionActividad, fechaActividad, keyActividad
                        , keyPlanta, idUsuario, direccionFoto);

                databaseReference.child("actividades").child(keyActividad).setValue(actividad).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Actividad creada correctamente", Toast.LENGTH_LONG).show();

//                            Intent intent = new Intent(getApplicationContext(), DetalleActividad.class);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.pbCrearActividadCargando.setVisibility(View.INVISIBLE);
                                    Bundle bundle = new Bundle();
//                                    bundle.putSerializable("huerto", huerto);
//                                    bundle.putSerializable("planta", planta);
//                                    bundle.putSerializable("actividad", actividad);
//                                    intent.putExtras(bundle);
//                                    startActivity(intent);
                                    finish();
                                }
                            }, 1400);

                        } else {
                            binding.pbCrearActividadCargando.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear la actividad, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setTitle("Subiendo imagen...");
//        pd.show();
        imageName = keyActividad + "." + getFileExtension(uri);
        StorageReference plantaFotoRef = storageReference.child("fotos/actividad/" + imageName);
        plantaFotoRef.putFile(imageUri)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                             pd.dismiss();
//                             Snackbar.make(findViewById(android.R.id.content), "Imagen subida correctamente", Snackbar.LENGTH_LONG).show();
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
//                             pd.dismiss();
                             Toast.makeText(getApplicationContext(), "Error en la subida, inténtelo de nuevo más tarde", Toast.LENGTH_LONG).show();
                         }
                     })
                     .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                             double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//                             pd.setMessage("Porcentaje: " + (int) progressPercent + "%");
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
                fecha = selectedDate;
                binding.etCrearActividadFecha.setText(selectedDate);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

}