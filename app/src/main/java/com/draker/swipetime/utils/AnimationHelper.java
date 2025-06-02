package com.draker.swipetime.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.draker.swipetime.R;

/**
 * Помощник для создания плавных анимаций в приложении
 */
public class AnimationHelper {
    
    // Стандартные длительности анимаций
    public static final int DURATION_SHORT = 200;
    public static final int DURATION_MEDIUM = 300;
    public static final int DURATION_LONG = 500;
    
    /**
     * Анимация появления элемента с масштабированием
     */
    public static void animateScaleIn(View view) {
        animateScaleIn(view, DURATION_MEDIUM, null);
    }
    
    public static void animateScaleIn(View view, int duration, Animator.AnimatorListener listener) {
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
    public static void animateScaleOut(View view) {
        animateScaleOut(view, DURATION_MEDIUM, null);
    }
    
    public static void animateScaleOut(View view, int duration, Animator.AnimatorListener listener) {
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
    public static void animateSlideInFromBottom(View view) {
        animateSlideInFromBottom(view, DURATION_MEDIUM, null);
    }
    
    public static void animateSlideInFromBottom(View view, int duration, Animator.AnimatorListener listener) {
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
    public static void animateSlideOutToBottom(View view) {
        animateSlideOutToBottom(view, DURATION_MEDIUM, null);
    }
    
    public static void animateSlideOutToBottom(View view, int duration, Animator.AnimatorListener listener) {
        ObjectAnimator slideDown = ObjectAnimator.ofFloat(view, "translationY", 0f, view.getHeight());
        slideDown.setDuration(duration);
        slideDown.setInterpolator(new AccelerateDecelerateInterpolator());
        
        slideDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                view.setTranslationY(0f); // Сброс позиции
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
    public static void animateFadeIn(View view) {
        animateFadeIn(view, DURATION_MEDIUM, null);
    }
    
    public static void animateFadeIn(View view, int duration, Animator.AnimatorListener listener) {
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
    public static void animateFadeOut(View view) {
        animateFadeOut(view, DURATION_MEDIUM, null);
    }
    
    public static void animateFadeOut(View view, int duration, Animator.AnimatorListener listener) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(duration);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                view.setAlpha(1f); // Сброс прозрачности
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
    public static void animatePulse(View view) {
        animatePulse(view, 2, null);
    }
    
    public static void animatePulse(View view, int repeatCount, Animator.AnimatorListener listener) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        
        // Устанавливаем повторения для каждого аниматора отдельно
        scaleX.setRepeatCount(repeatCount);
        scaleY.setRepeatCount(repeatCount);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(600);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        if (listener != null) {
            animatorSet.addListener(listener);
        }
        
        animatorSet.start();
    }
    
    /**
     * Анимация тряски для ошибок
     */
    public static void animateShake(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(600);
        shake.setInterpolator(new DecelerateInterpolator());
        shake.start();
    }
    
    /**
     * Анимация подпрыгивания для положительных действий
     */
    public static void animateBounce(View view) {
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(view, "translationY", 0f, -30f, 0f, -15f, 0f);
        bounceY.setDuration(600);
        bounceY.setInterpolator(new OvershootInterpolator(2f));
        bounceY.start();
    }
    
    /**
     * Анимированный переход между фрагментами
     */
    public static void animateFragmentTransition(View exitingView, View enteringView) {
        if (exitingView != null) {
            animateSlideOutToBottom(exitingView, DURATION_MEDIUM, null);
        }
        
        if (enteringView != null) {
            enteringView.postDelayed(() -> {
                animateSlideInFromBottom(enteringView);
            }, DURATION_MEDIUM / 2);
        }
    }
    
    /**
     * Анимация для свайпа лайка (зеленый эффект)
     */
    public static void animateLikeSwipe(View view) {
        // Создаем анимацию масштабирования и поворота
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 5f, 0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }
    
    /**
     * Анимация для свайпа дизлайка (красный эффект)
     */
    public static void animateDislikeSwipe(View view) {
        // Создаем анимацию тряски и изменения масштаба
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, -10f, 10f, -10f, 10f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(shake, scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }
    
    /**
     * Анимация загрузки (rotation)
     */
    public static ObjectAnimator createLoadingAnimation(View view) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        rotation.setDuration(1000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        return rotation;
    }
}
