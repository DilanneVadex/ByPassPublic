package com.dilanne.bypass.ui.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ItemPasswordBinding;
import com.dilanne.bypass.models.PasswordEntry;

import java.util.function.BiConsumer;

public class PasswordAdapter extends ListAdapter<PasswordEntry, PasswordAdapter.PasswordViewHolder> {

    private final BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility;
    private final BiConsumer<PasswordEntry, View> onEditClick;
    private final BiConsumer<PasswordEntry, View> onItemClick;

    public PasswordAdapter(BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility, 
                          BiConsumer<PasswordEntry, View> onEditClick,
                          BiConsumer<PasswordEntry, View> onItemClick) {
        super(new DiffUtil.ItemCallback<PasswordEntry>() {
            @Override
            public boolean areItemsTheSame(@NonNull PasswordEntry oldItem, @NonNull PasswordEntry newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PasswordEntry oldItem, @NonNull PasswordEntry newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getEmail().equals(newItem.getEmail()) &&
                        oldItem.getEncryptedPassword().equals(newItem.getEncryptedPassword());
            }
        });
        this.onToggleVisibility = onToggleVisibility;
        this.onEditClick = onEditClick;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPasswordBinding binding = ItemPasswordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PasswordViewHolder(binding, onToggleVisibility, onEditClick, onItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        private final ItemPasswordBinding binding;
        private final BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility;
        private final BiConsumer<PasswordEntry, View> onEditClick;
        private final BiConsumer<PasswordEntry, View> onItemClick;
        private boolean isVisible = false;

        public PasswordViewHolder(ItemPasswordBinding binding, 
                                 BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility, 
                                 BiConsumer<PasswordEntry, View> onEditClick,
                                 BiConsumer<PasswordEntry, View> onItemClick) {
            super(binding.getRoot());
            this.binding = binding;
            this.onToggleVisibility = onToggleVisibility;
            this.onEditClick = onEditClick;
            this.onItemClick = onItemClick;
        }

        public void bind(PasswordEntry entry) {
            isVisible = false;
            binding.tvServiceTitle.setText(entry.getTitle());
            binding.tvUserEmail.setText(entry.getEmail());
            binding.tvMaskedPassword.setText("••••••••");
            binding.tvMaskedPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            binding.ivToggleVisibility.setImageResource(R.drawable.eye_close_line);

            // Charger l'icône du site web avec Glide
            String faviconUrl = "";
            if (entry.getUrl() != null && !entry.getUrl().isEmpty()) {
                // Utilisation de l'API DuckDuckGo ou Google pour récupérer le favicon
                faviconUrl = "https://icons.duckduckgo.com/ip3/" + extractDomain(entry.getUrl()) + ".ico";
            }

            Glide.with(binding.ivServiceLogo.getContext())
                    .load(faviconUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.ivServiceLogo);
            
            binding.ivToggleVisibility.setOnClickListener(v -> {
                isVisible = !isVisible;
                onToggleVisibility.accept(entry, this);
            });

            binding.ivEdit.setOnClickListener(v -> onEditClick.accept(entry, v));
            
            binding.getRoot().setOnClickListener(v -> onItemClick.accept(entry, v));
        }

        public void setPasswordText(String text, boolean visible) {
            binding.tvMaskedPassword.setText(text);
            binding.tvMaskedPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, visible ? 12 : 16);
            binding.ivToggleVisibility.setImageResource(visible ? R.drawable.eye_2_line : R.drawable.eye_close_line);
        }
        
        public boolean isPasswordVisible() {
            return isVisible;
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
    }
}
