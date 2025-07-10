package com.example.cinestreamlive.model;

import org.json.JSONObject;
import org.json.JSONException;

public class Category {
    private String categoryId;
    private String categoryName;
    private String parentId; // Pode ser 0 ou null para categorias raiz

    public Category(String categoryId, String categoryName, String parentId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentId = parentId;
    }

    // Getters
    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getParentId() {
        return parentId;
    }

    public static Category fromJson(JSONObject jsonObject) throws JSONException {
        String categoryId = jsonObject.getString("category_id");
        String categoryName = jsonObject.getString("category_name");
        String parentId = jsonObject.optString("parent_id", "0"); // parent_id pode ser null ou não existir

        return new Category(categoryId, categoryName, parentId);
    }

    @Override
    public String toString() { // Útil para logging e debug
        return categoryName;
    }
}
