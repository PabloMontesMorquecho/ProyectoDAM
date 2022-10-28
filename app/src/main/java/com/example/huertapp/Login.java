package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.huertapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login Activity";

    ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;
    AwesomeValidation awesomeValidation;

    String emailErrorMessage = "Por favor, ingrese un email válido";
    String passwordErrorMessage = "Su contraseña tiene que tener al menos 6 caracteres";

    String IdUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        // AwesomeValidation
//        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC); //utilizamos la versión básica de
//        awesomeValidation.addValidation(binding.etLoginEmail, Patterns.EMAIL_ADDRESS, emailErrorMessage); //si no se mete una dirección de correo válida salta el error
//        awesomeValidation.addValidation(binding.etLoginPassword, ".{6,}", passwordErrorMessage); //si la contraseña no tiene al menos 6 caracteres salta el error
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){ // Si el usuario está ya logueado es dirigido a sus huertos
            goToMisHuertos();
        }

        binding.btnLoginAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!binding.etLoginEmail.getText().toString().trim().isEmpty() && !binding.etLoginPassword.getText().toString().isEmpty()) {
//                    if (awesomeValidation.validate()) {
                        firebaseAuth.signInWithEmailAndPassword(binding.etLoginEmail.getText().toString(),
                                                                binding.etLoginPassword.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                goToMisHuertos();
                                            } else {
//                                                String errorCode =
//                                                        ((FirebaseAuthException) task.getException()).getErrorCode();
//                                                errorToast(errorCode);
                                                Toast.makeText(Login.this, "Error",
                                                               Toast.LENGTH_SHORT).show();
                                                Log.w(TAG, "signInWithEmailAndPassword TASK EXCEPTION : " + task.getException());
                                                // en caso de que hubiese algún otro tipo de error incluso después de haber introducido todos los campos correctamente
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if (e instanceof FirebaseTooManyRequestsException) {
                                                Snackbar.make(findViewById(android.R.id.content), "Demasiados " +
                                                                                                  "intentos seguidos." +
                                                                                                  " Cierre la " +
                                                                                                  "aplicación e " +
                                                                                                  "inténtelo de nuevo" +
                                                                                                  " en unos 5 minutos" +
                                                                                                  "...",
                                                              Snackbar.LENGTH_LONG).show();
                                            }
                                            if( e instanceof FirebaseAuthInvalidUserException){
                                                Toast.makeText(Login.this, "This User Not Found , Create A New Account",
                                                               Toast.LENGTH_SHORT).show();
                                            }
                                            if( e instanceof FirebaseAuthInvalidCredentialsException){
                                                Toast.makeText(Login.this, "The Password Is Invalid, Please Try Valid Password", Toast.LENGTH_SHORT).show();
                                            }
                                            if(e instanceof FirebaseNetworkException){
                                                Toast.makeText(Login.this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });;
//                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Los campos no pueden estar vacios...",
                                  Snackbar.LENGTH_LONG).show();
                }
            }
        });

        binding.btnLoginRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Registro.class);
                startActivity(intent);
            }
        });
    }

    private void goToMisHuertos() {
        Intent intent = new Intent(Login.this, MisHuertos.class);
        IdUsuario = firebaseAuth.getCurrentUser().getUid();
        Bundle bundle = new Bundle();
        bundle.putSerializable("idUsuario", IdUsuario);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void errorToast(String errorCode) {

        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                Snackbar.make(findViewById(android.R.id.content), "La dirección de correo electrónico está mal formateada.",
                              Snackbar.LENGTH_LONG).show();
//                Toast.makeText(Login.this, "La dirección de correo electrónico está mal formateada.", Toast.LENGTH_LONG).show();

//                binding.etLoginEmail.setError("La dirección de correo electrónico está mal formateada.");
//                binding.etLoginEmail.requestFocus();
                break;

            case "ERROR_USER_NOT_FOUND":
                Snackbar.make(findViewById(android.R.id.content), "Este email no esta registrado en HuertAPP",
                              Snackbar.LENGTH_LONG).show();
//                Toast.makeText(Login.this, "Este email no esta registrado en HuertAPP", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WRONG_PASSWORD":
                Snackbar.make(findViewById(android.R.id.content), "La contraseña no es válida",
                              Snackbar.LENGTH_LONG).show();
//                Toast.makeText(Login.this, "La contraseña no es válida", Toast.LENGTH_LONG).show();
//                binding.etLoginPassword.setError("la contraseña es incorrecta ");
//                binding.etLoginPassword.requestFocus();
                break;

            case "ERROR_TOO_MANY_REQUESTS":
                Snackbar.make(findViewById(android.R.id.content), "Demasiados intentos ya. Inténtalo de nuevo más tarde",
                              Snackbar.LENGTH_LONG).show();
                break;

            // A partir de aquí son errores para el Log del administrador
            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(Login.this, "El formato del token personalizado es incorrecto. Por favor revise la documentación", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(Login.this, "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(Login.this, "La credencial de autenticación proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(Login.this, "Las credenciales proporcionadas no corresponden al usuario que inició sesión anteriormente..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(Login.this, "Esta operación es sensible y requiere autenticación reciente. Inicie sesión nuevamente antes de volver a intentar esta solicitud.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(Login.this, "Ya existe una cuenta con la misma dirección de correo electrónico pero diferentes credenciales de inicio de sesión. Inicie sesión con un proveedor asociado a esta dirección de correo electrónico.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(Login.this, "La dirección de correo electrónico ya está siendo utilizada por otra cuenta..   ", Toast.LENGTH_LONG).show();
                binding.etLoginEmail.setError("La dirección de correo electrónico ya está siendo utilizada por otra cuenta.");
                binding.etLoginEmail.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(Login.this, "Esta credencial ya está asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(Login.this, "La cuenta de usuario ha sido inhabilitada por un administrador..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(Login.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(Login.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(Login.this, "Esta operación no está permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;
        }

    }

}
