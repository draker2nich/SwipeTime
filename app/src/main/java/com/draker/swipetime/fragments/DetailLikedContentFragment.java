package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.ReviewEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.ReviewRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.draker.swipetime.utils.LikedItemsHelper;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Фрагмент для детального просмотра понравившегося контента,
 * с возможностью отметки "просмотрено" и добавления рецензии
 */
public class DetailLikedContentFragment extends Fragment {

    private static final String TAG = "DetailLikedContentFragment";
    private static final String ARG_CONTENT_ITEM = "content_item";

    // UI компоненты
    private ImageView coverImageView;
    private TextView titleTextView;
    private TextView categoryTextView;
    private TextView descriptionTextView;
    private SwitchMaterial watchedSwitch;
    private TextView watchedLabel;
    private RatingBar ratingBar;
    private TextView ratingValueTextView;
    private TextInputEditText reviewEditText;
    private Button saveReviewButton;
    private Toolbar toolbar;

    // Данные
    private ContentItem contentItem;
    private ReviewEntity currentReview;

    // Репозитории
    private ReviewRepository reviewRepository;
    private UserRepository userRepository;
    private ContentRepository contentRepository;
    private MovieRepository movieRepository;
    private TVShowRepository tvShowRepository;
    private GameRepository gameRepository;
    private BookRepository bookRepository;
    private AnimeRepository animeRepository;

    // ID текущего пользователя (для демонстрации используем захардкоженный ID)
    private static final String CURRENT_USER_ID = "user_1";

    /**
     * Создает новый экземпляр фрагмента с переданными данными о контенте
     */
    public static DetailLikedContentFragment newInstance(ContentItem contentItem) {
        DetailLikedContentFragment fragment = new DetailLikedContentFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTENT_ITEM, contentItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contentItem = (ContentItem) getArguments().getSerializable(ARG_CONTENT_ITEM);
        }

        // Инициализация репозиториев
        reviewRepository = new ReviewRepository(requireActivity().getApplication());
        userRepository = new UserRepository(requireActivity().getApplication());
        contentRepository = new ContentRepository(requireActivity().getApplication());
        movieRepository = new MovieRepository(requireActivity().getApplication());
        tvShowRepository = new TVShowRepository(requireActivity().getApplication());
        gameRepository = new GameRepository(requireActivity().getApplication());
        bookRepository = new BookRepository(requireActivity().getApplication());
        animeRepository = new AnimeRepository(requireActivity().getApplication());

        // Проверяем, существует ли текущий пользователь, если нет - создаем демо пользователя
        UserEntity currentUser = userRepository.getUserById(CURRENT_USER_ID);
        if (currentUser == null) {
            currentUser = new UserEntity(CURRENT_USER_ID, "Demo User", "demo@example.com", null);
            userRepository.insert(currentUser);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_liked_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация UI компонентов
        initViews(view);
        
        // Настройка тулбара и кнопки назад
        setupToolbar();
        
        // Заполнение данных о контенте
        if (contentItem != null) {
            populateContentData();
            
            // Загрузка существующего отзыва для этого контента, если есть
            loadReviewData();
        }
        
        // Установка слушателей
        setupListeners();
    }

    /**
     * Инициализация UI компонентов
     */
    private void initViews(View view) {
        coverImageView = view.findViewById(R.id.cover_detail);
        titleTextView = view.findViewById(R.id.title_detail);
        categoryTextView = view.findViewById(R.id.category_detail);
        descriptionTextView = view.findViewById(R.id.description_detail);
        watchedSwitch = view.findViewById(R.id.watched_switch);
        watchedLabel = view.findViewById(R.id.watched_label);
        ratingBar = view.findViewById(R.id.rating_bar);
        ratingValueTextView = view.findViewById(R.id.rating_value);
        reviewEditText = view.findViewById(R.id.review_edit_text);
        saveReviewButton = view.findViewById(R.id.save_review_button);
        toolbar = view.findViewById(R.id.toolbar_detail);
    }

    /**
     * Настройка тулбара
     */
    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            // Возвращаемся назад при нажатии на кнопку
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    /**
     * Заполнение UI данными о контенте
     */
    private void populateContentData() {
        titleTextView.setText(contentItem.getTitle());
        categoryTextView.setText(contentItem.getCategory());
        descriptionTextView.setText(contentItem.getDescription());
        
        // Настройка изображения (в реальном приложении здесь будет загрузка изображения)
        coverImageView.setImageResource(R.drawable.placeholder_image);
        
        // Обновляем заголовок тулбара
        toolbar.setTitle(contentItem.getTitle());
        
        // Обновляем текст переключателя в зависимости от типа контента
        String switchLabel = LikedItemsHelper.getWatchedSwitchLabel(contentItem.getCategory(), requireContext());
        watchedLabel.setText(switchLabel);
    }

    /**
     * Загрузка данных отзыва, если он уже существует
     */
    private void loadReviewData() {
        try {
            // Пытаемся найти существующий отзыв для данного пользователя и контента
            currentReview = reviewRepository.getByContentAndUserId(contentItem.getId(), CURRENT_USER_ID);
            
            if (currentReview != null) {
                // Если отзыв найден, заполняем UI
                ratingBar.setRating(currentReview.getRating());
                updateRatingValueText(currentReview.getRating());
                reviewEditText.setText(currentReview.getText());

                // Определяем статус "просмотрено" в зависимости от категории контента
                if (contentItem.getCategory().equals("Фильмы") || contentItem.getCategory().equals("Сериалы") || 
                    contentItem.getCategory().equals("Аниме")) {
                    watchedSwitch.setChecked(true);
                } else if (contentItem.getCategory().equals("Книги")) {
                    watchedSwitch.setChecked(true);
                } else if (contentItem.getCategory().equals("Игры")) {
                    watchedSwitch.setChecked(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке отзыва: " + e.getMessage());
            Toast.makeText(getContext(), "Не удалось загрузить отзыв", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Установка слушателей для UI элементов
     */
    private void setupListeners() {
        // Обновление текстового значения при изменении рейтинга
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingValueText(rating);
        });
        
        // Сохранение отзыва при нажатии на кнопку
        saveReviewButton.setOnClickListener(v -> saveReview());
    }

    /**
     * Обновление текстового представления рейтинга
     */
    private void updateRatingValueText(float rating) {
        ratingValueTextView.setText(String.format("%.1f", rating));
    }

    /**
     * Сохранение отзыва в базу данных
     */
    private void saveReview() {
        try {
            float rating = ratingBar.getRating();
            String reviewText = reviewEditText.getText() != null ? reviewEditText.getText().toString() : "";
            boolean isWatched = watchedSwitch.isChecked();
            
            // Создаем новый отзыв или обновляем существующий
            if (currentReview == null) {
                currentReview = new ReviewEntity(
                    CURRENT_USER_ID,
                    contentItem.getId(),
                    rating,
                    reviewText,
                    contentItem.getCategory()
                );
                // Сохраняем новый отзыв
                long reviewId = reviewRepository.insert(currentReview);
                currentReview.setId(reviewId);
            } else {
                // Обновляем существующий отзыв
                currentReview.setRating(rating);
                currentReview.setText(reviewText);
                reviewRepository.update(currentReview);
            }
            
            // Обновляем статус "просмотрено/прочитано" в соответствующем репозитории
            updateWatchedStatus(isWatched);
            
            // Начисляем опыт пользователю за отзыв
            addExperienceForReview();
            
            Toast.makeText(getContext(), "Отзыв сохранен", Toast.LENGTH_SHORT).show();
            
            // Возвращаемся к списку понравившегося
            requireActivity().getSupportFragmentManager().popBackStack();
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении отзыва: " + e.getMessage());
            Toast.makeText(getContext(), "Не удалось сохранить отзыв", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Обновление статуса "просмотрено/прочитано" в соответствующем репозитории
     */
    private void updateWatchedStatus(boolean isWatched) {
        try {
            String contentType = contentItem.getCategory();
            String contentId = contentItem.getId();
            
            // В зависимости от типа контента используем соответствующий репозиторий
            switch (contentType) {
                case "Фильмы":
                    movieRepository.updateWatchedStatus(contentId, isWatched);
                    break;
                case "Сериалы":
                    tvShowRepository.updateWatchedStatus(contentId, isWatched);
                    break;
                case "Игры":
                    gameRepository.updateCompletedStatus(contentId, isWatched);
                    break;
                case "Книги":
                    bookRepository.updateReadStatus(contentId, isWatched);
                    break;
                case "Аниме":
                    animeRepository.updateWatchedStatus(contentId, isWatched);
                    break;
                default:
                    contentRepository.updateWatchedStatus(contentId, isWatched);
                    break;
            }
            
            // Если статус изменен на "просмотрено/прочитано", начисляем опыт
            if (isWatched) {
                addExperienceForCompletion();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении статуса: " + e.getMessage());
        }
    }

    /**
     * Начисление опыта пользователю за рецензию
     */
    private void addExperienceForReview() {
        try {
            UserEntity user = userRepository.getUserById(CURRENT_USER_ID);
            if (user != null) {
                // Начисляем 15 опыта за отзыв
                boolean levelUp = user.addExperience(15);
                userRepository.update(user);
                
                if (levelUp) {
                    Toast.makeText(getContext(), "Поздравляем! Вы достигли уровня " + user.getLevel(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при начислении опыта за отзыв: " + e.getMessage());
        }
    }

    /**
     * Начисление опыта пользователю за просмотр/прочтение
     */
    private void addExperienceForCompletion() {
        try {
            UserEntity user = userRepository.getUserById(CURRENT_USER_ID);
            if (user != null) {
                // Начисляем 30 опыта за просмотр/прочтение
                boolean levelUp = user.addExperience(30);
                userRepository.update(user);
                
                if (levelUp) {
                    Toast.makeText(getContext(), "Поздравляем! Вы достигли уровня " + user.getLevel(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при начислении опыта за просмотр: " + e.getMessage());
        }
    }
}
