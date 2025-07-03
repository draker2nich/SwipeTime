package com.draker.swipetime;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.draker.swipetime.fragments.AuthProfileFragment;
import com.draker.swipetime.fragments.CategoriesFragment;
import com.draker.swipetime.fragments.LikedContentFragment;
import com.draker.swipetime.utils.FragmentMigrationHelper;
import com.draker.swipetime.utils.HapticFeedbackManager;
import com.draker.swipetime.utils.InfiniteContentManager;
import com.draker.swipetime.utils.NetworkHelper;
import com.draker.swipetime.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavigationRailView navigationRailView;
    private NetworkHelper networkHelper;
    private ThemeManager themeManager;
    private HapticFeedbackManager hapticManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Инициализируем менеджер тем перед setContentView
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Инициализируем помощники UI/UX
        hapticManager = HapticFeedbackManager.getInstance(this);

        // Инициализация мониторинга сети
        networkHelper = NetworkHelper.getInstance(this);
        
        // Устанавливаем использование бесконечных фрагментов по умолчанию
        FragmentMigrationHelper.setUseInfiniteFragments(this, true);
        
        // Предварительно инициализируем менеджер бесконечного контента
        InfiniteContentManager.getInstance();

        // Инициализация навигации (адаптивная для планшетов и телефонов)
        setupAdaptiveNavigation();
        
        // Настройка доступности для навигации
        setupNavigationAccessibility();
        
        // Установка фрагмента по умолчанию
        if (savedInstanceState == null) {
            boolean result = navigateToFragment(R.id.nav_categories);
            if (result) {
                syncNavigationSelection(R.id.nav_categories);
            }
        }
    }
    
    /**
     * Настройка адаптивной навигации для разных размеров экрана
     */
    private void setupAdaptiveNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationRailView = findViewById(R.id.navigation_rail);
        
        // Определяем тип навигации на основе размера экрана и ориентации
        boolean isTablet = getResources().getConfiguration().smallestScreenWidthDp >= 720;
        boolean isLandscape = getResources().getConfiguration().orientation == 
                android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        
        // Используем navigation rail для планшетов или телефонов в landscape
        boolean useNavigationRail = isTablet || isLandscape;
        
        if (useNavigationRail && navigationRailView != null) {
            // Боковая навигация
            showNavigationRail();
            setupNavigationRail();
        } else if (bottomNavigationView != null) {
            // Нижняя навигация
            showBottomNavigation();
            setupBottomNavigation();
        }
    }
    
    /**
     * Показать боковую навигацию и скрыть нижнюю
     */
    private void showNavigationRail() {
        if (navigationRailView != null) {
            navigationRailView.setVisibility(View.VISIBLE);
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        
        // Изменяем constraints для fragment_container
        adjustFragmentContainerForRail();
    }
    
    /**
     * Показать нижнюю навигацию и скрыть боковую
     */
    private void showBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        if (navigationRailView != null) {
            navigationRailView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Настроить constraints для fragment_container при использовании navigation rail
     */
    private void adjustFragmentContainerForRail() {
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            // Обновляем layout параметры если это необходимо
            // В данном случае планшетный layout уже правильно настроен
        }
    }
    
    /**
     * Настройка нижней навигации для телефонов
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            performHapticFeedback();
            boolean result = navigateToFragment(item.getItemId());
            if (result) {
                syncNavigationSelection(item.getItemId());
            }
            return result;
        });
    }
    
    /**
     * Настройка боковой навигации для планшетов
     */
    private void setupNavigationRail() {
        navigationRailView.setOnItemSelectedListener(item -> {
            performHapticFeedback();
            boolean result = navigateToFragment(item.getItemId());
            if (result) {
                syncNavigationSelection(item.getItemId());
            }
            return result;
        });
    }
    
    /**
     * Выполнить хаптическую обратную связь
     */
    private void performHapticFeedback() {
        if (hapticManager != null) {
            hapticManager.performLightHaptic(null);
        }
    }
    
    /**
     * Навигация к фрагменту по ID пункта меню
     */
    private boolean navigateToFragment(int itemId) {
        Fragment selectedFragment = null;
        
        if (itemId == R.id.nav_categories) {
            selectedFragment = new CategoriesFragment();
        } else if (itemId == R.id.nav_liked) {
            selectedFragment = new LikedContentFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new AuthProfileFragment();
        }

        if (selectedFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            
            // Добавляем анимации переходов если они не отключены
            if (!themeManager.isReduceMotionEnabled()) {
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            }
            
            transaction.replace(R.id.fragment_container, selectedFragment);
            transaction.commit();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Синхронизация выбора между навигациями без вызова listeners
     */
    private void syncNavigationSelection(int itemId) {
        // Временно отключаем listeners чтобы избежать рекурсии
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(null);
            bottomNavigationView.setSelectedItemId(itemId);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                performHapticFeedback();
                boolean result = navigateToFragment(item.getItemId());
                if (result) {
                    syncNavigationSelection(item.getItemId());
                }
                return result;
            });
        }
        
        if (navigationRailView != null) {
            navigationRailView.setOnItemSelectedListener(null);
            navigationRailView.setSelectedItemId(itemId);
            navigationRailView.setOnItemSelectedListener(item -> {
                performHapticFeedback();
                boolean result = navigateToFragment(item.getItemId());
                if (result) {
                    syncNavigationSelection(item.getItemId());
                }
                return result;
            });
        }
    }
    
    /**
     * Настройка доступности для навигации
     */
    private void setupNavigationAccessibility() {
        // Настройка для нижней навигации
        if (bottomNavigationView != null) {
            // Устанавливаем описания для пунктов навигации
            bottomNavigationView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        
        // Настройка для боковой навигации
        if (navigationRailView != null) {
            navigationRailView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }
    
    /**
     * Получить менеджер тем для использования в фрагментах
     */
    public ThemeManager getThemeManager() {
        return themeManager;
    }
    
    /**
     * Получить менеджер хаптической обратной связи
     */
    public HapticFeedbackManager getHapticManager() {
        return hapticManager;
    }
    
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Перенастраиваем навигацию при изменении конфигурации
        setupAdaptiveNavigation();
        
        // Применяем тему заново если нужно
        if (themeManager != null) {
            themeManager.applyTheme();
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
    
    @Override
    public void onBackPressed() {
        // Хаптическая обратная связь при нажатии назад
        performHapticFeedback();
        super.onBackPressed();
    }
}
