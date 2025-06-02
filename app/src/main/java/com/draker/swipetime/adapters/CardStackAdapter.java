package com.draker.swipetime.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.utils.ImageUtil;
import com.draker.swipetime.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Улучшенный адаптер для карточек контента с поддержкой анимаций и accessibility
 */
public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.CardViewHolder> {

    private Context context;
    private List<ContentItem> items;
    private ThemeManager themeManager;
    private OnCardInteractionListener listener;

    public interface OnCardInteractionListener {
        void onCardSwiped(ContentItem item, boolean isLiked);
        void onCardClicked(ContentItem item);
    }

    public CardStackAdapter(Context context, List<ContentItem> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.themeManager = new ThemeManager(context);
    }

    public void setOnCardInteractionListener(OnCardInteractionListener listener) {
        this.listener = listener;
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
        
        // Заполняем основную информацию
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        
        // Настраиваем accessibility
        setupAccessibility(holder, item, position);
        
        // Заполняем дополнительную информацию в зависимости от категории
        populateContentDetails(holder, item);
        
        // Загружаем изображение
        loadContentImage(holder, item);
        
        // Применяем настройки доступности
        applyAccessibilitySettings(holder);
        
        // Настраиваем click listeners
        setupClickListeners(holder, item);
        
        // Скрываем индикаторы свайпа в начальном состоянии
        resetSwipeIndicators(holder);
        
        // Анимация появления карточки
        animateCardEntry(holder);
    }

    private void setupAccessibility(CardViewHolder holder, ContentItem item, int position) {
        // Создаем детальное описание для screen readers
        String contentDescription = String.format(
            "%s. %s. %s. Карточка %d из %d. Проведите вправо чтобы добавить в избранное, влево чтобы пропустить, или нажмите для подробностей.",
            item.getTitle(),
            item.getCategory(),
            item.getDescription(),
            position + 1,
            getItemCount()
        );
        
        holder.itemView.setContentDescription(contentDescription);
        holder.itemView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        
        // Настраиваем custom accessibility actions
        holder.itemView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                
                // Добавляем custom actions для screen readers
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    AccessibilityNodeInfo.ACTION_CLICK, "Открыть детали"));
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.action_like, "Добавить в избранное"));
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.action_dislike, "Пропустить"));
            }
            
            @Override
            public boolean performAccessibilityAction(View host, int action, android.os.Bundle args) {
                if (action == R.id.action_like) {
                    if (listener != null) {
                        listener.onCardSwiped(item, true);
                    }
                    return true;
                } else if (action == R.id.action_dislike) {
                    if (listener != null) {
                        listener.onCardSwiped(item, false);
                    }
                    return true;
                } else {
                    return super.performAccessibilityAction(host, action, args);
                }
            }
        });
    }

    private void populateContentDetails(CardViewHolder holder, ContentItem item) {
        String subtitle = "";
        String details = "";
        float rating = 0f;
        
        switch (item.getCategory().toLowerCase()) {
            case "фильмы":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Режиссёр: " + (item.getDirector() != null && !item.getDirector().isEmpty() ? item.getDirector() : "Неизвестен");
                rating = item.getRating();
                holder.categoryBadge.setText("ФИЛЬМ");
                break;
            case "сериалы":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Сезонов: " + (item.getSeasons() > 0 ? item.getSeasons() : "N/A") + 
                          " • Эпизодов: " + (item.getEpisodes() > 0 ? item.getEpisodes() : "N/A");
                rating = item.getRating();
                holder.categoryBadge.setText("СЕРИАЛ");
                break;
            case "игры":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Разработчик: " + (item.getDeveloper() != null && !item.getDeveloper().isEmpty() ? item.getDeveloper() : "Неизвестен");
                if (item.getPlatforms() != null && !item.getPlatforms().isEmpty()) {
                    details += " • " + item.getPlatforms();
                }
                rating = item.getRating();
                holder.categoryBadge.setText("ИГРА");
                break;
            case "книги":
                subtitle = (item.getAuthor() != null && !item.getAuthor().isEmpty() ? item.getAuthor() : "Неизвестный автор");
                details = "Издательство: " + (item.getPublisher() != null && !item.getPublisher().isEmpty() ? item.getPublisher() : "Неизвестно");
                if (item.getYear() > 0) {
                    details += " • " + item.getYear();
                }
                if (item.getPages() > 0) {
                    details += " • " + item.getPages() + " стр.";
                }
                rating = item.getRating();
                holder.categoryBadge.setText("КНИГА");
                break;
            case "аниме":
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                details = "Студия: " + (item.getStudio() != null && !item.getStudio().isEmpty() ? item.getStudio() : "Неизвестна");
                if (item.getEpisodes() > 0) {
                    details += " • " + item.getEpisodes() + " эп.";
                }
                rating = item.getRating();
                holder.categoryBadge.setText("АНИМЕ");
                break;
            default:
                subtitle = item.getGenre() + (item.getYear() > 0 ? " • " + item.getYear() : "");
                holder.categoryBadge.setText("КОНТЕНТ");
                break;
        }
        
        holder.subtitle.setText(subtitle);
        holder.details.setText(details);
        holder.rating.setRating(rating / 2); // Преобразуем рейтинг в 5-звездочную шкалу
        
        // Настраиваем цвет badge в зависимости от категории
        setCategoryBadgeColor(holder, item.getCategory());
    }

    private void setCategoryBadgeColor(CardViewHolder holder, String category) {
        int colorResId;
        switch (category.toLowerCase()) {
            case "фильмы":
                colorResId = R.color.movieColor;
                break;
            case "сериалы":
                colorResId = R.color.tvShowColor;
                break;
            case "игры":
                colorResId = R.color.gameColor;
                break;
            case "книги":
                colorResId = R.color.bookColor;
                break;
            case "аниме":
                colorResId = R.color.animeColor;
                break;
            case "музыка":
                colorResId = R.color.musicColor;
                break;
            default:
                colorResId = R.color.defaultColor;
                break;
        }
        
        holder.categoryBadge.setBackgroundTintList(
            context.getResources().getColorStateList(colorResId, context.getTheme())
        );
    }

    private void loadContentImage(CardViewHolder holder, ContentItem item) {
        String imageUrl = ImageUtil.getFallbackImageUrl(item.getImageUrl(), item.getCategory());
        ImageUtil.loadCardImage(context, imageUrl, holder.image, item.getCategory());
    }

    private void applyAccessibilitySettings(CardViewHolder holder) {
        float textSizeMultiplier = themeManager.getTextSizeMultiplier();
        
        if (textSizeMultiplier != 1.0f) {
            // Применяем увеличенный размер текста
            holder.title.setTextSize(holder.title.getTextSize() * textSizeMultiplier);
            holder.subtitle.setTextSize(holder.subtitle.getTextSize() * textSizeMultiplier);
            holder.description.setTextSize(holder.description.getTextSize() * textSizeMultiplier);
            holder.details.setTextSize(holder.details.getTextSize() * textSizeMultiplier);
            holder.categoryBadge.setTextSize(holder.categoryBadge.getTextSize() * textSizeMultiplier);
        }
        
        // Применяем высокий контраст если включен
        if (themeManager.isHighContrastEnabled()) {
            int highContrastTextColor = context.getColor(R.color.high_contrast_text);
            holder.title.setTextColor(highContrastTextColor);
            holder.subtitle.setTextColor(highContrastTextColor);
            holder.description.setTextColor(highContrastTextColor);
            holder.details.setTextColor(highContrastTextColor);
        }
    }

    private void setupClickListeners(CardViewHolder holder, ContentItem item) {
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClicked(item);
            }
        });
        
        // Accessibility focus handling
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateCardFocus(holder, true);
            } else {
                animateCardFocus(holder, false);
            }
        });
    }

    private void resetSwipeIndicators(CardViewHolder holder) {
        holder.leftIndicator.setAlpha(0f);
        holder.rightIndicator.setAlpha(0f);
        holder.leftIndicator.setScaleX(0.8f);
        holder.leftIndicator.setScaleY(0.8f);
        holder.rightIndicator.setScaleX(0.8f);
        holder.rightIndicator.setScaleY(0.8f);
    }

    private void animateCardEntry(CardViewHolder holder) {
        if (themeManager.isReduceMotionEnabled()) {
            return; // Пропускаем анимации если включен режим упрощенных анимаций
        }
        
        holder.itemView.setAlpha(0f);
        holder.itemView.setScaleX(0.9f);
        holder.itemView.setScaleY(0.9f);
        
        long duration = themeManager.getAnimationDuration(300);
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 0.9f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animatorSet.start();
    }

    private void animateCardFocus(CardViewHolder holder, boolean focused) {
        if (themeManager.isReduceMotionEnabled()) {
            return;
        }
        
        float scale = focused ? 1.05f : 1f;
        long duration = themeManager.getAnimationDuration(150);
        
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(holder.itemView, "scaleX", scale);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(holder.itemView, "scaleY", scale);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        animatorSet.setDuration(duration);
        animatorSet.start();
    }

    /**
     * Анимирует индикаторы свайпа
     */
    public void animateSwipeIndicator(int position, float swipeProgress, boolean isLeftSwipe) {
        if (position < 0 || position >= items.size() || themeManager.isReduceMotionEnabled()) {
            return;
        }
        
        // Этот метод будет вызываться из CardStackView при свайпе
        // Здесь мы можем анимировать индикаторы
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ContentItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void clear() {
        int size = this.items.size();
        this.items.clear();
        notifyItemRangeRemoved(0, size);
    }
    
    public void addItems(List<ContentItem> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            return;
        }
        int startPos = this.items.size();
        this.items.addAll(newItems);
        notifyItemRangeInserted(startPos, newItems.size());
    }

    public List<ContentItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(ContentItem item) {
        if (item != null) {
            this.items.add(item);
            notifyItemInserted(this.items.size() - 1);
        }
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
