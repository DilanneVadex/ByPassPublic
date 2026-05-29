package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
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

        SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
        selectedLang = prefs.getString("Locale.Helper.Selected.Language", "en");
        
        preSelectLanguage();

        binding.btnBack.setOnClickListener(v -> finish());

        // Manual management of RadioButtons because they are nested in MaterialCardViews
        View.OnClickListener languageClickListener = v -> {
            int id = v.getId();
            updateRadioButtons(id);
            if (id == R.id.rbEnglish) {
                selectedLang = "en";
            } else if (id == R.id.rbFrench) {
                selectedLang = "fr";
            } else if (id == R.id.rbGerman) {
                selectedLang = "de";
            } else if (id == R.id.rbRussian) {
                selectedLang = "ru";
            }
        };

        binding.rbEnglish.setOnClickListener(languageClickListener);
        binding.rbFrench.setOnClickListener(languageClickListener);
        binding.rbGerman.setOnClickListener(languageClickListener);
        binding.rbRussian.setOnClickListener(languageClickListener);

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
            int idToCheck;
            switch (selectedLang) {
                case "fr":
                    idToCheck = R.id.rbFrench;
                    break;
                case "de":
                    idToCheck = R.id.rbGerman;
                    break;
                case "ru":
                    idToCheck = R.id.rbRussian;
                    break;
                default:
                    idToCheck = R.id.rbEnglish;
                    break;
            }
            updateRadioButtons(idToCheck);
    }

    private void updateRadioButtons(int checkedId) {
        binding.rbEnglish.setChecked(checkedId == R.id.rbEnglish);
        binding.rbFrench.setChecked(checkedId == R.id.rbFrench);
        binding.rbGerman.setChecked(checkedId == R.id.rbGerman);
        binding.rbRussian.setChecked(checkedId == R.id.rbRussian);
    }
}
