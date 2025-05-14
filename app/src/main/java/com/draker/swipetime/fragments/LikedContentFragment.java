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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.draker.swipetime.R;
import com.draker.swipetime.adapters.LikedContentAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class LikedContentFragment extends Fragment implements LikedContentAdapter.OnItemClickListener {

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
        
        // Инициализация UI компонентов
        recyclerView = view.findViewById(R.id.liked_content_recycler_view);
        emptyMessageTextView = view.findViewById(R.id.message_empty_liked);
        
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
            
            // Добавляем избранные фильмы
            List<MovieEntity> likedMovies = movieRepository.getLiked();
            for (MovieEntity movie : likedMovies) {
                likedItems.add(new ContentItem(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDescription(),
                    movie.getImageUrl(),
                    movie.getCategory()
                ));
                Log.d("LikedContentFragment", "Добавлен фильм в избранное: " + movie.getTitle());
            }
            
            // Добавляем избранные сериалы
            List<TVShowEntity> likedTVShows = tvShowRepository.getLiked();
            for (TVShowEntity tvShow : likedTVShows) {
                likedItems.add(new ContentItem(
                    tvShow.getId(),
                    tvShow.getTitle(),
                    tvShow.getDescription(),
                    tvShow.getImageUrl(),
                    tvShow.getCategory()
                ));
                Log.d("LikedContentFragment", "Добавлен сериал в избранное: " + tvShow.getTitle());
            }
            
            // Добавляем избранные игры
            List<GameEntity> likedGames = gameRepository.getLiked();
            for (GameEntity game : likedGames) {
                likedItems.add(new ContentItem(
                    game.getId(),
                    game.getTitle(),
                    game.getDescription(),
                    game.getImageUrl(),
                    game.getCategory()
                ));
                Log.d("LikedContentFragment", "Добавлена игра в избранное: " + game.getTitle());
            }
            
            // Добавляем избранные книги
            List<BookEntity> likedBooks = bookRepository.getLiked();
            for (BookEntity book : likedBooks) {
                likedItems.add(new ContentItem(
                    book.getId(),
                    book.getTitle(),
                    book.getDescription(),
                    book.getImageUrl(),
                    book.getCategory()
                ));
                Log.d("LikedContentFragment", "Добавлена книга в избранное: " + book.getTitle());
            }
            
            // Добавляем избранное аниме
            List<AnimeEntity> likedAnimes = animeRepository.getLiked();
            for (AnimeEntity anime : likedAnimes) {
                likedItems.add(new ContentItem(
                    anime.getId(),
                    anime.getTitle(),
                    anime.getDescription(),
                    anime.getImageUrl(),
                    anime.getCategory()
                ));
                Log.d("LikedContentFragment", "Добавлено аниме в избранное: " + anime.getTitle());
            }
            
            // Добавляем прочий избранный контент
            List<ContentEntity> likedContent = contentRepository.getLiked();
            for (ContentEntity content : likedContent) {
                // Проверяем, не добавлен ли уже этот контент из специализированного репозитория
                boolean alreadyAdded = false;
                for (ContentItem item : likedItems) {
                    if (item.getId().equals(content.getId())) {
                        alreadyAdded = true;
                        break;
                    }
                }
                
                if (!alreadyAdded) {
                    likedItems.add(new ContentItem(
                        content.getId(),
                        content.getTitle(),
                        content.getDescription(),
                        content.getImageUrl(),
                        content.getCategory()
                    ));
                    Log.d("LikedContentFragment", "Добавлен прочий контент в избранное: " + content.getTitle());
                }
            }
            
            // Обновляем адаптер
            adapter.setItems(likedItems);
            
            // Показываем сообщение, если список пуст
            if (likedItems.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyMessageTextView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessageTextView.setVisibility(View.GONE);
                Log.d("LikedContentFragment", "Всего загружено элементов избранного: " + likedItems.size());
            }
        } catch (Exception e) {
            Log.e("LikedContentFragment", "Ошибка при загрузке избранного: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при загрузке избранного", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(ContentItem item) {
        // Обработка клика по элементу избранного
        // В будущем здесь можно добавить переход к детальной информации о контенте
        Toast.makeText(getContext(), "Выбрано: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
