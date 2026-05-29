package com.dilanne.bypass.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dilanne.bypass.MainActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.auth.AuthManager;
import com.dilanne.bypass.databinding.ActivityGeneratorBinding;
import com.dilanne.bypass.security.PasswordGenerator;
import com.dilanne.bypass.util.LocaleHelper;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratorActivity extends AppCompatActivity {

    private ActivityGeneratorBinding binding;
    private final PasswordGenerator passwordGenerator = new PasswordGenerator();
    private AuthManager authManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);

        setupUI();
        setupUserInfo();
        setupActions();
        generatePassword(); // Initial generation
    }

    private void setupUserInfo() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            binding.tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Utilisateur");
            binding.tvUserEmail.setText(user.getEmail());
        }
    }

    private void setupUI() {
        binding.switchUpper.tvLabel.setText(R.string.label_upper);
        binding.switchLower.tvLabel.setText(R.string.label_lower);
        binding.switchNumbers.tvLabel.setText(R.string.label_numbers);
        binding.switchSymbols.tvLabel.setText(R.string.label_symbols);

        binding.sliderLength.addOnChangeListener((slider, value, fromUser) -> {
            binding.tvLengthValue.setText(String.valueOf((int) value));
        });
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnGenerate.setOnClickListener(v -> generatePassword());

        binding.btnCopy.setOnClickListener(v -> {
            String password = binding.tvGeneratedPassword.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Generated Password", password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.toast_pass_copied, Toast.LENGTH_SHORT).show();
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_generator);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_security) {
                Intent intent = new Intent(this, SecurityActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return true;
        });
    }

    private void generatePassword() {
        int length = (int) binding.sliderLength.getValue();
        boolean useUpper = binding.switchUpper.switchEnable.isChecked();
        boolean useLower = binding.switchLower.switchEnable.isChecked();
        boolean useNumbers = binding.switchNumbers.switchEnable.isChecked();
        boolean useSymbols = binding.switchSymbols.switchEnable.isChecked();

        String password = passwordGenerator.generate(length, useUpper, useLower, useNumbers, useSymbols);

        if (password.isEmpty()) {
            Toast.makeText(this, R.string.error_select_option, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvGeneratedPassword.setText(password);
    }
}
