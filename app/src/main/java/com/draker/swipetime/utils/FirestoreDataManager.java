package com.draker.swipetime.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.ReviewEntity;
import com.draker.swipetime.database.entities.UserAchievementCrossRef;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.ReviewRepository;
import com.draker.swipetime.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Менеджер для работы с Firestore базой данных и синхронизации данных
 */
public class FirestoreDataManager {

    private static final String TAG = "FirestoreDataManager";
    private static final String USERS_COLLECTION = "users";
    private static final String USER_STATS_COLLECTION = "user_stats";
    private static final String USER_PREFERENCES_COLLECTION = "user_preferences";
    private static final String ACHIEVEMENTS_COLLECTION = "achievements";
    private static final String USER_ACHIEVEMENTS_COLLECTION = "user_achievements";
    private static final String LIKED_CONTENT_COLLECTION = "liked_content";
    private static final String REVIEWS_COLLECTION = "reviews";

    private static FirestoreDataManager instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final Context context;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ReviewRepository reviewRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final NetworkHelper networkHelper;

    private FirestoreDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = new UserRepository((Application)context.getApplicationContext());
        this.contentRepository = new ContentRepository((Application)context.getApplicationContext());
        this.reviewRepository = new ReviewRepository((Application)context.getApplicationContext());
        this.networkHelper = NetworkHelper.getInstance(context);
    }

    /**
     * Получить экземпляр FirestoreDataManager
     * @param context контекст приложения
     * @return экземпляр FirestoreDataManager
     */
    public static synchronized FirestoreDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirestoreDataManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Синхронизировать данные пользователя с облаком
     * @param userId ID пользователя
     * @param syncCallback колбэк с результатом синхронизации
     */
    public void syncUserData(String userId, final SyncCallback syncCallback) {
        // Проверка доступности сети
        if (!networkHelper.isInternetAvailable()) {
            Log.w(TAG, "Отсутствует подключение к интернету, синхронизация отложена");
            if (syncCallback != null) {
                syncCallback.onFailure("Отсутствует подключение к интернету");
            }
            
            // Сохраняем информацию о необходимости синхронизации
            scheduleSync(userId);
            return;
        }

        // Проверка авторизации
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            Log.w(TAG, "Пользователь не авторизован или ID не совпадает");
            if (syncCallback != null) {
                syncCallback.onFailure("Пользователь не авторизован");
            }
            return;
        }

        // Запуск синхронизации в фоновом потоке
        executor.execute(() -> {
            try {
                // Загрузка локальных данных пользователя
                UserEntity localUser = userRepository.getUserById(userId);
                if (localUser == null) {
                    Log.e(TAG, "Пользователь не найден в локальной базе данных: " + userId);
                    if (syncCallback != null) {
                        syncCallback.onFailure("Пользователь не найден в локальной базе данных");
                    }
                    return;
                }

                Log.d(TAG, "Начинаем синхронизацию данных пользователя: " + userId);
                
                // Загрузка и сохранение данных пользователя
                syncUserProfile(localUser);
                
                // Синхронизация контента
                syncLikedContent(userId);
                
                // Синхронизация отзывов
                syncReviews(userId);
                
                // Убираем запись о необходимости синхронизации
                clearSyncFlag(userId);
                
                // Завершение синхронизации
                if (syncCallback != null) {
                    syncCallback.onSuccess();
                }
                
                Log.d(TAG, "Синхронизация данных пользователя успешно завершена: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка синхронизации данных", e);
                
                // Сохраняем информацию о необходимости синхронизации
                scheduleSync(userId);
                
                if (syncCallback != null) {
                    syncCallback.onFailure("Ошибка синхронизации: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Сохраняет информацию о необходимости синхронизации
     * @param userId ID пользователя
     */
    private void scheduleSync(String userId) {
        // TODO: Здесь можно реализовать механизм отложенной синхронизации
        // Например, сохранить в SharedPreferences время последней неудачной попытки
        // и ID пользователя для последующей синхронизации
        
        Log.d(TAG, "Синхронизация для пользователя " + userId + " отложена");
    }
    
    /**
     * Удаляет запись о необходимости синхронизации
     * @param userId ID пользователя
     */
    private void clearSyncFlag(String userId) {
        // TODO: Здесь можно удалить информацию об отложенной синхронизации
        
        Log.d(TAG, "Флаг синхронизации для пользователя " + userId + " очищен");
    }

    /**
     * Синхронизация профиля пользователя
     * @param localUser локальные данные пользователя
     */
    private void syncUserProfile(UserEntity localUser) {
        if (localUser == null || localUser.getId() == null) {
            Log.e(TAG, "Ошибка синхронизации: данные пользователя недействительны");
            return;
        }
        
        Log.d(TAG, "Синхронизация профиля пользователя: " + localUser.getUsername() + " (ID: " + localUser.getId() + ")");
        
        // Преобразование в Map для Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", localUser.getUsername());
        userData.put("email", localUser.getEmail());
        userData.put("avatarUrl", localUser.getAvatarUrl());
        userData.put("experience", localUser.getExperience());
        userData.put("level", localUser.getLevel());
        userData.put("lastUpdated", System.currentTimeMillis());

        // Сохранение в Firestore с обработкой результата
        firestore.collection(USERS_COLLECTION)
                .document(localUser.getId())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Профиль пользователя успешно синхронизирован");
                    syncUserStats(localUser.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка сохранения профиля пользователя", e);
                    // Сохраняем информацию об ошибке для возможности повторной попытки позже
                    // TODO: Добавить механизм отложенной синхронизации
                });
    }

    /**
     * Синхронизация статистики пользователя
     * @param userId ID пользователя
     */
    private void syncUserStats(String userId) {
        // Получение локальных данных статистики
        // В реальном приложении тут должна быть логика получения статистики из базы данных
        Map<String, Object> statsData = new HashMap<>();
        statsData.put("swipesCount", 0);
        statsData.put("ratingsCount", 0);
        statsData.put("reviewsCount", 0);
        statsData.put("viewedCount", 0);
        statsData.put("lastUpdated", System.currentTimeMillis());

        // Сохранение в Firestore
        firestore.collection(USER_STATS_COLLECTION)
                .document(userId)
                .set(statsData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "Статистика пользователя успешно синхронизирована")
                )
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Ошибка сохранения статистики пользователя", e)
                );
    }

    /**
     * Синхронизация понравившегося контента
     * @param userId ID пользователя
     */
    private void syncLikedContent(String userId) {
        // Получение локальных данных о понравившемся контенте
        List<ContentEntity> likedContent = contentRepository.getLikedContentForUser(userId);
        
        // Проверка наличия данных
        if (likedContent == null || likedContent.isEmpty()) {
            Log.d(TAG, "Нет понравившегося контента для синхронизации");
            return;
        }
        
        Log.d(TAG, "Начинаем синхронизацию " + likedContent.size() + " элементов понравившегося контента");
        
        // Для каждого элемента контента
        for (ContentEntity content : likedContent) {
            if (content == null || content.getId() == null) {
                Log.w(TAG, "Пропуск недействительного элемента контента");
                continue;
            }
            
            Log.d(TAG, "Синхронизация элемента: " + content.getTitle() + " (ID: " + content.getId() + ")");
            
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("contentId", content.getId());
            contentData.put("title", content.getTitle());
            contentData.put("category", content.getCategory());
            contentData.put("imageUrl", content.getImageUrl());
            contentData.put("description", content.getDescription());
            contentData.put("rating", content.getRating());
            contentData.put("completed", content.isCompleted());
            contentData.put("timestamp", content.getTimestamp());
            contentData.put("lastUpdated", System.currentTimeMillis());

            // Сохранение в Firestore с обработкой результата
            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(LIKED_CONTENT_COLLECTION)
                    .document(content.getId())
                    .set(contentData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> 
                        Log.d(TAG, "Контент успешно синхронизирован: " + content.getId())
                    )
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Ошибка сохранения контента: " + content.getId(), e);
                        // Здесь можно добавить логику для повторных попыток или отложенной синхронизации
                    });
        }
    }

    /**
     * Синхронизация отзывов пользователя
     * @param userId ID пользователя
     */
    private void syncReviews(String userId) {
        // Получение локальных данных об отзывах
        List<ReviewEntity> reviews = reviewRepository.getReviewsByUserId(userId);
        
        // Проверка наличия данных
        if (reviews == null || reviews.isEmpty()) {
            Log.d(TAG, "Нет отзывов для синхронизации");
            return;
        }
        
        Log.d(TAG, "Начинаем синхронизацию " + reviews.size() + " отзывов");
        
        // Для каждого отзыва
        for (ReviewEntity review : reviews) {
            if (review == null || review.getContentId() == null) {
                Log.w(TAG, "Пропуск недействительного отзыва");
                continue;
            }
            
            Log.d(TAG, "Синхронизация отзыва для элемента с ID: " + review.getContentId());
            
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("userId", review.getUserId());
            reviewData.put("contentId", review.getContentId());
            reviewData.put("text", review.getText());
            reviewData.put("rating", review.getRating());
            reviewData.put("timestamp", review.getTimestamp());
            reviewData.put("lastUpdated", System.currentTimeMillis());

            // Сохранение в Firestore с обработкой результата
            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(REVIEWS_COLLECTION)
                    .document(review.getContentId())
                    .set(reviewData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> 
                        Log.d(TAG, "Отзыв успешно синхронизирован: " + review.getContentId())
                    )
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Ошибка сохранения отзыва: " + review.getContentId(), e);
                        // Здесь можно добавить логику для повторных попыток или отложенной синхронизации
                    });
        }
    }

    /**
     * Загрузить данные пользователя из облака
     * @param userId ID пользователя
     * @param loadCallback колбэк с результатом загрузки
     */
    public void loadUserDataFromCloud(String userId, final LoadCallback loadCallback) {
        // Проверка доступности сети
        if (!networkHelper.isInternetAvailable()) {
            if (loadCallback != null) {
                loadCallback.onFailure("Отсутствует подключение к интернету");
            }
            return;
        }

        // Загрузка профиля пользователя из Firestore
        firestore.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Преобразование данных из Firestore в объект пользователя
                        UserEntity user = convertToUserEntity(documentSnapshot);
                        if (user != null) {
                            // Сохранение в локальную базу данных
                            executor.execute(() -> {
                                userRepository.update(user);
                                
                                // Продолжаем загрузку дополнительных данных
                                loadLikedContentFromCloud(userId);
                                loadReviewsFromCloud(userId);
                                
                                if (loadCallback != null) {
                                    loadCallback.onSuccess();
                                }
                            });
                        } else {
                            if (loadCallback != null) {
                                loadCallback.onFailure("Не удалось преобразовать данные пользователя");
                            }
                        }
                    } else {
                        if (loadCallback != null) {
                            loadCallback.onFailure("Данные пользователя не найдены в облаке");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки данных пользователя из облака", e);
                    if (loadCallback != null) {
                        loadCallback.onFailure("Ошибка загрузки данных: " + e.getMessage());
                    }
                });
    }

    /**
     * Преобразование DocumentSnapshot в UserEntity
     * @param documentSnapshot снимок документа из Firestore
     * @return объект пользователя или null, если преобразование невозможно
     */
    private UserEntity convertToUserEntity(DocumentSnapshot documentSnapshot) {
        try {
            String id = documentSnapshot.getId();
            String username = documentSnapshot.getString("username");
            String email = documentSnapshot.getString("email");
            String avatarUrl = documentSnapshot.getString("avatarUrl");
            Long experienceLong = documentSnapshot.getLong("experience");
            Long levelLong = documentSnapshot.getLong("level");
            
            int experience = (experienceLong != null) ? experienceLong.intValue() : 0;
            int level = (levelLong != null) ? levelLong.intValue() : 0;
            
            return new UserEntity(id, username, email, avatarUrl, experience, level);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка преобразования DocumentSnapshot в UserEntity", e);
            return null;
        }
    }

    /**
     * Загрузка понравившегося контента из облака
     * @param userId ID пользователя
     */
    private void loadLikedContentFromCloud(String userId) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(LIKED_CONTENT_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContentEntity> contentList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ContentEntity content = convertToContentEntity(document);
                        if (content != null) {
                            contentList.add(content);
                        }
                    }
                    
                    // Сохранение в локальную базу данных
                    executor.execute(() -> {
                        for (ContentEntity content : contentList) {
                            contentRepository.updateOrInsert(content);
                        }
                    });
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Ошибка загрузки понравившегося контента", e)
                );
    }

    /**
     * Преобразование DocumentSnapshot в ContentEntity
     * @param document снимок документа из Firestore
     * @return объект контента или null, если преобразование невозможно
     */
    private ContentEntity convertToContentEntity(QueryDocumentSnapshot document) {
        try {
            String id = document.getId();
            String title = document.getString("title");
            String category = document.getString("category");
            String imageUrl = document.getString("imageUrl");
            String description = document.getString("description");
            Double rating = document.getDouble("rating");
            Boolean completed = document.getBoolean("completed");
            Long timestamp = document.getLong("timestamp");
            
            ContentEntity content = new ContentEntity();
            content.setId(id);
            content.setTitle(title);
            content.setCategory(category);
            content.setImageUrl(imageUrl);
            content.setDescription(description);
            content.setRating(rating != null ? rating.floatValue() : 0f);
            content.setCompleted(completed != null && completed);
            content.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
            
            return content;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка преобразования DocumentSnapshot в ContentEntity", e);
            return null;
        }
    }

    /**
     * Загрузка отзывов из облака
     * @param userId ID пользователя
     */
    private void loadReviewsFromCloud(String userId) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(REVIEWS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReviewEntity> reviewList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReviewEntity review = convertToReviewEntity(document);
                        if (review != null) {
                            reviewList.add(review);
                        }
                    }
                    
                    // Сохранение в локальную базу данных
                    executor.execute(() -> {
                        for (ReviewEntity review : reviewList) {
                            reviewRepository.insertOrUpdate(review);
                        }
                    });
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Ошибка загрузки отзывов", e)
                );
    }

    /**
     * Преобразование DocumentSnapshot в ReviewEntity
     * @param document снимок документа из Firestore
     * @return объект отзыва или null, если преобразование невозможно
     */
    private ReviewEntity convertToReviewEntity(QueryDocumentSnapshot document) {
        try {
            String contentId = document.getId();
            String userId = document.getString("userId");
            String text = document.getString("text");
            Double rating = document.getDouble("rating");
            Long timestamp = document.getLong("timestamp");
            
            ReviewEntity review = new ReviewEntity();
            review.setContentId(contentId);
            review.setUserId(userId);
            review.setText(text);
            review.setRating(rating != null ? rating.floatValue() : 0f);
            review.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
            
            return review;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка преобразования DocumentSnapshot в ReviewEntity", e);
            return null;
        }
    }

    /**
     * Интерфейс колбэка для синхронизации
     */
    public interface SyncCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    /**
     * Интерфейс колбэка для загрузки данных
     */
    public interface LoadCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}