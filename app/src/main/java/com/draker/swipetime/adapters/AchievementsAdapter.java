package com.draker.swipetime.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.utils.GamificationManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

/**
 * Адаптер для отображения списка достижений пользователя
 */
public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private List<GamificationManager.UserAchievementInfo> achievements = new ArrayList<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    
    public AchievementsAdapter() {
    }
    
    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        GamificationManager.UserAchievementInfo achievementInfo = achievements.get(position);
        AchievementEntity achievement = achievementInfo.getAchievement();
        
        Log.d("AchievementsAdapter", "Привязка достижения " + position + ": " + achievement.getTitle());
        
        holder.titleText.setText(achievement.getTitle());
        holder.descriptionText.setText(achievement.getDescription());
        holder.experienceText.setText("+" + achievement.getExperienceReward() + " XP");
        
        // Устанавливаем прогресс
        holder.progressBar.setProgress(achievementInfo.getProgress());
        
        // Отображаем дату получения для выполненных достижений
        if (achievementInfo.isCompleted()) {
            String completionDate = DATE_FORMAT.format(new Date(achievementInfo.getCompletionDate()));
            holder.statusText.setText("Получено: " + completionDate);
            holder.statusText.setVisibility(View.VISIBLE);
            
            // Делаем иконку цветной, если достижение получено
            holder.iconImage.setAlpha(1.0f);
        } else {
            // Прогресс для невыполненных достижений
            holder.statusText.setText("Прогресс: " + achievementInfo.getProgress() + "%");
            holder.statusText.setVisibility(View.VISIBLE);
            
            // Делаем иконку серой, если достижение не получено
            holder.iconImage.setAlpha(0.5f);
        }
        
        // Устанавливаем иконку достижения
        int iconResourceId = holder.itemView.getContext().getResources().getIdentifier(
                achievement.getIconUrl(), "drawable", holder.itemView.getContext().getPackageName());
        
        if (iconResourceId != 0) {
            holder.iconImage.setImageResource(iconResourceId);
        } else {
            // Устанавливаем иконку по умолчанию
            holder.iconImage.setImageResource(R.drawable.ic_achievement);
        }
        
        // Устанавливаем иконку категории достижения
        setCategoryIcon(holder.categoryIcon, achievement.getCategory());
    }
    
    @Override
    public int getItemCount() {
        return achievements.size();
    }
    
    /**
     * Обновить список достижений
     * 
     * @param achievements новый список достижений
     */
    public void setAchievements(List<GamificationManager.UserAchievementInfo> achievements) {
        Log.d("AchievementsAdapter", "Установка достижений в адаптер: " + achievements.size());
        this.achievements = sortAchievements(achievements);
        Log.d("AchievementsAdapter", "После сортировки: " + this.achievements.size());
        notifyDataSetChanged();
    }
    
    /**
     * Сортировка достижений: сначала выполненные, затем по прогрессу выполнения
     * 
     * @param achievements список достижений
     * @return отсортированный список
     */
    private List<GamificationManager.UserAchievementInfo> sortAchievements(List<GamificationManager.UserAchievementInfo> achievements) {
        List<GamificationManager.UserAchievementInfo> sortedList = new ArrayList<>(achievements);
        
        Collections.sort(sortedList, new Comparator<GamificationManager.UserAchievementInfo>() {
            @Override
            public int compare(GamificationManager.UserAchievementInfo o1, GamificationManager.UserAchievementInfo o2) {
                // Сначала сортируем по статусу (выполненные впереди)
                if (o1.isCompleted() && !o2.isCompleted()) {
                    return -1;
                } else if (!o1.isCompleted() && o2.isCompleted()) {
                    return 1;
                }
                
                // Если оба выполнены или оба не выполнены, сортируем по прогрессу
                return Integer.compare(o2.getProgress(), o1.getProgress());
            }
        });
        
        return sortedList;
    }
    
    /**
     * Установить иконку категории достижения
     * 
     * @param imageView ImageView для иконки
     * @param category категория достижения
     */
    private void setCategoryIcon(ImageView imageView, String category) {
        int iconResourceId;
        
        switch (category) {
            case GamificationManager.CATEGORY_BEGINNER:
                iconResourceId = R.drawable.ic_category_beginner;
                break;
            case GamificationManager.CATEGORY_INTERMEDIATE:
                iconResourceId = R.drawable.ic_category_intermediate;
                break;
            case GamificationManager.CATEGORY_ADVANCED:
                iconResourceId = R.drawable.ic_category_advanced;
                break;
            case GamificationManager.CATEGORY_EXPERT:
                iconResourceId = R.drawable.ic_category_expert;
                break;
            case GamificationManager.CATEGORY_COLLECTOR:
                iconResourceId = R.drawable.ic_category_collector;
                break;
            case GamificationManager.CATEGORY_SOCIAL:
                iconResourceId = R.drawable.ic_category_social;
                break;
            case GamificationManager.CATEGORY_STREAK:
                iconResourceId = R.drawable.ic_category_streak;
                break;
            default:
                iconResourceId = R.drawable.ic_category_default;
                break;
        }
        
        imageView.setImageResource(iconResourceId);
    }
    
    /**
     * ViewHolder для элемента достижения
     */
    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView titleText;
        TextView descriptionText;
        TextView experienceText;
        TextView statusText;
        ProgressBar progressBar;
        ImageView categoryIcon;
        
        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.achievement_icon);
            titleText = itemView.findViewById(R.id.achievement_title);
            descriptionText = itemView.findViewById(R.id.achievement_description);
            experienceText = itemView.findViewById(R.id.achievement_experience);
            statusText = itemView.findViewById(R.id.achievement_status);
            progressBar = itemView.findViewById(R.id.achievement_progress);
            categoryIcon = itemView.findViewById(R.id.achievement_category_icon);
        }
    }
}