package com.dilanne.bypass.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.dilanne.bypass.MainActivity;
import com.dilanne.bypass.databinding.ActivityLoginBinding;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences encryptedPrefs;
    private static final String PREF_MASTER_PASSWORD = "master_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initEncryptedPrefs();

        binding.btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void initEncryptedPrefs() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    this,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin() {
        String password = binding.etPassword.getText().toString();
        
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        String savedMasterPassword = encryptedPrefs.getString(PREF_MASTER_PASSWORD, null);

        if (savedMasterPassword == null) {
            // First time use, register the master password
            encryptedPrefs.edit().putString(PREF_MASTER_PASSWORD, password).apply();
            Toast.makeText(this, "Master password set!", Toast.LENGTH_SHORT).show();
            startMainActivity();
        } else {
            if (savedMasterPassword.equals(password)) {
                startMainActivity();
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
