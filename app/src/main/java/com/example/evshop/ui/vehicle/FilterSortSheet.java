package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.evshop.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class FilterSortSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFilterApplied(Long minPrice, Long maxPrice, Boolean sortByPriceAsc);
    }

    private FilterListener listener;
    // Bỏ RangeSlider, thêm 2 TextInputEditText
    private TextInputEditText etMinPrice;
    private TextInputEditText etMaxPrice;
    private RadioGroup rgSort;

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

        // Ánh xạ các view mới
        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        rgSort = view.findViewById(R.id.rgSort);
        Button btnApply = view.findViewById(R.id.btnApply);
        Button btnCancel = view.findViewById(R.id.btnCancel);

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

                // ========================================================
                // ***      ĐỌC DỮ LIỆU TỪ HAI Ô NHẬP LIỆU             ***
                // ========================================================
                Long minPrice = null;
                Long maxPrice = null;

                // Lấy chuỗi từ ô minPrice, nếu không rỗng thì chuyển thành Long
                String minPriceStr = etMinPrice.getText().toString();
                if (!TextUtils.isEmpty(minPriceStr)) {
                    minPrice = Long.parseLong(minPriceStr);
                }

                // Lấy chuỗi từ ô maxPrice, nếu không rỗng thì chuyển thành Long
                String maxPriceStr = etMaxPrice.getText().toString();
                if (!TextUtils.isEmpty(maxPriceStr)) {
                    maxPrice = Long.parseLong(maxPriceStr);
                }

                // 3. Gửi kết quả về Activity (Không đổi)
                if (listener != null) {
                    listener.onFilterApplied(minPrice, maxPrice, sortByPriceAsc);
                }
                dismiss(); // Đóng BottomSheet
            } catch (NumberFormatException e) {
                // Bắt lỗi nếu người dùng nhập số quá lớn
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Giá trị nhập vào quá lớn!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                // Bắt các lỗi chung khác
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
