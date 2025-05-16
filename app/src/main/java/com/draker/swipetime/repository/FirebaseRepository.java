package com.draker.swipetime.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.ReviewEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.NetworkHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с данными Firebase
 */
public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";
    private static final String USERS_COLLECTION = "users";
    private static final String USER_STATS_COLLECTION = "user_stats";
    private static final String LIKED_CONTENT_COLLECTION = "liked_content";
    private static final String REVIEWS_COLLECTION = "reviews";

    private final FirebaseFirestore firestore;
    private final FirebaseAuthManager authManager;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ReviewRepository reviewRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final NetworkHelper networkHelper;

    public FirebaseRepository(Application application) {
        this.firestore = FirebaseFirestore.getInstance();
        this.authManager = FirebaseAuthManager.getInstance(application);
        this.userRepository = new UserRepository(application);
        this.contentRepository = new ContentRepository(application);
        this.reviewRepository = new ReviewRepository(application);
        this.networkHelper = NetworkHelper.getInstance(application);
    }

    /**
     * Проверить наличие данных пользователя в Firestore
     * @return LiveData с результатом проверки
     */
    public LiveData<Boolean> checkUserDataExists() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Получаем текущего пользователя
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null) {
            result.setValue(false);
            return result;
        }
        
        // Проверяем наличие интернета
        if (!networkHelper.isInternetAvailable()) {
            result.setValue(false);
            return result;
        }
        
        // Проверяем наличие документа пользователя в Firestore
        firestore.collection(USERS_COLLECTION).document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    result.setValue(documentSnapshot.exists());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка проверки данных пользователя", e);
                    result.setValue(false);
                });
        
        return result;
    }

    /**
     * Синхронизировать данные пользователя с Firebase
     * @param userId ID пользователя
     * @return Task с результатом синхронизации
     */
    public Task<Void> syncUserProfile(String userId) {
        UserEntity user = userRepository.getUserById(userId);
        if (user == null) {
            return null;
        }
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("avatarUrl", user.getAvatarUrl());
        userData.put("experience", user.getExperience());
        userData.put("level", user.getLevel());
        userData.put("lastUpdated", System.currentTimeMillis());
        
        return firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(userData, SetOptions.merge());
    }
    
    /**
     * Загрузить данные пользователя из Firebase
     * @param userId ID пользователя
     * @return LiveData с результатом загрузки
     */
    public LiveData<UserEntity> loadUserFromFirebase(String userId) {
        MutableLiveData<UserEntity> result = new MutableLiveData<>();
        
        firestore.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserEntity user = convertToUserEntity(documentSnapshot);
                        result.setValue(user);
                    } else {
                        result.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки данных пользователя", e);
                    result.setValue(null);
                });
        
        return result;
    }
    
    /**
     * Синхронизировать понравившийся контент с Firebase
     * @param userId ID пользователя
     * @return Task с результатом синхронизации
     */
    public Task<Void> syncLikedContent(String userId, ContentEntity content) {
        Map<String, Object> contentData = new HashMap<>();
        contentData.put("contentId", content.getId());
        contentData.put("title", content.getTitle());
        contentData.put("category", content.getCategory());
        contentData.put("imageUrl", content.getImageUrl());
        contentData.put("description", content.getDescription());
        contentData.put("rating", content.getRating());
        contentData.put("completed", content.isCompleted());
        contentData.put("timestamp", content.getTimestamp());
        
        return firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(LIKED_CONTENT_COLLECTION)
                .document(content.getId())
                .set(contentData, SetOptions.merge());
    }
    
    /**
     * Загрузить понравившийся контент из Firebase
     * @param userId ID пользователя
     * @return LiveData со списком понравившегося контента
     */
    public LiveData<List<ContentEntity>> loadLikedContentFromFirebase(String userId) {
        MutableLiveData<List<ContentEntity>> result = new MutableLiveData<>();
        
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
                    result.setValue(contentList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки понравившегося контента", e);
                    result.setValue(new ArrayList<>());
                });
        
        return result;
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
}