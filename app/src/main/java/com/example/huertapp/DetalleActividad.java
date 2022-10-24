package com.example.huertapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huertapp.databinding.ActivityDetalleActividadBinding;
import com.example.huertapp.modelo.Actividad;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.example.huertapp.ui.dialog.DatePickerFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetalleActividad extends AppCompatActivity {

    private static final String TAG = "DetalleActividad.Class";

    ActivityDetalleActividadBinding binding;

    Huerto huerto;
    Planta planta;
    Actividad actividad;

    DatabaseReference databaseReference;

    private FirebaseStorage storage;
    private ImageView imagenActividad;

    String nombreUsuarioCreador;
    String _dbFechaActividad, _dbTipoActividad, _dbDetallesActividad, _dbFotoActividad;

    String fechaDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityDetalleActividadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            huerto = (Huerto) getIntent().getSerializableExtra("huerto");
            planta = (Planta) getIntent().getSerializableExtra("planta");
            actividad = (Actividad) getIntent().getSerializableExtra("actividad");
            _dbFechaActividad = actividad.getFecha();
            _dbTipoActividad = actividad.getTipo();
            _dbDetallesActividad = actividad.getObservaciones();
            _dbFotoActividad = actividad.getFoto();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        imagenActividad = findViewById(R.id.imgDetalleActividad);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference
                srReference = storage.getReferenceFromUrl(actividad.getFoto());
        Glide.with(this)
             .load(srReference)
             .into(imagenActividad);

        binding.tvDetalleActividadFechaCreacion.setText(actividad.getFecha());
        binding.etDetalleActividadFecha.setText(actividad.getFecha());
        binding.etDetalleActividadTipoActividad.setText(actividad.getTipo());
        binding.etDetalleActividadDescripcion.setText(actividad.getObservaciones());

        cargaNombreCreadorActividad();

        binding.etDetalleActividadFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.btnActualizarActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbDetalleActividadCargando.setVisibility(View.VISIBLE);
                boolean camposValidos = validarCampos();
                if (camposValidos) {
                    actualizarDatosActividad();

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.pbDetalleActividadCargando.setVisibility(View.INVISIBLE);
                            finish();
                        }
                    }, 300);
                }
                binding.pbDetalleActividadCargando.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void cargaNombreCreadorActividad() {
        databaseReference.child("usuarios").orderByChild("idUsuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario usuario = ds.getValue(Usuario.class);
                    if (usuario.getIdUsuario().equals(actividad.getIdUsuario())) {
                        nombreUsuarioCreador = ds.child("nombre").getValue().toString();
                        binding.tvDetalleActividadNombreUsuarioCreador.setText(nombreUsuarioCreador);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                fechaDatePicker = selectedDate;
                binding.etDetalleActividadFecha.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }


    private boolean validarCampos() {
        if (isTypeChanged() || isDetailsChanged() || isDateChanged()) {
            return true;
        } else {
            Toast.makeText(this, "No hay datos que modificar", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private boolean isDateChanged() {
        if (_dbFechaActividad.equals(binding.etDetalleActividadFecha.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isTypeChanged() {
        if (_dbTipoActividad.equals(binding.etDetalleActividadTipoActividad.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isDetailsChanged() {
        if (_dbDetallesActividad.equals(binding.etDetalleActividadDescripcion.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }

    private void actualizarDatosActividad() {
        databaseReference.child("actividades").child(actividad.getIdActividad()).child("fecha").setValue(binding.etDetalleActividadFecha.getText().toString());
        _dbFechaActividad = binding.etDetalleActividadFecha.getText().toString();
        binding.tvDetalleActividadFechaCreacion.setText(binding.etDetalleActividadFecha.getText().toString());
        Log.d(TAG, "Fecha actividad actualizada. : " + _dbFechaActividad);

        databaseReference.child("actividades").child(actividad.getIdActividad()).child("tipo").setValue(binding.etDetalleActividadTipoActividad.getText().toString());
        _dbTipoActividad = binding.etDetalleActividadTipoActividad.getText().toString();
        Log.d(TAG, "Tipo actividad actualizada. : " + _dbTipoActividad);

        databaseReference.child("actividades").child(actividad.getIdActividad()).child("observaciones").setValue(binding.etDetalleActividadDescripcion.getText().toString());
        _dbDetallesActividad = binding.etDetalleActividadDescripcion.getText().toString();
        Log.d(TAG, "Detalles de la actividad actualizados. : " + _dbDetallesActividad);

        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
    }

}