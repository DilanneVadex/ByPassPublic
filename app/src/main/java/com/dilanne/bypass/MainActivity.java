package com.dilanne.bypass;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dilanne.bypass.databinding.ActivityMainBinding;
import com.dilanne.bypass.models.Category;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.adapters.CategoryAdapter;
import com.dilanne.bypass.ui.adapters.FavorisAdapter;
import com.dilanne.bypass.ui.adapters.PasswordAdapter;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PasswordViewModel viewModel;
    private PasswordAdapter passwordAdapter;
    private FavorisAdapter favorisAdapter;
    private CategoryAdapter categoryAdapter;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<PasswordEntry>> filteredPasswords = new MediatorLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        setupRecyclerViews();
        setupSearch();
        observeViewModel();
        setupBottomNav();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.etSearch.getParent().requestLayout();
                // On pourrait ajouter une animation ici
            }
        });
    }

    private void setupRecyclerViews() {
        // Categories
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(getString(R.string.cat_social), R.drawable.team_fill));
        categories.add(new Category(getString(R.string.cat_work), R.drawable.briefcase_4_fill));
        categories.add(new Category(getString(R.string.cat_bank), R.drawable.bank_card_fill));
        categories.add(new Category(getString(R.string.cat_shopping), R.drawable.shopping_cart_2_fill));
        
        categoryAdapter = new CategoryAdapter(categories);
        binding.rvCategories.setAdapter(categoryAdapter);

        // Passwords List (Vertical)
        passwordAdapter = new PasswordAdapter(this::handleTogglePassword, this::handleEditPassword);
        binding.rvPasswords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPasswords.setAdapter(passwordAdapter);

        // Favoris List (Horizontal)
        favorisAdapter = new FavorisAdapter();
        binding.rvFavoris.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvFavoris.setAdapter(favorisAdapter);
    }

    private void observeViewModel() {
        filteredPasswords.addSource(viewModel.getAllPasswords(), passwords -> {
            filterAndPopulate(passwords, searchQuery.getValue());
        });

        filteredPasswords.addSource(searchQuery, query -> {
            filterAndPopulate(viewModel.getAllPasswords().getValue(), query);
        });

        filteredPasswords.observe(this, passwords -> {
            passwordAdapter.submitList(passwords);
            
            // Update Favoris based on the full list (or filtered if preferred)
            List<PasswordEntry> favoris = passwords.stream()
                    .filter(PasswordEntry::isFavorite)
                    .collect(Collectors.toList());
            favorisAdapter.submitList(favoris);
        });
    }

    private void filterAndPopulate(List<PasswordEntry> passwords, String query) {
        if (passwords == null) return;
        if (query == null || query.isEmpty()) {
            filteredPasswords.setValue(passwords);
        } else {
            String lowerQuery = query.toLowerCase();
            List<PasswordEntry> filtered = passwords.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(lowerQuery) || 
                                 p.getEmail().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList());
            filteredPasswords.setValue(filtered);
        }
    }

    private void handleTogglePassword(PasswordEntry entry, PasswordAdapter.PasswordViewHolder holder) {
        if (!holder.isPasswordVisible()) {
            String decrypted = viewModel.decryptPassword(entry.getEncryptedPassword());
            holder.setPasswordText(decrypted, true);
        } else {
            holder.setPasswordText("••••••••", false);
        }
    }

    private void handleEditPassword(PasswordEntry entry, View view) {
        Toast.makeText(this, "Edit: " + entry.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_generator) {
                Toast.makeText(this, "Générateur", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_security) {
                Toast.makeText(this, "Sécurité", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Paramètres", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
