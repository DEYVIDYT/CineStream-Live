package com.example.cinestreamlive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinestreamlive.model.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Para destacar o item selecionado

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryNameTextView.setText(category.getCategoryName());

        holder.itemView.setSelected(selectedPosition == position); // Para feedback visual de seleção

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int previousSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousSelectedPosition); // Atualiza o item anteriormente selecionado
                notifyItemChanged(selectedPosition);       // Atualiza o novo item selecionado
                listener.onCategoryClick(category, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categoryList.clear();
        if (newCategories != null) {
            this.categoryList.addAll(newCategories);
        }
        selectedPosition = RecyclerView.NO_POSITION; // Reset selection
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousSelectedPosition = selectedPosition;
        selectedPosition = position;
        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    public Category getCategoryAt(int position) {
        if (position >= 0 && position < categoryList.size()) {
            return categoryList.get(position);
        }
        return null;
    }


    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.category_name_textview);
        }
    }
}
