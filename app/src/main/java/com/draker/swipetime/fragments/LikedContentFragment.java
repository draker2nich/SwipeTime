package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.adapters.LikedContentAdapter;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.ReviewEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.ReviewRepository;
import com.draker.swipetime.repository.TVShowRepository;

import java.util.ArrayList;
import java.util.List;

public class LikedContentFragment extends Fragment implements LikedContentAdapter.OnItemClickListener {

    private static final String TAG = "LikedContentFragment";
    private static final String CURRENT_USER_ID = "user_1"; // ID текущего пользователя для демонстрации

    private RecyclerView recyclerView;
    private TextView emptyMessageTextView;
    private LikedContentAdapter adapter;
    
    // Репозитории для получения данных
    private MovieRepository movieRepository;
    private TVShowRepository tvShowRepository;
    private GameRepository gameRepository;
    private BookRepository bookRepository;
    private AnimeRepository animeRepository;
    private ContentRepository contentRepository;
    private ReviewRepository reviewRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Инициализация репозиториев
        movieRepository = new MovieRepository(requireActivity().getApplication());
        tvShowRepository = new TVShowRepository(requireActivity().getApplication());
        gameRepository = new GameRepository(requireActivity().getApplication());
        bookRepository = new BookRepository(requireActivity().getApplication());
        animeRepository = new AnimeRepository(requireActivity().getApplication());
        contentRepository = new ContentRepository(requireActivity().getApplication());
        reviewRepository = new ReviewRepository(requireActivity().getApplication());
        
        // Инициализация UI компонентов
        recyclerView = view.findViewById(R.id.liked_content_recycler_view);
        emptyMessageTextView = view.findViewById(R.id.message_empty_liked);
        
        // Вывод отладочной информации
        Log.d(TAG, "Проверка данных в базе:");
        Log.d(TAG, "Количество фильмов: " + movieRepository.getCount() + ", понравившихся: " + movieRepository.getLikedCount());
        Log.d(TAG, "Количество сериалов: " + tvShowRepository.getCount() + ", понравившихся: " + tvShowRepository.getLikedCount());
        Log.d(TAG, "Количество игр: " + gameRepository.getCount() + ", понравившихся: " + gameRepository.getLikedCount());
        Log.d(TAG, "Количество книг: " + bookRepository.getCount() + ", понравившихся: " + bookRepository.getLikedCount());
        Log.d(TAG, "Количество аниме: " + animeRepository.getCount() + ", понравившихся: " + animeRepository.getLikedCount());
        Log.d(TAG, "Общее количество контента: " + contentRepository.getCount() + ", понравившегося: " + contentRepository.getLikedCount());
        
        // Проверка на любимое содержимое и добавление тестовых данных если ничего нет
        if (contentRepository.getLikedCount() == 0) {
            Log.d(TAG, "В базе нет избранных элементов, добавляем тестовые данные для отладки");
            addTestLikedData();
        }
        
        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LikedContentAdapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        
        // Загрузка избранного контента
        loadLikedContent();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список при возвращении на экран
        loadLikedContent();
    }
    
    /**
     * Загрузка избранного контента из всех репозиториев
     */
    private void loadLikedContent() {
        try {
            List<ContentItem> likedItems = new ArrayList<>();
            
            // Вывод отладочной информации для проверки
            Log.d(TAG, "Начинаем загрузку избранного. Проверяем статус по категориям:");
            Log.d(TAG, "Фильмы: в базе " + movieRepository.getCount() + ", из них понравились: " + movieRepository.getLikedCount());
            Log.d(TAG, "Сериалы: в базе " + tvShowRepository.getCount() + ", из них понравились: " + tvShowRepository.getLikedCount());
            Log.d(TAG, "Игры: в базе " + gameRepository.getCount() + ", из них понравились: " + gameRepository.getLikedCount());
            Log.d(TAG, "Книги: в базе " + bookRepository.getCount() + ", из них понравились: " + bookRepository.getLikedCount());
            Log.d(TAG, "Аниме: в базе " + animeRepository.getCount() + ", из них понравились: " + animeRepository.getLikedCount());
            
            // Проверяем контент по категориям
            List<ContentEntity> musicContent = contentRepository.getByCategory("Музыка");
            Log.d(TAG, "Музыка: найдено элементов: " + (musicContent != null ? musicContent.size() : 0));
            if (musicContent != null) {
                for (ContentEntity entity : musicContent) {
                    Log.d(TAG, "Музыка в базе: " + entity.getTitle() + ", liked=" + entity.isLiked());
                }
            }
            
            // Проверяем лайкнутый контент по категориям 
            List<ContentEntity> likedMusicContent = contentRepository.getLikedByCategory("Музыка");
            Log.d(TAG, "Понравившаяся музыка: " + (likedMusicContent != null ? likedMusicContent.size() : 0));
            
            List<ContentEntity> likedMoviesContent = contentRepository.getLikedByCategory("Фильмы");
            Log.d(TAG, "Понравившиеся фильмы: " + (likedMoviesContent != null ? likedMoviesContent.size() : 0));
            
            List<ContentEntity> likedTVShowsContent = contentRepository.getLikedByCategory("Сериалы");
            Log.d(TAG, "Понравившиеся сериалы: " + (likedTVShowsContent != null ? likedTVShowsContent.size() : 0));
            
            List<ContentEntity> likedGamesContent = contentRepository.getLikedByCategory("Игры");
            Log.d(TAG, "Понравившиеся игры: " + (likedGamesContent != null ? likedGamesContent.size() : 0));
            
            List<ContentEntity> likedBooksContent = contentRepository.getLikedByCategory("Книги");
            Log.d(TAG, "Понравившиеся книги: " + (likedBooksContent != null ? likedBooksContent.size() : 0));
            
            List<ContentEntity> likedAnimeContent = contentRepository.getLikedByCategory("Аниме");
            Log.d(TAG, "Понравившееся аниме: " + (likedAnimeContent != null ? likedAnimeContent.size() : 0));
            
            // Добавляем избранные фильмы
            List<MovieEntity> likedMovies = movieRepository.getLiked();
            Log.d(TAG, "Загружено избранных фильмов: " + likedMovies.size());
            for (MovieEntity movie : likedMovies) {
                ContentItem item = new ContentItem(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDescription(),
                    movie.getImageUrl(),
                    movie.getCategory()
                );
                item.setWatched(movie.isWatched());
                item.setLiked(true); // Устанавливаем статус "избранное"
                
                // Проверяем наличие отзыва и рейтинга
                ReviewEntity review = reviewRepository.getByContentAndUserId(movie.getId(), CURRENT_USER_ID);
                if (review != null) {
                    item.setRating(review.getRating());
                    item.setReview(review.getText());
                }
                
                likedItems.add(item);
                Log.d(TAG, "Добавлен фильм в избранное: " + movie.getTitle());
            }
            
            // Добавляем избранные сериалы
            List<TVShowEntity> likedTVShows = tvShowRepository.getLiked();
            Log.d(TAG, "Загружено избранных сериалов: " + likedTVShows.size());
            for (TVShowEntity tvShow : likedTVShows) {
                ContentItem item = new ContentItem(
                    tvShow.getId(),
                    tvShow.getTitle(),
                    tvShow.getDescription(),
                    tvShow.getImageUrl(),
                    tvShow.getCategory()
                );
                item.setWatched(tvShow.isWatched());
                item.setLiked(true); // Устанавливаем статус "избранное"
                
                // Проверяем наличие отзыва и рейтинга
                ReviewEntity review = reviewRepository.getByContentAndUserId(tvShow.getId(), CURRENT_USER_ID);
                if (review != null) {
                    item.setRating(review.getRating());
                    item.setReview(review.getText());
                }
                
                likedItems.add(item);
                Log.d(TAG, "Добавлен сериал в избранное: " + tvShow.getTitle());
            }
            
            // Добавляем избранные игры
            List<GameEntity> likedGames = gameRepository.getLiked();
            Log.d(TAG, "Загружено избранных игр: " + likedGames.size());
            for (GameEntity game : likedGames) {
                ContentItem item = new ContentItem(
                    game.getId(),
                    game.getTitle(),
                    game.getDescription(),
                    game.getImageUrl(),
                    game.getCategory()
                );
                item.setWatched(game.isCompleted());
                item.setLiked(true); // Устанавливаем статус "избранное"
                
                // Проверяем наличие отзыва и рейтинга
                ReviewEntity review = reviewRepository.getByContentAndUserId(game.getId(), CURRENT_USER_ID);
                if (review != null) {
                    item.setRating(review.getRating());
                    item.setReview(review.getText());
                }
                
                likedItems.add(item);
                Log.d(TAG, "Добавлена игра в избранное: " + game.getTitle());
            }
            
            // Добавляем избранные книги
            List<BookEntity> likedBooks = bookRepository.getLiked();
            Log.d(TAG, "Загружено избранных книг: " + likedBooks.size());
            for (BookEntity book : likedBooks) {
                ContentItem item = new ContentItem(
                    book.getId(),
                    book.getTitle(),
                    book.getDescription(),
                    book.getImageUrl(),
                    book.getCategory()
                );
                item.setWatched(book.isRead());
                item.setLiked(true); // Устанавливаем статус "избранное"
                
                // Проверяем наличие отзыва и рейтинга
                ReviewEntity review = reviewRepository.getByContentAndUserId(book.getId(), CURRENT_USER_ID);
                if (review != null) {
                    item.setRating(review.getRating());
                    item.setReview(review.getText());
                }
                
                likedItems.add(item);
                Log.d(TAG, "Добавлена книга в избранное: " + book.getTitle());
            }
            
            // Добавляем избранное аниме
            List<AnimeEntity> likedAnimes = animeRepository.getLiked();
            Log.d(TAG, "Загружено избранных аниме: " + likedAnimes.size());
            for (AnimeEntity anime : likedAnimes) {
                ContentItem item = new ContentItem(
                    anime.getId(),
                    anime.getTitle(),
                    anime.getDescription(),
                    anime.getImageUrl(),
                    anime.getCategory()
                );
                item.setWatched(anime.isWatched());
                item.setLiked(true); // Устанавливаем статус "избранное"
                
                // Проверяем наличие отзыва и рейтинга
                ReviewEntity review = reviewRepository.getByContentAndUserId(anime.getId(), CURRENT_USER_ID);
                if (review != null) {
                    item.setRating(review.getRating());
                    item.setReview(review.getText());
                }
                
                likedItems.add(item);
                Log.d(TAG, "Добавлено аниме в избранное: " + anime.getTitle());
            }
            
            // Добавляем прочий избранный контент (включая музыку и другие категории)
            List<ContentEntity> likedContent = contentRepository.getLiked();
            Log.d(TAG, "Загружено прочего избранного контента: " + likedContent.size());
            if (likedContent.size() > 0) {
                for (ContentEntity content : likedContent) {
                    Log.d(TAG, "Найден избранный контент: " + content.getTitle() + " (категория: " + content.getCategory() + ")");
                }
            }
            
            for (ContentEntity content : likedContent) {
                // Проверка на дубликат должна учитывать ID и категорию контента
                boolean alreadyAdded = false;
                for (ContentItem item : likedItems) {
                    if (item.getId().equals(content.getId()) && 
                        (item.getCategory() == null || content.getCategory() == null || 
                        item.getCategory().equals(content.getCategory()))) {
                        alreadyAdded = true;
                        Log.d(TAG, "Пропуск дубликата: " + content.getTitle() + " (уже добавлен в список)");
                        break;
                    }
                }
                
                // Если этого контента еще нет в списке, добавляем его
                if (!alreadyAdded) {
                    ContentItem item = new ContentItem(
                        content.getId(),
                        content.getTitle(),
                        content.getDescription(),
                        content.getImageUrl(),
                        content.getCategory()
                    );
                    item.setWatched(content.isWatched());
                    item.setLiked(true); // Устанавливаем статус "избранное"
                    
                    // Проверяем наличие отзыва и рейтинга
                    ReviewEntity review = reviewRepository.getByContentAndUserId(content.getId(), CURRENT_USER_ID);
                    if (review != null) {
                        item.setRating(review.getRating());
                        item.setReview(review.getText());
                    } else {
                        // Если отзыва нет, используем рейтинг из самой ContentEntity
                        item.setRating(content.getRating());
                    }
                    
                    likedItems.add(item);
                    Log.d(TAG, "Добавлен прочий контент в избранное: " + content.getTitle() + " (категория: " + content.getCategory() + ")");
                }
            }
            
            // Обновляем адаптер
            adapter.setItems(likedItems);
            
            // Показываем сообщение, если список пуст
            if (likedItems.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyMessageTextView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Список избранного пуст");
                
                // Если список пуст, добавляем тестовые данные для демонстрации
                addTestLikedData();
                // И загружаем их снова
                loadLikedContent();
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessageTextView.setVisibility(View.GONE);
                Log.d(TAG, "Всего загружено элементов избранного: " + likedItems.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке избранного: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при загрузке избранного", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(ContentItem item) {
        // Переход к детальной информации о контенте
        DetailLikedContentFragment detailFragment = DetailLikedContentFragment.newInstance(item);
        
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
    
    /**
     * Добавляет тестовые данные для каждой категории в "избранное"
     * Это нужно для отладки, чтобы не было пустых экранов
     */
    private void addTestLikedData() {
        try {
            // Добавляем по одному тестовому элементу для каждой категории
            
            // 1. Добавляем тестовый фильм
            MovieEntity movie = new MovieEntity();
            movie.setId("test_movie_1");
            movie.setTitle("Тестовый фильм");
            movie.setDescription("Это тестовый фильм для отображения в избранном");
            movie.setImageUrl("url_to_image");
            movie.setCategory("Фильмы");
            movie.setContentType("movie");
            movie.setLiked(true); // Важно: помечаем как избранное
            movieRepository.insert(movie);
            Log.d(TAG, "Добавлен тестовый фильм в избранное: " + movie.getTitle());
            
            // 2. Добавляем тестовый сериал
            TVShowEntity tvShow = new TVShowEntity();
            tvShow.setId("test_tvshow_1");
            tvShow.setTitle("Тестовый сериал");
            tvShow.setDescription("Это тестовый сериал для отображения в избранном");
            tvShow.setImageUrl("url_to_image");
            tvShow.setCategory("Сериалы");
            tvShow.setContentType("tvshow");
            tvShow.setLiked(true); // Помечаем как избранное
            tvShowRepository.insert(tvShow);
            Log.d(TAG, "Добавлен тестовый сериал в избранное: " + tvShow.getTitle());
            
            // 3. Добавляем тестовую игру
            GameEntity game = new GameEntity();
            game.setId("test_game_1");
            game.setTitle("Тестовая игра");
            game.setDescription("Это тестовая игра для отображения в избранном");
            game.setImageUrl("url_to_image");
            game.setCategory("Игры");
            game.setContentType("game");
            game.setLiked(true); // Помечаем как избранное
            gameRepository.insert(game);
            Log.d(TAG, "Добавлена тестовая игра в избранное: " + game.getTitle());
            
            // 4. Добавляем тестовую книгу
            BookEntity book = new BookEntity();
            book.setId("test_book_1");
            book.setTitle("Тестовая книга");
            book.setDescription("Это тестовая книга для отображения в избранном");
            book.setImageUrl("url_to_image");
            book.setCategory("Книги");
            book.setContentType("book");
            book.setLiked(true); // Помечаем как избранное
            bookRepository.insert(book);
            Log.d(TAG, "Добавлена тестовая книга в избранное: " + book.getTitle());
            
            // 5. Добавляем тестовое аниме
            AnimeEntity anime = new AnimeEntity();
            anime.setId("test_anime_1");
            anime.setTitle("Тестовое аниме");
            anime.setDescription("Это тестовое аниме для отображения в избранном");
            anime.setImageUrl("url_to_image");
            anime.setCategory("Аниме");
            anime.setContentType("anime");
            anime.setLiked(true); // Помечаем как избранное
            animeRepository.insert(anime);
            Log.d(TAG, "Добавлено тестовое аниме в избранное: " + anime.getTitle());
            
            // 6. Добавляем тестовую музыку (через общую таблицу контента)
            ContentEntity music = new ContentEntity();
            music.setId("test_music_1");
            music.setTitle("Тестовая музыка");
            music.setDescription("Это тестовая музыка для отображения в избранном");
            music.setImageUrl("url_to_image");
            music.setCategory("Музыка");
            music.setContentType("music");
            music.setLiked(true); // Помечаем как избранное
            contentRepository.insert(music);
            Log.d(TAG, "Добавлена тестовая музыка в избранное: " + music.getTitle());
            
            // Также добавляем все элементы в общую таблицу контента для согласованности
            // Фильм
            ContentEntity movieContent = new ContentEntity();
            movieContent.setId(movie.getId());
            movieContent.setTitle(movie.getTitle());
            movieContent.setDescription(movie.getDescription());
            movieContent.setImageUrl(movie.getImageUrl());
            movieContent.setCategory(movie.getCategory());
            movieContent.setContentType(movie.getContentType());
            movieContent.setLiked(true);
            contentRepository.insert(movieContent);
            
            // Сериал
            ContentEntity tvShowContent = new ContentEntity();
            tvShowContent.setId(tvShow.getId());
            tvShowContent.setTitle(tvShow.getTitle());
            tvShowContent.setDescription(tvShow.getDescription());
            tvShowContent.setImageUrl(tvShow.getImageUrl());
            tvShowContent.setCategory(tvShow.getCategory());
            tvShowContent.setContentType(tvShow.getContentType());
            tvShowContent.setLiked(true);
            contentRepository.insert(tvShowContent);
            
            // Игра
            ContentEntity gameContent = new ContentEntity();
            gameContent.setId(game.getId());
            gameContent.setTitle(game.getTitle());
            gameContent.setDescription(game.getDescription());
            gameContent.setImageUrl(game.getImageUrl());
            gameContent.setCategory(game.getCategory());
            gameContent.setContentType(game.getContentType());
            gameContent.setLiked(true);
            contentRepository.insert(gameContent);
            
            // Книга
            ContentEntity bookContent = new ContentEntity();
            bookContent.setId(book.getId());
            bookContent.setTitle(book.getTitle());
            bookContent.setDescription(book.getDescription());
            bookContent.setImageUrl(book.getImageUrl());
            bookContent.setCategory(book.getCategory());
            bookContent.setContentType(book.getContentType());
            bookContent.setLiked(true);
            contentRepository.insert(bookContent);
            
            // Аниме
            ContentEntity animeContent = new ContentEntity();
            animeContent.setId(anime.getId());
            animeContent.setTitle(anime.getTitle());
            animeContent.setDescription(anime.getDescription());
            animeContent.setImageUrl(anime.getImageUrl());
            animeContent.setCategory(anime.getCategory());
            animeContent.setContentType(anime.getContentType());
            animeContent.setLiked(true);
            contentRepository.insert(animeContent);
            
            Log.d(TAG, "Все тестовые данные добавлены в избранное");
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении тестовых данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
}