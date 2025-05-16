package com.draker.swipetime;

import android.app.Application;

import com.draker.swipetime.database.DataGenerator;
import com.draker.swipetime.utils.AchievementNotifier;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.GamificationManager;

/**
 * Класс приложения для выполнения инициализации при запуске
 */
public class SwipeTimeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Заполнить базу данных тестовыми данными
        DataGenerator.populateDatabase(this);
        
        // Инициализация системы геймификации
        GamificationIntegrator.ensureUserInitialized(this);
        
        // Дополнительная настройка может быть здесь, если нужно
    }
}
