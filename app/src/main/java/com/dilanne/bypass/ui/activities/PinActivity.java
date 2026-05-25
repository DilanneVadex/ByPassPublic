package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.dilanne.bypass.MainActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.auth.AuthManager;
import com.dilanne.bypass.databinding.ActivityPinBinding;
import com.dilanne.bypass.util.LocaleHelper;

public class PinActivity extends AppCompatActivity {

    private static final String TAG = "PinActivity";
    private ActivityPinBinding binding;
    private SharedPreferences encryptedPrefs;
    private static final String PREF_PIN = "user_pin";
    private boolean isSetupMode = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isSetupMode = getIntent().getBooleanExtra("IS_SETUP", false);
        initEncryptedPrefs();

        if (isSetupMode) {
            binding.tvPinTitle.setText(R.string.title_pin_setup);
            binding.tvPinSubtitle.setText(R.string.subtitle_pin_setup);
        } else {
            binding.tvPinTitle.setText(R.string.title_pin_entry);
            binding.tvPinSubtitle.setText(R.string.subtitle_pin_entry);
        }

        binding.tvLogout.setText(R.string.label_logout);

        binding.btnConfirmPin.setOnClickListener(v -> handlePinAction());
        binding.tvLogout.setOnClickListener(v -> {
            new AuthManager(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handlePinAction() {
        String pin = binding.etPin.getText().toString();
        if (pin.length() < 4) {
            Toast.makeText(this, R.string.error_pin_length, Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        String pinKey = PREF_PIN + "_" + userId;

        if (isSetupMode) {
            encryptedPrefs.edit().putString(pinKey, pin).apply();
            Toast.makeText(this, R.string.pin_setup_success, Toast.LENGTH_SHORT).show();
            syncAndNavigate();
        } else {
            String savedPin = encryptedPrefs.getString(pinKey, "");
            if (pin.equals(savedPin)) {
                syncAndNavigate();
            } else {
                Toast.makeText(this, R.string.error_invalid_pin, Toast.LENGTH_SHORT).show();
                binding.etPin.setText("");
            }
        }
    }

    private void syncAndNavigate() {
        // Déclencher la synchronisation avant de naviguer
        com.dilanne.bypass.data.repository.PasswordRepository repository = new com.dilanne.bypass.data.repository.PasswordRepository(getApplication());
        repository.syncFromRemote();
        
        navigateToMain();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
        } catch (Exception e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences, falling back to debug_prefs", e);
            encryptedPrefs = getSharedPreferences("debug_prefs", MODE_PRIVATE);
        }
    }
}
