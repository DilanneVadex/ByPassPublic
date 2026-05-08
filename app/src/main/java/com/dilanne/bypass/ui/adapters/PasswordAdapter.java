package com.dilanne.bypass.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ItemPasswordBinding;
import com.dilanne.bypass.models.PasswordEntry;

import java.util.function.BiConsumer;

public class PasswordAdapter extends ListAdapter<PasswordEntry, PasswordAdapter.PasswordViewHolder> {

    private  final BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility;
    private  final BiConsumer<PasswordEntry, View> onEditClick;

    public PasswordAdapter(BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility, BiConsumer<PasswordEntry, View> onEditClick) {
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
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPasswordBinding binding = ItemPasswordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PasswordViewHolder(binding, onToggleVisibility, onEditClick);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        private final ItemPasswordBinding binding;
        private final BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility;
        private final BiConsumer<PasswordEntry, View> onEditClick;
        private boolean isVisible = false;

        public PasswordViewHolder(ItemPasswordBinding binding, BiConsumer<PasswordEntry, PasswordViewHolder> onToggleVisibility, BiConsumer<PasswordEntry, View> onEditClick) {
            super(binding.getRoot());
            this.binding = binding;
            this.onToggleVisibility = onToggleVisibility;
            this.onEditClick = onEditClick;
        }

        public void bind(PasswordEntry entry) {
            binding.tvServiceTitle.setText(entry.getTitle());
            binding.tvUserEmail.setText(entry.getEmail());
            binding.tvMaskedPassword.setText("••••••••");
            
            binding.ivToggleVisibility.setOnClickListener(v -> {
                isVisible = !isVisible;
                onToggleVisibility.accept(entry, this);
            });

            binding.ivEdit.setOnClickListener(v -> onEditClick.accept(entry, v));
        }

        public void setPasswordText(String text, boolean visible) {
            binding.tvMaskedPassword.setText(text);
            binding.ivToggleVisibility.setImageResource(visible ? R.drawable.eye_2_line : android.R.drawable.ic_menu_view);
        }
        
        public boolean isPasswordVisible() {
            return isVisible;
        }
    }
}
