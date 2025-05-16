package com.draker.swipetime.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.Application;

import androidx.annotation.NonNull;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.repository.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Менеджер аутентификации Firebase
 */
public class FirebaseAuthManager {

    private static final String TAG = "FirebaseAuthManager";
    private static final int RC_SIGN_IN = 9001;

    private static FirebaseAuthManager instance;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private UserRepository userRepository;
    private Executor executor = Executors.newSingleThreadExecutor();

    private FirebaseAuthManager(Context context) {
        // Инициализация Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Настройка Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);

        // Инициализация репозитория пользователей
        userRepository = new UserRepository((Application) context.getApplicationContext());
    }

    /**
     * Получить экземпляр менеджера аутентификации
     * @param context контекст приложения
     * @return экземпляр менеджера аутентификации
     */
    public static synchronized FirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAuthManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Получить Intent для входа через Google
     * @return Intent для запуска активити выбора аккаунта Google
     */
    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    /**
     * Обработать результат входа через Google
     * @param data данные Intent с результатом
     * @param authCallback колбэк с результатом аутентификации
     */
    public void handleGoogleSignInResult(Intent data, final AuthCallback authCallback) {
        if (data == null) {
            Log.e(TAG, "Intent data is null");
            authCallback.onFailure("Данные Intent равны null");
            return;
        }
        
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Успешный вход в Google
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google Sign In успешно, аутентификация с Firebase");
            firebaseAuthWithGoogle(account, authCallback);
        } catch (ApiException e) {
            // Ошибка входа в Google
            Log.e(TAG, "Google Sign In неудачно, код ошибки: " + e.getStatusCode(), e);
            authCallback.onFailure("Не удалось войти через Google: " + e.getMessage());
        }
    }

    /**
     * Аутентификация в Firebase с помощью учетных данных Google
     * @param account аккаунт Google
     * @param authCallback колбэк с результатом аутентификации
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account, final AuthCallback authCallback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Успешная аутентификация
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Сохраняем или обновляем пользователя в локальной базе данных
                            saveUserToLocalDatabase(firebaseUser);
                            authCallback.onSuccess(firebaseUser);
                        } else {
                            authCallback.onFailure("Пользователь Firebase равен null");
                        }
                    } else {
                        // Ошибка аутентификации
                        Log.w(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                        authCallback.onFailure("Ошибка аутентификации в Firebase: " + 
                                (task.getException() != null ? task.getException().getMessage() : "неизвестная ошибка"));
                    }
                });
    }

    /**
     * Сохранение данных пользователя Firebase в локальную базу данных
     * @param firebaseUser пользователь Firebase
     */
    private void saveUserToLocalDatabase(FirebaseUser firebaseUser) {
        executor.execute(() -> {
            // Проверяем существует ли пользователь в локальной БД
            UserEntity existingUser = userRepository.getUserById(firebaseUser.getUid());
            
            if (existingUser == null) {
                // Создаем нового пользователя
                UserEntity newUser = new UserEntity(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                        firebaseUser.getEmail(),
                        firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                        0, // Начальный опыт
                        0  // Начальный уровень
                );
                userRepository.insert(newUser);
                
                // Инициализируем статистику пользователя через GamificationIntegrator
                GamificationManager.initUserStats(firebaseUser.getUid());
            } else {
                // Обновляем данные существующего пользователя
                existingUser.setUsername(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : existingUser.getUsername());
                existingUser.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : existingUser.getEmail());
                existingUser.setAvatarUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : existingUser.getAvatarUrl());
                userRepository.update(existingUser);
            }
        });
    }

    /**
     * Проверить, вошел ли пользователь
     * @return true, если пользователь вошел в систему
     */
    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Получить текущего пользователя Firebase
     * @return текущий пользователь или null
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Выход из учетной записи
     * @param activity активити для выхода из Google
     * @param logoutCallback колбэк с результатом выхода
     */
    public void signOut(Activity activity, final LogoutCallback logoutCallback) {
        // Выход из Firebase
        firebaseAuth.signOut();
        
        // Выход из Google
        googleSignInClient.signOut().addOnCompleteListener(activity,
                task -> {
                    if (logoutCallback != null) {
                        logoutCallback.onLogout();
                    }
                });
    }

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
}