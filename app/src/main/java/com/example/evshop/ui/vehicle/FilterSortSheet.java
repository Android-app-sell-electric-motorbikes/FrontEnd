package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.evshop.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class FilterSortSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFilterApplied(Long minPrice, Long maxPrice, Boolean sortByPriceAsc);
    }

    private FilterListener listener;
    private TextInputEditText etMinPrice;
    private TextInputEditText etMaxPrice;
    private ChipGroup cgSort;

    public static FilterSortSheet newInstance() {
        return new FilterSortSheet();
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_filter_sort, container, false);

        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        cgSort = view.findViewById(R.id.cgSort);
        Button btnApply = view.findViewById(R.id.btnApply);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnApply.setOnClickListener(v -> {
            try {
                Boolean sortByPriceAsc = null;
                int selectedSortId = cgSort.getCheckedChipId();

                if (selectedSortId == R.id.chipPriceAsc) {
                    sortByPriceAsc = true;
                } else if (selectedSortId == R.id.chipPriceDesc) {
                    sortByPriceAsc = false;
                }

                Long minPrice = null;
                Long maxPrice = null;

                if (etMinPrice.getText() != null) {
                    String minPriceStr = etMinPrice.getText().toString();
                    if (!TextUtils.isEmpty(minPriceStr)) {
                        minPrice = Long.parseLong(minPriceStr);
                    }
                }

                if (etMaxPrice.getText() != null) {
                    String maxPriceStr = etMaxPrice.getText().toString();
                    if (!TextUtils.isEmpty(maxPriceStr)) {
                        maxPrice = Long.parseLong(maxPriceStr);
                    }
                }

                if (listener != null) {
                    listener.onFilterApplied(minPrice, maxPrice, sortByPriceAsc);
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

        return view;
    }
}
