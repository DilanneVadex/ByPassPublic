package com.dilanne.bypass.ui.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dilanne.bypass.databinding.ActivityAddAccountBinding;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;

public class AddAccountActivity extends AppCompatActivity {

    public static final String EXTRA_PASSWORD_ENTRY = "extra_password_entry";

    private ActivityAddAccountBinding binding;
    private PasswordViewModel viewModel;
    private PasswordEntry editingEntry;

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
        binding.tvTitle.setText("Add Account");
    }

    private void setupEditMode() {
        binding.tvTitle.setText(editingEntry.getTitle() + " Edit Account");
        binding.etUrl.setText(editingEntry.getUrl());
        binding.etLogin.setText(editingEntry.getEmail()); // In this app email/login seem interchangeable in fields
        
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
    }

    private void handleSave() {
        String url = binding.etUrl.getText().toString().trim();
        String login = binding.etLogin.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (url.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Account updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.insert(entry, password);
            Toast.makeText(this, "Account added successfully!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
