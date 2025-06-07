package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.draker.swipetime.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Диагностический класс для проверки конфигурации Firebase
 */
public class FirebaseDiagnostics {

    private static final String TAG = "FirebaseDiagnostics";
    
    /**
     * Выполнить полную диагностику Firebase конфигурации
     * @param context контекст приложения
     * @return отчет о диагностике
     */
    public static DiagnosticsReport runFullDiagnostics(@NonNull Context context) {
        DiagnosticsReport report = new DiagnosticsReport();
        
        Log.d(TAG, "Запуск полной диагностики Firebase");
        
        // 1. Проверка Google Play Services
        report.googlePlayServicesAvailable = checkGooglePlayServices(context);
        
        // 2. Проверка Firebase Auth
        report.firebaseAuthConfigured = checkFirebaseAuth();
        
        // 3. Проверка Firestore
        report.firestoreConfigured = checkFirestore();
        
        // 4. Проверка Google Sign-In конфигурации
        report.googleSignInConfigured = checkGoogleSignInConfig(context);
        
        // 5. Проверка текущего пользователя
        report.userSignedIn = checkCurrentUser();
        
        // 6. Проверка сетевого соединения
        report.networkAvailable = NetworkHelper.getInstance(context).isInternetAvailable();
        
        // 7. Проверка Web Client ID
        report.webClientIdValid = checkWebClientId(context);
        
        Log.d(TAG, "Диагностика завершена: " + report.getOverallStatus());
        
        return report;
    }
    
    /**
     * Проверка доступности Google Play Services
     */
    private static boolean checkGooglePlayServices(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        boolean available = resultCode == ConnectionResult.SUCCESS;
        
        Log.d(TAG, "Google Play Services доступны: " + available + 
                (available ? "" : " (код ошибки: " + resultCode + ")"));
        
        return available;
    }
    
    /**
     * Проверка конфигурации Firebase Auth
     */
    private static boolean checkFirebaseAuth() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            // Проверяем, что Auth инициализирован и может получить текущего пользователя
            auth.getCurrentUser(); // Этот вызов покажет, работает ли Auth
            Log.d(TAG, "Firebase Auth сконфигурирован: true");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки Firebase Auth", e);
            return false;
        }
    }
    
    /**
     * Проверка конфигурации Firestore
     */
    private static boolean checkFirestore() {
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            // Проверяем, что Firestore может быть инициализирован
            FirebaseFirestoreSettings settings = firestore.getFirestoreSettings();
            Log.d(TAG, "Firestore настройки получены успешно");
            Log.d(TAG, "Firestore сконфигурирован: true");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки Firestore", e);
            return false;
        }
    }
    
    /**
     * Проверка конфигурации Google Sign-In
     */
    private static boolean checkGoogleSignInConfig(Context context) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            Log.d(TAG, "Последний вошедший аккаунт Google: " + 
                    (account != null ? account.getEmail() : "null"));
            return true; // Само API доступно
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки Google Sign-In", e);
            return false;
        }
    }
    
    /**
     * Проверка текущего пользователя
     */
    private static boolean checkCurrentUser() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean signedIn = user != null;
            
            if (signedIn) {
                Log.d(TAG, "Текущий пользователь: " + user.getUid() + " (" + user.getEmail() + ")");
                Log.d(TAG, "Провайдеры аутентификации: " + user.getProviderData().size() + " провайдеров");
            } else {
                Log.d(TAG, "Пользователь не вошел в систему");
            }
            
            return signedIn;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки текущего пользователя", e);
            return false;
        }
    }
    
    /**
     * Проверка корректности Web Client ID
     */
    private static boolean checkWebClientId(Context context) {
        try {
            String webClientId = context.getString(R.string.default_web_client_id);
            boolean valid = webClientId.contains(".apps.googleusercontent.com") &&
                          webClientId.startsWith("305522930969-") && // Начало вашего project number
                          webClientId.equals("305522930969-tk9vj77cqfnplae8ms5tesurd2oh1mir.apps.googleusercontent.com"); // Точное соответствие
            
            Log.d(TAG, "Web Client ID: " + webClientId);
            Log.d(TAG, "Web Client ID валиден: " + valid);
            
            return valid;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки Web Client ID", e);
            return false;
        }
    }
    
    /**
     * Класс для хранения результатов диагностики
     */
    public static class DiagnosticsReport {
        public boolean googlePlayServicesAvailable = false;
        public boolean firebaseAuthConfigured = false;
        public boolean firestoreConfigured = false;
        public boolean googleSignInConfigured = false;
        public boolean userSignedIn = false;
        public boolean networkAvailable = false;
        public boolean webClientIdValid = false;
        
        /**
         * Получить общий статус готовности системы
         */
        public boolean isSystemReady() {
            return googlePlayServicesAvailable && 
                   firebaseAuthConfigured && 
                   firestoreConfigured && 
                   googleSignInConfigured && 
                   networkAvailable &&
                   webClientIdValid;
        }
        
        /**
         * Получить строковое описание общего статуса
         */
        public String getOverallStatus() {
            if (isSystemReady()) {
                return "✅ Система готова к работе" + (userSignedIn ? " (пользователь вошел)" : " (пользователь не вошел)");
            } else {
                return "❌ Система не готова. Проблемы: " + getProblems();
            }
        }
        
        /**
         * Получить список проблем
         */
        public String getProblems() {
            StringBuilder problems = new StringBuilder();
            
            if (!googlePlayServicesAvailable) problems.append("Google Play Services недоступны; ");
            if (!firebaseAuthConfigured) problems.append("Firebase Auth не настроен; ");
            if (!firestoreConfigured) problems.append("Firestore не настроен; ");
            if (!googleSignInConfigured) problems.append("Google Sign-In не настроен; ");
            if (!networkAvailable) problems.append("Нет подключения к интернету; ");
            if (!webClientIdValid) problems.append("Неверный Web Client ID; ");
            
            return problems.toString();
        }
        
        /**
         * Получить детальный отчет
         */
        public String getDetailedReport() {
            return String.format(
                "🔍 ДИАГНОСТИКА FIREBASE\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Google Play Services: %s\n" +
                "Firebase Auth: %s\n" +
                "Firestore: %s\n" +
                "Google Sign-In: %s\n" +
                "Web Client ID: %s\n" +
                "Сетевое соединение: %s\n" +
                "Пользователь вошел: %s\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "СТАТУС: %s",
                formatStatus(googlePlayServicesAvailable),
                formatStatus(firebaseAuthConfigured),
                formatStatus(firestoreConfigured),
                formatStatus(googleSignInConfigured),
                formatStatus(webClientIdValid),
                formatStatus(networkAvailable),
                formatStatus(userSignedIn),
                getOverallStatus()
            );
        }
        
        private String formatStatus(boolean status) {
            return status ? "✅ ОК" : "❌ ПРОБЛЕМА";
        }
    }
}