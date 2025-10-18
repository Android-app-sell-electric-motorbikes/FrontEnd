package com.example.evshop.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evshop.R;
import com.example.evshop.ui.Notification;
import com.example.evshop.ui.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Object> items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu mẫu
        items = new ArrayList<>();
        items.add("Chủ nhật - 21/09/2025");
        items.add(new Notification(
                1,
                101,
                "Xe điện VinFast mới đã về cửa hàng!",       // title
                "Ưu đãi cực lớn cho dòng VinFast Evo",        // message
                "3 ngày trước",                              // time
                false,                                       // isRead
                "2025-09-21"                                 // createdAt
        ));

        items.add(new Notification(
                2,
                102,
                "Khuyến mãi Yamaha",                         // title
                "Giảm giá 10% cho xe điện Yamaha YZF",       // message
                "3 ngày trước",                              // time
                true,                                        // isRead
                "2025-09-21"                                 // createdAt
        ));

        items.add("Thứ 7 - 20/09/2025");
        items.add(new Notification(
                3,
                103,
                "Đơn hàng #1234 đã được xác nhận",           // title
                "Cửa hàng sẽ giao xe trong vòng 3 ngày",     // message
                "4 ngày trước",                              // time
                false,                                       // isRead
                "2025-09-20"                                 // createdAt
        ));

        items.add(new Notification(
                4,
                104,
                "Bảo dưỡng xe miễn phí",                     // title
                "Xe điện VinFast của bạn đến kỳ bảo dưỡng",  // message
                "5 ngày trước",                              // time
                false,                                       // isRead
                "2025-09-19"                                 // createdAt
        ));



        adapter = new NotificationAdapter(items);
        recyclerView.setAdapter(adapter);
    }
}
