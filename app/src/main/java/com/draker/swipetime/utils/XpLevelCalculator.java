package com.draker.swipetime.utils;

/**
 * Утилитный класс для расчета уровней и опыта
 */
public class XpLevelCalculator {

    // Максимальный доступный уровень
    public static final int MAX_LEVEL = 100;

    /**
     * Рассчитывает необходимый опыт для достижения определенного уровня
     * Формула: 100 * уровень^2
     * 
     * @param level уровень для расчета
     * @return количество опыта, необходимое для достижения этого уровня
     */
    public static int calculateRequiredXp(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level > MAX_LEVEL) {
            level = MAX_LEVEL;
        }
        return 100 * (level * level);
    }

    /**
     * Рассчитывает уровень на основе имеющегося опыта
     * 
     * @param experience количество опыта
     * @return текущий уровень пользователя
     */
    public static int calculateLevel(int experience) {
        if (experience <= 0) {
            return 1;
        }
        
        // Квадратный корень из (опыт / 100)
        int level = (int) Math.sqrt(experience / 100.0);
        
        // Ограничиваем максимальным уровнем
        return Math.min(level, MAX_LEVEL);
    }

    /**
     * Рассчитывает прогресс к следующему уровню (от 0 до 100%)
     * 
     * @param experience текущий опыт
     * @param level текущий уровень
     * @return процент прогресса к следующему уровню (0-100)
     */
    public static int calculateLevelProgress(int experience, int level) {
        if (level >= MAX_LEVEL) {
            return 100;
        }
        
        int currentLevelXp = calculateRequiredXp(level);
        int nextLevelXp = calculateRequiredXp(level + 1);
        int requiredXpForNextLevel = nextLevelXp - currentLevelXp;
        int currentXpProgress = experience - currentLevelXp;
        
        return (int) (((float) currentXpProgress / requiredXpForNextLevel) * 100);
    }

    /**
     * Рассчитывает опыт для действия в зависимости от его сложности
     * 
     * @param action тип действия ("swipe", "rate", "review")
     * @return количество опыта за действие
     */
    public static int getXpForAction(String action) {
        switch (action) {
            case "swipe": 
                return 5;  // простое действие свайпа
            case "rate":
                return 15; // оценка контента
            case "review":
                return 30; // написание рецензии (более сложное действие)
            case "achievement":
                return 50; // получение достижения
            default:
                return 1;  // неизвестное действие
        }
    }

    /**
     * Возвращает описание текущего ранга пользователя на основе его уровня
     * 
     * @param level текущий уровень пользователя
     * @return текстовое описание ранга
     */
    public static String getLevelRank(int level) {
        if (level < 5) {
            return "Новичок";
        } else if (level < 10) {
            return "Энтузиаст";
        } else if (level < 20) {
            return "Ценитель";
        } else if (level < 30) {
            return "Эксперт";
        } else if (level < 50) {
            return "Критик";
        } else if (level < 75) {
            return "Мастер";
        } else {
            return "Легенда";
        }
    }
}
