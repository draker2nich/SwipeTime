package com.draker.swipetime.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.draker.swipetime.R;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.utils.GlideUtils;
import com.draker.swipetime.utils.LikedItemsHelper;

import java.util.List;

/**
 * Улучшенный адаптер для отображения избранного контента с поддержкой загрузки изображений
 */
public class ImprovedLikedContentAdapter extends RecyclerView.Adapter<ImprovedLikedContentAdapter.LikedViewHolder> {

    private List<ContentItem> items;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ContentItem item);
    }

    public ImprovedLikedContentAdapter(Context context, List<ContentItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LikedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_liked_content, parent, false);
        return new LikedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LikedViewHolder holder, int position) {
        ContentItem item = items.get(position);
        holder.bind(item, listener, context);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(List<ContentItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    static class LikedViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private ImageView coverImageView;
        private Chip categoryTextView;
        private ImageView watchedIconView;
        private RatingBar ratingBar;

        public LikedViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_liked_item);
            descriptionTextView = itemView.findViewById(R.id.description_liked_item);
            coverImageView = itemView.findViewById(R.id.cover_liked_item);
            categoryTextView = itemView.findViewById(R.id.category_liked_item);
            watchedIconView = itemView.findViewById(R.id.watched_icon);
            ratingBar = itemView.findViewById(R.id.rating_bar_item);
        }

        public void bind(ContentItem item, OnItemClickListener listener, Context context) {
            titleTextView.setText(item.getTitle());
            descriptionTextView.setText(item.getDescription());
            
            // Загружаем изображение с помощью GlideUtils
            String imageUrl = item.getImageUrl();
            GlideUtils.loadLikedContentImage(context, imageUrl, coverImageView, item.getCategory());
            
            // Устанавливаем категорию в чип с цветовой индикацией
            String categoryText = item.getCategory() != null ? item.getCategory() : "Другое";
            categoryTextView.setText(categoryText);
            
            // Устанавливаем цвет чипа в зависимости от категории
            int chipColorResId = getCategoryColorResource(item.getCategory());
            categoryTextView.setChipBackgroundColor(context.getResources().getColorStateList(chipColorResId, context.getTheme()));
            
            // Устанавливаем видимость иконки "просмотрено/прочитано" и используем правильную иконку
            if (item.isWatched()) {
                watchedIconView.setVisibility(View.VISIBLE);
                
                // Выбираем иконку в зависимости от категории
                int iconResId = LikedItemsHelper.getWatchedIcon(item.getCategory());
                watchedIconView.setImageResource(iconResId);
            } else {
                watchedIconView.setVisibility(View.GONE);
            }
            
            // Устанавливаем рейтинг, если он есть
            if (item.getRating() > 0) {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating(item.getRating() / 2); // Преобразуем 10-балльную шкалу в 5-балльную для RatingBar
            } else {
                ratingBar.setVisibility(View.GONE);
            }
            
            // Обработка нажатия
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
        
        /**
         * Возвращает ресурс цвета для чипа категории
         * @param category название категории
         * @return ID ресурса цвета
         */
        private int getCategoryColorResource(String category) {
            if (category == null) {
                return R.color.chip_default_bg;
            }
            
            switch (category.toLowerCase()) {
                case "фильмы":
                    return R.color.chip_movies_bg;
                case "сериалы":
                    return R.color.chip_tv_shows_bg;
                case "игры":
                    return R.color.chip_games_bg;
                case "книги":
                    return R.color.chip_books_bg;
                case "аниме":
                    return R.color.chip_anime_bg;
                case "музыка":
                    return R.color.chip_music_bg;
                default:
                    return R.color.chip_default_bg;
            }
        }
    }
}
