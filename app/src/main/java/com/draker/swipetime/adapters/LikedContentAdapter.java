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
import com.draker.swipetime.models.ContentItem;

import java.util.List;

public class LikedContentAdapter extends RecyclerView.Adapter<LikedContentAdapter.LikedViewHolder> {

    private List<ContentItem> items;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ContentItem item);
    }

    public LikedContentAdapter(Context context, List<ContentItem> items, OnItemClickListener listener) {
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
        holder.bind(item, listener);
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
        private TextView categoryTextView;

        public LikedViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_liked_item);
            descriptionTextView = itemView.findViewById(R.id.description_liked_item);
            coverImageView = itemView.findViewById(R.id.cover_liked_item);
            categoryTextView = itemView.findViewById(R.id.category_liked_item);
        }

        public void bind(ContentItem item, OnItemClickListener listener) {
            titleTextView.setText(item.getTitle());
            descriptionTextView.setText(item.getDescription());
            
            // Устанавливаем заглушку для изображения
            coverImageView.setImageResource(R.drawable.placeholder_image);
            
            categoryTextView.setText(item.getCategory());
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
