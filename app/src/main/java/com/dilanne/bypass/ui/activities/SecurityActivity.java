package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dilanne.bypass.MainActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivitySecurityBinding;
import com.dilanne.bypass.ui.adapters.SecurityAdapter;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;
import java.util.List;

import com.dilanne.bypass.util.LocaleHelper;

public class SecurityActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ActivitySecurityBinding binding;
    private PasswordViewModel viewModel;
    private SecurityAdapter adapter;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LocaleHelper.onAttach(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        setupRecyclerView();
        setupActions();

        viewModel.getAllPasswords().observe(this, passwords -> {
            adapter.setPasswords(passwords);
            updateGlobalScore();
        });

        viewModel.isCryptoReady().observe(this, ready -> {
            if (ready) {
                adapter.notifyDataSetChanged();
                updateGlobalScore();
            }
        });

        // Initialize progress bar and button state if it's already scanning
        Boolean isScanning = viewModel.getIsScanning().getValue();
        if (isScanning != null && isScanning) {
            binding.btnScan.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void updateGlobalScore() {
        // Method kept for logic if needed, but UI element tvSecurityScore is removed.
    }

    private void setupRecyclerView() {
        adapter = new SecurityAdapter(
                encrypted -> viewModel.decryptPassword(encrypted),
                (entry, view) -> {
                    Intent intent = new Intent(this, com.dilanne.bypass.ui.activities.PasswordDetailActivity.class);
                    intent.putExtra(com.dilanne.bypass.ui.activities.PasswordDetailActivity.EXTRA_PASSWORD_ENTRY, entry);
                    startActivity(intent);
                }
        );
        binding.rvSecurityList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSecurityList.setAdapter(adapter);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnScan.setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_scanning_start, Toast.LENGTH_SHORT).show();
            viewModel.forceSecurityRefresh();
        });

        viewModel.getIsScanning().observe(this, isScanning -> {
            if (isScanning) {
                binding.btnScan.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvSecurityList.setAlpha(0.5f);
            } else {
                binding.btnScan.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                binding.rvSecurityList.setAlpha(1.0f);
                updateGlobalScore();
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_security);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_generator) {
                Intent intent = new Intent(this, GeneratorActivity.class);
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
}

