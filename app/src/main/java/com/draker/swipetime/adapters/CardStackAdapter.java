package com.draker.swipetime.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.CardViewHolder> {

    private Context context;
    private List<ContentItem> items;

    public CardStackAdapter(Context context, List<ContentItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_improved, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        ContentItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        
        // Заполняем дополнительную информацию
        // Подзаголовок: для фильмов и сериалов - жанр и год, для книг - автор, для игр - разработчик, для аниме - студия
        String subtitle = "";
        String details = "";
        float rating = 0f;
        
        switch (item.getCategory().toLowerCase()) {
            case "фильмы":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Режиссёр: " + (item.getDirector() != null && !item.getDirector().isEmpty() ? item.getDirector() : "Неизвестен");
                rating = item.getRating();
                holder.categoryBadge.setText("ФИЛЬМ");
                holder.categoryBadge.setBackgroundResource(R.color.movieColor);
                break;
            case "сериалы":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Сезонов: " + (item.getSeasons() > 0 ? item.getSeasons() : "N/A") + 
                          " • Эпизодов: " + (item.getEpisodes() > 0 ? item.getEpisodes() : "N/A");
                rating = item.getRating();
                holder.categoryBadge.setText("СЕРИАЛ");
                holder.categoryBadge.setBackgroundResource(R.color.tvShowColor);
                break;
            case "игры":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Разработчик: " + (item.getDeveloper() != null && !item.getDeveloper().isEmpty() ? item.getDeveloper() : "Неизвестен") +
                          " • Платформы: " + (item.getPlatforms() != null && !item.getPlatforms().isEmpty() ? item.getPlatforms() : "Разные");
                rating = item.getRating();
                holder.categoryBadge.setText("ИГРА");
                holder.categoryBadge.setBackgroundResource(R.color.gameColor);
                break;
            case "книги":
                subtitle = (item.getAuthor() != null && !item.getAuthor().isEmpty() ? item.getAuthor() : "Неизвестный автор");
                details = "Издательство: " + (item.getPublisher() != null && !item.getPublisher().isEmpty() ? item.getPublisher() : "Неизвестно") +
                          (item.getYear() > 0 ? " • " + item.getYear() : "") +
                          (item.getPages() > 0 ? " • " + item.getPages() + " стр." : "");
                rating = item.getRating();
                holder.categoryBadge.setText("КНИГА");
                holder.categoryBadge.setBackgroundResource(R.color.bookColor);
                break;
            case "аниме":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Студия: " + (item.getStudio() != null && !item.getStudio().isEmpty() ? item.getStudio() : "Неизвестна") +
                          " • Эпизодов: " + (item.getEpisodes() > 0 ? item.getEpisodes() : "N/A");
                rating = item.getRating();
                holder.categoryBadge.setText("АНИМЕ");
                holder.categoryBadge.setBackgroundResource(R.color.animeColor);
                break;
            default:
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                holder.categoryBadge.setText("КОНТЕНТ");
                holder.categoryBadge.setBackgroundResource(R.color.defaultColor);
                break;
        }
        
        holder.subtitle.setText(subtitle);
        holder.details.setText(details);
        holder.rating.setRating(rating / 2); // Преобразуем рейтинг в 5-звездочную шкалу
        
        // Проверяем валидность URL изображения и используем заглушки при необходимости
        String imageUrl = com.draker.swipetime.utils.ImageUtil.getFallbackImageUrl(
                item.getImageUrl(), 
                item.getCategory()
        );
        
        // Загрузка изображения с помощью нашей улучшенной утилиты
        ImageUtil.loadCardImage(context, imageUrl, holder.image, item.getCategory());
        
        // Скрываем индикаторы свайпа в начальном состоянии
        holder.leftIndicator.setAlpha(0f);
        holder.rightIndicator.setAlpha(0f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ContentItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * Очистить список элементов
     */
    public void clear() {
        int size = this.items.size();
        this.items.clear();
        notifyItemRangeRemoved(0, size);
    }
    
    /**
     * Добавить список элементов
     * @param newItems список новых элементов
     */
    public void addItems(List<ContentItem> newItems) {
        int startPos = this.items.size();
        this.items.addAll(newItems);
        notifyItemRangeInserted(startPos, newItems.size());
    }

    public List<ContentItem> getItems() {
        return items;
    }

    public void addItem(ContentItem item) {
        this.items.add(item);
        notifyItemInserted(this.items.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            this.items.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;
        TextView description;
        TextView details;
        android.widget.RatingBar rating;
        TextView categoryBadge;
        ImageView leftIndicator;
        ImageView rightIndicator;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
            description = itemView.findViewById(R.id.card_description);
            details = itemView.findViewById(R.id.card_details);
            rating = itemView.findViewById(R.id.card_rating);
            categoryBadge = itemView.findViewById(R.id.card_category_badge);
            leftIndicator = itemView.findViewById(R.id.left_indicator);
            rightIndicator = itemView.findViewById(R.id.right_indicator);
        }
    }
}
