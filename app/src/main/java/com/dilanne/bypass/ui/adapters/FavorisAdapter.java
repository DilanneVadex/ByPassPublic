package com.dilanne.bypass.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dilanne.bypass.databinding.ItemFavorisBinding;
import com.dilanne.bypass.models.PasswordEntry;

public class FavorisAdapter extends ListAdapter<PasswordEntry, FavorisAdapter.FavorisViewHolder> {

    public FavorisAdapter() {
        super(new DiffUtil.ItemCallback<PasswordEntry>() {
            @Override
            public boolean areItemsTheSame(@NonNull PasswordEntry oldItem, @NonNull PasswordEntry newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PasswordEntry oldItem, @NonNull PasswordEntry newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getEmail().equals(newItem.getEmail());
            }
        });
    }

    @NonNull
    @Override
    public FavorisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavorisBinding binding = ItemFavorisBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FavorisViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavorisViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class FavorisViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavorisBinding binding;

        public FavorisViewHolder(ItemFavorisBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PasswordEntry entry) {
            binding.tvFavorisTitle.setText(entry.getTitle());
            binding.tvFavorisEmail.setText(entry.getEmail());
            // Logo handling can be added later
        }
    }
}
