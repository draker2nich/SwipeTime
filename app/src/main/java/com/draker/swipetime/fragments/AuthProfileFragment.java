package com.draker.swipetime.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.FirestoreDataManager;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.NetworkHelper;
import com.draker.swipetime.utils.XpLevelCalculator;
import com.draker.swipetime.viewmodels.GamificationViewModel;
import com.draker.swipetime.viewmodels.ProfileViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;

/**
 * Фрагмент профиля пользователя с поддержкой аутентификации
 */
public class AuthProfileFragment extends Fragment {

    private static final String TAG = "AuthProfileFragment";
    private static final int RC_SIGN_IN = 9001;

    private ProfileViewModel profileViewModel;
    private GamificationViewModel gamificationViewModel;
    
    // UI элементы для неавторизованного пользователя
    private MaterialCardView authCardView;
    private Button signInButton;
    
    // UI элементы для авторизованного пользователя
    private MaterialCardView profileCardView;
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
    private Button syncButton;
    private Button signOutButton;
    private Button testSwipeButton;
    private Button testRatingButton;
    private Button testReviewButton;
    private Button testCompleteButton;
    
    // Менеджеры для работы с Firebase и сетью
    private FirebaseAuthManager authManager;
    private FirestoreDataManager firestoreManager;
    private NetworkHelper networkHelper;
    
    // Статус авторизации и синхронизации
    private boolean isAuthenticated = false;
    private MaterialCardView networkStatusCard;
    private TextView networkStatusText;
    
    // Обработчик результата активности входа через Google
    private ActivityResultLauncher<Intent> signInLauncher;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализация менеджеров
        authManager = FirebaseAuthManager.getInstance(requireContext());
        firestoreManager = FirestoreDataManager.getInstance(requireContext());
        networkHelper = NetworkHelper.getInstance(requireContext());
        
        // Настройка обработчика результата входа через Google
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Log.d(TAG, "Получен результат RESULT_OK от Google Sign In");
                        handleSignInResult(data);
                    } else {
                        Log.e(TAG, "Google Sign In не удался, код результата: " + result.getResultCode());
                        Toast.makeText(requireContext(), "Вход отменен или не удался", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_profile, container, false);
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
        
        // Проверка состояния авторизации
        checkAuthState();
        
        // Наблюдение за состоянием сети
        observeNetworkState();
    }
    
    /**
     * Инициализация UI элементов
     */
    private void initializeViews(View view) {
        // UI для неавторизованного пользователя
        authCardView = view.findViewById(R.id.auth_card_view);
        signInButton = view.findViewById(R.id.sign_in_button);
        
        // UI для авторизованного пользователя
        profileCardView = view.findViewById(R.id.profile_card_view);
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
        syncButton = view.findViewById(R.id.sync_button);
        signOutButton = view.findViewById(R.id.sign_out_button);
        testSwipeButton = view.findViewById(R.id.test_swipe_button);
        testRatingButton = view.findViewById(R.id.test_rating_button);
        testReviewButton = view.findViewById(R.id.test_review_button);
        testCompleteButton = view.findViewById(R.id.test_complete_button);
        
        // UI для статуса сети
        networkStatusCard = view.findViewById(R.id.network_status_card);
        networkStatusText = view.findViewById(R.id.network_status_text);
    }
    
    /**
     * Настройка обработчиков нажатий
     */
    private void setupClickListeners() {
        // Кнопка входа через Google
        signInButton.setOnClickListener(v -> signIn());
        
        // Кнопка синхронизации данных
        syncButton.setOnClickListener(v -> syncUserData());
        
        // Кнопка выхода
        signOutButton.setOnClickListener(v -> signOut());
        
        // Кнопка просмотра достижений
        viewAchievementsButton.setOnClickListener(v -> {
            Fragment achievementsFragment = new AchievementsFragment();
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
    }
    
    /**
     * Проверка состояния авторизации
     */
    private void checkAuthState() {
        isAuthenticated = authManager.isUserSignedIn();
        updateUI(isAuthenticated);
        
        if (isAuthenticated) {
            FirebaseUser user = authManager.getCurrentUser();
            if (user != null) {
                // Загрузка данных пользователя из локальной базы
                gamificationViewModel.loadUserData(user.getUid());
                
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
        }
    }
    
    /**
     * Метод для входа через Google
     */
    private void signIn() {
        try {
            Log.d(TAG, "Запуск процесса входа через Google");
            Intent signInIntent = authManager.getGoogleSignInIntent();
            signInLauncher.launch(signInIntent);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при запуске входа через Google", e);
            Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Обработка результата входа через Google
     */
    private void handleSignInResult(Intent data) {
        if (data == null) {
            Log.e(TAG, "Intent data is null in handleSignInResult");
            Toast.makeText(requireContext(), "Ошибка: данные Intent равны null", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Получен результат входа через Google, обрабатываем...");
        authManager.handleGoogleSignInResult(data, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Успешный вход
                Log.d(TAG, "Вход выполнен успешно: " + user.getEmail());
                Toast.makeText(requireContext(), "Добро пожаловать, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                
                // Обновление UI
                isAuthenticated = true;
                updateUI(true);
                
                // Загрузка данных пользователя
                loadUserData(user.getUid());
            }

            @Override
            public void onFailure(String errorMessage) {
                // Ошибка входа
                Log.e(TAG, "Ошибка входа: " + errorMessage);
                Toast.makeText(requireContext(), "Ошибка входа: " + errorMessage, Toast.LENGTH_SHORT).show();
                
                // Обновление UI
                isAuthenticated = false;
                updateUI(false);
            }
        });
    }
    
    /**
     * Загрузка данных пользователя
     */
    private void loadUserData(String userId) {
        // Загрузка данных из локальной базы данных
        gamificationViewModel.loadUserData(userId);
        
        // Если есть интернет, синхронизируем данные с облаком
        if (networkHelper.isInternetAvailable()) {
            firestoreManager.loadUserDataFromCloud(userId, new FirestoreDataManager.LoadCallback() {
                @Override
                public void onSuccess() {
                    // Обновление UI после успешной загрузки
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Данные успешно загружены из облака", Toast.LENGTH_SHORT).show();
                            gamificationViewModel.loadUserData(userId);
                        });
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Ошибка загрузки данных
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(requireContext(), "Ошибка загрузки данных: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        }
    }
    
    /**
     * Синхронизация данных пользователя с облаком
     */
    private void syncUserData() {
        if (!isAuthenticated) {
            Toast.makeText(requireContext(), "Войдите в аккаунт для синхронизации", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверка наличия интернета
        if (!networkHelper.isInternetAvailable()) {
            Toast.makeText(requireContext(), "Отсутствует подключение к интернету", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Получение текущего пользователя
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Синхронизация данных
        Toast.makeText(requireContext(), "Синхронизация данных...", Toast.LENGTH_SHORT).show();
        firestoreManager.syncUserData(user.getUid(), new FirestoreDataManager.SyncCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "Синхронизация успешно завершена", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "Ошибка синхронизации: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    
    /**
     * Выход из аккаунта
     */
    private void signOut() {
        if (!isAuthenticated) {
            return;
        }
        
        authManager.signOut(requireActivity(), () -> {
            // Успешный выход
            Toast.makeText(requireContext(), "Выход выполнен", Toast.LENGTH_SHORT).show();
            
            // Обновление UI
            isAuthenticated = false;
            updateUI(false);
        });
    }
    
    /**
     * Обновление UI в зависимости от статуса авторизации
     * @param isSignedIn статус авторизации
     */
    private void updateUI(boolean isSignedIn) {
        authCardView.setVisibility(isSignedIn ? View.GONE : View.VISIBLE);
        profileCardView.setVisibility(isSignedIn ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Наблюдение за состоянием сети
     */
    private void observeNetworkState() {
        networkHelper.getNetworkAvailability().observe(getViewLifecycleOwner(), isAvailable -> {
            // Обновление UI для статуса сети
            networkStatusCard.setVisibility(isAvailable ? View.GONE : View.VISIBLE);
            networkStatusText.setText(R.string.offline_mode_active);
            
            // Включение/отключение кнопки синхронизации
            syncButton.setEnabled(isAvailable);
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
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка наблюдателей
    }
}