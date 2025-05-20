package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.adapters.AchievementsAdapter;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.AchievementInitializer;
import com.draker.swipetime.viewmodels.ProfileViewModel;

import java.util.List;

/**
 * Фрагмент для отображения достижений пользователя
 */
public class AchievementsFragment extends Fragment {

    private ProfileViewModel viewModel;
    private RecyclerView recyclerView;
    private AchievementsAdapter adapter;
    private ProgressBar overallProgressBar;
    private TextView achievementsCountText;
    private Button initializeButton;
    private Button refreshButton;

    public AchievementsFragment() {
        // Пустой конструктор, требуется для фрагментов
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
        setupRecyclerView();
        observeData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("AchievementsFragment", "onResume - перезагружаем достижения");
        loadAchievementsDirectly();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.achievements_recycler_view);
        overallProgressBar = view.findViewById(R.id.overall_progress_bar);
        achievementsCountText = view.findViewById(R.id.achievements_count_text);
        initializeButton = view.findViewById(R.id.initialize_achievements_button);
        refreshButton = view.findViewById(R.id.refresh_achievements_button);
    }

    private void setupClickListeners() {
        if (initializeButton != null) {
            initializeButton.setOnClickListener(v -> initializeAchievements());
        }
        
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> {
                Log.d("AchievementsFragment", "Принудительное обновление по нажатию кнопки");
                loadAchievementsDirectly();
                Toast.makeText(requireContext(), "Обновляем достижения...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AchievementsAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void observeData() {
        loadAchievementsDirectly();
        
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Log.d("AchievementsFragment", "Пользователь из ViewModel: " + user.getId());
                loadAchievementsDirectly();
            }
        });
    }
    
    private void loadAchievementsDirectly() {
        try {
            String actualUserId = GamificationIntegrator.getCurrentUserId(requireContext());
            Log.d("AchievementsFragment", "Загружаем достижения для пользователя: " + actualUserId);
            
            GamificationManager gamificationManager = GamificationManager.getInstance(requireContext());
            List<GamificationManager.UserAchievementInfo> achievements = gamificationManager.getUserAchievements(actualUserId);
            
            Log.d("AchievementsFragment", "Получено достижений от менеджера: " + achievements.size());
            
            for (int i = 0; i < Math.min(5, achievements.size()); i++) {
                GamificationManager.UserAchievementInfo info = achievements.get(i);
                Log.d("AchievementsFragment", "Достижение " + (i+1) + ": " + 
                      info.getAchievement().getTitle() + 
                      " (выполнено: " + info.isCompleted() + 
                      ", прогресс: " + info.getProgress() + "%)");
            }
            
            adapter.setAchievements(achievements);
            
            int completedCount = gamificationManager.getCompletedAchievementsCount(actualUserId);
            int totalCount = gamificationManager.getTotalAchievementsCount();

            Log.d("AchievementsFragment", "Статистика достижений - выполнено: " + completedCount + ", всего: " + totalCount);

            achievementsCountText.setText(completedCount + " из " + totalCount);

            int overallProgress = totalCount > 0 ? (completedCount * 100) / totalCount : 0;
            overallProgressBar.setProgress(overallProgress);

            if (initializeButton != null) {
                initializeButton.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
            }
            
        } catch (Exception e) {
            Log.e("AchievementsFragment", "Ошибка загрузки достижений: " + e.getMessage());
        }
    }

    private void initializeAchievements() {
        if (initializeButton != null) {
            initializeButton.setEnabled(false);
            initializeButton.setText("Инициализация...");
        }

        AchievementInitializer.forceInitializeAchievements(requireContext(), new AchievementInitializer.InitializationCallback() {
            @Override
            public void onInitialized(boolean success, int achievementsCount) {
                requireActivity().runOnUiThread(() -> {
                    if (initializeButton != null) {
                        initializeButton.setEnabled(true);
                        initializeButton.setText("Инициализировать достижения");
                    }

                    if (success) {
                        Toast.makeText(requireContext(),
                                "Достижения инициализированы: " + achievementsCount,
                                Toast.LENGTH_LONG).show();
                        loadAchievementsDirectly();
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка инициализации достижений",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
