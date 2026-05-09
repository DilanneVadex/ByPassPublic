package com.dilanne.bypass.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dilanne.bypass.databinding.ItemFavorisBinding;
import com.dilanne.bypass.models.PasswordEntry;

import java.util.function.BiConsumer;
import android.view.View;

public class FavorisAdapter extends ListAdapter<PasswordEntry, FavorisAdapter.FavorisViewHolder> {

    private final BiConsumer<PasswordEntry, View> onItemClick;

    public FavorisAdapter(BiConsumer<PasswordEntry, View> onItemClick) {
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
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public FavorisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavorisBinding binding = ItemFavorisBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FavorisViewHolder(binding, onItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull FavorisViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class FavorisViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavorisBinding binding;
        private final BiConsumer<PasswordEntry, View> onItemClick;

        public FavorisViewHolder(ItemFavorisBinding binding, BiConsumer<PasswordEntry, View> onItemClick) {
            super(binding.getRoot());
            this.binding = binding;
            this.onItemClick = onItemClick;
        }

        public void bind(PasswordEntry entry) {
            binding.tvFavorisTitle.setText(entry.getTitle());
            binding.tvFavorisEmail.setText(entry.getEmail());
            
            binding.getRoot().setOnClickListener(v -> onItemClick.accept(entry, v));
        }
    }
}
