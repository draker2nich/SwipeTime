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

        // Настройка Google Sign In с правильным Web Client ID
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);

        // Инициализация репозитория пользователей
        userRepository = new UserRepository((Application) context.getApplicationContext());
        
        Log.d(TAG, "FirebaseAuthManager инициализирован с Web Client ID: " + context.getString(R.string.default_web_client_id));
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
        Log.d(TAG, "Создание Intent для Google Sign In");
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
        
        Log.d(TAG, "Обработка результата Google Sign In");
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Успешный вход в Google
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google Sign In успешно для аккаунта: " + account.getEmail());
            Log.d(TAG, "Получен ID Token: " + (account.getIdToken() != null ? "YES" : "NO"));
            firebaseAuthWithGoogle(account, authCallback);
        } catch (ApiException e) {
            // Ошибка входа в Google
            Log.e(TAG, "Google Sign In неудачно, код ошибки: " + e.getStatusCode() + ", сообщение: " + e.getMessage(), e);
            
            String errorMessage = getGoogleSignInErrorMessage(e.getStatusCode());
            authCallback.onFailure(errorMessage);
        }
    }

    /**
     * Получить понятное сообщение об ошибке Google Sign In
     */
    private String getGoogleSignInErrorMessage(int errorCode) {
        switch (errorCode) {
            case 7: // NETWORK_ERROR
                return "Проблемы с интернет соединением. Проверьте подключение и попробуйте снова.";
            case 8: // INTERNAL_ERROR
                return "Внутренняя ошибка Google Services. Попробуйте позже.";
            case 10: // DEVELOPER_ERROR
                return "Ошибка конфигурации приложения. Проверьте настройки Firebase.";
            case 12500: // SIGN_IN_CANCELLED
                return "Вход отменен пользователем.";
            case 12501: // SIGN_IN_CURRENTLY_IN_PROGRESS
                return "Процесс входа уже выполняется.";
            case 12502: // SIGN_IN_FAILED
                return "Не удалось войти. Попробуйте снова.";
            default:
                return "Не удалось войти через Google (код: " + errorCode + "). Попробуйте снова.";
        }
    }

    /**
     * Аутентификация в Firebase с помощью учетных данных Google
     * @param account аккаунт Google
     * @param authCallback колбэк с результатом аутентификации
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account, final AuthCallback authCallback) {
        Log.d(TAG, "Начинаем аутентификацию в Firebase");
        
        String idToken = account.getIdToken();
        if (idToken == null) {
            Log.e(TAG, "ID Token равен null");
            authCallback.onFailure("Не удалось получить токен аутентификации от Google");
            return;
        }
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Успешная аутентификация
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Firebase аутентификация успешна для пользователя: " + firebaseUser.getUid());
                            // Сохраняем или обновляем пользователя в локальной базе данных
                            saveUserToLocalDatabase(firebaseUser);
                            authCallback.onSuccess(firebaseUser);
                        } else {
                            Log.e(TAG, "FirebaseUser равен null после успешной аутентификации");
                            authCallback.onFailure("Пользователь Firebase равен null");
                        }
                    } else {
                        // Ошибка аутентификации
                        Exception exception = task.getException();
                        Log.w(TAG, "firebaseAuthWithGoogle:failure", exception);
                        
                        String errorMessage = "Ошибка аутентификации в Firebase";
                        if (exception != null) {
                            errorMessage += ": " + exception.getMessage();
                        }
                        authCallback.onFailure(errorMessage);
                    }
                });
    }

    /**
     * Сохранение данных пользователя Firebase в локальную базу данных
     * @param firebaseUser пользователь Firebase
     */
    private void saveUserToLocalDatabase(FirebaseUser firebaseUser) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Сохранение пользователя в локальную БД: " + firebaseUser.getUid());
                
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
                            1  // Начальный уровень
                    );
                    userRepository.insert(newUser);
                    
                    Log.d(TAG, "Создан новый пользователь: " + newUser.getUsername());
                    
                    // Инициализируем статистику пользователя через GamificationManager
                    GamificationManager.initUserStats(firebaseUser.getUid());
                } else {
                    // Обновляем данные существующего пользователя
                    existingUser.setUsername(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : existingUser.getUsername());
                    existingUser.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : existingUser.getEmail());
                    existingUser.setAvatarUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : existingUser.getAvatarUrl());
                    userRepository.update(existingUser);
                    
                    Log.d(TAG, "Обновлен существующий пользователь: " + existingUser.getUsername());
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка сохранения пользователя в локальную БД", e);
            }
        });
    }

    /**
     * Проверить, вошел ли пользователь
     * @return true, если пользователь вошел в систему
     */
    public boolean isUserSignedIn() {
        boolean isSignedIn = firebaseAuth.getCurrentUser() != null;
        Log.d(TAG, "Проверка входа пользователя: " + isSignedIn);
        return isSignedIn;
    }

    /**
     * Получить текущего пользователя Firebase
     * @return текущий пользователь или null
     */
    public FirebaseUser getCurrentUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Текущий пользователь: " + user.getUid() + " (" + user.getEmail() + ")");
        } else {
            Log.d(TAG, "Текущий пользователь: null");
        }
        return user;
    }

    /**
     * Выход из учетной записи
     * @param activity активити для выхода из Google
     * @param logoutCallback колбэк с результатом выхода
     */
    public void signOut(Activity activity, final LogoutCallback logoutCallback) {
        Log.d(TAG, "Выполняется выход из аккаунта");
        
        // Выход из Firebase
        firebaseAuth.signOut();
        
        // Выход из Google
        googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
                    Log.d(TAG, "Выход завершен");
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