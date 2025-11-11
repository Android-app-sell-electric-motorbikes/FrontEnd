package com.example.evshop.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.evshop.BuildConfig;
import com.example.evshop.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Icon;
import vn.vietmap.vietmapsdk.annotations.IconFactory;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.geometry.LatLngBounds;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class VietMapMapViewActivity extends AppCompatActivity
        implements VietMapGL.OnMapClickListener, VietMapGL.OnMarkerClickListener {

    private static final int REQ_LOC = 11;

    private MapView mapView;
    private VietMapGL vietMapGL;
    private LocationComponent locationComponent;
    private FusedLocationProviderClient fusedLocationClient;

    private NavigationMapRoute navigationMapRoute;

    private LinearLayout storeInfoPanel;

    private final Map<Long, Store> markerStoreMap = new HashMap<>();
    private Store selectedStore = null;

    private List<Store> getDemoStores() {
        List<Store> list = new ArrayList<>();
        list.add(new Store("S1", "Cửa hàng EVShop Quận 1", "22 Lê Lợi, Q1, TP.HCM", 10.772153, 106.701977));
        list.add(new Store("S2", "Cửa hàng EVShop Quận 3", "100 CMT8, Q3", 10.777628, 106.684900));
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
        storeInfoPanel = findViewById(R.id.storeInfoPanel);
        Button btnFindRoute = findViewById(R.id.btnFindRoute);
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);

        mapView.onCreate(savedInstanceState);

        btnFindRoute.setOnClickListener(v -> navigateToSelectedStore());

        // ** SỬA LẠI LOGIC NÚT BẤM **
        fabMyLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                if (locationComponent != null && locationComponent.isLocationComponentActivated()) {
                    locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
                    locationComponent.zoomWhileTracking(16.5);
                } else {
                    // Có quyền nhưng component chưa bật, thử bật lại
                    if (vietMapGL != null && vietMapGL.getStyle() != null) {
                        tryEnableLocationComponent(vietMapGL.getStyle());
                    }
                }
            } else {
                // Chưa có quyền, hỏi xin quyền
                requestLocationPermission();
            }
        });

        mapView.getMapAsync(map -> {
            vietMapGL = map;
            String styleUrl = "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=" + BuildConfig.VIETMAP_API_KEY;
            vietMapGL.setStyle(new Style.Builder().fromUri(styleUrl),
                    style -> {
                        if (hasLocationPermission()) {
                            tryEnableLocationComponent(style);
                        }
                        addStoreMarkers(getDemoStores());
                        moveCameraToUserOrDefault();
                    });

            vietMapGL.addOnMapClickListener(VietMapMapViewActivity.this);
            vietMapGL.setOnMarkerClickListener(VietMapMapViewActivity.this);
        });
    }

    private void addStoreMarkers(List<Store> stores) {
        Drawable storeDrawable = ContextCompat.getDrawable(this, R.drawable.ic_store_location);
        if (storeDrawable == null) return;

        Bitmap resizedBitmap = resizeBitmap(storeDrawable, 80, 80);
        Icon icon = IconFactory.getInstance(this).fromBitmap(resizedBitmap);

        for (Store s : stores) {
            Marker m = vietMapGL.addMarker(new MarkerOptions()
                    .position(new LatLng(s.lat, s.lng))
                    .title(s.name)
                    .snippet(s.address)
                    .icon(icon));
            markerStoreMap.put(m.getId(), s);
        }
    }

    private Bitmap resizeBitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Store s = markerStoreMap.get(marker.getId());
        if (s != null) {
            selectedStore = s;
            ((TextView) findViewById(R.id.txtStoreName)).setText(s.name);
            ((TextView) findViewById(R.id.txtStoreAddr)).setText(s.address);
            storeInfoPanel.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        storeInfoPanel.setVisibility(View.GONE);
        selectedStore = null;
        return false;
    }

    private void navigateToSelectedStore(){
        if (selectedStore == null) return;
        Point origin = getCurrentPointOrNull();
        if (origin == null) {
            Toast.makeText(this, "Chưa lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }
        Point dest = Point.fromLngLat(selectedStore.lng, selectedStore.lat);
        fetchAndDrawRoute(origin, dest, true);
    }

    private void moveCameraToUserOrDefault() {
        if (!hasLocationPermission()) return; // Không có quyền thì không làm gì
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            LatLng target = (location != null)
                    ? new LatLng(location.getLatitude(), location.getLongitude())
                    : new LatLng(10.776, 106.700);

            CameraPosition pos = new CameraPosition.Builder().target(target).zoom(15.5).build();
            if(vietMapGL != null) vietMapGL.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 800);
        });
    }

    private void tryEnableLocationComponent(@NonNull Style style) {
        try {
            locationComponent = vietMapGL.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
            locationComponent.setRenderMode(RenderMode.GPS);
        } catch (SecurityException ignored) {}
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (vietMapGL != null && vietMapGL.getStyle() != null) {
                tryEnableLocationComponent(vietMapGL.getStyle());
                moveCameraToUserOrDefault();
            }
        } else {
            Toast.makeText(this, "Bạn đã từ chối quyền vị trí.", Toast.LENGTH_LONG).show();
        }
    }

    private Point getCurrentPointOrNull() {
        if (hasLocationPermission() && locationComponent != null && locationComponent.getLastKnownLocation() != null) {
            return Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());
        }
        return null;
    }

    private void fetchAndDrawRoute(Point origin, Point dest, boolean overview) {
        if (navigationMapRoute == null) {
            navigationMapRoute = new NavigationMapRoute(mapView, vietMapGL, null);
        } else {
            navigationMapRoute.removeRoute();
        }
        NavigationRoute.builder(this)
                .apikey(BuildConfig.VIETMAP_API_KEY)
                .origin(origin)
                .destination(dest)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        if (response.body() == null || response.body().routes().isEmpty()) {
                            Toast.makeText(VietMapMapViewActivity.this, "Không tìm thấy tuyến phù hợp", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        navigationMapRoute.addRoute(response.body().routes().get(0));
                        if (overview) {
                            LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(origin.latitude(), origin.longitude())).include(new LatLng(dest.latitude(), dest.longitude())).build();
                            vietMapGL.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 800);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                        Toast.makeText(VietMapMapViewActivity.this, "Lỗi lấy tuyến: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
