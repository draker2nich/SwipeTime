package com.draker.swipetime.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.draker.swipetime.MainActivity;
import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.utils.GamificationIntegrator;

/**
 * Класс для отображения уведомлений о новых достижениях
 */
public class AchievementNotifier implements GamificationManager.OnAchievementListener {

    private static final String CHANNEL_ID = "achievement_channel";
    private static final String CHANNEL_NAME = "Достижения";
    private static final String CHANNEL_DESCRIPTION = "Уведомления о новых достижениях и повышении уровня";
    private static final int ACHIEVEMENT_NOTIFICATION_ID = 1001;
    private static final int LEVEL_UP_NOTIFICATION_ID = 2001;
    
    private final Context context;
    
    public AchievementNotifier(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannel();
    }
    
    /**
     * Создание канала уведомлений (требуется для Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            // Регистрируем канал в системе
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Показать уведомление о новом достижении
     */
    @Override
    public void onAchievementUnlocked(AchievementEntity achievement, int experienceGained) {
        // Создаем интент для открытия приложения при нажатии на уведомление
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_achievements", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Получаем ID ресурса для иконки достижения
        int iconResourceId = getAchievementIconResourceId(achievement.getIconUrl());
        
        // Создаем уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_achievement) // Иконка достижения в строке состояния
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconResourceId))
                .setContentTitle("Новое достижение!")
                .setContentText(achievement.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(achievement.getDescription() + "\n+" + experienceGained + " опыта"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Показываем уведомление
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ACHIEVEMENT_NOTIFICATION_ID, builder.build());
        
        // Дополнительно показываем Toast для пользователя
        GamificationIntegrator.showAchievementToast(context, achievement, experienceGained);
    }
    
    /**
     * Показать уведомление о повышении уровня
     */
    @Override
    public void onLevelUp(int newLevel, int experienceGained) {
        // Создаем интент для открытия приложения при нажатии на уведомление
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_profile", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Получаем звание для нового уровня
        String rank = XpLevelCalculator.getLevelRank(newLevel);
        
        // Создаем уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_level_up) // Иконка повышения уровня
                .setContentTitle("Уровень повышен!")
                .setContentText("Вы достигли " + newLevel + " уровня")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Поздравляем! Вы достигли " + newLevel + " уровня.\n" +
                                "Новое звание: " + rank + "\n+" + 
                                experienceGained + " опыта."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Показываем уведомление
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(LEVEL_UP_NOTIFICATION_ID, builder.build());
        
        // Дополнительно показываем Toast для пользователя
        GamificationIntegrator.showLevelUpToast(context, newLevel);
    }
    
    /**
     * Получить ID ресурса иконки достижения
     * 
     * @param iconName имя иконки
     * @return ID ресурса
     */
    private int getAchievementIconResourceId(String iconName) {
        // Получаем ID ресурса по имени
        int resourceId = context.getResources().getIdentifier(
                iconName, "drawable", context.getPackageName());
        
        // Если ресурс не найден, возвращаем иконку по умолчанию
        if (resourceId == 0) {
            resourceId = R.drawable.ic_achievement;
        }
        
        return resourceId;
    }
}