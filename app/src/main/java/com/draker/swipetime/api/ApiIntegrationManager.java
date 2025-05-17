package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.fragments.CardStackFragmentHelper;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;

/**
 * Менеджер интеграции внешних API в приложение
 */
public class ApiIntegrationManager {
    private static final String TAG = "ApiIntegrationManager";
    private static ApiIntegrationManager instance;
    
    private final Application application;
    private final ApiManager apiManager;
    private final MovieRepository movieRepository;
    private final TVShowRepository tvShowRepository;
    private final GameRepository gameRepository;
    private final BookRepository bookRepository;
    private final AnimeRepository animeRepository;
    private final ContentRepository contentRepository;
    
    /**
     * Интерфейс для обратного вызова при инициализации API
     */
    public interface ApiInitCallback {
        void onComplete(boolean success);
        void onError(String errorMessage);
    }
    
    /**
     * Получить экземпляр менеджера интеграции API
     * @param application Application
     * @return экземпляр ApiIntegrationManager
     */
    public static synchronized ApiIntegrationManager getInstance(Application application) {
        if (instance == null) {
            instance = new ApiIntegrationManager(application);
        }
        return instance;
    }
    
    /**
     * Конструктор
     * @param application Application
     */
    private ApiIntegrationManager(Application application) {
        this.application = application;
        this.apiManager = new ApiManager(application);
        this.movieRepository = new MovieRepository(application);
        this.tvShowRepository = new TVShowRepository(application);
        this.gameRepository = new GameRepository(application);
        this.bookRepository = new BookRepository(application);
        this.animeRepository = new AnimeRepository(application);
        this.contentRepository = new ContentRepository(application);
    }
    
    /**
     * Инициализировать интеграцию внешних API
     * @param callback обратный вызов по завершении инициализации
     */
    public void initializeApiIntegration(ApiInitCallback callback) {
        Log.d(TAG, "Начало инициализации интеграции API");
        
        // Проверяем, есть ли уже данные в базе данных
        boolean hasMovies = movieRepository.getCount() >= 10;
        boolean hasTVShows = tvShowRepository.getCount() >= 10;
        boolean hasGames = gameRepository.getCount() >= 10;
        boolean hasBooks = bookRepository.getCount() >= 10;
        boolean hasAnime = animeRepository.getCount() >= 10;
        
        if (hasMovies && hasTVShows && hasGames && hasBooks && hasAnime) {
            Log.d(TAG, "Все категории контента уже загружены в базу данных");
            callback.onComplete(true);
            return;
        }
        
        Log.d(TAG, "Необходимо загрузить данные для следующих категорий: " + 
                (!hasMovies ? "Фильмы " : "") + 
                (!hasTVShows ? "Сериалы " : "") + 
                (!hasGames ? "Игры " : "") + 
                (!hasBooks ? "Книги " : "") + 
                (!hasAnime ? "Аниме" : ""));
        
        // Последовательно загружаем данные для каждой категории
        loadApiDataForCategory("Фильмы", hasMovies, new CardStackFragmentHelper.ApiLoadCallback() {
            @Override
            public void onComplete(boolean success) {
                loadApiDataForCategory("Сериалы", hasTVShows, new CardStackFragmentHelper.ApiLoadCallback() {
                    @Override
                    public void onComplete(boolean success) {
                        loadApiDataForCategory("Игры", hasGames, new CardStackFragmentHelper.ApiLoadCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                Log.d(TAG, "Завершена инициализация интеграции API");
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                Log.e(TAG, "Ошибка при загрузке данных аниме: " + errorMessage);
                                                callback.onComplete(true); // Всё равно считаем успешным, т.к. не критично
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Log.e(TAG, "Ошибка при загрузке данных книг: " + errorMessage);
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "Ошибка при загрузке данных игр: " + errorMessage);
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Ошибка при загрузке данных сериалов: " + errorMessage);
                        loadApiDataForCategory("Игры", hasGames, new CardStackFragmentHelper.ApiLoadCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке данных фильмов: " + errorMessage);
                loadApiDataForCategory("Сериалы", hasTVShows, new CardStackFragmentHelper.ApiLoadCallback() {
                    @Override
                    public void onComplete(boolean success) {
                        loadApiDataForCategory("Игры", hasGames, new CardStackFragmentHelper.ApiLoadCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadApiDataForCategory("Игры", hasGames, new CardStackFragmentHelper.ApiLoadCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                loadApiDataForCategory("Книги", hasBooks, new CardStackFragmentHelper.ApiLoadCallback() {
                                    @Override
                                    public void onComplete(boolean success) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        loadApiDataForCategory("Аниме", hasAnime, new CardStackFragmentHelper.ApiLoadCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                callback.onComplete(true);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                callback.onComplete(true);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Загрузить данные для указанной категории
     * @param categoryName название категории
     * @param skipIfLoaded пропустить загрузку, если данные уже загружены
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadApiDataForCategory(String categoryName, boolean skipIfLoaded, CardStackFragmentHelper.ApiLoadCallback callback) {
        if (skipIfLoaded) {
            Log.d(TAG, "Пропуск загрузки данных для категории " + categoryName + ", т.к. данные уже загружены");
            callback.onComplete(true);
            return;
        }
        
        Log.d(TAG, "Начало загрузки данных для категории " + categoryName);
        
        CardStackFragmentHelper.loadApiDataForCategory(
                categoryName,
                apiManager,
                movieRepository,
                tvShowRepository,
                gameRepository,
                bookRepository,
                animeRepository,
                callback
        );
    }
    
    /**
     * Поиск контента по запросу для указанной категории
     * @param query поисковый запрос
     * @param categoryName название категории
     * @param callback обратный вызов по завершении поиска
     */
    public void searchContentForCategory(String query, String categoryName, CardStackFragmentHelper.ApiLoadCallback callback) {
        Log.d(TAG, "Поиск контента по запросу: " + query + " для категории: " + categoryName);
        
        CardStackFragmentHelper.searchContentForCategory(
                query,
                categoryName,
                apiManager,
                callback
        );
    }
}