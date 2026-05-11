package com.dilanne.bypass.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.dilanne.bypass.auth.AuthManager;
import com.dilanne.bypass.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private AuthManager authManager;
    private SharedPreferences encryptedPrefs;
    private static final int RC_SIGN_IN = 9001;
    private static final String PREF_PIN = "user_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        initEncryptedPrefs();

        // Si déjà connecté à Firebase, on va au PIN (vérification)
        if (authManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in Firebase, navigating to PIN...");
            navigateToPin(false);
            return;
        }

        binding.btnLogin.setOnClickListener(v -> handleEmailLogin());
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());
        binding.btnGithub.setOnClickListener(v -> signInWithGitHub());
        
        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleEmailLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        authManager.loginWithEmail(email, password).addOnCompleteListener(this, task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                navigateToPin(true); // Toujours demander un nouveau PIN à la connexion
            } else {
                Exception e = task.getException();
                Log.e(TAG, "Login failed", e);
                
                String errorMsg = "Échec de la connexion";
                if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
                    String errorCode = ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode();
                    switch (errorCode) {
                        case "ERROR_OPERATION_NOT_ALLOWED":
                            errorMsg = "L'authentification par email n'est pas activée dans Firebase.";
                            break;
                        case "ERROR_WRONG_PASSWORD":
                            errorMsg = "Mot de passe incorrect.";
                            break;
                        case "ERROR_USER_NOT_FOUND":
                            errorMsg = "Utilisateur non trouvé.";
                            break;
                        case "ERROR_INVALID_EMAIL":
                            errorMsg = "Format d'email invalide.";
                            break;
                        default:
                            errorMsg = e.getLocalizedMessage();
                    }
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signInWithGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = authManager.getGoogleSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithGitHub() {
        binding.progressBar.setVisibility(View.VISIBLE);
        authManager.signInWithGitHub(this).addOnCompleteListener(this, task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                navigateToPin(true);
            } else {
                Toast.makeText(this, "Échec GitHub", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                authManager.signInWithGoogle(account.getIdToken()).addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        navigateToPin(true);
                    } else {
                        Toast.makeText(this, "Firebase Error", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ApiException e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Google error: " + e.getStatusCode());
                Toast.makeText(this, "Annulé ou Erreur Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToPin(boolean isNewConnection) {
        if (encryptedPrefs == null) {
            initEncryptedPrefs();
        }

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        String savedPin = (encryptedPrefs != null) ? encryptedPrefs.getString(PREF_PIN + "_" + userId, null) : null;
        
        Intent intent = new Intent(this, PinActivity.class);
        // Si c'est une nouvelle connexion OU s'il n'y a pas de PIN sauvegardé pour cet utilisateur
        intent.putExtra("IS_SETUP", isNewConnection || savedPin == null);
        startActivity(intent);
        finish();
    }

    private void initEncryptedPrefs() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            encryptedPrefs = EncryptedSharedPreferences.create(this, "secret_shared_prefs", masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            Log.e(TAG, "Prefs initialization error", e);
            // Fallback sur SharedPreferences standards si le chiffrement échoue (pour le debug)
            encryptedPrefs = getSharedPreferences("debug_prefs", MODE_PRIVATE);
        }
    }
}
