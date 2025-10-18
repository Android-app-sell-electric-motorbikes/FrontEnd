package com.example.evshop.data;

import android.os.Handler;
import android.os.Looper;

import com.example.evshop.R;
import com.example.evshop.common.Callback;
import com.example.evshop.domain.models.Product;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class HomeRepository {
    private final Random random = new Random();


    @Inject
    public HomeRepository() {
    }


    public void loadPage(int page,
                         String category,
                         String query,
                         Filters filters,
                         Callback<List<Product>> cb) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (random.nextFloat() < 0.1f) {
                    cb.onError(new RuntimeException("Mock error"));
                    return;
                }
                List<Product> data = mockData(page, category, query, filters);
                boolean hasMore = page < 3; // 4 pages total
                cb.onSuccess(data, hasMore);
            } catch (Throwable t) {
                cb.onError(t);
            }
        }, 800);
    }


    private List<Product> mockData(int page, String category, String query, Filters filters) {
        List<Product> all = new ArrayList<>();
        String[] cats = {"Tất cả", "City", "Sport", "Off-road", "Eco"};
        String[] brands = {"VoltX", "EVM", "GreenGo", "Thunder", "EcoRide"};

        for (int i = 0; i < 12; i++) {
            Product p = new Product();
            p.setId("P" + page + "_" + i);
            p.setName((i % 2 == 0 ? "EV " : "Moto ") + (100 + page * 10 + i));
            p.setBrand(brands[i % brands.length]);
            p.setImageUrl(demoImage(i));
            p.setPriceVnd(12_000_000L + (long) (i * 1_500_000L) + page * 500_000L);
            p.setRating(3.0f + (i % 30) / 10f); // 3.0 .. 5.9
            p.setCategory(cats[(i % (cats.length - 1)) + 1]);
            all.add(p);
        }

// Filter by category
        if (category != null && !category.equals("Tất cả")) {
            all.removeIf(p -> !p.getCategory().equalsIgnoreCase(category));
        }
// Query
        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase(Locale.ROOT);
            all.removeIf(p -> !(p.getName().toLowerCase().contains(q) || p.getBrand().toLowerCase().contains(q)));
        }
// Filters
        if (filters != null) {
            if (filters.brands != null && !filters.brands.isEmpty()) {
                all.removeIf(p -> !filters.brands.contains(p.getBrand()));
            }
            all.removeIf(p -> p.getPriceVnd() > filters.maxPriceVnd);
            all.removeIf(p -> p.getRating() < filters.minRating);

            Filters.Sort sort = (filters.sort != null) ? filters.sort : Filters.Sort.POPULAR;
            all.sort((a, b) -> {
                switch (sort) {
                    case PRICE_ASC:
                        return Long.compare(a.getPriceVnd(), b.getPriceVnd());
                    case PRICE_DESC:
                        return Long.compare(b.getPriceVnd(), a.getPriceVnd());
                    case RATING:
                    return Float.compare(b.getRating(), a.getRating());
                    case POPULAR:
                    default:
                        return a.getId().compareTo(b.getId());
                }
            });
        }

        return all;
    }


    private int demoImage(int i) {
// royalty‑free demo images
        int[] ids = new int[]{
                R.drawable.ev_scooter,
                R.drawable.ev_scooter2,
                R.drawable.ev_scooter3,
                R.drawable.xe1
        };
        return ids[i % ids.length];
    }


    public static class Filters {
        public float minRating = 0f;
        public Sort sort = Sort.POPULAR;

        public enum Sort {POPULAR, PRICE_ASC, PRICE_DESC, RATING}

        public Set<String> brands = new HashSet<>();
        public long maxPriceVnd = 50_000_000L;
    }
}
