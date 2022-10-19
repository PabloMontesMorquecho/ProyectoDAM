package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.huertapp.databinding.ActivityRegistroBinding;
import com.example.huertapp.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registro extends AppCompatActivity {

    private static final String TAG = "Registro Activity";

    ActivityRegistroBinding binding;
    AwesomeValidation awesomeValidation;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    String emailErrorMessage = "Por favor, ingrese un email válido";
    String passwordErrorMessage = "Su contraseña tiene que tener al menos 6 caracteres";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC); //utilizamos la versión básica de AwesomeValidation
        awesomeValidation.addValidation(binding.etRegistroEmail, Patterns.EMAIL_ADDRESS, emailErrorMessage); //si no se mete una dirección de correo válida salta el error
        awesomeValidation.addValidation(binding.etRegistroPassword, ".{6,}", passwordErrorMessage); //si la contraseña no tiene al menos 6 caracteres salta el error

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbRegistroCargando.setVisibility(View.VISIBLE);

                if (awesomeValidation.validate() && bothPasswordsAreEqual() && !isUserEmpty()) {

                    firebaseAuth.createUserWithEmailAndPassword(binding.etRegistroEmail.getText().toString(), binding.etRegistroPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                String idUsuario = firebaseAuth.getCurrentUser().getUid();
                                String nombre  = binding.etRegistroNombre.getText().toString();
                                String email  = binding.etRegistroEmail.getText().toString();
                                String password  = binding.etRegistroPassword.getText().toString();
                                Usuario usuario = new Usuario(idUsuario, nombre, email, password);
                                databaseReference.child("usuarios").child(idUsuario).setValue(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful())
                                            Log.w(TAG, "!!! No se pudo guardar el usuario en Realtime DB ;(.");
                                    }
                                });

                                Toast.makeText(Registro.this, "Usuario creado con éxito", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.pbRegistroCargando.setVisibility(View.INVISIBLE);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 1000);

                            } else {
                                binding.pbRegistroCargando.setVisibility(View.INVISIBLE);
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                errorToast(errorCode); // en caso de que hubiese algún otro tipo de error incluso después de haber introducido todos los campos correctamente
                            }
                        }
                    });

                } else if (isUserEmpty()) {
                    binding.pbRegistroCargando.setVisibility(View.INVISIBLE);
                    Toast.makeText(Registro.this, "Introduzca un usuario", Toast.LENGTH_SHORT).show();
                } else if (!bothPasswordsAreEqual()) {
                    binding.pbRegistroCargando.setVisibility(View.INVISIBLE);
                    Toast.makeText(Registro.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void errorToast(String errorCode) {
        binding.pbRegistroCargando.setVisibility(View.INVISIBLE);
        switch (errorCode) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(Registro.this, "El formato del token personalizado es incorrecto. Por favor revise la documentación", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(Registro.this, "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(Registro.this, "La credencial de autenticación proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(Registro.this, "La dirección de correo electrónico está mal formateada.", Toast.LENGTH_LONG).show();
                binding.etRegistroEmail.setError("La dirección de correo electrónico está mal formateada.");
                binding.etRegistroEmail.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(Registro.this, "La contraseña no es válida o el usuario no tiene contraseña.", Toast.LENGTH_LONG).show();
                binding.etRegistroPassword.setError("la contraseña es incorrecta ");
                binding.etRegistroPassword.requestFocus();
                binding.etRegistroPassword.setText("");
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(Registro.this, "Las credenciales proporcionadas no corresponden al usuario que inició sesión anteriormente..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(Registro.this, "Esta operación es sensible y requiere autenticación reciente. Inicie sesión nuevamente antes de volver a intentar esta solicitud.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(Registro.this, "Ya existe una cuenta con la misma dirección de correo electrónico pero diferentes credenciales de inicio de sesión. Inicie sesión con un proveedor asociado a esta dirección de correo electrónico.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(Registro.this, "La dirección de correo electrónico ya está siendo utilizada por otra cuenta..   ", Toast.LENGTH_LONG).show();
                binding.etRegistroEmail.setError("La dirección de correo electrónico ya está siendo utilizada por otra cuenta.");
                binding.etRegistroEmail.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(Registro.this, "Esta credencial ya está asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(Registro.this, "La cuenta de usuario ha sido inhabilitada por un administrador..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(Registro.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(Registro.this, "No hay ningún registro de usuario que corresponda a este identificador. Es posible que se haya eliminado al usuario.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(Registro.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(Registro.this, "Esta operación no está permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(Registro.this, "La contraseña proporcionada no es válida..", Toast.LENGTH_LONG).show();
                binding.etRegistroPassword.setError("La contraseña no es válida, debe tener al menos 6 caracteres");
                binding.etRegistroPassword.requestFocus();
                break;

        }
    }

    public boolean bothPasswordsAreEqual() {
        return (binding.etRegistroPassword.getText().toString().equals(binding.etRegistroPasswordMatch.getText().toString()));
    }

    public boolean isUserEmpty() {
        return (binding.etRegistroNombre.getText().toString().isEmpty());
    }
}