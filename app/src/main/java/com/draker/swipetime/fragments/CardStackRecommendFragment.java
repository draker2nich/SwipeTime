package com.draker.swipetime.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.draker.swipetime.R;
import com.draker.swipetime.adapters.CardStackAdapter;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.recommendations.RecommendationService;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;
import com.draker.swipetime.utils.ActionLogger;
import com.draker.swipetime.utils.CardFilterIntegratorV2;
import com.draker.swipetime.utils.CardInfoHelper;
import com.draker.swipetime.utils.ContentFilterHelper;
import com.draker.swipetime.utils.FirestoreDataManager;
// import com.draker.swipetime.utils.GamificationIntegrator; // Класс удален в рамках рефакторинга
import com.draker.swipetime.utils.LikedItemsHelper;
import com.draker.swipetime.viewmodels.FilterViewModel;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент для отображения карточек контента с интегрированной системой рекомендаций
 */
public class CardStackRecommendFragment extends Fragment implements CardStackListener, FilterSettingsFragment.OnFilterSettingsClosedListener {
    private static final String TAG = "CardStackRecommendFrag";
    private static final int DEFAULT_RECOMMENDATIONS_COUNT = 30;

    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private ConstraintLayout emptyCardsContainer;
    private Button reloadButton;
    private ImageButton filterButton;
    private TextView categoryTitleView;
    private TextView recommendationStatusText;
    private String categoryName;

    // Репозитории для работы с базой данных
    private MovieRepository movieRepository;
    private TVShowRepository tvShowRepository;
    private GameRepository gameRepository;
    private BookRepository bookRepository;
    private AnimeRepository animeRepository;
    private ContentRepository contentRepository;
    private UserPreferencesRepository preferencesRepository;
    
    // Сервис рекомендаций
    private RecommendationService recommendationService;
    
    // ViewModel для фильтров
    private FilterViewModel filterViewModel;

    // Флаг для отслеживания применения фильтров
    private boolean filtersApplied = false;

    private static final String ARG_CATEGORY = "category";

    public static CardStackRecommendFragment newInstance(String category) {
        CardStackRecommendFragment fragment = new CardStackRecommendFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Получить ID текущего пользователя
     */
    private String getCurrentUserId() {
        String userId = "user_1"; // Заглушка вместо удаленного GamificationIntegrator.getCurrentUserId(requireContext());
        Log.d(TAG, "Используется ID пользователя: " + userId);
        return userId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY);
        }

        // Инициализация репозиториев
        movieRepository = new MovieRepository(requireActivity().getApplication());
        tvShowRepository = new TVShowRepository(requireActivity().getApplication());
        gameRepository = new GameRepository(requireActivity().getApplication());
        bookRepository = new BookRepository(requireActivity().getApplication());
        animeRepository = new AnimeRepository(requireActivity().getApplication());
        contentRepository = new ContentRepository(requireActivity().getApplication());
        preferencesRepository = new UserPreferencesRepository(requireActivity().getApplication());
        
        // Инициализация сервиса рекомендаций
        recommendationService = RecommendationService.getInstance(requireActivity().getApplication());
        
        // Инициализация ViewModel
        filterViewModel = new ViewModelProvider(this).get(FilterViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_stack, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация компонентов
        cardStackView = view.findViewById(R.id.card_stack_view);
        emptyCardsContainer = view.findViewById(R.id.empty_cards_container);
        reloadButton = view.findViewById(R.id.reload_button);
        filterButton = view.findViewById(R.id.btn_filter);
        categoryTitleView = view.findViewById(R.id.category_title);
        recommendationStatusText = view.findViewById(R.id.filters_applied_indicator);
        
        // Устанавливаем название категории
        categoryTitleView.setText(categoryName);
        
        // Делаем индикатор рекомендаций более заметным
        recommendationStatusText = view.findViewById(R.id.filters_applied_indicator);
        if (recommendationStatusText != null) {
            recommendationStatusText.setText("⭐ ИИ-РЕКОМЕНДАЦИИ АКТИВНЫ ⭐");
            recommendationStatusText.setVisibility(View.VISIBLE);
            recommendationStatusText.setTextSize(14);
            recommendationStatusText.setTextColor(Color.GREEN);
            recommendationStatusText.setPadding(10, 10, 10, 10);
            // Если доступен, установим фон
            try {
                recommendationStatusText.setBackgroundColor(Color.parseColor("#33000000"));
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при установке цвета фона: " + e.getMessage());
            }
        } else {
            // Если не можем найти TextView, создадим новый
            Log.w(TAG, "Не найден TextView с id filters_applied_indicator, создаем новый");
            TextView newStatusText = new TextView(getContext());
            newStatusText.setText("⭐ ИИ-РЕКОМЕНДАЦИИ АКТИВНЫ ⭐");
            newStatusText.setTextColor(Color.GREEN);
            newStatusText.setTextSize(14);
            newStatusText.setPadding(10, 10, 10, 10);
            newStatusText.setGravity(Gravity.CENTER);
            
            try {
                newStatusText.setBackgroundColor(Color.parseColor("#33000000"));
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при установке цвета фона: " + e.getMessage());
            }
            
            // Найдем родительский контейнер и добавим TextView
            ViewGroup parent = (ViewGroup) categoryTitleView.getParent();
            if (parent != null) {
                parent.addView(newStatusText);
                recommendationStatusText = newStatusText;
            }
        }
        
        // Установка слушателя для кнопки фильтров
        filterButton.setOnClickListener(v -> openFilterSettings());

        // Настройка CardStackLayoutManager
        setupCardStackView();

        // Получение рекомендованных элементов
        String userId = getCurrentUserId();
        List<ContentItem> recommendedItems = CardFilterIntegratorV2.getRecommendedContentItems(
                requireActivity().getApplication(),
                categoryName,
                userId,
                DEFAULT_RECOMMENDATIONS_COUNT
        );
        
        Log.d(TAG, "Получено рекомендаций: " + recommendedItems.size());

        // Проверка наличия рекомендаций
        if (recommendedItems.isEmpty()) {
            // Если рекомендаций нет, используем обычную фильтрацию
            recommendedItems = CardFilterIntegratorV2.getFilteredAndRecommendedContentItems(
                    requireActivity().getApplication(),
                    categoryName,
                    userId,
                    movieRepository,
                    tvShowRepository,
                    gameRepository,
                    bookRepository,
                    animeRepository,
                    contentRepository,
                    preferencesRepository
            );
            
            Log.d(TAG, "Использованы альтернативные рекомендации: " + recommendedItems.size());
        }
        
        // Если все равно нет элементов, показываем сообщение об отсутствии карточек
        if (recommendedItems.isEmpty()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
        } else {
            cardStackView.setVisibility(View.VISIBLE);
            emptyCardsContainer.setVisibility(View.GONE);
            
            // Инициализация адаптера с рекомендованными элементами
            adapter = new CardStackAdapter(getContext(), recommendedItems);
            cardStackView.setAdapter(adapter);
        }

        // Настройка кнопки перезагрузки
        reloadButton.setOnClickListener(v -> reloadCards());
        
        // Проверяем статус применения фильтров
        checkFiltersStatus();
    }
    
    /**
     * Проверяет, были ли применены какие-либо фильтры
     */
    private void checkFiltersStatus() {
        String userId = getCurrentUserId();
        filtersApplied = ContentFilterHelper.hasActiveFilters(preferencesRepository.getByUserId(userId));
        updateFiltersIndicator();
    }

    /**
     * Обновляет индикатор применения фильтров и рекомендаций
     */
    private void updateFiltersIndicator() {
        if (filtersApplied) {
            recommendationStatusText.setText("⭐ ИИ-РЕКОМЕНДАЦИИ + ФИЛЬТРЫ ⭐");
            recommendationStatusText.setTextColor(Color.YELLOW);
        } else {
            recommendationStatusText.setText("⭐ ИИ-РЕКОМЕНДАЦИИ АКТИВНЫ ⭐");
            recommendationStatusText.setTextColor(Color.GREEN);
        }
        recommendationStatusText.setVisibility(View.VISIBLE);
        
        // Выводим информацию о работе рекомендательной системы в лог
        Log.d(TAG, "Рекомендательная система активна. Фильтры " + 
                (filtersApplied ? "применены" : "не применены"));
        
        // Показываем Toast сообщение для наглядности
        Toast.makeText(getContext(), 
                "ИИ-рекомендации активны" + (filtersApplied ? " + фильтры" : ""), 
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Открывает экран настроек фильтров
     */
    private void openFilterSettings() {
        FilterSettingsFragment filterFragment = FilterSettingsFragment.newInstance();
        filterFragment.setOnFilterSettingsClosedListener(this);

        // Заменяем текущий фрагмент на фрагмент настроек фильтров
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, filterFragment)
                .addToBackStack("filter_settings")
                .commit();
    }

    @Override
    public void onFilterSettingsClosed(boolean filtersApplied) {
        this.filtersApplied = filtersApplied;
        updateFiltersIndicator();

        if (filtersApplied) {
            // Перезагружаем карточки с учетом новых фильтров
            reloadCardsWithFilters();
        }
    }

    /**
     * Перезагружает карточки с применением фильтров и рекомендаций
     */
    private void reloadCardsWithFilters() {
        String userId = getCurrentUserId();
        
        // Получаем отфильтрованные и рекомендованные элементы
        List<ContentItem> filteredItems = CardFilterIntegratorV2.getFilteredAndRecommendedContentItems(
                requireActivity().getApplication(),
                categoryName,
                userId,
                movieRepository,
                tvShowRepository,
                gameRepository,
                bookRepository,
                animeRepository,
                contentRepository,
                preferencesRepository
        );
        
        // Обновляем адаптер с отфильтрованным списком
        adapter.setItems(filteredItems);
        
        // Показываем сообщение об отсутствии карточек, если после фильтрации ничего не осталось
        if (filteredItems.isEmpty()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "По вашим фильтрам ничего не найдено", Toast.LENGTH_SHORT).show();
        } else {
            cardStackView.setVisibility(View.VISIBLE);
            emptyCardsContainer.setVisibility(View.GONE);
        }
    }

    private void setupCardStackView() {
        manager = new CardStackLayoutManager(getContext(), this);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(false);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        cardStackView.setLayoutManager(manager);
    }

    /**
     * Обновляет информацию о текущей карточке на экране
     * @param item текущий элемент
     */
    private void updateCurrentCardInfo(ContentItem item) {
        // Используем CardInfoHelper для получения и отображения информации о карточке
        CardInfoHelper.logDetailedInfo(item, contentRepository);
    }

    private void reloadCards() {
        // Обновляем предпочтения пользователя на основе его лайков
        recommendationService.updateCurrentUserPreferences(requireContext());
        
        // Запустим тестирование рекомендаций для демонстрации работы системы
        new Thread(() -> {
            double quality = recommendationService.testRecommendationQuality();
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(),
                        "Качество рекомендаций: " + String.format("%.2f", quality * 100) + "%",
                        Toast.LENGTH_LONG).show();
            });
        }).start();
        
        String userId = getCurrentUserId();
        List<ContentItem> recommendedItems;
        
        // Если установлены фильтры, применяем их вместе с рекомендациями
        if (filtersApplied) {
            recommendedItems = CardFilterIntegratorV2.getFilteredAndRecommendedContentItems(
                    requireActivity().getApplication(),
                    categoryName,
                    userId,
                    movieRepository,
                    tvShowRepository,
                    gameRepository,
                    bookRepository,
                    animeRepository,
                    contentRepository,
                    preferencesRepository
            );
        } else {
            // Иначе используем только рекомендации
            recommendedItems = CardFilterIntegratorV2.getRecommendedContentItems(
                    requireActivity().getApplication(),
                    categoryName,
                    userId,
                    DEFAULT_RECOMMENDATIONS_COUNT
            );
            
            // Если рекомендаций нет, используем обычную фильтрацию
            if (recommendedItems.isEmpty()) {
                recommendedItems = CardFilterIntegratorV2.getFilteredAndRecommendedContentItems(
                        requireActivity().getApplication(),
                        categoryName,
                        userId,
                        movieRepository,
                        tvShowRepository,
                        gameRepository,
                        bookRepository,
                        animeRepository,
                        contentRepository,
                        preferencesRepository
                );
            }
        }
        
        // Выводим информацию о полученных рекомендациях
        Log.d(TAG, "\n=== СИСТЕМА РЕКОМЕНДАЦИЙ ===\n" +
                "Получено " + recommendedItems.size() + " рекомендаций\n" +
                "Применены фильтры: " + (filtersApplied ? "ДА" : "НЕТ") + "\n" +
                "Категория: " + categoryName + "\n" +
                "Пользователь: " + userId + "\n" +
                "===========================");
        
        // Обновляем адаптер
        adapter.setItems(recommendedItems);
        
        // Обновляем видимость элементов
        if (recommendedItems.isEmpty()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Нет рекомендаций для отображения", Toast.LENGTH_SHORT).show();
        } else {
            cardStackView.setVisibility(View.VISIBLE);
            emptyCardsContainer.setVisibility(View.GONE);
            Toast.makeText(getContext(), 
                    "Загружено " + recommendedItems.size() + " рекомендаций", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Кнопка для свайпа влево программно
    public void swipeLeft() {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(new AccelerateInterpolator())
                .build();
        manager.setSwipeAnimationSetting(setting);
        cardStackView.swipe();
    }

    // Кнопка для свайпа вправо программно
    public void swipeRight() {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(new AccelerateInterpolator())
                .build();
        manager.setSwipeAnimationSetting(setting);
        cardStackView.swipe();
    }

    // Текущее видимое представление карточки
    private View topView = null;

    // Реализация методов CardStackListener
    @Override
    public void onCardDragging(Direction direction, float ratio) {
        // Отображаем индикаторы свайпа при перетаскивании карточки
        if (topView != null) {
            if (direction == Direction.Left) {
                topView.findViewById(R.id.left_indicator).setAlpha(ratio);
                topView.findViewById(R.id.right_indicator).setAlpha(0f);
            } else if (direction == Direction.Right) {
                topView.findViewById(R.id.right_indicator).setAlpha(ratio);
                topView.findViewById(R.id.left_indicator).setAlpha(0f);
            }
        }
    }

    @Override
    public void onCardSwiped(Direction direction) {
        // Обработка свайпа карточки
        int position = manager.getTopPosition() - 1;
        if (position >= 0 && position < adapter.getItems().size()) {
            ContentItem item = adapter.getItems().get(position);
            Log.d(TAG, "Выполнен свайп карточки: " + item.getTitle() + " (ID: " + item.getId() + ", категория: " + categoryName + ")");

            String userId = getCurrentUserId();
            boolean isRightSwipe = direction == Direction.Right;
            
            // Обрабатываем свайп в рекомендательной системе
            CardFilterIntegratorV2.handleSwipeEvent(
                    requireActivity().getApplication(),
                    item.getId(),
                    isRightSwipe
            );
            
            // Выводим информативный лог о действии рекомендательной системы
            Log.d(TAG, "\n=== СИСТЕМА РЕКОМЕНДАЦИЙ ===\n" +
                    "Обработан свайп " + (isRightSwipe ? "ВПРАВО (ЛАЙК)" : "ВЛЕВО (ДИЗЛАЙК)") + "\n" +
                    "ID контента: " + item.getId() + "\n" +
                    "Название: " + item.getTitle() + "\n" +
                    "Категория: " + categoryName + "\n" +
                    "Данные будут использованы для уточнения рекомендаций\n" +
                    "===========================");
            
            if (isRightSwipe) {
                // Пользователю понравился элемент
                item.setLiked(true);
                Toast.makeText(getContext(), "Добавлено в избранное: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                // Логируем действие
                ActionLogger.logSwipe(true, item.getId(), item.getTitle());

                // Используем вспомогательный класс для добавления элемента в избранное
                LikedItemsHelper.addToLiked(item, categoryName,
                        movieRepository, tvShowRepository,
                        gameRepository, bookRepository,
                        animeRepository, contentRepository);

                // Начисляем опыт за свайп вправо
                // Начисляем опыт за свайп вправо (временно отключено)
                // boolean levelUp = GamificationIntegrator.registerSwipe(getContext(), true);
                // if (levelUp) {
                //     Toast.makeText(getContext(), "Уровень повышен!", Toast.LENGTH_SHORT).show();
                // }
                
                // Синхронизация с Firebase после добавления в избранное
                syncWithFirebase(userId, item);
            } else {
                // Пользователю не понравился элемент
                Toast.makeText(getContext(), "Пропущено: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                // Логируем действие
                ActionLogger.logSwipe(false, item.getId(), item.getTitle());

                // Начисляем опыт за свайп влево (временно отключено)
                // boolean levelUp = GamificationIntegrator.registerSwipe(getContext(), false);
                boolean levelUp = false; // Заглушка
                if (levelUp) {
                    Toast.makeText(getContext(), "Уровень повышен!", Toast.LENGTH_SHORT).show();
                    
                    // Синхронизация информации о пользователе при повышении уровня
                    syncUserProfileWithFirebase(userId);
                }
            }
        } else {
            Log.e(TAG, "Ошибка позиции при свайпе: position=" + position + ", размер адаптера=" + adapter.getItemCount());
        }

        // Если карточки закончились, показываем сообщение
        if (manager.getTopPosition() == adapter.getItemCount()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Синхронизирует данные о лайкнутом контенте с Firebase
     * @param userId ID пользователя
     * @param item элемент контента
     */
    private void syncWithFirebase(String userId, ContentItem item) {
        if (userId == null || userId.isEmpty() || userId.equals("user_1")) {
            Log.w(TAG, "Синхронизация с Firebase невозможна: пользователь не авторизован");
            return;
        }
        
        // Синхронизация с Firebase (код из оригинального CardStackFragment)
        FirestoreDataManager firestoreManager = FirestoreDataManager.getInstance(requireContext());
        firestoreManager.syncUserData(userId, new FirestoreDataManager.SyncCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Синхронизация с Firebase успешно выполнена");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Ошибка синхронизации с Firebase: " + errorMessage);
            }
        });
    }
    
    /**
     * Синхронизирует профиль пользователя с Firebase
     * @param userId ID пользователя
     */
    private void syncUserProfileWithFirebase(String userId) {
        if (userId == null || userId.isEmpty() || userId.equals("user_1")) {
            Log.w(TAG, "Синхронизация профиля с Firebase невозможна: пользователь не авторизован");
            return;
        }
        
        FirestoreDataManager firestoreManager = FirestoreDataManager.getInstance(requireContext());
        firestoreManager.syncUserData(userId, new FirestoreDataManager.SyncCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Синхронизация профиля с Firebase успешно выполнена");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Ошибка синхронизации профиля с Firebase: " + errorMessage);
            }
        });
    }

    @Override
    public void onCardRewound() {
        // Действие при возврате карточки (если будет реализовано)
    }

    @Override
    public void onCardCanceled() {
        // Действие при отмене свайпа
        if (topView != null) {
            topView.findViewById(R.id.left_indicator).setAlpha(0f);
            topView.findViewById(R.id.right_indicator).setAlpha(0f);
        }
    }

    @Override
    public void onCardAppeared(View view, int position) {
        // Сохраняем ссылку на текущую верхнюю карточку
        topView = view;
        
        // Если позиция в пределах списка, отображаем информацию о карточке
        if (position >= 0 && position < adapter.getItemCount()) {
            ContentItem currentItem = adapter.getItems().get(position);
            Log.d(TAG, "Отображается карточка: " + currentItem.getTitle() + " (#" + position + ")");
            
            // Обновляем UI с информацией о текущей карточке
            updateCurrentCardInfo(currentItem);
        }
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        // Действие при исчезновении карточки
    }
}
