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
import com.draker.swipetime.database.entities.CollectibleItemEntity;
import com.draker.swipetime.database.entities.DailyQuestEntity;
import com.draker.swipetime.database.entities.SeasonalEventEntity;
import com.draker.swipetime.database.entities.ThematicChallengeEntity;
import com.draker.swipetime.database.entities.UserRankEntity;

/**
 * Класс для отображения уведомлений о всех событиях геймификации
 */
public class AchievementNotifier implements GamificationManager.OnAchievementListener {

    private static final String CHANNEL_ID = "achievement_channel";
    private static final String CHANNEL_NAME = "Достижения";
    private static final String CHANNEL_DESCRIPTION = "Уведомления о новых достижениях и повышении уровня";
    
    // ID для разных типов уведомлений
    private static final int ACHIEVEMENT_NOTIFICATION_ID = 1001;
    private static final int LEVEL_UP_NOTIFICATION_ID = 2001;
    private static final int QUEST_NOTIFICATION_ID = 3001;
    private static final int EVENT_NOTIFICATION_ID = 4001;
    private static final int ITEM_NOTIFICATION_ID = 5001;
    private static final int CHALLENGE_NOTIFICATION_ID = 6001;
    private static final int RANK_NOTIFICATION_ID = 7001;
    
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
        showAchievementUnlocked(achievement, experienceGained);
    }
    
    /**
     * Показать уведомление о повышении уровня
     */
    @Override
    public void onLevelUp(int newLevel, int experienceGained) {
        showLevelUp(newLevel, experienceGained);
    }
    
    /**
     * Показать уведомление о новом достижении
     */
    public void showAchievementUnlocked(AchievementEntity achievement, int experienceGained) {
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
                .setSmallIcon(R.drawable.ic_notification) // Иконка достижения в строке состояния
                .setContentTitle("🏆 Новое достижение!")
                .setContentText(achievement.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(achievement.getDescription() + "\n+" + experienceGained + " опыта"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Показываем уведомление
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ACHIEVEMENT_NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Показать уведомление о повышении уровня
     */
    public void showLevelUp(int newLevel, int experienceGained) {
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
                .setSmallIcon(R.drawable.ic_notification) // Иконка повышения уровня
                .setContentTitle("⬆️ Уровень повышен!")
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
    }
    
    /**
     * Показать уведомление о выполнении ежедневного задания
     */
    public void showQuestCompleted(DailyQuestEntity quest, int experienceGained) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_quests", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("✅ Задание выполнено!")
                .setContentText(quest.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(quest.getDescription() + "\n+" + experienceGained + " опыта"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(QUEST_NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Показать уведомление о начале сезонного события
     */
    public void showEventStarted(SeasonalEventEntity event) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_events", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🎉 Новое событие!")
                .setContentText(event.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(event.getDescription() + "\nНачинается сейчас!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EVENT_NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Показать уведомление о завершении сезонного события
     */
    public void showEventEnded(SeasonalEventEntity event) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🎭 Событие завершено")
                .setContentText(event.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Событие \"" + event.getTitle() + "\" завершено. Спасибо за участие!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EVENT_NOTIFICATION_ID + 1, builder.build());
    }
    
    /**
     * Показать уведомление о получении предмета
     */
    public void showItemObtained(CollectibleItemEntity item, String source) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_collection", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        String sourceText = getSourceDisplayName(source);
        String rarityEmoji = getRarityEmoji(item.getRarity());
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("💎 Новый предмет!")
                .setContentText(rarityEmoji + " " + item.getName())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(item.getDescription() + "\nИсточник: " + sourceText + 
                                "\nРедкость: " + item.getRarityText()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ITEM_NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Показать уведомление о завершении тематического испытания
     */
    public void showChallengeCompleted(ThematicChallengeEntity challenge, int experienceGained) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_challenges", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🚩 Испытание завершено!")
                .setContentText(challenge.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(challenge.getDescription() + "\n+" + experienceGained + " опыта"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(CHALLENGE_NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Показать уведомление о разблокировке ранга
     */
    public void showRankUnlocked(UserRankEntity rank, int experienceGained) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_ranks", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("👑 Новый ранг!")
                .setContentText(rank.getName())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(rank.getDescription() + "\n+" + experienceGained + " опыта" +
                                "\nБонус к опыту: +" + ((rank.getBonusXpMultiplier() - 1.0f) * 100) + "%"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(RANK_NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Показать уведомление об активации ранга
     */
    public void showRankActivated(UserRankEntity rank) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_profile", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⭐ Ранг активирован!")
                .setContentText(rank.getName())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Вы активировали ранг: " + rank.getName() + "\n" +
                                rank.getDescription() + 
                                "\nБонус к опыту: +" + ((rank.getBonusXpMultiplier() - 1.0f) * 100) + "%"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(RANK_NOTIFICATION_ID + 1, builder.build());
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
            resourceId = R.drawable.ic_notification;
        }
        
        return resourceId;
    }
    
    /**
     * Получить отображаемое название источника предмета
     * @param source источник
     * @return отображаемое название
     */
    private String getSourceDisplayName(String source) {
        switch (source) {
            case "starter":
                return "Новичку";
            case "achievement":
                return "Достижение";
            case "quest":
                return "Ежедневное задание";
            case "challenge":
                return "Тематическое испытание";
            case "event":
                return "Сезонное событие";
            case "rank":
                return "Ранг";
            case "purchase":
                return "Покупка";
            default:
                return "Неизвестный источник";
        }
    }
    
    /**
     * Получить эмодзи для редкости предмета
     * @param rarity уровень редкости (1-5)
     * @return эмодзи
     */
    private String getRarityEmoji(int rarity) {
        switch (rarity) {
            case 1:
                return "⚪"; // Обычный
            case 2:
                return "🔵"; // Редкий
            case 3:
                return "🟣"; // Эпический
            case 4:
                return "🟡"; // Легендарный
            case 5:
                return "🔴"; // Уникальный
            default:
                return "⚫"; // Неизвестный
        }
    }
}
