package com.draker.swipetime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.FirebaseDiagnostics;
import com.draker.swipetime.utils.FirestoreDataManager;
import com.google.firebase.auth.FirebaseUser;

/**
 * Тестовая активность для диагностики и тестирования Firebase
 */
public class FirebaseTestActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseTestActivity";
    
    private TextView diagnosticsText;
    private Button runDiagnosticsButton;
    private Button signInButton;
    private Button signOutButton;
    private Button syncDataButton;
    private TextView statusText;
    
    private FirebaseAuthManager authManager;
    private FirestoreDataManager firestoreManager;
    
    private ActivityResultLauncher<Intent> signInLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_test);
        
        initializeViews();
        initializeManagers();
        setupClickListeners();
        
        // Автоматический запуск диагностики при старте
        runDiagnostics();
    }
    
    private void initializeViews() {
        diagnosticsText = findViewById(R.id.diagnostics_text);
        runDiagnosticsButton = findViewById(R.id.run_diagnostics_button);
        signInButton = findViewById(R.id.test_sign_in_button);
        signOutButton = findViewById(R.id.test_sign_out_button);
        syncDataButton = findViewById(R.id.test_sync_button);
        statusText = findViewById(R.id.status_text);
    }
    
    private void initializeManagers() {
        authManager = FirebaseAuthManager.getInstance(this);
        firestoreManager = FirestoreDataManager.getInstance(this);
        
        // Обработчик результата входа
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        handleSignInResult(data);
                    } else {
                        updateStatus("Вход отменен или не удался");
                    }
                });
    }
    
    private void setupClickListeners() {
        runDiagnosticsButton.setOnClickListener(v -> runDiagnostics());
        
        signInButton.setOnClickListener(v -> testSignIn());
        
        signOutButton.setOnClickListener(v -> testSignOut());
        
        syncDataButton.setOnClickListener(v -> testSyncData());
    }
    
    private void runDiagnostics() {
        updateStatus("Запуск диагностики...");
        
        FirebaseDiagnostics.DiagnosticsReport report = FirebaseDiagnostics.runFullDiagnostics(this);
        
        // Обновляем UI с результатами диагностики
        diagnosticsText.setText(report.getDetailedReport());
        
        // Обновляем статус
        updateStatus(report.getOverallStatus());
        
        // Обновляем состояние кнопок
        updateButtonStates(report);
        
        Log.d(TAG, "Диагностика завершена");
    }
    
    private void testSignIn() {
        updateStatus("Попытка входа через Google...");
        
        try {
            Intent signInIntent = authManager.getGoogleSignInIntent();
            signInLauncher.launch(signInIntent);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при запуске входа", e);
            updateStatus("Ошибка: " + e.getMessage());
        }
    }
    
    private void handleSignInResult(Intent data) {
        authManager.handleGoogleSignInResult(data, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                updateStatus("✅ Вход успешен: " + user.getEmail());
                Log.d(TAG, "Успешный вход: " + user.getUid());
                
                // Повторный запуск диагностики для обновления статуса
                runDiagnostics();
                
                Toast.makeText(FirebaseTestActivity.this, "Добро пожаловать!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                updateStatus("❌ Ошибка входа: " + errorMessage);
                Log.e(TAG, "Ошибка входа: " + errorMessage);
                
                Toast.makeText(FirebaseTestActivity.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void testSignOut() {
        updateStatus("Выход из аккаунта...");
        
        authManager.signOut(this, () -> {
            updateStatus("✅ Выход выполнен");
            Log.d(TAG, "Выход выполнен");
            
            // Повторный запуск диагностики для обновления статуса
            runDiagnostics();
            
            Toast.makeText(FirebaseTestActivity.this, "Выход выполнен", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void testSyncData() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null) {
            updateStatus("❌ Для синхронизации нужно войти в аккаунт");
            return;
        }
        
        updateStatus("Синхронизация данных...");
        
        firestoreManager.syncUserData(user.getUid(), new FirestoreDataManager.SyncCallback() {
            @Override
            public void onSuccess() {
                updateStatus("✅ Синхронизация успешна");
                Log.d(TAG, "Данные синхронизированы");
                Toast.makeText(FirebaseTestActivity.this, "Данные синхронизированы", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                updateStatus("❌ Ошибка синхронизации: " + errorMessage);
                Log.e(TAG, "Ошибка синхронизации: " + errorMessage);
                Toast.makeText(FirebaseTestActivity.this, "Ошибка синхронизации", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateStatus(String status) {
        statusText.setText("Статус: " + status);
        Log.d(TAG, "Статус: " + status);
    }
    
    private void updateButtonStates(FirebaseDiagnostics.DiagnosticsReport report) {
        // Кнопка входа доступна, если система готова и пользователь не вошел
        signInButton.setEnabled(report.isSystemReady() && !report.userSignedIn);
        
        // Кнопка выхода доступна, если пользователь вошел
        signOutButton.setEnabled(report.userSignedIn);
        
        // Кнопка синхронизации доступна, если пользователь вошел и есть интернет
        syncDataButton.setEnabled(report.userSignedIn && report.networkAvailable);
    }
}