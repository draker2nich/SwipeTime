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

        // Настройка CardStackLayoutManager
        setupCardStackView();

        // Инициализация адаптера с данными из базы данных
        adapter = new CardStackAdapter(getContext(), createTestItems());
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
        
        if (categoryName.equals("Фильмы")) {
            // Использование репозитория вместо хардкода данных
            List<MovieEntity> movies = movieRepository.getAll();
            for (MovieEntity movie : movies) {
                items.add(new ContentItem(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDescription(),
                    movie.getImageUrl(),
                    movie.getCategory()
                ));
            }
        } else if (categoryName.equals("Сериалы")) {
            // Использование репозитория вместо хардкода данных
            List<TVShowEntity> tvShows = tvShowRepository.getAll();
            for (TVShowEntity tvShow : tvShows) {
                items.add(new ContentItem(
                    tvShow.getId(),
                    tvShow.getTitle(),
                    tvShow.getDescription(),
                    tvShow.getImageUrl(),
                    tvShow.getCategory()
                ));
            }
        } else if (categoryName.equals("Игры")) {
            // Использование репозитория вместо хардкода данных
            List<GameEntity> games = gameRepository.getAll();
            for (GameEntity game : games) {
                items.add(new ContentItem(
                    game.getId(),
                    game.getTitle(),
                    game.getDescription(),
                    game.getImageUrl(),
                    game.getCategory()
                ));
            }
        } else if (categoryName.equals("Книги")) {
            // Использование репозитория вместо хардкода данных
            List<BookEntity> books = bookRepository.getAll();
            for (BookEntity book : books) {
                items.add(new ContentItem(
                    book.getId(),
                    book.getTitle(),
                    book.getDescription(),
                    book.getImageUrl(),
                    book.getCategory()
                ));
            }
        } else if (categoryName.equals("Аниме")) {
            // Использование репозитория вместо хардкода данных
            List<AnimeEntity> animes = animeRepository.getAll();
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
            // Для всех остальных категорий добавляем базовые элементы
            List<com.draker.swipetime.database.entities.ContentEntity> baseEntities = contentRepository.getByCategory(categoryName);
            if (baseEntities != null && !baseEntities.isEmpty()) {
                for (com.draker.swipetime.database.entities.ContentEntity entity : baseEntities) {
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
                for (int i = 1; i <= 5; i++) {
                    items.add(new ContentItem(
                            String.valueOf(i),
                            categoryName + " - Элемент " + i,
                            "Описание элемента " + i + " из категории " + categoryName + ". Это тестовое описание для демонстрации функционала свайпа карточек.",
                            "url_to_image",
                            categoryName
                    ));
                }
            }
        }
        
        return items;
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
            
            if (direction == Direction.Right) {
                // Пользователю понравился элемент
                item.setLiked(true);
                Toast.makeText(getContext(), "Добавлено в избранное: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                
                // Сохраняем статус "нравится" в базе данных
                updateLikedStatus(item.getId(), true);
                
                // Добавляем в список избранного
                addToLikedList(item);
            } else if (direction == Direction.Left) {
                // Пользователю не понравился элемент
                Toast.makeText(getContext(), "Пропущено: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        // Если карточки закончились, показываем сообщение
        if (manager.getTopPosition() == adapter.getItemCount()) {
            cardStackView.setVisibility(View.GONE);
            emptyCardsContainer.setVisibility(View.VISIBLE);
        }
    }
    
    // Добавить элемент в список избранного
    private void addToLikedList(ContentItem item) {
        try {
            // В зависимости от категории обновляем соответствующую сущность
            if (categoryName.equals("Фильмы")) {
                MovieEntity movie = movieRepository.getById(item.getId());
                if (movie != null) {
                    movie.setLiked(true);
                    movieRepository.update(movie);
                    Log.d("CardStackFragment", "Фильм добавлен в избранное: " + movie.getTitle());
                }
            } else if (categoryName.equals("Сериалы")) {
                TVShowEntity tvShow = tvShowRepository.getById(item.getId());
                if (tvShow != null) {
                    tvShow.setLiked(true);
                    tvShowRepository.update(tvShow);
                    Log.d("CardStackFragment", "Сериал добавлен в избранное: " + tvShow.getTitle());
                }
            } else if (categoryName.equals("Игры")) {
                GameEntity game = gameRepository.getById(item.getId());
                if (game != null) {
                    game.setLiked(true);
                    gameRepository.update(game);
                    Log.d("CardStackFragment", "Игра добавлена в избранное: " + game.getTitle());
                }
            } else if (categoryName.equals("Книги")) {
                BookEntity book = bookRepository.getById(item.getId());
                if (book != null) {
                    book.setLiked(true);
                    bookRepository.update(book);
                    Log.d("CardStackFragment", "Книга добавлена в избранное: " + book.getTitle());
                }
            } else if (categoryName.equals("Аниме")) {
                AnimeEntity anime = animeRepository.getById(item.getId());
                if (anime != null) {
                    anime.setLiked(true);
                    animeRepository.update(anime);
                    Log.d("CardStackFragment", "Аниме добавлено в избранное: " + anime.getTitle());
                }
            } else {
                // Для других категорий используем ContentEntity
                ContentEntity content = contentRepository.getById(item.getId());
                if (content != null) {
                    content.setLiked(true);
                    contentRepository.update(content);
                    Log.d("CardStackFragment", "Контент добавлен в избранное: " + content.getTitle());
                }
            }
        } catch (Exception e) {
            Log.e("CardStackFragment", "Ошибка при добавлении в избранное: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Обновление статуса "нравится" в базе данных
    private void updateLikedStatus(String id, boolean liked) {
        if (id != null && !id.isEmpty()) {
            try {
                // Обновляем статус в базе данных через репозиторий
                contentRepository.updateLikedStatus(id, liked);
                
                // Для отладки добавим вывод в лог
                Log.d("CardStackFragment", "Обновлен статус liked=" + liked + " для id=" + id);
                
                // Если это фильм, также обновляем в MovieEntity
                MovieEntity movie = movieRepository.getById(id);
                if (movie != null) {
                    movie.setLiked(liked);
                    movieRepository.update(movie);
                    Log.d("CardStackFragment", "Обновлен статус liked в MovieEntity");
                }
                
                // Если это сериал, также обновляем в TVShowEntity
                TVShowEntity tvShow = tvShowRepository.getById(id);
                if (tvShow != null) {
                    tvShow.setLiked(liked);
                    tvShowRepository.update(tvShow);
                    Log.d("CardStackFragment", "Обновлен статус liked в TVShowEntity");
                }
                
                // Если это игра, также обновляем в GameEntity
                GameEntity game = gameRepository.getById(id);
                if (game != null) {
                    game.setLiked(liked);
                    gameRepository.update(game);
                    Log.d("CardStackFragment", "Обновлен статус liked в GameEntity");
                }
                
                // Если это книга, также обновляем в BookEntity
                BookEntity book = bookRepository.getById(id);
                if (book != null) {
                    book.setLiked(liked);
                    bookRepository.update(book);
                    Log.d("CardStackFragment", "Обновлен статус liked в BookEntity");
                }
                
                // Если это аниме, также обновляем в AnimeEntity
                AnimeEntity anime = animeRepository.getById(id);
                if (anime != null) {
                    anime.setLiked(liked);
                    animeRepository.update(anime);
                    Log.d("CardStackFragment", "Обновлен статус liked в AnimeEntity");
                }
            } catch (Exception e) {
                Log.e("CardStackFragment", "Ошибка при обновлении статуса liked: " + e.getMessage());
                e.printStackTrace();
            }
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
