package com.draker.swipetime;

import android.app.Application;

import com.draker.swipetime.database.DataGenerator;

/**
 * Класс приложения для выполнения инициализации при запуске
 */
public class SwipeTimeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Заполнить базу данных тестовыми данными
        DataGenerator.populateDatabase(this);
    }
}
