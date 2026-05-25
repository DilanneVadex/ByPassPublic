package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dilanne.bypass.MainActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.api.HibpService;
import com.dilanne.bypass.databinding.ActivitySecurityBinding;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.adapters.SecurityAdapter;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.scoring.Result;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SecurityActivity extends AppCompatActivity {

    private ActivitySecurityBinding binding;
    private PasswordViewModel viewModel;
    private SecurityAdapter adapter;
    private HibpService hibpService;
    private Nbvcxz nbvcxz;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        nbvcxz = new Nbvcxz();
        initRetrofit();
        setupRecyclerView();
        setupActions();

        viewModel.getAllPasswords().observe(this, passwords -> {
            adapter.setPasswords(passwords);
        });
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.pwnedpasswords.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(new OkHttpClient())
                .build();
        hibpService = retrofit.create(HibpService.class);
    }

    private void setupRecyclerView() {
        adapter = new SecurityAdapter(encrypted -> viewModel.decryptPassword(encrypted));
        binding.rvSecurityList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSecurityList.setAdapter(adapter);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnScan.setOnClickListener(v -> scanAllPasswords());

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

    private void scanAllPasswords() {
        List<PasswordEntry> passwords = adapter.getPasswords();
        if (passwords.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_pass_to_scan), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.toast_scanning, passwords.size()), Toast.LENGTH_SHORT).show();

        for (int i = 0; i < passwords.size(); i++) {
            analyzePassword(i, passwords.get(i));
        }
    }

    private void analyzePassword(int position, PasswordEntry entry) {
        String plainPassword = viewModel.decryptPassword(entry.getEncryptedPassword());
        if (plainPassword == null) {
            SecurityAdapter.PasswordSecurityInfo info = new SecurityAdapter.PasswordSecurityInfo(this);
            info.strength = getString(R.string.strength_unknown);
            info.strengthColor = Color.GRAY;
            info.status = getString(R.string.status_error);
            info.statusColor = Color.GRAY;
            adapter.updateSecurityInfo(position, info);
            return;
        }

        SecurityAdapter.PasswordSecurityInfo info = new SecurityAdapter.PasswordSecurityInfo(this);

        // 1. Nbvcxz Analysis
        Result result = nbvcxz.estimate(plainPassword);
        double entropy = result.getEntropy();
        
        if (entropy < 40) {
            info.strength = getString(R.string.strength_weak);
            info.strengthColor = Color.parseColor("#F44336"); // Red
        } else if (entropy < 80) {
            info.strength = getString(R.string.strength_medium);
            info.strengthColor = Color.parseColor("#FF9800"); // Orange
        } else {
            info.strength = getString(R.string.strength_strong);
            info.strengthColor = Color.parseColor("#4CAF50"); // Green
        }

        // 2. HIBP Analysis
        checkHibp(position, plainPassword, info);
    }

    private void checkHibp(int position, String password, SecurityAdapter.PasswordSecurityInfo info) {
        String sha1 = getSha1(password);
        if (sha1 == null) return;

        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5).toUpperCase();

        hibpService.getPasswordRange(prefix).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean found = response.body().contains(suffix);
                    if (found) {
                        info.status = getString(R.string.status_compromised);
                        info.statusColor = Color.parseColor("#F44336");
                        info.isCompromised = true;
                    } else {
                        info.status = getString(R.string.status_secure);
                        info.statusColor = Color.parseColor("#4CAF50");
                        info.isCompromised = false;
                    }
                    adapter.updateSecurityInfo(position, info);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                info.status = getString(R.string.status_error);
                info.statusColor = Color.GRAY;
                adapter.updateSecurityInfo(position, info);
            }
        });
    }

    private String getSha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
