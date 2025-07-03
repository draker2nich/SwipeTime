package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.fragments.AchievementsFragment;
import com.draker.swipetime.utils.GamificationManager;
// import com.draker.swipetime.utils.AchievementInitializer; // Класс удален в рамках рефакторинга
// import com.draker.swipetime.utils.AchievementDiagnostics; // Класс удален в рамках рефакторинга
import com.draker.swipetime.utils.XpLevelCalculator;

import android.app.AlertDialog;
import com.draker.swipetime.viewmodels.GamificationViewModel;
import com.draker.swipetime.viewmodels.ProfileViewModel;

/**
 * Фрагмент профиля пользователя с отображением статистики и достижений
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private GamificationViewModel gamificationViewModel;
    
    // UI элементы
    private ImageView avatarImage;
    private TextView usernameText;
    private TextView levelRankText;
    private TextView emailText;
    private ProgressBar levelProgressBar;
    private TextView progressText;
    private TextView swipesCount;
    private TextView ratingsCount;
    private TextView reviewsCount;
    private TextView achievementsCount;
    private Button viewAchievementsButton;
    private Button testSwipeButton;
    private Button testRatingButton;
    private Button testReviewButton;
    private Button testCompleteButton;
    private Button initializeAchievementsButton;
    private Button diagnosticsButton;
    private Button forceSyncButton;
    
    // Текущий ID пользователя (в реальном приложении должен быть получен из аутентификации)
    private static final String CURRENT_USER_ID = "user_1";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Инициализация ViewModel
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        gamificationViewModel = new ViewModelProvider(requireActivity()).get(GamificationViewModel.class);
        
        // Инициализация UI компонентов
        initializeViews(view);
        
        // Настройка обработчиков событий
        setupClickListeners();
        
        // Загрузка данных пользователя
        loadUserData();
    }
    
    /**
     * Инициализация UI элементов
     */
    private void initializeViews(View view) {
        avatarImage = view.findViewById(R.id.profile_avatar);
        usernameText = view.findViewById(R.id.username_text);
        levelRankText = view.findViewById(R.id.level_rank_text);
        emailText = view.findViewById(R.id.email_text);
        levelProgressBar = view.findViewById(R.id.level_progress_bar);
        progressText = view.findViewById(R.id.progress_text);
        
        swipesCount = view.findViewById(R.id.swipes_count);
        ratingsCount = view.findViewById(R.id.ratings_count);
        reviewsCount = view.findViewById(R.id.reviews_count);
        achievementsCount = view.findViewById(R.id.achievements_count);
        
        viewAchievementsButton = view.findViewById(R.id.view_achievements_button);
        testSwipeButton = view.findViewById(R.id.test_swipe_button);
        testRatingButton = view.findViewById(R.id.test_rating_button);
        testReviewButton = view.findViewById(R.id.test_review_button);
        testCompleteButton = view.findViewById(R.id.test_complete_button);
        initializeAchievementsButton = view.findViewById(R.id.initialize_achievements_button);
        diagnosticsButton = view.findViewById(R.id.diagnostics_button);
        forceSyncButton = view.findViewById(R.id.force_sync_button);
    }
    
    /**
     * Настройка обработчиков нажатий
     */
    private void setupClickListeners() {
        viewAchievementsButton.setOnClickListener(v -> {
            // Открываем фрагмент с достижениями
            Fragment achievementsFragment = new com.draker.swipetime.fragments.AchievementsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, achievementsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        
        // Обработчики тестовых кнопок для имитации действий
        testSwipeButton.setOnClickListener(v -> {
            // Имитируем свайп вправо
            boolean levelUp = gamificationViewModel.registerSwipe(true, "test_content_id", "Тестовый контент");
            
            // Обновляем UI
            showActionResult("Свайп", levelUp);
        });
        
        testRatingButton.setOnClickListener(v -> {
            // Имитируем оценку
            boolean levelUp = gamificationViewModel.registerRating("test_content_id", "Тестовый контент", 4.5f);
            
            // Обновляем UI
            showActionResult("Оценка", levelUp);
        });
        
        testReviewButton.setOnClickListener(v -> {
            // Имитируем написание рецензии
            boolean levelUp = gamificationViewModel.registerReview("test_content_id", "Тестовый контент");
            
            // Обновляем UI
            showActionResult("Рецензия", levelUp);
        });
        
        testCompleteButton.setOnClickListener(v -> {
            // Имитируем просмотр/прочтение
            boolean levelUp = gamificationViewModel.registerCompletion("test_content_id", "Тестовый контент", "Фильмы");
            
            // Обновляем UI
            showActionResult("Просмотр", levelUp);
        });
        
        initializeAchievementsButton.setOnClickListener(v -> {
            // Система достижений временно отключена
            Toast.makeText(requireContext(), 
                "Система достижений временно отключена", 
                Toast.LENGTH_LONG).show();
        });
        
        diagnosticsButton.setOnClickListener(v -> {
            // Диагностика достижений временно отключена
            Toast.makeText(requireContext(), 
                "Диагностика достижений временно отключена", 
                Toast.LENGTH_SHORT).show();
        });
        
        forceSyncButton.setOnClickListener(v -> {
            // Принудительная синхронизация (временно отключена)
            new Thread(() -> {
                // int syncedCount = AchievementDiagnostics.forceSyncAchievements(requireContext()); // Временно отключено
                int syncedCount = 0; // Заглушка
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                        "Функция синхронизации временно отключена", 
                        Toast.LENGTH_LONG).show();
                    // Перезагружаем данные
                    loadUserData();
                });
            }).start();
        });
    }
    
    /**
     * Загрузка данных пользователя
     */
    private void loadUserData() {
        // Загружаем данные геймификации пользователя
        gamificationViewModel.loadUserData();
        
        // Наблюдаем за изменением данных пользователя
        gamificationViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUserUI);
        
        // Наблюдаем за изменением статистики пользователя
        gamificationViewModel.getUserStats().observe(getViewLifecycleOwner(), this::updateStatsUI);
        
        // Наблюдаем за изменением прогресса уровня
        gamificationViewModel.getLevelProgress().observe(getViewLifecycleOwner(), this::updateLevelProgressUI);
        
        // Наблюдаем за количеством выполненных достижений
        gamificationViewModel.getCompletedAchievementsCount().observe(getViewLifecycleOwner(), completed -> {
            gamificationViewModel.getTotalAchievementsCount().observe(getViewLifecycleOwner(), total -> {
                achievementsCount.setText(completed + " / " + total);
            });
        });
    }
    
    /**
     * Обновление UI элементов данными пользователя
     */
    private void updateUserUI(UserEntity user) {
        if (user != null) {
            usernameText.setText(user.getUsername());
            emailText.setText(user.getEmail());
            
            // Формируем текст с уровнем и званием
            String rank = XpLevelCalculator.getLevelRank(user.getLevel());
            levelRankText.setText(getString(R.string.level_rank_format, user.getLevel(), rank));
            
            // Загрузка аватара (в реальном приложении используйте Glide или Picasso)
            // Glide.with(this).load(user.getAvatarUrl()).into(avatarImage);
        }
    }
    
    /**
     * Обновление UI элементов статистикой пользователя
     */
    private void updateStatsUI(UserStatsEntity stats) {
        if (stats != null) {
            swipesCount.setText(String.valueOf(stats.getSwipesCount()));
            ratingsCount.setText(String.valueOf(stats.getRatingsCount()));
            reviewsCount.setText(String.valueOf(stats.getReviewsCount()));
        }
    }
    
    /**
     * Обновление прогресс-бара уровня
     */
    private void updateLevelProgressUI(Integer progress) {
        if (progress != null) {
            levelProgressBar.setProgress(progress);
            
            // Получаем текущий уровень пользователя
            UserEntity user = gamificationViewModel.getCurrentUser().getValue();
            if (user != null) {
                progressText.setText(getString(R.string.level_progress_format, progress, user.getLevel() + 1));
            }
        }
    }
    
    /**
     * Отображение результата действия
     */
    private void showActionResult(String actionName, boolean levelUp) {
        String message = actionName + " выполнен" + (levelUp ? ". Уровень повышен!" : "!");
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
