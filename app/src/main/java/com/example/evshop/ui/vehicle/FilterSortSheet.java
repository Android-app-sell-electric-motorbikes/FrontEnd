// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/vehicle/FilterSortSheet.java

package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
// *** BƯỚC 5.2.1: THÊM IMPORT CHO RATINGBAR ***
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.evshop.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class FilterSortSheet extends BottomSheetDialogFragment {

    // *** BƯỚC 5.2.2: SỬA LẠI INTERFACE ĐỂ NHẬN THÊM RATING ***
    public interface FilterListener {
        void onFilterApplied(Long minPrice, Long maxPrice, Boolean sortByPriceAsc, Integer minRating);
    }

    private FilterListener listener;
    private TextInputEditText etMinPrice;
    private TextInputEditText etMaxPrice;
    private RadioGroup rgSort;
    // *** BƯỚC 5.2.3: KHAI BÁO RATINGBAR ***
    private RatingBar filterRatingBar;

    public static FilterSortSheet newInstance() {
        return new FilterSortSheet();
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dòng này không thay đổi
        return inflater.inflate(R.layout.sheet_filter_sort, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view đã có
        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        rgSort = view.findViewById(R.id.rgSort);
        Button btnApply = view.findViewById(R.id.btnApply);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // *** BƯỚC 5.2.4: ÁNH XẠ RATINGBAR ***
        filterRatingBar = view.findViewById(R.id.filter_rating_bar);

        btnApply.setOnClickListener(v -> {
            try {
                // 1. Lấy giá trị sắp xếp (Không đổi)
                Boolean sortByPriceAsc = null;
                int selectedSortId = rgSort.getCheckedRadioButtonId();
                if (selectedSortId == R.id.rbPriceAsc) {
                    sortByPriceAsc = true;
                } else if (selectedSortId == R.id.rbPriceDesc) {
                    sortByPriceAsc = false;
                }

                // 2. Lấy giá trị lọc giá (Không đổi)
                Long minPrice = null;
                Long maxPrice = null;
                String minPriceStr = etMinPrice.getText().toString();
                if (!TextUtils.isEmpty(minPriceStr)) {
                    minPrice = Long.parseLong(minPriceStr);
                }
                String maxPriceStr = etMaxPrice.getText().toString();
                if (!TextUtils.isEmpty(maxPriceStr)) {
                    maxPrice = Long.parseLong(maxPriceStr);
                }

                // *** BƯỚC 5.2.5: LẤY GIÁ TRỊ TỪ RATINGBAR ***
                int minRating = (int) filterRatingBar.getRating();

                // 3. Gửi kết quả (đã bao gồm rating) về Activity
                if (listener != null) {
                    listener.onFilterApplied(minPrice, maxPrice, sortByPriceAsc, minRating);
                }
                dismiss();
            } catch (NumberFormatException e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Giá trị nhập vào quá lớn!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi áp dụng bộ lọc: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
