package com.example.evshop.data;

import android.os.Handler;
import android.os.Looper;

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
            p.id = "P" + page + "_" + i;
            p.name = (i % 2 == 0 ? "EV " : "Moto ") + (100 + page * 10 + i);
            p.brand = brands[i % brands.length];
            p.imageUrl = demoImage(i);
            p.priceVnd = 12_000_000L + (long) (i * 1_500_000L) + page * 500_000L;
            p.rating = 3.0f + (i % 30) / 10f; // 3.0 .. 5.9
            p.category = cats[(i % (cats.length - 1)) + 1];
            all.add(p);
        }
// Filter by category
        if (category != null && !category.equals("Tất cả")) {
            all.removeIf(p -> !p.category.equalsIgnoreCase(category));
        }
// Query
        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase(Locale.ROOT);
            all.removeIf(p -> !(p.name.toLowerCase().contains(q) || p.brand.toLowerCase().contains(q)));
        }
// Filters
        if (filters != null) {
            if (filters.brands != null && !filters.brands.isEmpty()) {
                all.removeIf(p -> !filters.brands.contains(p.brand));
            }
            all.removeIf(p -> p.priceVnd > filters.maxPriceVnd);
            all.removeIf(p -> p.rating < filters.minRating);

            Filters.Sort sort = (filters.sort != null) ? filters.sort : Filters.Sort.POPULAR;
            all.sort((a, b) -> {
                switch (sort) {
                    case PRICE_ASC:
                        return Long.compare(a.priceVnd, b.priceVnd);
                    case PRICE_DESC:
                        return Long.compare(b.priceVnd, a.priceVnd);
                    case RATING:
                        return Float.compare(b.rating, a.rating);
                    case POPULAR:
                    default:
                        return a.id.compareTo(b.id);
                }
            });
        }

        return all;
    }


    private String demoImage(int i) {
// royalty‑free demo images
        String[] urls = new String[]{
                "https://images.unsplash.com/photo-1542367597-8849eb227ebb",
                "https://images.unsplash.com/photo-1516116216624-53e697fedbea",
                "https://images.unsplash.com/photo-1519648023493-d82b5f8d7fd9",
                "https://images.unsplash.com/photo-1518779578993-ec3579fee39f"
        };
        return urls[i % urls.length] + "?auto=format&fit=crop&w=800&q=60";
    }


    public static class Filters {
        public float minRating = 0f;
        public Sort sort = Sort.POPULAR;

        public enum Sort {POPULAR, PRICE_ASC, PRICE_DESC, RATING}

        public Set<String> brands = new HashSet<>();
        public long maxPriceVnd = 50_000_000L;
    }
}
