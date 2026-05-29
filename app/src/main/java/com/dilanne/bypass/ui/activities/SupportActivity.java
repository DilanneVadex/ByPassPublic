package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.dilanne.bypass.databinding.ActivitySupportBinding;
import com.dilanne.bypass.util.LocaleHelper;

import com.dilanne.bypass.util.LocaleHelper;

public class SupportActivity extends AppCompatActivity {



    private ActivitySupportBinding binding;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
