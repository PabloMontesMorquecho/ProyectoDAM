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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.huertapp.databinding.ActivityCrearHuertoBinding;
import com.example.huertapp.modelo.Huerto;
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

public class CrearHuerto extends AppCompatActivity {

    private static final String TAG = "CrearHuerto Activity";

    ActivityCrearHuertoBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    String keyHuerto, idUsuario, imageName, fecha;

    public Uri imageUri;
    private ImageView huertoImage;
    private FirebaseStorage storage, gsReference;
    private StorageReference storageReference;

    private boolean imagenElegida = false;
    private String direccionFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityCrearHuertoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarCrearHuerto);

        huertoImage = findViewById(R.id.imgHuerto);
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth = FirebaseAuth.getInstance();
        idUsuario = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        keyHuerto = databaseReference.child("huertos").push().getKey();

        storage = FirebaseStorage.getInstance();
        gsReference = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        huertoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });

        binding.etCrearHuertoFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnCrearHuerto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearHuertoCargando.setVisibility(View.VISIBLE);

                if (imagenElegida) {
                    subirFoto(imageUri);
                    direccionFoto = "gs://huertapp-db.appspot.com/fotos/huerto/" + imageName;
                } else {
                    direccionFoto = "gs://huertapp-db.appspot.com/fotos/huerto/placeholder.jpg";
                }

                String nombreHuerto = binding.etCrearHuertoNombreHuerto.getText().toString().trim();
                String descripcionHuerto = binding.etCrearHuertoDescripcion.getText().toString().trim();
                Huerto huerto = new Huerto(nombreHuerto, descripcionHuerto, direccionFoto, keyHuerto, idUsuario,
                                           fecha);
                Log.d(TAG, "NOMBRE DE LA IMAGEN: " + imageName);

                // Guardar datos del huerto en Realtime Database
                databaseReference.child("huertos").child(keyHuerto).setValue(huerto).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Huerto creado correctamente", Toast.LENGTH_LONG).show();
//
                            Intent intent = new Intent(getApplicationContext(), DetalleHuerto.class);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.pbCrearHuertoCargando.setVisibility(View.INVISIBLE);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("idHuerto",keyHuerto);
                                    bundle.putSerializable("huerto", huerto);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 3000);

                        } else {
                            binding.pbCrearHuertoCargando.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear el huerto, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
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
            huertoImage.setImageURI(imageUri);
            imagenElegida = true;
        }
    }

    private void subirFoto(Uri uri) {
//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setTitle("Subiendo imagen...");
//        pd.show();
        imageName = keyHuerto + "." + getFileExtension(uri);
        StorageReference huertoFotoRef = storageReference.child("fotos/huerto/" + imageName);
        huertoFotoRef.putFile(imageUri)
                   .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                           pd.dismiss();
//                           Snackbar.make(findViewById(android.R.id.content), "Imagen subida correctamente", Snackbar.LENGTH_LONG).show();
                       }
                   })
                   .addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
//                           pd.dismiss();
                           Toast.makeText(getApplicationContext(), "Error en la subida, inténtelo de nuevo más tarde", Toast.LENGTH_LONG).show();
                       }
                   })
                   .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                           double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//                           pd.setMessage("Porcentaje: " + (int) progressPercent + "%");
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
                binding.etCrearHuertoFecha.setText(selectedDate);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

}