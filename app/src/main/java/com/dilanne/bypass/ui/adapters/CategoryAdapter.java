package com.dilanne.bypass.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ItemCategoryBinding;
import com.dilanne.bypass.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private final OnCategoryClickListener listener;
    private int selectedPosition = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    public void setSelectedCategory(String categoryName) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equalsIgnoreCase(categoryName)) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        public CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Category category, boolean isSelected) {
            binding.tvCategoryName.setText(category.getName());
            binding.ivCategoryIcon.setImageResource(category.getIconRes());
            
            // Visual feedback for selection
            if (isSelected) {
                binding.getRoot().setBackgroundResource(R.drawable.bg_category_item_selected);
                binding.ivCategoryIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.white));
                binding.tvCategoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.white));
            } else {
                binding.getRoot().setBackgroundResource(R.drawable.bg_category_item);
                binding.ivCategoryIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.secondary));
                binding.tvCategoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.white));
            }

            binding.getRoot().setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                if (selectedPosition == getAdapterPosition()) {
                    selectedPosition = -1; // Deselect
                } else {
                    selectedPosition = getAdapterPosition();
                }
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                
                listener.onCategoryClick(selectedPosition == -1 ? null : category);
            });
        }
    }
}
