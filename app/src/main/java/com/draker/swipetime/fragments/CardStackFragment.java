package com.draker.swipetime.fragments;

import android.os.Bundle;
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
import com.draker.swipetime.models.ContentItem;
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

        // Инициализация адаптера с тестовыми данными
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

    // Создание тестовых данных для CardStack
    private List<ContentItem> createTestItems() {
        List<ContentItem> items = new ArrayList<>();
        
        if (categoryName.equals("Фильмы")) {
            items.add(new ContentItem("1", "Начало", "Вор, который крадет корпоративные секреты через использование технологии разделения сна, получает задание внедрить идею в сознание директора компании.", "url_to_image", categoryName));
            items.add(new ContentItem("2", "Интерстеллар", "Группа исследователей путешествует через червоточину в поисках новой планеты для человечества.", "url_to_image", categoryName));
            items.add(new ContentItem("3", "Матрица", "Хакер узнает от таинственных повстанцев о фальшивой природе его реальности и о его роли в войне против её создателей.", "url_to_image", categoryName));
            items.add(new ContentItem("4", "Бойцовский клуб", "Страдающий от бессонницы офисный работник и харизматичный торговец мылом основывают подпольный бойцовский клуб.", "url_to_image", categoryName));
            items.add(new ContentItem("5", "Форрест Гамп", "История жизни простого человека с IQ 75, который невольно стал участником многих исторических событий.", "url_to_image", categoryName));
        } else if (categoryName.equals("Сериалы")) {
            items.add(new ContentItem("6", "Игра престолов", "Несколько знатных семей ведут борьбу за Железный трон, в то время как древняя угроза возвращается после тысячелетнего сна.", "url_to_image", categoryName));
            items.add(new ContentItem("7", "Во все тяжкие", "Учитель химии, у которого диагностировали неоперабельный рак легких, начинает производить метамфетамин, чтобы обеспечить будущее своей семьи.", "url_to_image", categoryName));
            items.add(new ContentItem("8", "Чернобыль", "В апреле 1986 года взрыв на Чернобыльской АЭС стал одной из самых страшных ядерных катастроф в истории человечества.", "url_to_image", categoryName));
            items.add(new ContentItem("9", "Шерлок", "Современная адаптация произведений сэра Артура Конан Дойла о детективе Шерлоке Холмсе и его напарнике докторе Ватсоне.", "url_to_image", categoryName));
            items.add(new ContentItem("10", "Мандалорец", "Одинокий охотник за головами путешествует по дальним уголкам галактики, вдали от власти Новой Республики.", "url_to_image", categoryName));
        } else {
            // Для всех остальных категорий добавляем базовые элементы
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
                // Здесь можно добавить код для сохранения элемента в базу данных
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
