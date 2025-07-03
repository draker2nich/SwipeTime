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
import com.draker.swipetime.utils.ActionLogger;
// import com.draker.swipetime.utils.GamificationIntegrator; // Класс удален в рамках рефакторинга
import com.draker.swipetime.utils.GamificationManager;
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
    
    // Менеджер геймификации
    private GamificationManager gamificationManager;

    // ID текущего пользователя
    private String currentUserId;

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

        // Получаем ID текущего пользователя
        currentUserId = "user_1"; // Заглушка вместо удаленного GamificationIntegrator.getCurrentUserId(requireContext());
        Log.d(TAG, "Используется ID пользователя: " + currentUserId);

        // Инициализация репозиториев
        reviewRepository = new ReviewRepository(requireActivity().getApplication());
        userRepository = new UserRepository(requireActivity().getApplication());
        contentRepository = new ContentRepository(requireActivity().getApplication());
        movieRepository = new MovieRepository(requireActivity().getApplication());
        tvShowRepository = new TVShowRepository(requireActivity().getApplication());
        gameRepository = new GameRepository(requireActivity().getApplication());
        bookRepository = new BookRepository(requireActivity().getApplication());
        animeRepository = new AnimeRepository(requireActivity().getApplication());
        
        // Инициализация менеджера геймификации
        gamificationManager = GamificationManager.getInstance(requireActivity().getApplication());

        // Проверяем, существует ли текущий пользователь, если нет - создаем демо пользователя
        UserEntity currentUser = userRepository.getUserById(currentUserId);
        if (currentUser == null) {
            currentUser = new UserEntity(currentUserId, "Demo User", "demo@example.com", null);
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
        
        // Проверка ориентации экрана и корректировка UI для ландшафтной ориентации
        adjustUIForScreenOrientation();
        
        // Установка слушателей
        setupListeners();
    }
    
    /**
     * Корректирует UI для различных ориентаций экрана
     */
    private void adjustUIForScreenOrientation() {
        if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            // В ландшафтной ориентации изменяем размер изображения
            ViewGroup.LayoutParams params = coverImageView.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(R.dimen.cover_height_landscape);
            coverImageView.setLayoutParams(params);
        }
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
        
        // Находим рейтинг бар внутри его родительской карточки
        View ratingCardView = view.findViewById(R.id.rating_card);
        if (ratingCardView != null) {
            ratingBar = ratingCardView.findViewById(R.id.rating_bar);
        } else {
            // Если не нашли карточку, попробуем найти рейтинг бар напрямую
            ratingBar = view.findViewById(R.id.rating_bar);
            Log.w(TAG, "Не найдена rating_card, пытаемся найти rating_bar напрямую");
        }
        
        if (ratingBar == null) {
            Log.e(TAG, "ОШИБКА: Не удалось найти рейтинг бар!");
        }
        
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
        
        // Используем GlideUtils для загрузки изображения
        try {
            if (contentItem.getImageUrl() != null && !contentItem.getImageUrl().isEmpty()) {
                com.draker.swipetime.utils.GlideUtils.loadDetailContentImage(
                    requireContext(), 
                    contentItem.getImageUrl(), 
                    coverImageView,
                    contentItem.getCategory()
                );
                Log.d(TAG, "Загружено изображение: " + contentItem.getImageUrl());
            } else {
                // Если нет URL изображения, используем placeholder
                coverImageView.setImageResource(R.drawable.placeholder_image);
                Log.d(TAG, "Установлено placeholder-изображение, так как URL отсутствует");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке изображения: " + e.getMessage());
            // Используем placeholder в случае ошибки
            coverImageView.setImageResource(R.drawable.placeholder_image);
        }
        
        // Обновляем заголовок тулбара
        toolbar.setTitle(contentItem.getTitle());
        
        // Обновляем текст переключателя в зависимости от типа контента
        String switchLabel = LikedItemsHelper.getWatchedSwitchLabel(contentItem.getCategory(), requireContext());
        watchedLabel.setText(switchLabel);
        
        // Устанавливаем начальное состояние переключателя
        watchedSwitch.setChecked(contentItem.isWatched());
        
        // Если есть рейтинг, устанавливаем его
        if (contentItem.getRating() > 0) {
            float rating = contentItem.getRating();
            // Если рейтинг по 10-балльной шкале, преобразуем в 5-балльную для UI
            if (rating > 5) {
                rating = rating / 2;
            }
            ratingBar.setRating(rating);
            updateRatingValueText(rating);
        }
        
        // Если есть отзыв, устанавливаем его
        if (contentItem.getReview() != null && !contentItem.getReview().isEmpty()) {
            reviewEditText.setText(contentItem.getReview());
        }
    }

    /**
     * Загрузка данных отзыва, если он уже существует
     */
    private void loadReviewData() {
        try {
            // Пытаемся найти существующий отзыв для данного пользователя и контента
            currentReview = reviewRepository.getByContentAndUserId(contentItem.getId(), currentUserId);
            
            if (currentReview != null) {
                // Если отзыв найден, заполняем UI
                // Преобразуем рейтинг из 10-балльной шкалы в 5-балльную, если он больше 5
                float displayRating = currentReview.getRating();
                if (displayRating > 5) {
                    displayRating = displayRating / 2;
                }
                ratingBar.setRating(displayRating);
                updateRatingValueText(displayRating);
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
        // Обновление текстового представления рейтинга
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
        // Преобразуем обратно в 10-балльную шкалу для отображения
        float displayRating = rating;
        if (rating <= 5) {
            displayRating = rating * 2;
        }
        ratingValueTextView.setText(String.format("%.1f", displayRating));
    }

    /**
     * Сохранение отзыва в базу данных
     */
    private void saveReview() {
        try {
            float rating = ratingBar.getRating();
            // Преобразуем рейтинг из 5-балльной шкалы в 10-балльную для сохранения в базе
            float databaseRating = rating * 2; // Рейтинг для сохранения в базе данных (10-балльная шкала)
            
            String reviewText = reviewEditText.getText() != null ? reviewEditText.getText().toString() : "";
            boolean isWatched = watchedSwitch.isChecked();
            
            // Логируем действия пользователя
            ActionLogger.logRating(contentItem.getId(), contentItem.getTitle(), databaseRating);
            
            if (!reviewText.isEmpty()) {
                ActionLogger.logReview(contentItem.getId(), contentItem.getTitle());
            }
            
            if (isWatched) {
                ActionLogger.logCompleted(contentItem.getId(), contentItem.getTitle(), contentItem.getCategory());
            }
            
            // Начисляем опыт за оценку, если рейтинг > 0
            if (rating > 0) {
                addExperienceForRating(databaseRating);
            }
            
            // Начисляем опыт за рецензию, если она не пустая
            if (!reviewText.isEmpty()) {
                addExperienceForReview();
            }
            
            // Создаем новый отзыв или обновляем существующий
            if (currentReview == null) {
                currentReview = new ReviewEntity(
                    currentUserId,
                    contentItem.getId(),
                    databaseRating,
                    reviewText,
                    contentItem.getCategory()
                );
                // Сохраняем новый отзыв
                long reviewId = reviewRepository.insert(currentReview);
                currentReview.setId(reviewId);
            } else {
                // Обновляем существующий отзыв
                currentReview.setRating(databaseRating);
                currentReview.setText(reviewText);
                reviewRepository.update(currentReview);
            }
            
            // Обновляем статус "просмотрено/прочитано" в соответствующем репозитории
            updateWatchedStatus(isWatched);
            
            // Начисляем опыт за просмотр/прочтение, если переключатель включен
            if (isWatched) {
                addExperienceForCompletion();
            }
            
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
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении статуса: " + e.getMessage());
        }
    }

    /**
     * Начисление опыта пользователю за оценку
     * 
     * @param rating оценка (от 0 до 5)
     */
    private void addExperienceForRating(float rating) {
        try {
            // Используем GamificationIntegrator для начисления опыта (временно отключено)
            // boolean levelUp = GamificationIntegrator.registerRating(getContext(), contentItem.getId(), contentItem.getTitle(), rating);
            boolean levelUp = false; // Заглушка
            
            if (levelUp) {
                Toast.makeText(getContext(), "Поздравляем! Вы повысили свой уровень!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при начислении опыта за оценку: " + e.getMessage());
        }
    }

    /**
     * Начисление опыта пользователю за рецензию
     */
    private void addExperienceForReview() {
        try {
            // Используем GamificationIntegrator для начисления опыта (временно отключено)
            // boolean levelUp = GamificationIntegrator.registerReview(getContext(), contentItem.getId(), contentItem.getTitle());
            boolean levelUp = false; // Заглушка
            
            if (levelUp) {
                Toast.makeText(getContext(), "Поздравляем! Вы повысили свой уровень!", Toast.LENGTH_SHORT).show();
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
            // Используем GamificationIntegrator для начисления опыта (временно отключено)
            // boolean levelUp = GamificationIntegrator.registerCompletion(
            //     getContext(), 
            //     contentItem.getId(), 
            //     contentItem.getTitle(),
            //     contentItem.getCategory()
            // );
            boolean levelUp = false; // Заглушка
            
            if (levelUp) {
                Toast.makeText(getContext(), "Поздравляем! Вы повысили свой уровень!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при начислении опыта за просмотр: " + e.getMessage());
        }
    }
}
