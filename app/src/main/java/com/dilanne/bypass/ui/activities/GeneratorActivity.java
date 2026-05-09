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
import com.dilanne.bypass.databinding.ActivityGeneratorBinding;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratorActivity extends AppCompatActivity {

    private ActivityGeneratorBinding binding;
    private final SecureRandom random = new SecureRandom();

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "#&@!$*-+?_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupActions();
        generatePassword(); // Initial generation
    }

    private void setupUI() {
        binding.switchUpper.tvLabel.setText("Include Uppercase (A-Z)");
        binding.switchLower.tvLabel.setText("Include Lowercase (a-z)");
        binding.switchNumbers.tvLabel.setText("Include Numbers (0-9)");
        binding.switchSymbols.tvLabel.setText("Include Symbols (#&@!)");

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
            Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
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

        StringBuilder characterPool = new StringBuilder();
        List<Character> result = new ArrayList<>();

        if (useUpper) {
            characterPool.append(UPPER);
            result.add(UPPER.charAt(random.nextInt(UPPER.length())));
        }
        if (useLower) {
            characterPool.append(LOWER);
            result.add(LOWER.charAt(random.nextInt(LOWER.length())));
        }
        if (useNumbers) {
            characterPool.append(NUMBERS);
            result.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (useSymbols) {
            characterPool.append(SYMBOLS);
            result.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }

        if (characterPool.length() == 0) {
            Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show();
            return;
        }

        while (result.size() < length) {
            result.add(characterPool.charAt(random.nextInt(characterPool.length())));
        }

        Collections.shuffle(result);
        StringBuilder password = new StringBuilder();
        for (char c : result) {
            password.append(c);
        }

        binding.tvGeneratedPassword.setText(password.toString());
    }
}
