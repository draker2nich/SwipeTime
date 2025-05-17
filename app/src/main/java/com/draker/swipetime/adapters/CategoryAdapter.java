package com.draker.swipetime.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.draker.swipetime.R;
import com.draker.swipetime.models.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    // Интерфейс для обработки кликов
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        
        holder.categoryName.setText(category.getName());
        holder.categoryIcon.setImageResource(category.getIconResId());
        
        // Настройка цвета иконки в зависимости от категории
        int colorResId;
        int itemCount = 0;
        
        switch(category.getName()) {
            case "Фильмы":
                colorResId = R.color.movieColor;
                itemCount = 120; // Примерные значения, в реальном приложении должно быть из репозитория
                break;
            case "Сериалы":
                colorResId = R.color.tvShowColor;
                itemCount = 85;
                break;
            case "Игры":
                colorResId = R.color.gameColor;
                itemCount = 70;
                break;
            case "Книги":
                colorResId = R.color.bookColor;
                itemCount = 95;
                break;
            case "Аниме":
                colorResId = R.color.animeColor;
                itemCount = 60;
                break;
            default:
                colorResId = R.color.defaultColor;
                itemCount = 50;
                break;
        }
        
        // Устанавливаем цвет иконки
        holder.categoryIcon.setColorFilter(context.getResources().getColor(colorResId, null));
        
        // Устанавливаем количество элементов
        holder.categoryCount.setText(itemCount + " элементов");
        
        holder.itemView.setOnClickListener(v -> {
            // Вызов метода в слушателе при клике на категорию
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // ViewHolder для элемента категории
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;
        TextView categoryCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryCount = itemView.findViewById(R.id.category_count);
        }
    }
}
