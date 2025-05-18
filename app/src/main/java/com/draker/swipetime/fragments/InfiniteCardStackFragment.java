package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;
import com.draker.swipetime.utils.ActionLogger;
import com.draker.swipetime.utils.CardInfoHelper;
import com.draker.swipetime.utils.FirestoreDataManager;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.AnalyticsTracker;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.InfiniteContentManager;
import com.draker.swipetime.utils.LikedItemsHelper;
import com.draker.swipetime.viewmodels.FilterViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
 * Фрагмент для отображения бесконечной ленты карточек с контентом
 */
public class InfiniteCardStackFragment extends Fragment implements CardStackListener, FilterSettingsFragment.OnFilterSettingsClosedListener {

    private static final String TAG = "InfiniteCardStack";

    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private ConstraintLayout emptyCardsContainer;
    private Button reloadButton;
    private ImageButton filterButton;
    private TextView categoryTitleView;
    private TextView filtersAppliedIndicator;
    private String categoryName;
    
    // Для отслеживания загрузки данных
    private boolean isLoading = false;

    // Репозитории для работы с базой данных
    private MovieRepository movieRepository;
    private TVShowRepository tvShowRepository;
    private GameRepository gameRepository;
    private BookRepository bookRepository;
    private AnimeRepository animeRepository;
    private ContentRepository contentRepository;
    private UserPreferencesRepository preferencesRepository;
    private GamificationManager gamificationManager;

    // ViewModel для фильтров
    private FilterViewModel filterViewModel;

    // Менеджер бесконечного контента
    private InfiniteContentManager infiniteContentManager;

    private static final String ARG_CATEGORY = "category";

    // Флаг для отслеживания применения фильтров
    private boolean filtersApplied = false;
    
    // Минимальное количество карточек, при котором запрашиваем дополнительные
    private static final int CARDS_THRESHOLD = 5;
    
    // Количество карточек для предварительной загрузки
    private static final int PRELOAD_BATCH_SIZE = 10;

    public static InfiniteCardStackFragment newInstance(String category) {
        InfiniteCardStackFragment fragment = new InfiniteCardStackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Получить ID текущего пользователя
     */
    private String getCurrentUserId() {
        String userId = GamificationIntegrator.getCurrentUserId(requireContext());
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

        // Инициализация менеджера геймификации
        gamificationManager = GamificationManager.getInstance(requireActivity().getApplication());

        // Инициализация ViewModel
        filterViewModel = new ViewModelProvider(this).get(FilterViewModel.class);
        
        // Инициализация менеджера бесконечного контента
        infiniteContentManager = InfiniteContentManager.getInstance();
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
        filtersAppliedIndicator = view.findViewById(R.id.filters_applied_indicator);

        // Устанавливаем название категории
        categoryTitleView.setText(categoryName);

        // Установка слушателя для кнопки фильтров
        filterButton.setOnClickListener(v -> openFilterSettings());

        // Настройка CardStackLayoutManager
        setupCardStackView();

        // Инициализация адаптера с пустым списком
        adapter = new CardStackAdapter(getContext(), new ArrayList<>());
        cardStackView.setAdapter(adapter);

        // Настройка кнопки перезагрузки
        reloadButton.setOnClickListener(v -> reloadCards());

        // Загружаем первую партию карточек
        loadInitialBatch();

        // Проверяем статус применения фильтров
        checkFiltersStatus();
    }

    /**
     * Загружает первую партию карточек
     */
    private void loadInitialBatch() {
        showLoading(true);
        
        String userId = getCurrentUserId();
        infiniteContentManager.getNextBatch(categoryName, userId, PRELOAD_BATCH_SIZE, 
                requireContext(), items -> {
            if (getActivity() == null) return;
            
            getActivity().runOnUiThread(() -> {
                showLoading(false);
                
                if (items.isEmpty()) {
                    // Если карточек нет, показываем сообщение
                    showEmptyState(true);
                    // Отслеживаем пустое состояние в аналитике
                    AnalyticsTracker.trackEmptyState(requireContext(), categoryName);
                } else {
                    // Иначе отображаем карточки
                    adapter.setItems(items);
                    showEmptyState(false);
                    // Отслеживаем загрузку контента в аналитике
                    AnalyticsTracker.trackContentLoad(requireContext(), categoryName, items.size(), true);
                }
            });
        });
    }
    
    /**
     * Отображает или скрывает индикатор загрузки
     */
    private void showLoading(boolean isLoading) {
        this.isLoading = isLoading;
        
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            if (isLoading) {
                // Здесь можно добавить отображение прогресс-бара при загрузке
                Toast.makeText(getContext(), "Загружаем рекомендации...", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Отображает или скрывает сообщение об отсутствии карточек
     */
    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
        } else {
            cardStackView.setVisibility(View.VISIBLE);
            emptyCardsContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Проверяет, были ли применены какие-либо фильтры
     */
    private void checkFiltersStatus() {
        String userId = getCurrentUserId();
        UserPreferencesEntity preferences = preferencesRepository.getByUserId(userId);

        // Проверяем, есть ли какие-либо установленные фильтры
        boolean hasActiveFilters = false;

        if (preferences != null) {
            hasActiveFilters = (preferences.getPreferredGenres() != null && !preferences.getPreferredGenres().isEmpty()) ||
                    (preferences.getPreferredCountries() != null && !preferences.getPreferredCountries().isEmpty()) ||
                    (preferences.getPreferredLanguages() != null && !preferences.getPreferredLanguages().isEmpty()) ||
                    (preferences.getInterestsTags() != null && !preferences.getInterestsTags().isEmpty()) ||
                    preferences.getMinDuration() > 0 ||
                    preferences.getMaxDuration() < Integer.MAX_VALUE ||
                    preferences.getMinYear() > 1900 ||
                    preferences.getMaxYear() < 2025 ||
                    preferences.isAdultContentEnabled();
        }

        // Обновляем UI в соответствии с статусом фильтров
        filtersApplied = hasActiveFilters;
        updateFiltersIndicator();
    }

    /**
     * Обновляет индикатор применения фильтров
     */
    private void updateFiltersIndicator() {
        if (filtersApplied) {
            filtersAppliedIndicator.setVisibility(View.VISIBLE);
        } else {
            filtersAppliedIndicator.setVisibility(View.GONE);
        }
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
     * Перезагружает карточки с применением фильтров
     */
    private void reloadCardsWithFilters() {
        // Сбрасываем кэш в менеджере бесконечного контента
        infiniteContentManager.resetCache(categoryName);
        
        // Загружаем новую партию карточек
        loadInitialBatch();
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
        // Сбрасываем кэш в менеджере бесконечного контента
        infiniteContentManager.resetCache(categoryName);
        
        // Загружаем новую партию карточек
        loadInitialBatch();
    }
    
    /**
     * Загружает следующую партию карточек
     */
    private void loadNextBatch() {
        if (isLoading) return;
        
        showLoading(true);
        
        String userId = getCurrentUserId();
        infiniteContentManager.getNextBatch(categoryName, userId, PRELOAD_BATCH_SIZE, 
                requireContext(), items -> {
            if (getActivity() == null) return;
            
            getActivity().runOnUiThread(() -> {
                showLoading(false);
                
                if (!items.isEmpty()) {
                    // Добавляем новые карточки в адаптер
                    adapter.addItems(items);
                    
                    Log.d(TAG, "Загружена новая партия карточек: " + items.size() + 
                            ", теперь всего: " + adapter.getItemCount());
                    
                    // Отслеживаем загрузку контента в аналитике
                    AnalyticsTracker.trackContentLoad(requireContext(), categoryName, items.size(), true);
                } else {
                    Toast.makeText(getContext(), "Не удалось загрузить новые рекомендации", 
                            Toast.LENGTH_SHORT).show();
                    
                    // Отслеживаем неудачную загрузку в аналитике
                    AnalyticsTracker.trackContentLoad(requireContext(), categoryName, 0, false);
                }
            });
        });
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

    /**
     * Обработка свайпа карточки
     * @param direction направление свайпа
     */
    @Override
    public void onCardSwiped(Direction direction) {
        // Обработка свайпа карточки
        int position = manager.getTopPosition() - 1;
        if (position >= 0 && position < adapter.getItems().size()) {
            ContentItem item = adapter.getItems().get(position);
            Log.d(TAG, "Выполнен свайп карточки: " + item.getTitle() + " (ID: " + item.getId() + ", категория: " + categoryName + ")");

            String userId = getCurrentUserId();
            
            if (direction == Direction.Right) {
                // Пользователю понравился элемент
                item.setLiked(true);
                Toast.makeText(getContext(), "Добавлено в избранное: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                // Логируем действие
                ActionLogger.logSwipe(true, item.getId(), item.getTitle());
                
                // Отслеживаем свайп в аналитике
                AnalyticsTracker.trackSwipe(requireContext(), categoryName, true);

                // Используем вспомогательный класс для добавления элемента в избранное
                LikedItemsHelper.addToLiked(item, categoryName,
                        movieRepository, tvShowRepository,
                        gameRepository, bookRepository,
                        animeRepository, contentRepository);

                // Начисляем опыт за свайп вправо
                boolean levelUp = GamificationIntegrator.registerSwipe(getContext(), true);
                if (levelUp) {
                    Toast.makeText(getContext(), "Уровень повышен!", Toast.LENGTH_SHORT).show();
                }
                
                // Синхронизация с Firebase после добавления в избранное
                syncWithFirebase(userId, item);
            } else if (direction == Direction.Left) {
                // Пользователю не понравился элемент
                Toast.makeText(getContext(), "Пропущено: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                // Логируем действие
                ActionLogger.logSwipe(false, item.getId(), item.getTitle());
                
                // Отслеживаем свайп в аналитике
                AnalyticsTracker.trackSwipe(requireContext(), categoryName, false);

                // Начисляем опыт за свайп влево
                boolean levelUp = GamificationIntegrator.registerSwipe(getContext(), false);
                if (levelUp) {
                    Toast.makeText(getContext(), "Уровень повышен!", Toast.LENGTH_SHORT).show();
                    
                    // Синхронизация информации о пользователе при повышении уровня
                    syncUserProfileWithFirebase(userId);
                }
            }
        } else {
            Log.e(TAG, "Ошибка позиции при свайпе: position=" + position + ", размер адаптера=" + adapter.getItemCount());
        }

        // Если карточки заканчиваются, подгружаем еще
        if (manager.getTopPosition() >= adapter.getItemCount() - CARDS_THRESHOLD) {
            // Подгружаем дополнительные карточки
            loadNextBatch();
        }
        
        // Если карточки закончились, показываем сообщение
        if (manager.getTopPosition() == adapter.getItemCount()) {
            showEmptyState(true);
            // Пытаемся загрузить еще карточки
            loadNextBatch();
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
        
        // Преобразуем ContentItem в ContentEntity для синхронизации
        ContentEntity contentEntity = new ContentEntity();
        contentEntity.setId(item.getId());
        contentEntity.setTitle(item.getTitle());
        contentEntity.setCategory(item.getCategory());
        contentEntity.setImageUrl(item.getImageUrl());
        contentEntity.setDescription(item.getDescription());
        contentEntity.setRating(item.getRating());
        contentEntity.setCompleted(item.isWatched()); // Используем существующий метод isWatched
        contentEntity.setTimestamp(System.currentTimeMillis());
        
        // Используем FirestoreDataManager для синхронизации
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
            
            // Отслеживаем просмотр карточки в аналитике
            AnalyticsTracker.trackCardView(requireContext(), categoryName);
        }
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        // Действие при исчезновении карточки
    }
}