package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
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

    ActivityRegistroBinding binding;
    AwesomeValidation awesomeValidation;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    String emailError = "Por favor, ingrese un email válido";
    String passwError = "Su contraseña tiene que tener al menos 6 caracteres";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC); //utilizamos la versión básica de AwesomeValidation
        awesomeValidation.addValidation(binding.etRegistroEmail, Patterns.EMAIL_ADDRESS, emailError); //si no se mete una dirección de correo válida salta el error
        awesomeValidation.addValidation(binding.etRegistroPassword, ".{6,}", passwError); //si la contraseña no tiene al menos 6 caracteres salta el error

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pbCargando.setVisibility(View.VISIBLE);

                if (awesomeValidation.validate() && coincidenPassw() && !campoUser()) { //si email y password tienen el formato correcto y las dos contraseñas coinciden y el campoUser esta rellenado

                    firebaseAuth.createUserWithEmailAndPassword(binding.etRegistroEmail.getText().toString(), binding.etRegistroPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Usuario usuario = new Usuario(binding.etRegistroEmail.getText().toString(), binding.etRegistroNombre.getText().toString());

                                String id = firebaseAuth.getCurrentUser().getUid();
                                databaseReference.child("usuarios").child(id).setValue(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Datos de usuario guardados en Realtime DB correctamente", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "!!! No se pudieron guardar los datos en Realtime DB ;(", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                                Toast.makeText(Registro.this, "Usuario creado con éxito", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.pbCargando.setVisibility(View.INVISIBLE);
                                        startActivity(intent);
                                    }
                                }, 1000);

                            } else {
                                binding.pbCargando.setVisibility(View.INVISIBLE);
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                errorToast(errorCode); // en caso de que hubiese algún otro tipo de error incluso después de haber introducido todos los campos correctamente
                            }
                        }
                    });

                } else if (campoUser()) {
                    binding.pbCargando.setVisibility(View.INVISIBLE);
                    Toast.makeText(Registro.this, "Introduzca un usuario", Toast.LENGTH_SHORT).show();
                } else if (!coincidenPassw()) {
                    binding.pbCargando.setVisibility(View.INVISIBLE);
                    Toast.makeText(Registro.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void errorToast(String errorCode) {

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

    public boolean coincidenPassw() {
        return (binding.etRegistroPassword.getText().toString().equals(binding.etRegistroPasswordMatch.getText().toString()));
    }

    public boolean campoUser() {
        return (binding.etRegistroNombre.getText().toString().isEmpty());
    }
}