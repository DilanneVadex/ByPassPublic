package com.dilanne.bypass.models;

import com.dilanne.bypass.R;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private int iconRes;

    public Category(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getName() { return name; }
    public int getIconRes() { return iconRes; }

    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Social", R.drawable.team_fill));
        categories.add(new Category("Work", R.drawable.briefcase_4_fill));
        categories.add(new Category("Finance", R.drawable.bank_card_fill));
        categories.add(new Category("Shopping", R.drawable.shopping_cart_2_fill));
        categories.add(new Category("Personal", R.drawable.user_6_fill));
        categories.add(new Category("Tools", R.drawable.settings_3_fill));
        categories.add(new Category("Emails", R.drawable.mail_unread_line));
        categories.add(new Category("Security", R.drawable.lock_2_fill));
        categories.add(new Category("Support", R.drawable.customer_service_2_fill));
        categories.add(new Category("Entertainment", R.drawable.shining_fill));
        categories.add(new Category("Education", R.drawable.information_2_fill));
        categories.add(new Category("Others", R.drawable.layout_grid_fill));
        return categories;
    }

    public static int getIconForCategory(String categoryName) {
        for (Category cat : getAllCategories()) {
            if (cat.getName().equalsIgnoreCase(categoryName)) {
                return cat.getIconRes();
            }
        }
        return R.drawable.layout_grid_fill; // Default
    }
}
