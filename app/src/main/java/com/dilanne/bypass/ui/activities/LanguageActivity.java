package com.dilanne.bypass.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivityLanguageBinding;

public class LanguageActivity extends AppCompatActivity {

    private ActivityLanguageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnConfirm.setOnClickListener(v -> {
            // Logic to change language would go here
            finish();
        });

        // Optional: Pre-select current language
        // binding.rbEnglish.setChecked(true);
    }
}
