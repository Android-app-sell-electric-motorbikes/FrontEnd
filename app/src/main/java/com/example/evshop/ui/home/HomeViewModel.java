package com.example.evshop.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.common.Callback;
import com.example.evshop.data.HomeRepository;
import com.example.evshop.data.HomeRepository.Filters;
import com.example.evshop.domain.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private final HomeRepository repo;

    private final MutableLiveData<List<Product>> _items = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<Product>> items = _items;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> error   = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);

    private int page = 0;
    private String category = "Tất cả";
    private String query = "";
    public Filters filters = new Filters();

    @Inject
    public HomeViewModel(HomeRepository repo) {
        this.repo = repo;
    }

    public void refresh() {
        page = 0;
        load(true);
    }

    public void setCategory(String cat) {
        this.category = cat;
        refresh();
    }

    public void setQuery(String q) {
        this.query = q;
        refresh();
    }

    public void applyFilters(Filters f) {
        this.filters = (f != null) ? f : new Filters();
        refresh();
    }

    public void loadMore() {
        if (Boolean.FALSE.equals(hasMore.getValue())) return;
        page++;
        load(false);
    }

    private void load(boolean replace) {
        loading.setValue(true);
        error.setValue(false);

        final int p = replace ? 0 : page;

        repo.loadPage(p, category, query, filters, new Callback<List<Product>>() { // 👈 dùng generic
            @Override
            public void onSuccess(List<Product> data, boolean more) {              // 👈 chỉ 1 hàm này
                loading.postValue(false);
                hasMore.postValue(more);

                if (replace) {
                    _items.postValue(data);
                } else {
                    List<Product> cur = new ArrayList<>(Objects.requireNonNullElse(_items.getValue(), new ArrayList<>()));
                    cur.addAll(data);
                    _items.postValue(cur);
                }
            }

            @Override
            public void onError(Throwable t) {
                loading.postValue(false);
                error.postValue(true);
            }
        });
    }
}