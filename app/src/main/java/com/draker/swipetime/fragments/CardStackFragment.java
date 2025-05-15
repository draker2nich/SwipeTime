package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.draker.swipetime.R;
import com.draker.swipetime.adapters.CardStackAdapter;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.utils.LikedItemsHelper;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.List;

public class CardStackFragment extends Fragment implements CardStackListener {

    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private ConstraintLayout emptyCardsContainer;
    private Button reloadButton;
    private String categoryName;

    // Репозитории для работы с базой данных
    private MovieRepository movieRepository;
    private TVShowRepository tvShowRepository;
    private GameRepository gameRepository;
    private BookRepository bookRepository;
    private AnimeRepository animeRepository;
    private ContentRepository contentRepository;

    private static final String ARG_CATEGORY = "category";

    public static CardStackFragment newInstance(String category) {
        CardStackFragment fragment = new CardStackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
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

        // Выводим отладочную информацию о количестве элементов в базе данных
        Log.d("CardStackFragment", "Категория: " + categoryName);
        Log.d("CardStackFragment", "Количество фильмов в базе: " + movieRepository.getCount());
        Log.d("CardStackFragment", "Количество сериалов в базе: " + tvShowRepository.getCount());
        Log.d("CardStackFragment", "Количество игр в базе: " + gameRepository.getCount());
        Log.d("CardStackFragment", "Количество книг в базе: " + bookRepository.getCount());
        Log.d("CardStackFragment", "Количество аниме в базе: " + animeRepository.getCount());
        Log.d("CardStackFragment", "Общее количество контента в базе: " + contentRepository.getCount());
        
        if (categoryName.equals("Музыка")) {
            List<ContentEntity> musicContent = contentRepository.getByCategory("Музыка");
            Log.d("CardStackFragment", "Найдено элементов музыки в базе: " + (musicContent != null ? musicContent.size() : 0));
        }

        // Настройка CardStackLayoutManager
        setupCardStackView();

        // Инициализация адаптера с данными из базы данных
        List<ContentItem> items = createTestItems();
        Log.d("CardStackFragment", "Создано элементов для отображения: " + items.size());
        
        adapter = new CardStackAdapter(getContext(), items);
        cardStackView.setAdapter(adapter);

        // Настройка кнопки перезагрузки
        reloadButton.setOnClickListener(v -> reloadCards());
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

    // Создание данных для CardStack из базы данных
    private List<ContentItem> createTestItems() {
        List<ContentItem> items = new ArrayList<>();
        
        Log.d("CardStackFragment", "Создание списка элементов для категории: " + categoryName);
        
        if (categoryName.equals("Фильмы")) {
            // Использование репозитория вместо хардкода данных
            List<MovieEntity> movies = movieRepository.getAll();
            Log.d("CardStackFragment", "Получено фильмов из базы данных: " + movies.size());
            
            if (movies != null && !movies.isEmpty()) {
                for (MovieEntity movie : movies) {
                    items.add(new ContentItem(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getDescription(),
                        movie.getImageUrl(),
                        movie.getCategory()
                    ));
                }
            } else {
                // Если в базе нет фильмов, создаем тестовые данные
                Log.d("CardStackFragment", "В базе нет фильмов, создаем тестовые");
                createTestMovies();
                
                // Получаем созданные фильмы
                movies = movieRepository.getAll();
                for (MovieEntity movie : movies) {
                    items.add(new ContentItem(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getDescription(),
                        movie.getImageUrl(),
                        movie.getCategory()
                    ));
                }
            }
        } else if (categoryName.equals("Сериалы")) {
            // Использование репозитория вместо хардкода данных
            List<TVShowEntity> tvShows = tvShowRepository.getAll();
            Log.d("CardStackFragment", "Получено сериалов из базы данных: " + tvShows.size());
            
            if (tvShows != null && !tvShows.isEmpty()) {
                for (TVShowEntity tvShow : tvShows) {
                    items.add(new ContentItem(
                        tvShow.getId(),
                        tvShow.getTitle(),
                        tvShow.getDescription(),
                        tvShow.getImageUrl(),
                        tvShow.getCategory()
                    ));
                }
            } else {
                // Если в базе нет сериалов, создаем тестовые данные
                Log.d("CardStackFragment", "В базе нет сериалов, создаем тестовые");
                createTestTVShows();
                
                // Получаем созданные сериалы
                tvShows = tvShowRepository.getAll();
                for (TVShowEntity tvShow : tvShows) {
                    items.add(new ContentItem(
                        tvShow.getId(),
                        tvShow.getTitle(),
                        tvShow.getDescription(),
                        tvShow.getImageUrl(),
                        tvShow.getCategory()
                    ));
                }
            }
        } else if (categoryName.equals("Игры")) {
            // Использование репозитория вместо хардкода данных
            List<GameEntity> games = gameRepository.getAll();
            Log.d("CardStackFragment", "Получено игр из базы данных: " + games.size());
            
            if (games != null && !games.isEmpty()) {
                for (GameEntity game : games) {
                    items.add(new ContentItem(
                        game.getId(),
                        game.getTitle(),
                        game.getDescription(),
                        game.getImageUrl(),
                        game.getCategory()
                    ));
                }
            } else {
                // Если в базе нет игр, создаем тестовые данные
                Log.d("CardStackFragment", "В базе нет игр, создаем тестовые");
                createTestGames();
                
                // Получаем созданные игры
                games = gameRepository.getAll();
                for (GameEntity game : games) {
                    items.add(new ContentItem(
                        game.getId(),
                        game.getTitle(),
                        game.getDescription(),
                        game.getImageUrl(),
                        game.getCategory()
                    ));
                }
            }
        } else if (categoryName.equals("Книги")) {
            // Использование репозитория вместо хардкода данных
            List<BookEntity> books = bookRepository.getAll();
            Log.d("CardStackFragment", "Получено книг из базы данных: " + books.size());
            
            if (books != null && !books.isEmpty()) {
                for (BookEntity book : books) {
                    items.add(new ContentItem(
                        book.getId(),
                        book.getTitle(),
                        book.getDescription(),
                        book.getImageUrl(),
                        book.getCategory()
                    ));
                }
            } else {
                // Если в базе нет книг, создаем тестовые данные
                Log.d("CardStackFragment", "В базе нет книг, создаем тестовые");
                createTestBooks();
                
                // Получаем созданные книги
                books = bookRepository.getAll();
                for (BookEntity book : books) {
                    items.add(new ContentItem(
                        book.getId(),
                        book.getTitle(),
                        book.getDescription(),
                        book.getImageUrl(),
                        book.getCategory()
                    ));
                }
            }
        } else if (categoryName.equals("Аниме")) {
            // Использование репозитория вместо хардкода данных
            List<AnimeEntity> animes = animeRepository.getAll();
            Log.d("CardStackFragment", "Получено аниме из базы данных: " + animes.size());
            
            if (animes != null && !animes.isEmpty()) {
                for (AnimeEntity anime : animes) {
                    items.add(new ContentItem(
                        anime.getId(),
                        anime.getTitle(),
                        anime.getDescription(),
                        anime.getImageUrl(),
                        anime.getCategory()
                    ));
                }
            } else {
                // Если в базе нет аниме, создаем тестовые данные
                Log.d("CardStackFragment", "В базе нет аниме, создаем тестовые");
                createTestAnimes();
                
                // Получаем созданные аниме
                animes = animeRepository.getAll();
                for (AnimeEntity anime : animes) {
                    items.add(new ContentItem(
                        anime.getId(),
                        anime.getTitle(),
                        anime.getDescription(),
                        anime.getImageUrl(),
                        anime.getCategory()
                    ));
                }
            }
        } else {
            // Для всех остальных категорий (включая Музыку) добавляем элементы из ContentEntity
            List<ContentEntity> baseEntities = contentRepository.getByCategory(categoryName);
            Log.d("CardStackFragment", "Получено элементов контента для категории " + categoryName + ": " + (baseEntities != null ? baseEntities.size() : 0));
            if (baseEntities != null && !baseEntities.isEmpty()) {
                for (ContentEntity entity : baseEntities) {
                    items.add(new ContentItem(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getDescription(),
                        entity.getImageUrl(),
                        entity.getCategory()
                    ));
                }
            } else {
                // Если в базе данных нет элементов для этой категории, добавляем тестовые
                Log.d("CardStackFragment", "Для категории " + categoryName + " в базе нет данных, создаем тестовые");
                for (int i = 1; i <= 5; i++) {
                    String itemId = categoryName.toLowerCase() + "_" + i;
                    items.add(new ContentItem(
                            itemId,
                            categoryName + " - Элемент " + i,
                            "Описание элемента " + i + " из категории " + categoryName + ". Это тестовое описание для демонстрации функционала свайпа карточек.",
                            "url_to_image",
                            categoryName
                    ));
                    
                    // Добавляем тестовые данные в базу
                    ContentEntity newEntity = new ContentEntity();
                    newEntity.setId(itemId);
                    newEntity.setTitle(categoryName + " - Элемент " + i);
                    newEntity.setDescription("Описание элемента " + i + " из категории " + categoryName + ". Это тестовое описание для демонстрации функционала свайпа карточек.");
                    newEntity.setImageUrl("url_to_image");
                    newEntity.setCategory(categoryName);
                    newEntity.setContentType(categoryName.toLowerCase());
                    contentRepository.insert(newEntity);
                    Log.d("CardStackFragment", "Создан тестовый элемент для " + categoryName + ": " + newEntity.getTitle());
                }
            }
        }
        
        Log.d("CardStackFragment", "Всего создано элементов для категории " + categoryName + ": " + items.size());
        return items;
    }
    
    // Методы для создания тестовых данных
    private void createTestMovies() {
        try {
            for (int i = 1; i <= 5; i++) {
                MovieEntity movie = new MovieEntity();
                movie.setId("movie_" + i);
                movie.setTitle("Фильм " + i);
                movie.setDescription("Описание фильма " + i + ". Увлекательный фильм для всей семьи.");
                movie.setImageUrl("url_to_image");
                movie.setCategory("Фильмы");
                movie.setContentType("movie");
                movieRepository.insert(movie);
                
                // Добавляем в общую таблицу контента
                ContentEntity contentMovie = new ContentEntity();
                contentMovie.setId(movie.getId());
                contentMovie.setTitle(movie.getTitle());
                contentMovie.setDescription(movie.getDescription());
                contentMovie.setImageUrl(movie.getImageUrl());
                contentMovie.setCategory(movie.getCategory());
                contentMovie.setContentType(movie.getContentType());
                contentRepository.insert(contentMovie);
                
                Log.d("CardStackFragment", "Создан тестовый фильм: " + movie.getTitle());
            }
        } catch (Exception e) {
            Log.e("CardStackFragment", "Ошибка при создании тестовых фильмов: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTestTVShows() {
        try {
            for (int i = 1; i <= 5; i++) {
                TVShowEntity tvShow = new TVShowEntity();
                tvShow.setId("tvshow_" + i);
                tvShow.setTitle("Сериал " + i);
                tvShow.setDescription("Описание сериала " + i + ". Захватывающий сериал с множеством сезонов.");
                tvShow.setImageUrl("url_to_image");
                tvShow.setCategory("Сериалы");
                tvShow.setContentType("tvshow");
                tvShowRepository.insert(tvShow);
                
                // Добавляем в общую таблицу контента
                ContentEntity contentTVShow = new ContentEntity();
                contentTVShow.setId(tvShow.getId());
                contentTVShow.setTitle(tvShow.getTitle());
                contentTVShow.setDescription(tvShow.getDescription());
                contentTVShow.setImageUrl(tvShow.getImageUrl());
                contentTVShow.setCategory(tvShow.getCategory());
                contentTVShow.setContentType(tvShow.getContentType());
                contentRepository.insert(contentTVShow);
                
                Log.d("CardStackFragment", "Создан тестовый сериал: " + tvShow.getTitle());
            }
        } catch (Exception e) {
            Log.e("CardStackFragment", "Ошибка при создании тестовых сериалов: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTestGames() {
        try {
            for (int i = 1; i <= 5; i++) {
                GameEntity game = new GameEntity();
                game.setId("game_" + i);
                game.setTitle("Игра " + i);
                game.setDescription("Описание игры " + i + ". Увлекательная игра с открытым миром.");
                game.setImageUrl("url_to_image");
                game.setCategory("Игры");
                game.setContentType("game");
                gameRepository.insert(game);
                
                // Добавляем в общую таблицу контента
                ContentEntity contentGame = new ContentEntity();
                contentGame.setId(game.getId());
                contentGame.setTitle(game.getTitle());
                contentGame.setDescription(game.getDescription());
                contentGame.setImageUrl(game.getImageUrl());
                contentGame.setCategory(game.getCategory());
                contentGame.setContentType(game.getContentType());
                contentRepository.insert(contentGame);
                
                Log.d("CardStackFragment", "Создана тестовая игра: " + game.getTitle());
            }
        } catch (Exception e) {
            Log.e("CardStackFragment", "Ошибка при создании тестовых игр: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTestBooks() {
        try {
            for (int i = 1; i <= 5; i++) {
                BookEntity book = new BookEntity();
                book.setId("book_" + i);
                book.setTitle("Книга " + i);
                book.setDescription("Описание книги " + i + ". Интересное произведение с глубоким смыслом.");
                book.setImageUrl("url_to_image");
                book.setCategory("Книги");
                book.setContentType("book");
                bookRepository.insert(book);
                
                // Добавляем в общую таблицу контента
                ContentEntity contentBook = new ContentEntity();
                contentBook.setId(book.getId());
                contentBook.setTitle(book.getTitle());
                contentBook.setDescription(book.getDescription());
                contentBook.setImageUrl(book.getImageUrl());
                contentBook.setCategory(book.getCategory());
                contentBook.setContentType(book.getContentType());
                contentRepository.insert(contentBook);
                
                Log.d("CardStackFragment", "Создана тестовая книга: " + book.getTitle());
            }
        } catch (Exception e) {
            Log.e("CardStackFragment", "Ошибка при создании тестовых книг: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTestAnimes() {
        try {
            for (int i = 1; i <= 5; i++) {
                AnimeEntity anime = new AnimeEntity();
                anime.setId("anime_" + i);
                anime.setTitle("Аниме " + i);
                anime.setDescription("Описание аниме " + i + ". Красочное аниме с интересным сюжетом.");
                anime.setImageUrl("url_to_image");
                anime.setCategory("Аниме");
                anime.setContentType("anime");
                animeRepository.insert(anime);
                
                // Добавляем в общую таблицу контента
                ContentEntity contentAnime = new ContentEntity();
                contentAnime.setId(anime.getId());
                contentAnime.setTitle(anime.getTitle());
                contentAnime.setDescription(anime.getDescription());
                contentAnime.setImageUrl(anime.getImageUrl());
                contentAnime.setCategory(anime.getCategory());
                contentAnime.setContentType(anime.getContentType());
                contentRepository.insert(contentAnime);
                
                Log.d("CardStackFragment", "Создано тестовое аниме: " + anime.getTitle());
            }
        } catch (Exception e) {
            Log.e("CardStackFragment", "Ошибка при создании тестовых аниме: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reloadCards() {
        adapter.setItems(createTestItems());
        cardStackView.setVisibility(View.VISIBLE);
        emptyCardsContainer.setVisibility(View.GONE);
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
            Log.d("CardStackFragment", "Выполнен свайп карточки: " + item.getTitle() + " (ID: " + item.getId() + ", категория: " + categoryName + ")");
            
            if (direction == Direction.Right) {
                // Пользователю понравился элемент
                item.setLiked(true);
                Toast.makeText(getContext(), "Добавлено в избранное: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                
                // Используем вспомогательный класс для добавления элемента в избранное
                LikedItemsHelper.addToLiked(item, categoryName, 
                                          movieRepository, tvShowRepository, 
                                          gameRepository, bookRepository, 
                                          animeRepository, contentRepository);
                
                // Проверяем наличие элемента в базе после добавления
                boolean isFound = false;
                
                if (categoryName.equals("Музыка") || categoryName.equals("Фильмы") || 
                    categoryName.equals("Сериалы") || categoryName.equals("Игры") || 
                    categoryName.equals("Книги") || categoryName.equals("Аниме")) {
                    // Проверка для всех категорий
                    List<ContentEntity> categoryContent = contentRepository.getByCategory(categoryName);
                    Log.d("CardStackFragment", "После свайпа: найдено элементов " + categoryName + ": " + 
                         (categoryContent != null ? categoryContent.size() : 0));
                    boolean foundInCategory = false;
                    if (categoryContent != null) {
                        for (ContentEntity content : categoryContent) {
                            if (content.getId().equals(item.getId())) {
                                foundInCategory = true;
                                Log.d("CardStackFragment", categoryName + " найден(о) по ID: " + content.getTitle() + 
                                     ", liked=" + content.isLiked());
                                break;
                            }
                        }
                    }
                    if (!foundInCategory) {
                        Log.d("CardStackFragment", categoryName + " с ID=" + item.getId() + 
                             " не найден(о) в списке категории");
                    } else {
                        isFound = true;
                    }
                }
                
                // Проверка в общем хранилище контента
                ContentEntity contentCheck = contentRepository.getById(item.getId());
                if (contentCheck != null) {
                    Log.d("CardStackFragment", "Контент найден в общем хранилище: " + contentCheck.getTitle() + ", liked=" + contentCheck.isLiked());
                    isFound = true;
                }
                
                if (!isFound) {
                    Log.d("CardStackFragment", "ВНИМАНИЕ: После добавления в избранное, элемент НЕ найден в базе данных!");
                }
            } else if (direction == Direction.Left) {
                // Пользователю не понравился элемент
                Toast.makeText(getContext(), "Пропущено: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("CardStackFragment", "Ошибка позиции при свайпе: position=" + position + ", размер адаптера=" + adapter.getItemCount());
        }

        // Если карточки закончились, показываем сообщение
        if (manager.getTopPosition() == adapter.getItemCount()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
        }
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
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        // Действие при исчезновении карточки
    }
}