package com.dilanne.bypass.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dilanne.bypass.auth.AuthManager;
import com.dilanne.bypass.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);

        binding.btnRegister.setOnClickListener(v -> handleRegister());
        binding.tvLoginLink.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to register user: " + email);
        binding.progressBar.setVisibility(View.VISIBLE);
        authManager.registerWithEmail(email, password).addOnCompleteListener(this, task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Log.d(TAG, "Registration successful for: " + email);
                Toast.makeText(RegisterActivity.this, "Compte créé !", Toast.LENGTH_SHORT).show();
                // Redirect to PIN Setup
                Intent intent = new Intent(RegisterActivity.this, PinActivity.class);
                intent.putExtra("IS_SETUP", true);
                startActivity(intent);
                finish();
            } else {
                Exception e = task.getException();
                Log.e(TAG, "Registration failed", e);
                
                String errorMsg = "Erreur d'inscription";
                if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
                    String errorCode = ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode();
                    switch (errorCode) {
                        case "ERROR_OPERATION_NOT_ALLOWED":
                            errorMsg = "L'inscription par email n'est pas activée dans Firebase.";
                            break;
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            errorMsg = "Cet email est déjà utilisé.";
                            break;
                        case "ERROR_INVALID_EMAIL":
                            errorMsg = "Format d'email invalide.";
                            break;
                        case "ERROR_WEAK_PASSWORD":
                            errorMsg = "Le mot de passe est trop court.";
                            break;
                        default:
                            errorMsg = e.getLocalizedMessage();
                    }
                }
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Registration task failed listener", e);
        });
    }
}
