package com.draker.swipetime.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.ReviewEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.ReviewRepository;
import com.draker.swipetime.repository.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
 * Объединенный класс для работы с Firebase, включая:
 * - Аутентификацию через Firebase Auth
 * - Синхронизацию данных с Firestore
 * - Управление миграцией фрагментов
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static final int RC_SIGN_IN = 9001;

    // Firestore collections
    private static final String USERS_COLLECTION = "users";
    private static final String USER_STATS_COLLECTION = "user_stats";
    private static final String LIKED_CONTENT_COLLECTION = "liked_content";
    private static final String REVIEWS_COLLECTION = "reviews";

    // Fragment migration preferences
    private static final String PREFS_NAME = "fragment_migration_prefs";
    private static final String KEY_USE_INFINITE_FRAGMENTS = "use_infinite_fragments";

    // Singleton instance
    private static FirebaseManager instance;

    // Firebase components
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final GoogleSignInClient googleSignInClient;

    // Repositories
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ReviewRepository reviewRepository;

    // Other components
    private final Context context;
    private final NetworkHelper networkHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private FirebaseManager(Context context) {
        this.context = context.getApplicationContext();

        // Initialize Firebase components
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();

        // Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);

        // Initialize repositories
        this.userRepository = new UserRepository((Application) context.getApplicationContext());
        this.contentRepository = new ContentRepository((Application) context.getApplicationContext());
        this.reviewRepository = new ReviewRepository((Application) context.getApplicationContext());

        // Initialize network helper
        this.networkHelper = NetworkHelper.getInstance(context);
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FirebaseManager не инициализирован. Вызовите getInstance(Context) сначала.");
        }
        return instance;
    }

    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Сохраняет элемент в избранное
     */
    public void saveFavoriteItem(Context context, String itemId) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            // Синхронизируем данные пользователя
            syncUserData(currentUser.getUid(), new SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Избранный элемент сохранен: " + itemId);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Ошибка сохранения избранного элемента: " + errorMessage);
                }
            });
        }
    }

    /**
     * Синхронизирует данные пользователя с определенным контентом
     */
    public void syncUserData(Context context, String userId, ContentEntity contentEntity, SyncCallback callback) {
        if (!networkHelper.isInternetAvailable()) {
            Log.w(TAG, "Отсутствует подключение к интернету, синхронизация отложена");
            if (callback != null) {
                callback.onFailure("Отсутствует подключение к интернету");
            }
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            Log.w(TAG, "Пользователь не авторизован или ID не совпадает");
            if (callback != null) {
                callback.onFailure("Пользователь не авторизован");
            }
            return;
        }

        // Синхронизируем конкретный элемент контента
        if (contentEntity != null) {
            syncSingleContentItem(userId, contentEntity, callback);
        } else {
            // Синхронизируем все данные пользователя
            syncUserData(userId, callback);
        }
    }

    /**
     * Синхронизирует профиль пользователя
     */
    public void syncUserProfile(Context context, String userId, SyncCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity localUser = userRepository.getUserById(userId);
                if (localUser != null) {
                    syncUserProfile(localUser);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onFailure("Пользователь не найден в локальной базе данных");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка синхронизации профиля", e);
                if (callback != null) {
                    callback.onFailure("Ошибка синхронизации профиля: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Синхронизирует отдельный элемент контента
     */
    private void syncSingleContentItem(String userId, ContentEntity content, SyncCallback callback) {
        if (content == null || content.getId() == null) {
            Log.w(TAG, "Недействительный элемент контента для синхронизации");
            if (callback != null) {
                callback.onFailure("Недействительный элемент контента");
            }
            return;
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

        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(LIKED_CONTENT_COLLECTION)
                .document(content.getId())
                .set(contentData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Контент успешно синхронизирован: " + content.getId());
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка сохранения контента: " + content.getId(), e);
                    if (callback != null) {
                        callback.onFailure("Ошибка сохранения контента: " + e.getMessage());
                    }
                });
    }

    // ==================== AUTHENTICATION SECTION ====================

    /**
     * Получить Intent для входа через Google
     */
    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    /**
     * Обработать результат входа через Google
     */
    public void handleGoogleSignInResult(Intent data, final AuthCallback authCallback) {
        if (data == null) {
            Log.e(TAG, "Intent data is null");
            authCallback.onFailure("Данные Intent равны null");
            return;
        }

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google Sign In успешно, аутентификация с Firebase");
            firebaseAuthWithGoogle(account, authCallback);
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign In неудачно, код ошибки: " + e.getStatusCode(), e);
            authCallback.onFailure("Не удалось войти через Google: " + e.getMessage());
        }
    }

    /**
     * Аутентификация в Firebase с помощью учетных данных Google
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account, final AuthCallback authCallback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToLocalDatabase(firebaseUser);
                            authCallback.onSuccess(firebaseUser);
                        } else {
                            authCallback.onFailure("Пользователь Firebase равен null");
                        }
                    } else {
                        Log.w(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                        authCallback.onFailure("Ошибка аутентификации в Firebase: " +
                                (task.getException() != null ? task.getException().getMessage() : "неизвестная ошибка"));
                    }
                });
    }

    /**
     * Сохранение данных пользователя Firebase в локальную базу данных
     */
    private void saveUserToLocalDatabase(FirebaseUser firebaseUser) {
        executor.execute(() -> {
            UserEntity existingUser = userRepository.getUserById(firebaseUser.getUid());

            if (existingUser == null) {
                UserEntity newUser = new UserEntity(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                        firebaseUser.getEmail(),
                        firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                        0, // Начальный опыт
                        0  // Начальный уровень
                );
                userRepository.insert(newUser);

                GamificationManager.initUserStats(firebaseUser.getUid());
            } else {
                existingUser.setUsername(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : existingUser.getUsername());
                existingUser.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : existingUser.getEmail());
                existingUser.setAvatarUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : existingUser.getAvatarUrl());
                userRepository.update(existingUser);
            }
        });
    }

    /**
     * Проверить, вошел ли пользователь
     */
    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Получить текущего пользователя Firebase
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Выход из учетной записи
     */
    public void signOut(Activity activity, final LogoutCallback logoutCallback) {
        firebaseAuth.signOut();

        googleSignInClient.signOut().addOnCompleteListener(activity,
                task -> {
                    if (logoutCallback != null) {
                        logoutCallback.onLogout();
                    }
                });
    }

    // ==================== FIRESTORE DATA SYNC SECTION ====================

    /**
     * Синхронизировать данные пользователя с облаком
     */
    public void syncUserData(String userId, final SyncCallback syncCallback) {
        if (!networkHelper.isInternetAvailable()) {
            Log.w(TAG, "Отсутствует подключение к интернету, синхронизация отложена");
            if (syncCallback != null) {
                syncCallback.onFailure("Отсутствует подключение к интернету");
            }
            scheduleSync(userId);
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            Log.w(TAG, "Пользователь не авторизован или ID не совпадает");
            if (syncCallback != null) {
                syncCallback.onFailure("Пользователь не авторизован");
            }
            return;
        }

        executor.execute(() -> {
            try {
                UserEntity localUser = userRepository.getUserById(userId);
                if (localUser == null) {
                    Log.e(TAG, "Пользователь не найден в локальной базе данных: " + userId);
                    if (syncCallback != null) {
                        syncCallback.onFailure("Пользователь не найден в локальной базе данных");
                    }
                    return;
                }

                Log.d(TAG, "Начинаем синхронизацию данных пользователя: " + userId);

                syncUserProfile(localUser);
                syncLikedContent(userId);
                syncReviews(userId);
                clearSyncFlag(userId);

                if (syncCallback != null) {
                    syncCallback.onSuccess();
                }

                Log.d(TAG, "Синхронизация данных пользователя успешно завершена: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка синхронизации данных", e);
                scheduleSync(userId);

                if (syncCallback != null) {
                    syncCallback.onFailure("Ошибка синхронизации: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Синхронизация профиля пользователя
     */
    private void syncUserProfile(UserEntity localUser) {
        if (localUser == null || localUser.getId() == null) {
            Log.e(TAG, "Ошибка синхронизации: данные пользователя недействительны");
            return;
        }

        Log.d(TAG, "Синхронизация профиля пользователя: " + localUser.getUsername() + " (ID: " + localUser.getId() + ")");

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", localUser.getUsername());
        userData.put("email", localUser.getEmail());
        userData.put("avatarUrl", localUser.getAvatarUrl());
        userData.put("experience", localUser.getExperience());
        userData.put("level", localUser.getLevel());
        userData.put("lastUpdated", System.currentTimeMillis());

        firestore.collection(USERS_COLLECTION)
                .document(localUser.getId())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Профиль пользователя успешно синхронизирован");
                    syncUserStats(localUser.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка сохранения профиля пользователя", e);
                });
    }

    /**
     * Синхронизация статистики пользователя
     */
    private void syncUserStats(String userId) {
        Map<String, Object> statsData = new HashMap<>();
        statsData.put("swipesCount", 0);
        statsData.put("ratingsCount", 0);
        statsData.put("reviewsCount", 0);
        statsData.put("viewedCount", 0);
        statsData.put("lastUpdated", System.currentTimeMillis());

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
     */
    private void syncLikedContent(String userId) {
        List<ContentEntity> likedContent = contentRepository.getLikedContentForUser(userId);

        if (likedContent == null || likedContent.isEmpty()) {
            Log.d(TAG, "Нет понравившегося контента для синхронизации");
            return;
        }

        Log.d(TAG, "Начинаем синхронизацию " + likedContent.size() + " элементов понравившегося контента");

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
                    });
        }
    }

    /**
     * Синхронизация отзывов пользователя
     */
    private void syncReviews(String userId) {
        List<ReviewEntity> reviews = reviewRepository.getReviewsByUserId(userId);

        if (reviews == null || reviews.isEmpty()) {
            Log.d(TAG, "Нет отзывов для синхронизации");
            return;
        }

        Log.d(TAG, "Начинаем синхронизацию " + reviews.size() + " отзывов");

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
                    });
        }
    }

    /**
     * Загрузить данные пользователя из облака
     */
    public void loadUserDataFromCloud(String userId, final LoadCallback loadCallback) {
        if (!networkHelper.isInternetAvailable()) {
            if (loadCallback != null) {
                loadCallback.onFailure("Отсутствует подключение к интернету");
            }
            return;
        }

        firestore.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserEntity user = convertToUserEntity(documentSnapshot);
                        if (user != null) {
                            executor.execute(() -> {
                                userRepository.update(user);
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
     * Загрузка понравившегося контента из облака
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
     * Загрузка отзывов из облака
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

    // ==================== FRAGMENT MIGRATION SECTION ====================

    /**
     * Устанавливает флаг использования бесконечных фрагментов
     */
    public void setUseInfiniteFragments(boolean useInfiniteFragments) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_USE_INFINITE_FRAGMENTS, useInfiniteFragments).apply();
            Log.d(TAG, "Установлен режим бесконечных фрагментов: " + useInfiniteFragments);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при установке режима фрагментов: " + e.getMessage());
        }
    }

    /**
     * Проверяет, следует ли использовать бесконечные фрагменты
     */
    public boolean shouldUseInfiniteFragments() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_USE_INFINITE_FRAGMENTS, true);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении режима фрагментов: " + e.getMessage());
            return true;
        }
    }

    /**
     * Переключает режим фрагментов
     */
    public boolean toggleFragmentMode() {
        boolean currentMode = shouldUseInfiniteFragments();
        boolean newMode = !currentMode;
        setUseInfiniteFragments(newMode);
        return newMode;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void scheduleSync(String userId) {
        Log.d(TAG, "Синхронизация для пользователя " + userId + " отложена");
    }

    private void clearSyncFlag(String userId) {
        Log.d(TAG, "Флаг синхронизации для пользователя " + userId + " очищен");
    }

    /**
     * Преобразование DocumentSnapshot в UserEntity
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
     * Преобразование DocumentSnapshot в ContentEntity
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
     * Преобразование DocumentSnapshot в ReviewEntity
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

    // ==================== CALLBACK INTERFACES ====================

    /**
     * Интерфейс для колбэков аутентификации
     */
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    /**
     * Интерфейс для колбэков выхода из системы
     */
    public interface LogoutCallback {
        void onLogout();
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