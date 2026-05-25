package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import com.dilanne.bypass.MainActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivityLanguageBinding;
import com.dilanne.bypass.util.LocaleHelper;

public class LanguageActivity extends AppCompatActivity {

    private ActivityLanguageBinding binding;
    private String selectedLang = "en";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        selectedLang = prefs.getString("Locale.Helper.Selected.Language", "en");
        
        preSelectLanguage();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rgLanguages.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEnglish) {
                selectedLang = "en";
            } else if (checkedId == R.id.rbFrench) {
                selectedLang = "fr";
            } else if (checkedId == R.id.rbGerman) {
                selectedLang = "de";
            } else if (checkedId == R.id.rbRussian) {
                selectedLang = "ru";
            }
        });

        binding.btnConfirm.setOnClickListener(v -> {
            LocaleHelper.setLocale(this, selectedLang);
            
            // Restart the app to apply changes
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void preSelectLanguage() {
        switch (selectedLang) {
            case "fr":
                binding.rbFrench.setChecked(true);
                break;
            case "de":
                binding.rbGerman.setChecked(true);
                break;
            case "ru":
                binding.rbRussian.setChecked(true);
                break;
            default:
                binding.rbEnglish.setChecked(true);
                break;
        }
    }
}
