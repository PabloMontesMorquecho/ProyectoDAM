package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    String fecha, idUsuario;

    private FirebaseStorage storage;
    private ImageView imagenPlanta;

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            keyHuerto = bundle.getString("idHuerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            keyPlanta = bundle.getString("idPlanta");
            System.out.println("yeah, keyHUERTO: "+keyHuerto);
            System.out.println("yeah, keyPLANTA: "+keyPlanta);
        }

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(planta.getFoto());
        Glide.with(this)
             .load(srReference)
             .into(imagenPlanta);

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
                String tipoActividad = binding.etCrearActividadTipoActividad.getText().toString().trim();
                String descripcionActividad = binding.etCrearActividadDescripcion.getText().toString().trim();
                String fechaActividad = binding.etCrearActividadFecha.getText().toString();
                String keyActividad = databaseReference.child("actividades").push().getKey();
                Actividad actividad = new Actividad(tipoActividad, descripcionActividad, fechaActividad, keyActividad
                        , keyPlanta, idUsuario);

                databaseReference.child("actividades").child(keyActividad).setValue(actividad).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Actividad creada correctamente", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(getApplicationContext(), DetallePlanta.class);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.pbCrearActividadCargando.setVisibility(View.INVISIBLE);
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
                            Toast.makeText(getApplicationContext(), "!!! No se ha podido crear la actividad, intentelo de nuevo más tarde ;(", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
//                final String selectedDate = day + " / " + (month+1) + " / " + year;
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_crear_actividad, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//
//        switch (menuItem.getItemId()) {
//
//            case R.id.perfilID: {
//                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
//                startActivity(intent);
//                break;
//            }
//
//            case R.id.logOutID: {
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