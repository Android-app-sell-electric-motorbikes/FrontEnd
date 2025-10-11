package com.example.evshop.domain.filters;

import java.util.HashSet;
import java.util.Set;

public final class ProductFilters {
    public enum Sort { POPULAR, PRICE_ASC, PRICE_DESC, RATING }

    public final float minRating;
    public final Set<String> brands;
    public final long maxPriceVnd;
    public final Sort sort;

    private ProductFilters(float minRating, Set<String> brands, long maxPriceVnd, Sort sort) {
        this.minRating = minRating;
        this.brands = brands;
        this.maxPriceVnd = maxPriceVnd;
        this.sort = sort;
    }

    public static class Builder {
        private float minRating = 0f;
        private Set<String> brands = new HashSet<>();
        private long maxPriceVnd = 50_000_000L;
        private Sort sort = Sort.POPULAR;

        public Builder minRating(float v) { this.minRating = v; return this; }
        public Builder addBrand(String b) { this.brands.add(b); return this; }
        public Builder brands(Set<String> bs) { this.brands = new HashSet<>(bs); return this; }
        public Builder maxPrice(long v) { this.maxPriceVnd = v; return this; }
        public Builder sort(Sort s) { this.sort = s; return this; }

        public ProductFilters build() {
            return new ProductFilters(minRating, new HashSet<>(brands), maxPriceVnd, sort);
        }
    }
}
