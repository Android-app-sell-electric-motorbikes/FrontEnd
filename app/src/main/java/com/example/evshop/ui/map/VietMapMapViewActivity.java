package com.example.evshop.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.evshop.BuildConfig;
import com.example.evshop.R;
import com.example.evshop.util.IconUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider;
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.geometry.LatLngBounds;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class VietMapMapViewActivity extends AppCompatActivity
        implements VietMapGL.OnMapClickListener, VietMapGL.OnMarkerClickListener {

    private static final int REQ_LOC = 11;

    private MapView mapView;
    private VietMapGL vietMapGL;
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
    private FusedLocationProviderClient fusedLocationClient;

    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRoute;

    // UI panel
    private LinearLayout storeInfoPanel;
    private TextView txtStoreName, txtStoreAddr;
    private Button btnFindRoute;

    // Lưu marker<->store
    private final Map<Long, Store> markerStoreMap = new HashMap<>();
    private Store selectedStore = null;
    private boolean mapReady = false;
    private boolean styleLoaded = false;
    private boolean permissionsGranted = false;
    private static String API_KEY = BuildConfig.VIETMAP_API_KEY;

    // ===== Demo store list =====
    private List<Store> getDemoStores() {
        List<Store> list = new ArrayList<>();
        list.add(new Store("S1", "Cửa hàng EVShop Quận 1", "22 Lê Lợi, Q1, TP.HCM", 10.772153, 106.701977));
        list.add(new Store("S2", "Cửa hàng EVShop Quận 3", "100 Cách Mạng Tháng 8, Q3", 10.777628, 106.684900));
        list.add(new Store("S3", "Cửa hàng EVShop Quận 10", "285 CMT8, Q10", 10.778930, 106.666912));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Vietmap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viet_map_map_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.vmMapView);
        mapView.onCreate(savedInstanceState);

        storeInfoPanel = findViewById(R.id.storeInfoPanel);
        txtStoreName = findViewById(R.id.txtStoreName);
        txtStoreAddr = findViewById(R.id.txtStoreAddr);
        btnFindRoute = findViewById(R.id.btnFindRoute);

        btnFindRoute.setOnClickListener(v -> {
            if (selectedStore == null) return;
            Point origin = getCurrentPointOrNull();
            if (origin == null) {
                Toast.makeText(this, "Chưa lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }
            Point dest = Point.fromLngLat(selectedStore.lng, selectedStore.lat);
            fetchAndDrawRoute(origin, dest, true);
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull VietMapGL map) {
                vietMapGL = map;

                vietMapGL.setStyle(new Style.Builder()
                                .fromUri("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=" + API_KEY),
                        style -> {
                            styleLoaded = true;

                            initLocationEngine();
                            // Chỉ bật location nếu có quyền
                            if (hasLocationPermission()) {
                                permissionsGranted = true;
                                tryEnableLocationComponent();
                            } else {
                                permissionsGranted = false;
                                requestLocationPermission();
                            }

                            addStoreMarkers(getDemoStores());
                            moveCameraToUserOrDefault();
                        });

                vietMapGL.addOnMapClickListener(VietMapMapViewActivity.this);
                vietMapGL.setOnMarkerClickListener(VietMapMapViewActivity.this);
            }
        });

        // xin quyền nếu chưa có
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOC);
        }
    }

    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
    }

    private void enableLocationComponent(@NonNull Style style) throws SecurityException {
        if (!hasLocationPermission()) throw new SecurityException("Location permission missing");

        locationComponent = vietMapGL.getLocationComponent();
        if (locationComponent == null) return;

        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build()
        );
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 0.0, 0.0, null);
        locationComponent.zoomWhileTracking(18.5);
        locationComponent.setRenderMode(RenderMode.GPS);
        locationComponent.setLocationEngine(locationEngine);
    }

    private void addStoreMarkers(List<Store> stores) {
        for (Store s : stores) {
            Marker m = vietMapGL.addMarker(new MarkerOptions()
                    .position(new LatLng(s.lat, s.lng))
                    .title(s.name)
                    .snippet(s.address)
                    .icon(new IconUtils().drawableToIcon(
                            this,
                            R.drawable.ic_launcher_foreground,
                            ResourcesCompat.getColor(getResources(), R.color.black, getTheme())
                    )));
            markerStoreMap.put(m.getId(), s);
        }
    }

    private void moveCameraToUserOrDefault() {
        LatLng target = new LatLng(10.776, 106.700); // fallback Q1
        Location last = (locationComponent != null) ? locationComponent.getLastKnownLocation() : null;
        if (last != null) target = new LatLng(last.getLatitude(), last.getLongitude());

        CameraPosition pos = new CameraPosition.Builder()
                .target(target).zoom(15.5).tilt(0.0).build();
        vietMapGL.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 800);
    }

    // marker clicked
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Store s = markerStoreMap.get(marker.getId());
        if (s != null) {
            selectedStore = s;
            txtStoreName.setText(s.name);
            txtStoreAddr.setText(s.address);
            storeInfoPanel.setVisibility(View.VISIBLE);
        }
        return true; // đã xử lý
    }

    // map clicked: ẩn panel
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        storeInfoPanel.setVisibility(View.GONE);
        selectedStore = null;
        return false;
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQ_LOC
        );
    }

    private void openAppSettings() {
        try {
            Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception ignored) {
        }
    }

    private Point getCurrentPointOrNull() {
        // Ưu tiên từ LocationComponent
        try {
            if (hasLocationPermission() && locationComponent != null && locationComponent.getLastKnownLocation() != null) {
                Location l = locationComponent.getLastKnownLocation();
                return Point.fromLngLat(l.getLongitude(), l.getLatitude());
            }
        } catch (SecurityException ignored) {
            permissionsGranted = false;
        }

        // Fallback fused (cũng phải bọc try/catch)
        try {
            final Point[] out = {null};
            if (hasLocationPermission()) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                    if (loc != null) {
                        out[0] = Point.fromLngLat(loc.getLongitude(), loc.getLatitude());
                    }
                });
            }
            return out[0];
        } catch (SecurityException ignored) {
            permissionsGranted = false;
            return null;
        }
    }

    private void fetchAndDrawRoute(Point origin, Point dest, boolean overview) {
        // Khởi tạo lớp vẽ route nếu chưa có
        if (navigationMapRoute == null) {
            navigationMapRoute = new NavigationMapRoute(mapView, vietMapGL, /*layerAboveId*/ null);
        } else {
            navigationMapRoute.removeRoute();
        }

        NavigationRoute.builder(this)
                .apikey(API_KEY)
                .origin(origin)
                .destination(dest)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        if (response.body() == null || response.body().routes() == null || response.body().routes().isEmpty()) {
                            Toast.makeText(VietMapMapViewActivity.this, "Không tìm thấy tuyến phù hợp", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        navigationMapRoute.addRoute(currentRoute);

                        if (overview) {
                            // fit bounds theo origin-dest
                            List<LatLng> pts = new ArrayList<>();
                            pts.add(new LatLng(origin.latitude(), origin.longitude()));
                            pts.add(new LatLng(dest.latitude(), dest.longitude()));
                            LatLngBounds b = new LatLngBounds.Builder().includes(pts).build();
                            vietMapGL.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(b, 80, 320, 80, 320),
                                    800
                            );
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                        Toast.makeText(VietMapMapViewActivity.this, "Lỗi lấy tuyến: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC) {
            permissionsGranted = hasLocationPermission();
            if (permissionsGranted) {
                // Nếu style đã load → bật location ngay
                if (mapReady && styleLoaded) {
                    tryEnableLocationComponent();
                    moveCameraToUserOrDefault(); // zoom vào user
                }
            } else {
                Toast.makeText(this, "Bạn đã từ chối quyền vị trí. Một số tính năng sẽ bị hạn chế.", Toast.LENGTH_LONG).show();
                // Tuỳ chọn: hiển thị nút mở cài đặt nếu người dùng chọn “Don’t ask again”
                // openAppSettings();
            }
        }
    }

    private void tryEnableLocationComponent() {
        try {
            Style style = vietMapGL.getStyle();
            if (style == null) return;
            enableLocationComponent(style);
        } catch (SecurityException se) {
            // Người dùng vừa rút quyền hoặc thiết bị chặn – xử lý an toàn
            permissionsGranted = false;
            Toast.makeText(this, "Không thể bật vị trí do thiếu quyền.", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== lifecycle =====
    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    // ===== simple inner model (nếu cô không muốn tạo file riêng) =====
    public static class Store {
        public final String id, name, address;
        public final double lat, lng;

        public Store(String id, String name, String address, double lat, double lng) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }
    }
}