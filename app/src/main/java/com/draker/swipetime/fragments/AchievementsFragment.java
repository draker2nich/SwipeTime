package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.adapters.AchievementsAdapter;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.viewmodels.ProfileViewModel;

/**
 * Фрагмент для отображения достижений пользователя
 */
public class AchievementsFragment extends Fragment {

    private ProfileViewModel viewModel;
    private RecyclerView recyclerView;
    private AchievementsAdapter adapter;
    private ProgressBar overallProgressBar;
    private TextView achievementsCountText;
    
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
        
        // Инициализация UI компонентов
        recyclerView = view.findViewById(R.id.achievements_recycler_view);
        overallProgressBar = view.findViewById(R.id.overall_progress_bar);
        achievementsCountText = view.findViewById(R.id.achievements_count_text);
        
        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AchievementsAdapter();
        recyclerView.setAdapter(adapter);
        
        // Наблюдение за данными
        observeData();
    }
    
    private void observeData() {
        // Наблюдаем за текущим пользователем
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Получаем достижения пользователя
                GamificationManager gamificationManager = GamificationManager.getInstance(requireContext());
                adapter.setAchievements(gamificationManager.getUserAchievements(user.getId()));
                
                // Обновляем счетчик и прогресс
                int completedCount = gamificationManager.getCompletedAchievementsCount(user.getId());
                int totalCount = gamificationManager.getTotalAchievementsCount();
                
                achievementsCountText.setText(completedCount + " из " + totalCount);
                
                // Устанавливаем прогресс в ProgressBar
                int overallProgress = totalCount > 0 ? (completedCount * 100) / totalCount : 0;
                overallProgressBar.setProgress(overallProgress);
            }
        });
    }
}