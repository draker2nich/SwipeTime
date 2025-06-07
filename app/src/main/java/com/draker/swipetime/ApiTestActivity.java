package com.draker.swipetime;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.repositories.JikanRepository;
import com.draker.swipetime.utils.NetworkHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Активити для тестирования и мониторинга API улучшений
 * Временный класс для отладки и проверки работы новых компонентов
 */
public class ApiTestActivity extends Activity {
    private static final String TAG = "ApiTestActivity";
    
    private TextView statusTextView;
    private TextView metricsTextView;
    private Button testJikanButton;
    private Button resetStatsButton;
    private Button rateLimitStatusButton;
    
    private JikanRepository jikanRepository;
    private NetworkHelper networkHelper;
    private CompositeDisposable disposables = new CompositeDisposable();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Простой layout в коде для тестирования
        createTestLayout();
        
        // Инициализация компонентов
        jikanRepository = new JikanRepository();
        networkHelper = NetworkHelper.getInstance(this);
        
        Log.d(TAG, "ApiTestActivity создано для тестирования rate limiting");
        updateStatus("ApiTestActivity инициализировано");
    }
    
    private void createTestLayout() {
        // Создаем простой LinearLayout в коде
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // Заголовок
        TextView titleView = new TextView(this);
        titleView.setText("API Rate Limiting Test");
        titleView.setTextSize(20);
        titleView.setPadding(0, 0, 0, 32);
        layout.addView(titleView);
        
        // Статус
        statusTextView = new TextView(this);
        statusTextView.setText("Готов к тестированию");
        statusTextView.setPadding(0, 0, 0, 16);
        layout.addView(statusTextView);
        
        // Метрики
        metricsTextView = new TextView(this);
        metricsTextView.setText("Метрики будут отображены здесь");
        metricsTextView.setPadding(0, 0, 0, 32);
        metricsTextView.setTextSize(12);
        layout.addView(metricsTextView);
        
        // Кнопка тестирования Jikan API
        testJikanButton = new Button(this);
        testJikanButton.setText("Тест Jikan API (5 запросов)");
        testJikanButton.setOnClickListener(v -> testJikanApi());
        layout.addView(testJikanButton);
        
        // Кнопка сброса статистики
        resetStatsButton = new Button(this);
        resetStatsButton.setText("Сбросить статистику");
        resetStatsButton.setOnClickListener(v -> resetStatistics());
        layout.addView(resetStatsButton);
        
        // Кнопка статуса rate limiting
        rateLimitStatusButton = new Button(this);
        rateLimitStatusButton.setText("Показать статус Rate Limiting");
        rateLimitStatusButton.setOnClickListener(v -> showRateLimitStatus());
        layout.addView(rateLimitStatusButton);
        
        setContentView(layout);
    }
    
    private void testJikanApi() {
        updateStatus("Запуск теста Jikan API...");
        testJikanButton.setEnabled(false);
        
        Log.d(TAG, "Начинаем тест Jikan API с 5 последовательными запросами");
        
        // Делаем 5 запросов подряд для тестирования rate limiting
        for (int i = 1; i <= 5; i++) {
            final int requestNumber = i;
            
            disposables.add(
                jikanRepository.getTopAnime(requestNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        animeList -> {
                            String message = "Запрос " + requestNumber + ": Получено " + animeList.size() + " аниме";
                            Log.d(TAG, message);
                            updateStatus(message);
                            updateMetrics();
                            
                            if (requestNumber == 5) {
                                testJikanButton.setEnabled(true);
                                showTestResults();
                            }
                        },
                        error -> {
                            String message = "Запрос " + requestNumber + " ошибка: " + error.getMessage();
                            Log.e(TAG, message, error);
                            updateStatus(message);
                            updateMetrics();
                            
                            if (requestNumber == 5) {
                                testJikanButton.setEnabled(true);
                                showTestResults();
                            }
                        }
                    )
            );
            
            // Небольшая задержка между запросами
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void resetStatistics() {
        networkHelper.resetApiStatistics();
        RetrofitClient.resetAllLimiters();
        updateStatus("Статистика сброшена");
        updateMetrics();
        
        Toast.makeText(this, "Статистика API и rate limiters сброшены", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Вся статистика сброшена");
    }
    
    private void showRateLimitStatus() {
        String rateLimitStatus = RetrofitClient.getRateLimitStatus();
        Log.d(TAG, "Rate Limit Status:\n" + rateLimitStatus);
        
        // Показываем в AlertDialog для лучшего отображения
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Rate Limiting Status");
        builder.setMessage(rateLimitStatus);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    private void showTestResults() {
        String performanceReport = networkHelper.getApiPerformanceReport();
        Log.d(TAG, "Test Results:\n" + performanceReport);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Результаты теста API");
        builder.setMessage(performanceReport);
        builder.setPositiveButton("OK", null);
        builder.show();
        
        updateStatus("Тест завершен. Проверьте результаты.");
    }
    
    private void updateStatus(String message) {
        runOnUiThread(() -> {
            if (statusTextView != null) {
                statusTextView.setText("Статус: " + message);
            }
        });
    }
    
    private void updateMetrics() {
        runOnUiThread(() -> {
            if (metricsTextView != null) {
                String metrics = networkHelper.getApiPerformanceReport();
                // Показываем только первые несколько строк для компактности
                String[] lines = metrics.split("\n");
                StringBuilder shortMetrics = new StringBuilder();
                for (int i = 0; i < Math.min(lines.length, 8); i++) {
                    shortMetrics.append(lines[i]).append("\n");
                }
                metricsTextView.setText(shortMetrics.toString());
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Очищаем ресурсы
        disposables.clear();
        if (networkHelper != null) {
            networkHelper.cleanup();
        }
        
        Log.d(TAG, "ApiTestActivity уничтожено");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateMetrics(); // Обновляем метрики при возврате к активити
    }
}
