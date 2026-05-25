package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivityAddAccountBinding;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;

public class AddAccountActivity extends AppCompatActivity {

    public static final String EXTRA_PASSWORD_ENTRY = "extra_password_entry";

    private ActivityAddAccountBinding binding;
    private PasswordViewModel viewModel;
    private PasswordEntry editingEntry;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        if (getIntent().hasExtra(EXTRA_PASSWORD_ENTRY)) {
            editingEntry = (PasswordEntry) getIntent().getSerializableExtra(EXTRA_PASSWORD_ENTRY);
            setupEditMode();
        } else {
            setupAddMode();
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> handleSave());

        setupLivePreview();
    }

    private void setupAddMode() {
        binding.tvTitle.setText(R.string.title_add_account);
    }

    private void setupEditMode() {
        binding.tvTitle.setText(getString(R.string.title_edit_account_header, editingEntry.getTitle()));
        binding.etUrl.setText(editingEntry.getUrl());
        binding.etLogin.setText(editingEntry.getEmail()); 
        
        String decrypted = viewModel.decryptPassword(editingEntry.getEncryptedPassword());
        binding.etPassword.setText(decrypted);
        
        updatePreview();
    }

    private void setupLivePreview() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        binding.etUrl.addTextChangedListener(watcher);
        binding.etLogin.addTextChangedListener(watcher);
    }

    private void updatePreview() {
        String url = binding.etUrl.getText().toString().trim();
        String login = binding.etLogin.getText().toString().trim();

        String displayUrl = url.isEmpty() ? (editingEntry != null ? editingEntry.getTitle() : "Service") : url;
        String displayMail = login.isEmpty() ? (editingEntry != null ? editingEntry.getEmail() : "email") : login;

        binding.tvPreview.setText(displayUrl + " • " + displayMail);

        // Mise à jour de l'icône en temps réel
        String faviconUrl = "";
        if (!url.isEmpty()) {
            faviconUrl = "https://icons.duckduckgo.com/ip3/" + extractDomain(url) + ".ico";
        } else if (editingEntry != null && editingEntry.getUrl() != null) {
            faviconUrl = "https://icons.duckduckgo.com/ip3/" + extractDomain(editingEntry.getUrl()) + ".ico";
        }

        Glide.with(this)
                .load(faviconUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(binding.ivServiceIcon);
    }

    private String extractDomain(String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            java.net.URI uri = new java.net.URI(url);
            String domain = uri.getHost();
            if (domain != null) {
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
        } catch (Exception e) {
            return url;
        }
        return url;
    }

    private void handleSave() {
        String url = binding.etUrl.getText().toString().trim();
        String login = binding.etLogin.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (url.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        PasswordEntry entry = editingEntry != null ? editingEntry : new PasswordEntry();
        
        if (editingEntry == null) {
            // New entry logic for title
            String title = url;
            if (title.contains(".")) {
                title = title.substring(0, title.lastIndexOf("."));
                if (title.startsWith("www.")) title = title.substring(4);
                title = title.substring(0, 1).toUpperCase() + title.substring(1);
            }
            entry.setTitle(title);
            entry.setCategory("General");
            entry.setFavorite(false);
        }

        entry.setUrl(url);
        entry.setEmail(login);
        entry.setLastModified(System.currentTimeMillis());

        if (editingEntry != null) {
            viewModel.update(entry, password);
            Toast.makeText(this, R.string.toast_account_updated, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.insert(entry, password);
            Toast.makeText(this, R.string.toast_account_added, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
