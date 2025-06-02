package com.draker.swipetime.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.draker.swipetime.R;
import com.draker.swipetime.utils.ThemeManager;

/**
 * Фрагмент настроек приложения с поддержкой тем и доступности
 */
public class SettingsFragment extends Fragment {
    
    private ThemeManager themeManager;
    private Vibrator vibrator;
    
    // UI элементы
    private TextView currentThemeText;
    private MaterialButton changeThemeButton;
    private Switch hapticFeedbackSwitch;
    private Switch largeTextSwitch;
    private Switch highContrastSwitch;
    private Switch reduceMotionSwitch;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeManager = new ThemeManager(requireContext());
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupThemeSettings();
        setupAccessibilitySettings();
        updateUI();
    }
    
    private void initializeViews(View view) {
        currentThemeText = view.findViewById(R.id.current_theme_text);
        changeThemeButton = view.findViewById(R.id.change_theme_button);
        hapticFeedbackSwitch = view.findViewById(R.id.haptic_feedback_switch);
        largeTextSwitch = view.findViewById(R.id.large_text_switch);
        highContrastSwitch = view.findViewById(R.id.high_contrast_switch);
        reduceMotionSwitch = view.findViewById(R.id.reduce_motion_switch);
    }
    
    private void setupThemeSettings() {
        changeThemeButton.setOnClickListener(v -> showThemeSelectionDialog());
    }
    
    private void setupAccessibilitySettings() {
        // Настройка вибрации
        hapticFeedbackSwitch.setChecked(themeManager.isHapticFeedbackEnabled());
        hapticFeedbackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setHapticFeedbackEnabled(isChecked);
            if (isChecked && vibrator != null) {
                performHapticFeedback();
            }
        });
        
        // Настройка крупного текста
        largeTextSwitch.setChecked(themeManager.isLargeTextEnabled());
        largeTextSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setLargeTextEnabled(isChecked);
            if (themeManager.isHapticFeedbackEnabled()) {
                performHapticFeedback();
            }
            // Можно добавить перезагрузку активности для применения изменений
            showRestartRecommendation();
        });
        
        // Настройка высокого контраста
        highContrastSwitch.setChecked(themeManager.isHighContrastEnabled());
        highContrastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setHighContrastEnabled(isChecked);
            if (themeManager.isHapticFeedbackEnabled()) {
                performHapticFeedback();
            }
            showRestartRecommendation();
        });
        
        // Настройка упрощенных анимаций
        reduceMotionSwitch.setChecked(themeManager.isReduceMotionEnabled());
        reduceMotionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setReduceMotionEnabled(isChecked);
            if (themeManager.isHapticFeedbackEnabled()) {
                performHapticFeedback();
            }
        });
    }
    
    private void showThemeSelectionDialog() {
        String[] themeOptions = {
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_auto)
        };
        
        int currentTheme = themeManager.getThemeMode();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.theme_settings))
               .setSingleChoiceItems(themeOptions, currentTheme, (dialog, which) -> {
                   themeManager.setThemeMode(which);
                   if (themeManager.isHapticFeedbackEnabled()) {
                       performHapticFeedback();
                   }
                   updateUI();
                   dialog.dismiss();
               })
               .setNegativeButton(android.R.string.cancel, null);
        
        builder.create().show();
    }
    
    private void updateUI() {
        // Обновляем текст текущей темы
        String currentTheme = themeManager.getThemeName(requireContext());
        currentThemeText.setText(getString(R.string.current_theme, currentTheme));
        
        // Применяем настройки доступности к элементам UI
        applyAccessibilitySettings();
    }
    
    private void applyAccessibilitySettings() {
        float textSizeMultiplier = themeManager.getTextSizeMultiplier();
        
        if (textSizeMultiplier != 1.0f) {
            // Применяем увеличенный размер текста
            currentThemeText.setTextSize(currentThemeText.getTextSize() * textSizeMultiplier);
        }
        
        // Применяем настройки высокого контраста если необходимо
        if (themeManager.isHighContrastEnabled()) {
            // Можно изменить стили для высокого контраста
            currentThemeText.setTextColor(
                requireContext().getColor(R.color.high_contrast_text)
            );
        }
    }
    
    private void performHapticFeedback() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }
    
    private void showRestartRecommendation() {
        // Показываем уведомление о том, что рекомендуется перезапустить приложение
        new AlertDialog.Builder(requireContext())
            .setTitle("Изменения применены")
            .setMessage("Для полного применения изменений рекомендуется перезапустить приложение.")
            .setPositiveButton("Понятно", null)
            .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
