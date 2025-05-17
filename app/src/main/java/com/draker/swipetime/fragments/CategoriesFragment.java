package com.draker.swipetime.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.draker.swipetime.R;
import com.draker.swipetime.adapters.CategoryAdapter;
import com.draker.swipetime.models.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.categories_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Инициализация списка категорий
        List<Category> categories = createCategoryList();
        
        // Создание и установка адаптера с обработчиком кликов
        adapter = new CategoryAdapter(getContext(), categories, this);
        recyclerView.setAdapter(adapter);
    }

    // Метод для создания тестового списка категорий
    private List<Category> createCategoryList() {
        List<Category> categories = new ArrayList<>();
        
        categories.add(new Category("Фильмы", R.drawable.ic_category_movies));
        categories.add(new Category("Сериалы", R.drawable.ic_category_tv_shows));
        categories.add(new Category("Игры", R.drawable.ic_category_games));
        categories.add(new Category("Книги", R.drawable.ic_category_books));
        categories.add(new Category("Аниме", R.drawable.ic_category_anime));
        
        return categories;
    }
    
    @Override
    public void onCategoryClick(Category category) {
        // Переход к фрагменту с карточками при клике на категорию
        CardStackFragment cardStackFragment = CardStackFragment.newInstance(category.getName());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, cardStackFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
