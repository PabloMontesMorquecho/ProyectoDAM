package com.example.huertapp;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.huertapp.databinding.ActivityCrearPlantaBinding;
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

public class CrearPlanta extends AppCompatActivity {

    private static final String TAG = "CrearPlanta Activity";

    ActivityCrearPlantaBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    Huerto huerto;
    String keyHuerto;
    Planta planta;
    String keyPlanta;

    public Uri imageUri;
    private ImageView plantaImage;
    private FirebaseStorage storage, gsReference;
    private StorageReference storageReference;

    private boolean imagenElegida = false;
    private String imageName, fecha, direccionFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityCrearPlantaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbarCrearPlanta);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        plantaImage = findViewById(R.id.imgPlanta);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = bundle.getString("idPlanta");
            System.out.println("yeah, keyHUERTO: "+keyHuerto);
            System.out.println("yeah, keyPLANTA: "+keyPlanta);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        storage = FirebaseStorage.getInstance();
//        gsReference = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        keyPlanta = databaseReference.child("plantas").push().getKey();

        plantaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });

        binding.etCrearPlantaFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnCrearPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCrearPlantaCargando.setVisibility(View.VISIBLE);

                if (imagenElegida) {
                    subirFoto(imageUri);
                    direccionFoto = "gs://huertapp-db.appspot.com/fotos/planta/" + imageName;
                } else {
                    direccionFoto = "gs://huertapp-db.appspot.com/fotos/planta/placeholder.jpg";
                }

                String nombrePlanta = binding.etCrearPlantaNombre.getText().toString().trim();
                String descripcionPlanta = binding.etCrearPlantaDescripcion.getText().toString().trim();
//                String keyPlanta = databaseReference.child("plantas").push().getKey();
                Planta planta = new Planta(nombrePlanta, descripcionPlanta, direccionFoto, keyPlanta, keyHuerto,
                                           fecha);

                databaseReference.child("plantas").child(keyPlanta).setValue(planta).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            binding.pbCrearPlantaCargando.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Planta creada correctamente", Toast.LENGTH_LONG).show();

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(), DetallePlanta.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("idHuerto", keyHuerto);
                                    bundle.putSerializable("huerto", huerto);
                                    bundle.putString("idPlanta", keyPlanta);
                                    bundle.putSerializable("planta", planta);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1000);

                        } else {
                            binding.pbCrearPlantaCargando.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear la planta, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
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
            plantaImage.setImageURI(imageUri);
            imagenElegida = true;
        }
    }

    private void subirFoto(Uri uri) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Subiendo imagen...");
        pd.show();
        imageName = keyPlanta + "." + getFileExtension(uri);
        StorageReference plantaFotoRef = storageReference.child("fotos/planta/" + imageName);
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
//                final String selectedDate = day + " / " + (month+1) + " / " + year;
                final String selectedDate = twoDigits(year) + " / " + twoDigits(month+1) + " / " + twoDigits(day);
                fecha = selectedDate;
                binding.etCrearPlantaFecha.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_crear_planta, menu);
//        binding.toolbarCrearPlanta.setTitle("Crear Planta");
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//
//        switch (menuItem.getItemId()) {
//
//            case R.id.mnCrearPlantaPerfil: {
//                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
//                startActivity(intent);
//                break;
//            }
//
//            case R.id.mnCrearPlantaLogout: {
//                FirebaseAuth.getInstance().signOut();
//                Toast.makeText(CrearPlanta.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), Login.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                break;
//            }
//        }
//        return true;
//    }

}