package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Менеджер для управления хаптической обратной связью
 */
public class HapticFeedbackManager {
    private static final String PREFS_NAME = "haptic_prefs";
    private static final String KEY_HAPTIC_ENABLED = "haptic_enabled";
    
    private static HapticFeedbackManager instance;
    private SharedPreferences prefs;
    private Vibrator vibrator;
    private boolean isHapticEnabled;
    
    private HapticFeedbackManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        isHapticEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true);
    }
    
    public static HapticFeedbackManager getInstance(Context context) {
        if (instance == null) {
            instance = new HapticFeedbackManager(context);
        }
        return instance;
    }
    
    /**
     * Включает/выключает хаптическую обратную связь
     */
    public void setHapticEnabled(boolean enabled) {
        isHapticEnabled = enabled;
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply();
    }
    
    /**
     * Проверяет, включена ли хаптическая обратная связь
     */
    public boolean isHapticEnabled() {
        return isHapticEnabled && vibrator != null && vibrator.hasVibrator();
    }
    
    /**
     * Легкая вибрация для обычных действий (клики, выборы)
     */
    public void performLightHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else {
            performCustomVibration(10); // 10ms для легкой вибрации
        }
    }
    
    /**
     * Средняя вибрация для важных действий (свайпы, переходы)
     */
    public void performMediumHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        } else {
            performCustomVibration(25); // 25ms для средней вибрации
        }
    }
    
    /**
     * Сильная вибрация для критических действий (ошибки, достижения)
     */
    public void performStrongHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT);
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        } else {
            performCustomVibration(50); // 50ms для сильной вибрации
        }
    }
    
    /**
     * Хаптическая обратная связь для свайпа лайка
     */
    public void performLikeSwipeHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        // Двойная короткая вибрация для положительного действия
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            long[] pattern = {0, 15, 50, 15}; // пауза, вибрация, пауза, вибрация
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(effect);
        } else {
            performMediumHaptic(view);
        }
    }
    
    /**
     * Хаптическая обратная связь для свайпа дизлайка
     */
    public void performDislikeSwipeHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        // Одна более длительная вибрация для отрицательного действия
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            VibrationEffect effect = VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        } else {
            performMediumHaptic(view);
        }
    }
    
    /**
     * Хаптическая обратная связь для достижений
     */
    public void performAchievementHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        // Паттерн вибрации для достижения: короткий-длинный-короткий
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            long[] pattern = {0, 20, 30, 60, 30, 20}; 
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(effect);
        } else {
            performStrongHaptic(view);
        }
    }
    
    /**
     * Хаптическая обратная связь для уровня up
     */
    public void performLevelUpHaptic(View view) {
        if (!isHapticEnabled()) return;
        
        // Возрастающий паттерн для повышения уровня
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            long[] pattern = {0, 15, 20, 25, 20, 35, 20, 45}; 
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(effect);
        } else {
            performStrongHaptic(view);
        }
    }
    
    /**
     * Выполняет кастомную вибрацию с заданной длительностью
     */
    private void performCustomVibration(long duration) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        } else {
            // Устаревший API для старых версий Android
            vibrator.vibrate(duration);
        }
    }
}
