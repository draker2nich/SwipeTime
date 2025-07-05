package com.draker.swipetime.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.draker.swipetime.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Объединенный класс для работы с UI, включая:
 * - Анимации
 * - Хаптическую обратную связь
 * - Работу с изображениями
 * - Управление темами
 */
public class UIHelper {
    private static final String TAG = "UIHelper";

    // Singleton instance
    private static UIHelper instance;

    // Animation constants
    public static final int DURATION_SHORT = 200;
    public static final int DURATION_MEDIUM = 300;
    public static final int DURATION_LONG = 500;

    // Theme constants
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_AUTO = 2;

    // Preferences keys
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String PREF_HIGH_CONTRAST = "high_contrast_mode";
    private static final String PREF_LARGE_TEXT = "large_text_mode";
    private static final String PREF_REDUCE_MOTION = "reduce_motion_mode";
    private static final String PREF_HAPTIC_FEEDBACK = "haptic_feedback_enabled";
    private static final String HAPTIC_PREFS_NAME = "haptic_prefs";
    private static final String KEY_HAPTIC_ENABLED = "haptic_enabled";

    // Image validation
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final String[] IMAGE_EXTENSIONS = {
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    };

    // Context and preferences
    private final Context context;
    private final SharedPreferences preferences;
    private final SharedPreferences hapticPrefs;
    private final Vibrator vibrator;
    private boolean isHapticEnabled;

    private UIHelper(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences("swipetime_prefs", Context.MODE_PRIVATE);
        this.hapticPrefs = this.context.getSharedPreferences(HAPTIC_PREFS_NAME, Context.MODE_PRIVATE);
        this.vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        this.isHapticEnabled = hapticPrefs.getBoolean(KEY_HAPTIC_ENABLED, true);
    }

    public static synchronized UIHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UIHelper(context);
        }
        return instance;
    }

    // ==================== ANIMATION SECTION ====================

    /**
     * Анимация появления элемента с масштабированием
     */
    public void animateScaleIn(View view) {
        animateScaleIn(view, getAnimationDuration(DURATION_MEDIUM), null);
    }

    public void animateScaleIn(View view, int duration, Animator.AnimatorListener listener) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));

        if (listener != null) {
            animatorSet.addListener(listener);
        }

        animatorSet.start();
    }

    /**
     * Анимация исчезновения элемента с масштабированием
     */
    public void animateScaleOut(View view) {
        animateScaleOut(view, getAnimationDuration(DURATION_MEDIUM), null);
    }

    public void animateScaleOut(View view, int duration, Animator.AnimatorListener listener) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });

        animatorSet.start();
    }

    /**
     * Анимация появления элемента снизу
     */
    public void animateSlideInFromBottom(View view) {
        animateSlideInFromBottom(view, getAnimationDuration(DURATION_MEDIUM), null);
    }

    public void animateSlideInFromBottom(View view, int duration, Animator.AnimatorListener listener) {
        float originalY = view.getTranslationY();
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), originalY);
        slideUp.setDuration(duration);
        slideUp.setInterpolator(new DecelerateInterpolator());

        if (listener != null) {
            slideUp.addListener(listener);
        }

        slideUp.start();
    }

    /**
     * Анимация исчезновения элемента вниз
     */
    public void animateSlideOutToBottom(View view) {
        animateSlideOutToBottom(view, getAnimationDuration(DURATION_MEDIUM), null);
    }

    public void animateSlideOutToBottom(View view, int duration, Animator.AnimatorListener listener) {
        ObjectAnimator slideDown = ObjectAnimator.ofFloat(view, "translationY", 0f, view.getHeight());
        slideDown.setDuration(duration);
        slideDown.setInterpolator(new AccelerateDecelerateInterpolator());

        slideDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                view.setTranslationY(0f);
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });

        slideDown.start();
    }

    /**
     * Анимация постепенного появления (fade in)
     */
    public void animateFadeIn(View view) {
        animateFadeIn(view, getAnimationDuration(DURATION_MEDIUM), null);
    }

    public void animateFadeIn(View view, int duration, Animator.AnimatorListener listener) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeIn.setDuration(duration);
        fadeIn.setInterpolator(new DecelerateInterpolator());

        if (listener != null) {
            fadeIn.addListener(listener);
        }

        fadeIn.start();
    }

    /**
     * Анимация постепенного исчезновения (fade out)
     */
    public void animateFadeOut(View view) {
        animateFadeOut(view, getAnimationDuration(DURATION_MEDIUM), null);
    }

    public void animateFadeOut(View view, int duration, Animator.AnimatorListener listener) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(duration);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                view.setAlpha(1f);
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });

        fadeOut.start();
    }

    /**
     * Анимация пульсации для привлечения внимания
     */
    public void animatePulse(View view) {
        animatePulse(view, 2, null);
    }

    public void animatePulse(View view, int repeatCount, Animator.AnimatorListener listener) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        scaleX.setRepeatCount(repeatCount);
        scaleY.setRepeatCount(repeatCount);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(getAnimationDuration(600));
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        if (listener != null) {
            animatorSet.addListener(listener);
        }

        animatorSet.start();
    }

    /**
     * Анимация тряски для ошибок
     */
    public void animateShake(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(getAnimationDuration(600));
        shake.setInterpolator(new DecelerateInterpolator());
        shake.start();
    }

    /**
     * Анимация подпрыгивания для положительных действий
     */
    public void animateBounce(View view) {
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(view, "translationY", 0f, -30f, 0f, -15f, 0f);
        bounceY.setDuration(getAnimationDuration(600));
        bounceY.setInterpolator(new OvershootInterpolator(2f));
        bounceY.start();
    }

    /**
     * Анимация для свайпа лайка (зеленый эффект)
     */
    public void animateLikeSwipe(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 5f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.setDuration(getAnimationDuration(300));
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }

    /**
     * Анимация для свайпа дизлайка (красный эффект)
     */
    public void animateDislikeSwipe(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, -10f, 10f, -10f, 10f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(shake, scaleX, scaleY);
        animatorSet.setDuration(getAnimationDuration(300));
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    /**
     * Анимация загрузки (rotation)
     */
    public ObjectAnimator createLoadingAnimation(View view) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        rotation.setDuration(1000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        return rotation;
    }

    // ==================== HAPTIC FEEDBACK SECTION ====================

    /**
     * Включает/выключает хаптическую обратную связь
     */
    public void setHapticEnabled(boolean enabled) {
        isHapticEnabled = enabled;
        hapticPrefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply();
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
            performCustomVibration(10);
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
            performCustomVibration(25);
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
            performCustomVibration(50);
        }
    }

    /**
     * Хаптическая обратная связь для свайпа лайка
     */
    public void performLikeSwipeHaptic(View view) {
        if (!isHapticEnabled()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            long[] pattern = {0, 15, 50, 15};
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            long[] pattern = {0, 15, 20, 25, 20, 35, 20, 45};
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(effect);
        } else {
            performStrongHaptic(view);
        }
    }

    // ==================== IMAGE UTILITIES SECTION ====================

    /**
     * Проверить, является ли строка корректным URL изображения
     */
    public boolean isValidImageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        if (!URL_PATTERN.matcher(url).matches()) {
            return false;
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        boolean hasImageExtension = false;
        String lowerCaseUrl = url.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lowerCaseUrl.contains(ext)) {
                hasImageExtension = true;
                break;
            }
        }

        return hasImageExtension || isApiImageUrl(url);
    }

    /**
     * Проверить, является ли URL адресом изображения из известного API
     */
    private boolean isApiImageUrl(String url) {
        return url.contains("image.tmdb.org") ||
                url.contains("media.rawg.io") ||
                url.contains("books.google.com") ||
                url.contains("cdn.animenewsnetwork.com") ||
                url.contains("cdn.myanimelist.net") ||
                url.contains("media-amazon.com") ||
                url.contains("images-na.ssl-images-amazon.com") ||
                url.contains("i.pravatar.cc") ||
                url.contains("api.jikan.moe") ||
                url.contains("cloudflare.steamstatic.com") ||
                url.contains("steamuserimages") ||
                url.contains("assets.nintendo.com");
    }

    /**
     * Определить, является ли изображение горизонтальным (для игр)
     */
    public boolean isLikelyHorizontalImage(String url) {
        if (url == null) return false;

        if (url.contains("media.rawg.io")) {
            return url.contains("screenshots") ||
                    !url.contains("crop/600/400") && !url.contains("crop/400/600");
        }

        return false;
    }

    /**
     * Получить URL изображения заглушки, если оригинальный URL недействителен
     */
    public String getFallbackImageUrl(String originalUrl, String category) {
        if (isValidImageUrl(originalUrl)) {
            return originalUrl;
        }

        switch (category.toLowerCase()) {
            case "фильмы":
            case "movie":
                return "https://m.media-amazon.com/images/M/MV5BMzUzNDM2NjQ5M15BMl5BanBnXkFtZTgwNTM3NTg4OTE@._V1_UX182_CR0,0,182,268_AL_.jpg";
            case "сериалы":
            case "tv_show":
                return "https://m.media-amazon.com/images/M/MV5BMjA5MTE1MjQyNV5BMl5BanBnXkFtZTgwMzI5Njc0ODE@._V1_UX182_CR0,0,182,268_AL_.jpg";
            case "игры":
            case "game":
                return "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/capsule_616x353.jpg";
            case "книги":
            case "book":
                return "https://m.media-amazon.com/images/I/51bVNTqHFlL._SX323_BO1,204,203,200_.jpg";
            case "аниме":
            case "anime":
                return "https://cdn.myanimelist.net/images/anime/1171/109222.jpg";
            default:
                return "https://i.pravatar.cc/300?img=15";
        }
    }

    /**
     * Загрузить изображение в ImageView с учетом особенностей категории (карточки)
     */
    public void loadCardImage(String imageUrl, ImageView imageView, String category) {
        String finalImageUrl = getFallbackImageUrl(imageUrl, category);

        try {
            if ((category.equalsIgnoreCase("игры") || category.equalsIgnoreCase("game"))
                    && isLikelyHorizontalImage(finalImageUrl)) {
                Glide.with(context)
                        .load(finalImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .transform(new CenterCrop())
                        .into(imageView);
            } else {
                Glide.with(context)
                        .load(finalImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(imageView);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.placeholder_image);
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
    }

    // ==================== THEME MANAGEMENT SECTION ====================

    /**
     * Применяет сохраненную тему
     */
    public void applyTheme() {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_AUTO:
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
        }
    }

    /**
     * Устанавливает режим темы
     */
    public void setThemeMode(int themeMode) {
        preferences.edit().putInt(PREF_THEME_MODE, themeMode).apply();
        applyTheme();
    }

    /**
     * Получает текущий режим темы
     */
    public int getThemeMode() {
        return preferences.getInt(PREF_THEME_MODE, THEME_AUTO);
    }

    /**
     * Получает название текущей темы
     */
    public String getThemeName() {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return "Светлая тема";
            case THEME_DARK:
                return "Темная тема";
            case THEME_AUTO:
            default:
                return "Автоматически";
        }
    }

    /**
     * Проверяет, включен ли режим высокого контраста
     */
    public boolean isHighContrastEnabled() {
        return preferences.getBoolean(PREF_HIGH_CONTRAST, false);
    }

    /**
     * Устанавливает режим высокого контраста
     */
    public void setHighContrastEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_HIGH_CONTRAST, enabled).apply();
    }

    /**
     * Проверяет, включен ли режим крупного текста
     */
    public boolean isLargeTextEnabled() {
        return preferences.getBoolean(PREF_LARGE_TEXT, false);
    }

    /**
     * Устанавливает режим крупного текста
     */
    public void setLargeTextEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_LARGE_TEXT, enabled).apply();
    }

    /**
     * Проверяет, включен ли режим упрощенных анимаций
     */
    public boolean isReduceMotionEnabled() {
        return preferences.getBoolean(PREF_REDUCE_MOTION, false);
    }

    /**
     * Устанавливает режим упрощенных анимаций
     */
    public void setReduceMotionEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_REDUCE_MOTION, enabled).apply();
    }

    /**
     * Проверяет, включена ли вибрация
     */
    public boolean isHapticFeedbackEnabled() {
        return preferences.getBoolean(PREF_HAPTIC_FEEDBACK, true);
    }

    /**
     * Устанавливает режим вибрации
     */
    public void setHapticFeedbackEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_HAPTIC_FEEDBACK, enabled).apply();
    }

    /**
     * Проверяет, используется ли темная тема в данный момент
     */
    public boolean isDarkTheme() {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Переключает между светлой и темной темой
     */
    public void toggleTheme() {
        int currentMode = getThemeMode();
        int newMode = (currentMode == THEME_LIGHT) ? THEME_DARK : THEME_LIGHT;
        setThemeMode(newMode);
    }

    /**
     * Проверяет системные настройки доступности
     */
    public boolean isSystemAccessibilityEnabled() {
        return android.provider.Settings.System.getInt(
                context.getContentResolver(),
                "accessibility_enabled", 0) == 1;
    }

    /**
     * Возвращает рекомендуемую длительность анимации на основе настроек
     */
    public int getAnimationDuration(int defaultDuration) {
        if (isReduceMotionEnabled()) {
            return 0; // Отключаем анимации
        }
        return defaultDuration;
    }

    /**
     * Получает короткую длительность анимации
     */
    public long getShortAnimTime() {
        return getAnimationDuration(200); // Короткая анимация 200мс
    }

    /**
     * Получает среднюю длительность анимации
     */
    public long getMediumAnimTime() {
        return getAnimationDuration(300); // Средняя анимация 300мс
    }

    /**
     * Получает длинную длительность анимации
     */
    public long getLongAnimTime() {
        return getAnimationDuration(500); // Длинная анимация 500мс
    }

    /**
     * Возвращает множитель размера текста
     */
    public float getTextSizeMultiplier() {
        if (isLargeTextEnabled()) {
            return 1.3f; // Увеличиваем текст на 30%
        }
        return 1.0f;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Выполняет кастомную вибрацию с заданной длительностью
     */
    private void performCustomVibration(long duration) {
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(duration);
        }
    }
}