package com.example.huertapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.huertapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login Activity";

    ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;
    AwesomeValidation awesomeValidation;

    String emailErrorMessage = "Por favor, ingrese un email válido";
    String passwordErrorMessage = "Su contraseña tiene que tener al menos 6 caracteres";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC); //utilizamos la versión básica de AwesomeValidation
        awesomeValidation.addValidation(binding.etLoginEmail, Patterns.EMAIL_ADDRESS, emailErrorMessage); //si no se mete una dirección de correo válida salta el error
        awesomeValidation.addValidation(binding.etLoginPassword, ".{6,}", passwordErrorMessage); //si la contraseña no tiene al menos 6 caracteres salta el error
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){ // Si el usuario está ya logueado es dirigido a sus huertos
            startActivity(new Intent(Login.this, MisHuertos.class));
            finish();
        }

        binding.btnLoginAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (awesomeValidation.validate()) {
                    firebaseAuth.signInWithEmailAndPassword(binding.etLoginEmail.getText().toString(), binding.etLoginPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), MisHuertos.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                errorToast(errorCode); // en caso de que hubiese algún otro tipo de error incluso después de haber introducido todos los campos correctamente
                            }
                        }
                    });
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

    private void errorToast(String errorCode) {

        switch (errorCode) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(Login.this, "El formato del token personalizado es incorrecto. Por favor revise la documentación", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(Login.this, "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(Login.this, "La credencial de autenticación proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(Login.this, "La dirección de correo electrónico está mal formateada.", Toast.LENGTH_LONG).show();
                binding.etLoginEmail.setError("La dirección de correo electrónico está mal formateada.");
                binding.etLoginEmail.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(Login.this, "La contraseña no es válida o el usuario no tiene contraseña.", Toast.LENGTH_LONG).show();
                binding.etLoginPassword.setError("la contraseña es incorrecta ");
                binding.etLoginPassword.requestFocus();
                binding.etLoginPassword.setText("");
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

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(Login.this, "No hay ningún registro de usuario que corresponda a este identificador. Es posible que se haya eliminado al usuario.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(Login.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(Login.this, "Esta operación no está permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(Login.this, "La contraseña proporcionada no es válida..", Toast.LENGTH_LONG).show();
                binding.etLoginPassword.setError("La contraseña no es válida, debe tener al menos 6 caracteres");
                binding.etLoginPassword.requestFocus();
                break;

        }
    }

}
