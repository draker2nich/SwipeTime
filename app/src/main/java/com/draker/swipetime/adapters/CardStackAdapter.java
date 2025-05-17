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
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        ContentItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        
        // Проверяем валидность URL изображения и используем заглушки при необходимости
        String imageUrl = com.draker.swipetime.utils.ImageUtil.getFallbackImageUrl(
                item.getImageUrl(), 
                item.getCategory()
        );
        
        // Загрузка изображения с помощью Glide
        try {
            com.bumptech.glide.Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.image);
        } catch (Exception e) {
            // В случае ошибки показываем заглушку
            holder.image.setImageResource(R.drawable.placeholder_image);
            Log.e("CardStackAdapter", "Error loading image: " + e.getMessage());
        }
        
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
        TextView description;
        ImageView leftIndicator;
        ImageView rightIndicator;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            leftIndicator = itemView.findViewById(R.id.left_indicator);
            rightIndicator = itemView.findViewById(R.id.right_indicator);
        }
    }
}
