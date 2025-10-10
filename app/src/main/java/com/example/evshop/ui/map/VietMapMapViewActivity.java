package com.example.evshop.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.evshop.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.evshop.BuildConfig;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineDefault;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineRequest;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.provider.Settings;
import android.location.LocationManager;

import javax.annotation.Nullable;

public class VietMapMapViewActivity extends AppCompatActivity {

    private static final int RC_LOCATION = 101;
    private static final long LOCATION_INTERVAL = 1000L;

    private MapView mapView;
    private VietMapGL vietMapGL;
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;

    private String API_KEY = BuildConfig.VIETMAP_API_KEY;;

    private double STORE_LAT, STORE_LNG;
    private Polyline routePolyline;
    private LocationEngineRequest locationRequest;
    private LocationEngineCallback<LocationEngineResult> locationCallback;
    private LatLng storeLatLng;
    private boolean hasRouted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BẮT BUỘC: khởi tạo SDK trước super.onCreate
        Vietmap.getInstance(this); // theo hướng dẫn chính thức. :contentReference[oaicite:1]{index=1}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viet_map_map_view);

        STORE_LAT = getIntent().getDoubleExtra("STORE_LAT", 0d);
        STORE_LNG = getIntent().getDoubleExtra("STORE_LNG", 0d);

        mapView = findViewById(R.id.vietmapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {
            vietMapGL = map;

            // Áp dụng style VietMap có apikey (đúng mẫu tài liệu)
            vietMapGL.setStyle(
                    new Style.Builder().fromUri(
                            "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=" + API_KEY
                    ),
                    this::onStyleLoaded
            );
        });
    }

    private void onStyleLoaded(@NonNull Style style) {
        locationEngine = LocationEngineDefault.INSTANCE.getDefaultLocationEngine(this);

        enableLocationComponent(style);

        storeLatLng = new LatLng(STORE_LAT, STORE_LNG);
        vietMapGL.addMarker(new MarkerOptions().position(storeLatLng).title("Cửa hàng"));
        vietMapGL.animateCamera(CameraUpdateFactory.newLatLngZoom(storeLatLng, 15));

        // request cấu hình
        locationRequest = new LocationEngineRequest.Builder(LOCATION_INTERVAL)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .build();

        // giữ callback ở field để tránh GC
        locationCallback = new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                android.location.Location loc = result.getLastLocation();
                if (loc == null) return;
                LatLng user = new LatLng(loc.getLatitude(), loc.getLongitude());
                if (!hasRouted) { // vẽ tuyến 1 lần là đủ
                    hasRouted = true;
                    drawRouteUserToStore(user, storeLatLng);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VietMapMapViewActivity.this,
                        "Không lấy được vị trí hiện tại: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        // bắt đầu lấy vị trí & update
        startLocationFlowOrAsk();

        findViewById(R.id.btnRoute).setOnClickListener(v -> {
            hasRouted = false;

            // 1) Thử lấy ngay từ chấm xanh:
            LatLng quick = getCurrentLatLngQuick();
            if (quick != null) {
                hasRouted = true;
                // nếu đã có polyline cũ thì dọn trước
                if (routePolyline != null) { try { routePolyline.remove(); } catch (Exception ignore) {} }
                drawRouteUserToStore(quick, storeLatLng);
                return;
            }

            // 2) Chưa có thì mới rơi về flow xin GPS / request updates
            Toast.makeText(this, "Đang tìm vị trí hiện tại...", Toast.LENGTH_SHORT).show();
            startLocationFlowOrAsk(); // sẽ gọi callback và vẽ tuyến
        });
        findViewById(R.id.btnExternalNav).setOnClickListener(v -> openExternalGoogleMaps());
    }

    private void openExternalGoogleMaps() {
        String lat = String.valueOf(STORE_LAT);
        String lng = String.valueOf(STORE_LNG);

        // Ưu tiên Intent điều hướng Google Maps
        android.net.Uri gmm = android.net.Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, gmm);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            return;
        }

        // Fallback: Mở trình duyệt
        android.net.Uri uriWeb = android.net.Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng + "&travelmode=driving"
        );
        startActivity(new Intent(Intent.ACTION_VIEW, uriWeb));
    }

    @SuppressLint("MissingPermission")
    private void startLocationFlowOrAsk() {
        if (!ensureLocationPermissionOrRequest()) return;

        if (!isLocationEnabled()) {
            Toast.makeText(this, "Vui lòng bật Dịch vụ vị trí (GPS)", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        try {
            // gọi cả lastLocation và updates để chắc chắn nhận được toạ độ
            locationEngine.getLastLocation(locationCallback);
            locationEngine.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền vị trí", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        if (!ensureLocationPermissionOrRequest()) return;

        try {
            locationComponent = vietMapGL.getLocationComponent();
            if (locationComponent == null) return;

            locationComponent.activateLocationComponent(
                    vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
                            .builder(this, style).build()
            );
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(
                    CameraMode.TRACKING_GPS_NORTH,
                    750L,
                    18.0,
                    0.0,
                    0.0,
                    null
            );
            locationComponent.setRenderMode(RenderMode.GPS);
            locationComponent.zoomWhileTracking(18.5);
            locationComponent.setLocationEngine(locationEngine);
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        return lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }
    private boolean ensureLocationPermissionOrRequest() {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!fine && !coarse) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    RC_LOCATION
            );
            return false;
        }
        return true;
    }

    private boolean hasLocationPermission() {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_LOCATION);
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private void requestUserLocationAndRoute(LatLng store) {
        if (!ensureLocationPermissionOrRequest()) return;

        LocationEngineRequest req = new LocationEngineRequest.Builder(LOCATION_INTERVAL)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .build();

        try {
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    android.location.Location loc = result.getLastLocation();
                    if (loc == null) {
                        try {
                            locationEngine.requestLocationUpdates(req, this, getMainLooper());
                        } catch (SecurityException se) {
                            Toast.makeText(VietMapMapViewActivity.this, "Thiếu quyền vị trí", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    LatLng user = new LatLng(loc.getLatitude(), loc.getLongitude());
                    drawRouteUserToStore(user, store);
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(VietMapMapViewActivity.this, "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRouteUserToStore(LatLng user, LatLng store) {
        // Gọi VietMap Route API (GET), dùng points_encoded=false để khỏi giải mã polyline. :contentReference[oaicite:2]{index=2}
        String url = "https://maps.vietmap.vn/api/route?api-version=1.1"
                + "&apikey=" + API_KEY
                + "&point=" + user.getLatitude() + "," + user.getLongitude()
                + "&point=" + store.getLatitude() + "," + store.getLongitude()
                + "&vehicle=car&points_encoded=false";

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(url).get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(VietMapMapViewActivity.this, "Route API lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onFailure(call, new IOException("HTTP " + response.code()));
                    return;
                }
                String body = response.body().string();
                try {
                    JSONObject root = new JSONObject(body);
                    JSONArray paths = root.getJSONArray("paths");
                    if (paths.length() == 0) {
                        throw new Exception("Không có tuyến");
                    }

                    JSONObject first = paths.getJSONObject(0);
                    JSONArray pts = first.getJSONArray("points"); // vì points_encoded=false ⇒ mảng [lat, lng]
                    final List<LatLng> line = new ArrayList<>();
                    for (int i = 0; i < pts.length(); i++) {
                        JSONArray p = pts.getJSONArray(i);
                        double lat = p.getDouble(0);
                        double lng = p.getDouble(1);
                        line.add(new LatLng(lat, lng));
                    }

                    runOnUiThread(() -> {
                        if (routePolyline != null) routePolyline.remove();
                        routePolyline = vietMapGL.addPolyline(new PolylineOptions()
                                .addAll(line)
                                .color(Color.BLUE));

                        vietMapGL.animateCamera(CameraUpdateFactory.newLatLngZoom(line.get(0), 15));

                        // dừng nhận location nếu đã vẽ xong
                        if (locationEngine != null && locationCallback != null) {
                            try { locationEngine.removeLocationUpdates(locationCallback); } catch (Exception ignore) {}
                        }
                    });

                } catch (Exception ex) {
                    runOnUiThread(() ->
                            Toast.makeText(VietMapMapViewActivity.this, "Parse route lỗi: " + ex.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // Lifecycle MapView
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() {
        super.onStop();
        if (locationEngine != null && locationCallback != null) {
            try { locationEngine.removeLocationUpdates(locationCallback); } catch (Exception ignore) {}
        }
        mapView.onStop();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_LOCATION && hasLocationPermission()) {
            if (vietMapGL != null) {
                vietMapGL.getStyle(style -> {
                    if (style != null) {
                        enableLocationComponent(style);
                        // QUAN TRỌNG: chạy lại flow lấy vị trí
                        startLocationFlowOrAsk();
                    }
                });
            }
        }
    }

    @Nullable
    private LatLng getCurrentLatLngQuick() {
        try {
            if (locationComponent != null && locationComponent.getLastKnownLocation() != null) {
                android.location.Location l = locationComponent.getLastKnownLocation();
                return new LatLng(l.getLatitude(), l.getLongitude());
            }
        } catch (Throwable ignore) {}

        // Fallback: hỏi LastKnownLocation từ LocationManager (trường hợp engine chưa trả về)
        try {
            android.location.LocationManager lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
            if (lm != null) {
                java.util.List<String> providers = lm.getProviders(true);
                android.location.Location best = null;
                for (String p : providers) {
                    try {
                        android.location.Location l = lm.getLastKnownLocation(p);
                        if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
                    } catch (Throwable ignore) {}
                }
                if (best != null) return new LatLng(best.getLatitude(), best.getLongitude());
            }
        } catch (Throwable ignore) {}

        return null;
    }
}