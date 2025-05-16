package com.draker.swipetime;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.draker.swipetime.fragments.AuthProfileFragment;
import com.draker.swipetime.fragments.CategoriesFragment;
import com.draker.swipetime.fragments.LikedContentFragment;
import com.draker.swipetime.database.DbCleanerUtil;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.NetworkHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Закомментировано для сохранения данных между запусками
        // DbCleanerUtil.deleteDatabase(getApplicationContext());

        // Инициализация системы геймификации
        GamificationIntegrator.ensureUserInitialized(getApplicationContext());
        
        // Инициализация мониторинга сети
        networkHelper = NetworkHelper.getInstance(this);

        // Инициализация BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Установка слушателя для навигации
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_categories) {
                selectedFragment = new CategoriesFragment();
            } else if (itemId == R.id.nav_liked) {
                selectedFragment = new LikedContentFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new AuthProfileFragment(); // Заменяем на AuthProfileFragment
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                return true;
            }
            
            return false;
        });

        // Установка фрагмента по умолчанию
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_categories);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Освобождаем ресурсы NetworkHelper
        if (networkHelper != null) {
            networkHelper.cleanup();
        }
    }
}
