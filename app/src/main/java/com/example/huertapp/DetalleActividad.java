package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
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
import com.example.huertapp.databinding.ActivityDetalleActividadBinding;
import com.example.huertapp.modelo.Actividad;
import com.example.huertapp.modelo.Huerto;
import com.example.huertapp.modelo.Planta;
import com.example.huertapp.modelo.Usuario;
import com.example.huertapp.ui.dialog.DatePickerFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class DetalleActividad extends AppCompatActivity {

    private static final String TAG = "DetalleActividad.Class";

    ActivityDetalleActividadBinding binding;

    Huerto huerto;
    Planta planta;
    Actividad actividad;

    DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imagenActividad;
    public Uri imageUri;
    private boolean imagenElegida = false;
    String nombreImagenNueva, direccionFoto;

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

        // Primera carga onCreate
        StorageReference srReference = storage.getReferenceFromUrl(actividad.getFoto());
        Glide.with(this)
             .load(srReference)
             .into(imagenActividad);
    }

    @Override
    protected void onStart() {
        super.onStart();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

            binding.tvDetalleActividadFechaCreacion.setText(actividad.getFecha());
            binding.etDetalleActividadFecha.setText(actividad.getFecha());
            binding.etDetalleActividadTipoActividad.setText(actividad.getTipo());
            binding.etDetalleActividadDescripcion.setText(actividad.getObservaciones());

            cargaNombreCreadorActividad();

        imagenActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFoto();
            }
        });

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

        binding.btnBorrarActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarBorrado();
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
            imagenActividad.setImageURI(imageUri);
            imagenElegida = true;
        }
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
        if (imagenElegida || isTypeChanged() || isDetailsChanged() || isDateChanged()) {
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
        if (imagenElegida) {
           // Actualizar foto en F.Storage y Realtime Database
            subirFoto(imageUri);
            direccionFoto = "gs://huertapp-db.appspot.com/fotos/actividad/" + nombreImagenNueva;
            databaseReference.child("actividades").child(actividad.getIdActividad()).child("foto").setValue(direccionFoto);
            Log.d(TAG, "Foto de la actividad actualizada. : " + direccionFoto);
        }
        if (isDateChanged()) {
            databaseReference.child("actividades").child(actividad.getIdActividad()).child("fecha").setValue(binding.etDetalleActividadFecha.getText().toString());
            _dbFechaActividad = binding.etDetalleActividadFecha.getText().toString();
            binding.tvDetalleActividadFechaCreacion.setText(binding.etDetalleActividadFecha.getText().toString());
            Log.d(TAG, "Fecha actividad actualizada. : " + _dbFechaActividad);
        }
        if (isTypeChanged()) {
            databaseReference.child("actividades").child(actividad.getIdActividad()).child("tipo").setValue(binding.etDetalleActividadTipoActividad.getText().toString());
            _dbTipoActividad = binding.etDetalleActividadTipoActividad.getText().toString();
            Log.d(TAG, "Tipo actividad actualizada. : " + _dbTipoActividad);
        }
        if (isDetailsChanged()) {
            databaseReference.child("actividades").child(actividad.getIdActividad()).child("observaciones").setValue(binding.etDetalleActividadDescripcion.getText().toString());
            _dbDetallesActividad = binding.etDetalleActividadDescripcion.getText().toString();
            Log.d(TAG, "Detalles de la actividad actualizados. : " + _dbDetallesActividad);
        }
        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
    }

    private void subirFoto(Uri uri) {
//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setTitle("Actualizando imagen...");
//        pd.show();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        nombreImagenNueva = actividad.getIdActividad() + ts + "." + getFileExtension(uri);
        StorageReference plantaFotoRef = storageReference.child("fotos/actividad/" + nombreImagenNueva);
        plantaFotoRef.putFile(uri)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                             pd.dismiss();
//                             Snackbar.make(findViewById(android.R.id.content), "Imagen actualizada correctamente",
//                                           Snackbar.LENGTH_LONG).show();
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
//                             pd.dismiss();
                             Toast.makeText(getApplicationContext(), "Error en la actualización, inténtelo de nuevo " +
                                                                     "más tarde", Toast.LENGTH_LONG).show();
                         }
                     })
                     .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                             double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//                             pd.setMessage("Porcentaje: " + (int) progressPercent + "%");
                         }
                     });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void confirmarBorrado() {
        new MaterialAlertDialogBuilder(DetalleActividad.this)
                .setTitle("Atención")
                .setMessage("Si continuas, no podras recuperar los datos borrados.")
                .setPositiveButton("BORRAR REGISTRO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        borrarRegistro();
                    }
                })
                .setNeutralButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void borrarRegistro() {
        databaseReference.child("actividades").child(actividad.getIdActividad()).removeValue();
        Log.i(TAG, "Registro borrado.");
        Toast.makeText(getApplicationContext(), "Registro borrado correctamente",
                       Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), DetallePlanta.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("huerto", huerto);
        bundle.putSerializable("planta", planta);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }


}