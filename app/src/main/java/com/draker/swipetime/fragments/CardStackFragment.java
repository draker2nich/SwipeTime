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
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
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
import com.draker.swipetime.utils.CardFilterIntegrator;
import com.draker.swipetime.utils.CardInfoHelper;
import com.draker.swipetime.utils.ContentFilterHelper;
import com.draker.swipetime.utils.FirestoreDataManager;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.GamificationManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardStackFragment extends Fragment implements CardStackListener, FilterSettingsFragment.OnFilterSettingsClosedListener {

    private static final String TAG = "CardStackFragment";

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

    private static final String ARG_CATEGORY = "category";

    // Флаг для отслеживания применения фильтров
    private boolean filtersApplied = false;
    
    // Флаги для отслеживания наличия данных в категориях
    private boolean hasMovies = false;
    private boolean hasTVShows = false;
    private boolean hasGames = false;
    private boolean hasBooks = false;
    private boolean hasAnime = false;

    public static CardStackFragment newInstance(String category) {
        CardStackFragment fragment = new CardStackFragment();
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
        
        // Проверяем наличие данных в категориях
        hasMovies = movieRepository.getCount() >= 10;
        hasTVShows = tvShowRepository.getCount() >= 10;
        hasGames = gameRepository.getCount() >= 10;
        hasBooks = bookRepository.getCount() >= 10;
        hasAnime = animeRepository.getCount() >= 10;
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

        // Выводим отладочную информацию о количестве элементов в базе данных
        Log.d(TAG, "Категория: " + categoryName);
        Log.d(TAG, "Количество фильмов в базе: " + movieRepository.getCount());
        Log.d(TAG, "Количество сериалов в базе: " + tvShowRepository.getCount());
        Log.d(TAG, "Количество игр в базе: " + gameRepository.getCount());
        Log.d(TAG, "Количество книг в базе: " + bookRepository.getCount());
        Log.d(TAG, "Количество аниме в базе: " + animeRepository.getCount());
        Log.d(TAG, "Общее количество контента в базе: " + contentRepository.getCount());

        // Настройка CardStackLayoutManager
        setupCardStackView();

        // Инициализация адаптера с данными, используя CardFilterIntegrator
        String userId = getCurrentUserId();
        List<ContentItem> items = CardFilterIntegrator.getFilteredContentItems(
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
        
        Log.d(TAG, "Создано элементов для отображения: " + items.size());

        adapter = new CardStackAdapter(getContext(), items);
        cardStackView.setAdapter(adapter);

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
        // Используем новый CardFilterIntegrator для получения отфильтрованного списка
        String userId = getCurrentUserId();
        List<ContentItem> filteredItems = CardFilterIntegrator.getFilteredContentItems(
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
        
        Log.d(TAG, "Отфильтрованных элементов: " + filteredItems.size());
        
        // Если отфильтрованных элементов мало, загружаем дополнительные из API
        if (filteredItems.size() < 10) {
            Toast.makeText(getContext(), "Загрузка дополнительного контента...", Toast.LENGTH_SHORT).show();
            
            // Загружаем данные из API
            loadAdditionalContentFromApi();
        }
        
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
        // Если установлены фильтры, перезагружаем с их применением
        String userId = getCurrentUserId();
        if (filtersApplied) {
            reloadCardsWithFilters();
        } else {
            // Иначе загружаем все элементы
            List<ContentItem> allItems = CardFilterIntegrator.getFilteredContentItems(
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
            
            // Если элементов мало, загружаем дополнительные из API
            if (allItems.size() < 10) {
                Toast.makeText(getContext(), "Загрузка дополнительного контента...", Toast.LENGTH_SHORT).show();
                
                // Загружаем данные из API
                loadAdditionalContentFromApi();
            }
            
            adapter.setItems(allItems);
        }

        cardStackView.setVisibility(View.VISIBLE);
        emptyCardsContainer.setVisibility(View.GONE);
    }
    
    /**
     * Загружает дополнительный контент из внешних API
     */
    private void loadAdditionalContentFromApi() {
        // Проверяем, не загружается ли уже контент
        if (isLoading) {
            return;
        }
        
        isLoading = true;
        
        // Показываем индикатор загрузки
        Toast.makeText(getContext(), "Загружаем дополнительный контент...", Toast.LENGTH_SHORT).show();
        
        // Создаем API менеджер
        com.draker.swipetime.api.ApiIntegrationManager apiManager = 
                com.draker.swipetime.api.ApiIntegrationManager.getInstance(
                        requireActivity().getApplication()
                );
        
        // Обновляем данные из API для текущей категории
        apiManager.refreshCategoryContent(categoryName, 15, new com.draker.swipetime.api.ApiIntegrationManager.ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    // Обновляем список элементов после загрузки
                    refreshCardsAfterApiLoad();
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Не удалось загрузить дополнительный контент", Toast.LENGTH_SHORT).show();
                        isLoading = false;
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Ошибка загрузки контента: " + errorMessage, Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
            }
        });
    }
    
    /**
     * Обновляет список карточек после загрузки данных из API
     */
    private void refreshCardsAfterApiLoad() {
        // Сбрасываем сессионную историю перемешивания
        com.draker.swipetime.utils.ContentShuffler.resetSessionHistory(categoryName);
        
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // Получаем текущее количество карточек
            int currentCardCount = adapter.getItemCount();
            
            // Получаем свежие данные с применением фильтров
            String userId = getCurrentUserId();
            List<ContentItem> freshItems = CardFilterIntegrator.getFilteredContentItems(
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
            
            Log.d(TAG, "Получено свежих элементов после загрузки API: " + freshItems.size());
            
            // Фильтруем только новые элементы, которых нет в текущем адаптере
            List<ContentItem> newItems = new ArrayList<>();
            Set<String> currentIds = new HashSet<>();
            
            // Собираем ID текущих элементов
            for (int i = 0; i < adapter.getItemCount(); i++) {
                currentIds.add(adapter.getItems().get(i).getId());
            }
            
            // Добавляем только новые элементы
            for (ContentItem item : freshItems) {
                if (!currentIds.contains(item.getId())) {
                    newItems.add(item);
                }
            }
            
            Log.d(TAG, "Новых уникальных элементов: " + newItems.size());
            
            if (!newItems.isEmpty()) {
                // Перемешиваем новые элементы
                List<ContentItem> shuffledNewItems = com.draker.swipetime.utils.ContentShuffler.shuffleContent(newItems, categoryName);
                
                // Добавляем новые элементы в адаптер
                adapter.addItems(shuffledNewItems);
                
                // Отображаем сообщение
                Toast.makeText(getContext(), "Добавлено " + shuffledNewItems.size() + " новых элементов", Toast.LENGTH_SHORT).show();
                
                // Показываем карточки, если они были скрыты
                if (cardStackView.getVisibility() != View.VISIBLE) {
                    cardStackView.setVisibility(View.VISIBLE);
                    emptyCardsContainer.setVisibility(View.GONE);
                }
            } else {
                // Если нет новых элементов, отображаем сообщение
                Toast.makeText(getContext(), "Нет новых элементов для отображения", Toast.LENGTH_SHORT).show();
            }
            
            isLoading = false;
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
            
            // Отмечаем элемент как показанный в ContentShuffler
            com.draker.swipetime.utils.ContentShuffler.markContentAsPermanentlyShown(categoryName, item.getId());
            
            if (direction == Direction.Right) {
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

                // Начисляем опыт за свайп влево
                boolean levelUp = GamificationIntegrator.registerSwipe(getContext(), false);
                if (levelUp) {
                    Toast.makeText(getContext(), "Уровень повышен!", Toast.LENGTH_SHORT).show();
                    
                    // Синхронизация информации о пользователе при повышении уровня
                    syncUserProfileWithFirebase(userId);
                }
                
                // Удаляем из базы данных неинтересный контент, чтобы не повторялся
                removeUnlikedContent(item);
            }
            
            // Удаляем карточку из адаптера, чтобы избежать повторений
            adapter.removeItem(position);
        } else {
            Log.e(TAG, "Ошибка позиции при свайпе: position=" + position + ", размер адаптера=" + adapter.getItemCount());
        }

        // Если карточки заканчиваются, подгружаем еще
        if (manager.getTopPosition() >= adapter.getItemCount() - 3) {
            // Подгружаем дополнительные карточки
            loadAdditionalContentFromApi();
        }
        
        // Если карточки закончились, показываем сообщение
        if (manager.getTopPosition() == adapter.getItemCount()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Удаляет неинтересный контент из базы данных
     * @param item элемент контента
     */
    private void removeUnlikedContent(ContentItem item) {
        if (item == null || item.getId() == null) {
            return;
        }
        
        // Запуск в отдельном потоке для избежания блокировки UI
        new Thread(() -> {
            try {
                switch (categoryName.toLowerCase()) {
                    case "фильмы":
                        movieRepository.deleteById(item.getId());
                        break;
                    case "сериалы":
                        tvShowRepository.deleteById(item.getId());
                        break;
                    case "игры":
                        gameRepository.deleteById(item.getId());
                        break;
                    case "книги":
                        bookRepository.deleteById(item.getId());
                        break;
                    case "аниме":
                        animeRepository.deleteById(item.getId());
                        break;
                    default:
                        contentRepository.deleteById(item.getId());
                        break;
                }
                
                Log.d(TAG, "Удален элемент из базы данных: " + item.getTitle() + " (ID: " + item.getId() + ")");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при удалении элемента: " + e.getMessage());
            }
        }).start();
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
                // Сохраняем флаг, что данные требуют повторной синхронизации
                // TODO: Реализовать механизм отложенной синхронизации
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