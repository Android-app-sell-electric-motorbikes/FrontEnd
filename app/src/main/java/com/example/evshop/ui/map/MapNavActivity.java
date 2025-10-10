package com.example.evshop.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.evshop.BuildConfig;
import com.example.evshop.R;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vietmap.services.android.navigation.ui.v5.camera.CameraOverviewCancelableCallback;
import vn.vietmap.services.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.RouteListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider;
import vn.vietmap.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import vn.vietmap.services.android.navigation.v5.milestone.MilestoneEventListener;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationEventListener;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions;
import vn.vietmap.services.android.navigation.v5.navigation.camera.RouteInformation;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteListener;
import vn.vietmap.services.android.navigation.v5.route.FasterRouteListener;
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class MapNavActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        VietMapGL.OnMapClickListener,
        VietMapGL.OnMapLongClickListener,
        ProgressChangeListener,
        NavigationListener,
        NavigationEventListener,
        RouteListener,
        SpeechAnnouncementListener,
        BannerInstructionsListener,
        OffRouteListener,
        FasterRouteListener,
        MilestoneEventListener {

    private MapView mapView;
    private VietMapGL vietmapGL;

    private LocationComponent locationComponent;
    private LocationEngine locationEngine;

    private NavigationMapRoute navigationMapRoute;
    private VietmapNavigation navigation;
    private final VietmapNavigationOptions navigationOptions = VietmapNavigationOptions.builder().build();

    private DirectionsRoute currentRoute;
    private List<DirectionsRoute> directionsRoutes;

    private boolean simulateRoute = false;
    private boolean isNavigationInProgress = false;
    private boolean isOverviewing = false;

    private final Point origin = Point.fromLngLat(106.675789, 10.759050);
    private Point destination = Point.fromLngLat(106.686777, 10.775056);

    private double zoom = 18.0;
    private double tilt = 0.0;
    private final int[] padding = new int[]{150, 500, 150, 500};
    private static String API_KEY = BuildConfig.VIETMAP_API_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Vietmap SDK khuyến nghị gọi trước super.onCreate nếu SDK yêu cầu
        Vietmap.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_nav);

        mapView = findViewById(R.id.ktMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // location engine (mock nếu simulateRoute)
        locationEngine = simulateRoute
                ? new ReplayRouteLocationEngine()
                : LocationEngineProvider.getBestLocationEngine(this);

        // Navigation core
        navigation = new VietmapNavigation(this, navigationOptions, locationEngine);
        navigation.addNavigationEventListener(this);
        navigation.addOffRouteListener(this);
        navigation.addFasterRouteListener(this);
        navigation.addMilestoneEventListener(this);
        navigation.addProgressChangeListener(this);

        // Buttons
        Button btnStopNavigation = findViewById(R.id.btnStopNavigation);
        btnStopNavigation.setOnClickListener(v -> stopNavigation());

        Button btnStartNavigation = findViewById(R.id.btnStartNavigation);
        btnStartNavigation.setOnClickListener(v -> startNavigation());

        Button btnOverview = findViewById(R.id.btnOverview);
        btnOverview.setOnClickListener(v -> overViewRoute());

        Button btnRecenter = findViewById(R.id.btnRecenter);
        btnRecenter.setOnClickListener(v -> recenter());
    }

    // ===== Map setup =====
    @Override
    public void onMapReady(@NonNull VietMapGL map) {
        this.vietmapGL = map;

        map.setStyle(new Style.Builder()
                        .fromUri("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=" + API_KEY),
                this::enableLocationComponent);

        map.addOnMapClickListener(this);
        map.addOnMapLongClickListener(this);

        // Khởi tạo lớp vẽ route
        navigationMapRoute = new NavigationMapRoute(mapView, map, "vmadmin_province");
    }

    private void enableLocationComponent(Style style) {
        locationComponent = vietmapGL.getLocationComponent();
        if (locationComponent == null) return;

        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build()
        );

        if (!hasLocationPermission()) return;

        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 0.0, 0.0, null);
        locationComponent.zoomWhileTracking(18.5);
        locationComponent.setRenderMode(RenderMode.GPS);
        locationComponent.setLocationEngine(locationEngine);
    }

    // ===== UI actions =====
    private void startNavigation() {
        if (currentRoute == null) {
            // Nếu chưa có route thì fetch trước rồi start
            fetchRoute(true);
            return;
        }

        isNavigationInProgress = true;
        isOverviewing = false;

        // tắt engine của location component để điều hướng tự quản lý
        if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
            vietmapGL.getLocationComponent().setLocationEngine(null);
        }

        navigation.startNavigation(currentRoute);
        recenter();
        Toast.makeText(this, "Navigation started", Toast.LENGTH_SHORT).show();
    }

    private void stopNavigation() {
        if (navigation != null) {
            navigation.stopNavigation();
        }
        isNavigationInProgress = false;

        // trả engine lại cho location component
        if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
            vietmapGL.getLocationComponent().setLocationEngine(locationEngine);
        }

        Toast.makeText(this, "Navigation stopped", Toast.LENGTH_SHORT).show();
    }

    private void overViewRoute() {
        if (currentRoute == null) return;
        isOverviewing = true;

        RouteInformation info = RouteInformation.create(currentRoute, null, null);

        List<Point> points = navigation.getCameraEngine().overview(info);
        if (points == null || points.size() <= 1) return;

        CameraUpdate reset = CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder().tilt(0.0).bearing(0.0).build()
        );
        CameraUpdate overview = CameraUpdateFactory.newLatLngBounds(
                toBounds(points), padding[0], padding[1], padding[2], padding[3]
        );

        vietmapGL.animateCamera(reset, 150, new CameraOverviewCancelableCallback(overview, vietmapGL));
    }

    private void recenter() {
        isOverviewing = false;
        if (locationComponent != null && locationComponent.getLastKnownLocation() != null) {
            Location loc = locationComponent.getLastKnownLocation();
            CameraPosition pos = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .zoom(zoom)
                    .tilt(tilt)
                    .build();
            vietmapGL.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 800);
        }
    }

    // ===== Fetch route =====
    private void fetchRoute(boolean startAfter) {
        NavigationRoute.Builder builder = NavigationRoute.builder(this)
                .apikey(API_KEY)
                .origin(origin)
                .destination(destination);

        builder.build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call,
                                   @NonNull Response<DirectionsResponse> response) {
                if (response.body() == null || response.body().routes() == null || response.body().routes().isEmpty()) {
                    Toast.makeText(MapNavActivity.this, "No routes found", Toast.LENGTH_SHORT).show();
                    return;
                }
                directionsRoutes = response.body().routes();
                currentRoute = directionsRoutes.get(0);

                // vẽ route
                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                    if (directionsRoutes.size() > 1) {
                        navigationMapRoute.addRoutes(directionsRoutes);
                    } else {
                        navigationMapRoute.addRoute(currentRoute);
                    }
                }

                // overview camera nhẹ để nhìn toàn tuyến
                overViewRoute();

                if (startAfter) {
                    startNavigation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(MapNavActivity.this, "Fetch route failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Helpers =====
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private vn.vietmap.vietmapsdk.geometry.LatLngBounds toBounds(List<Point> routePoints) {
        List<LatLng> latLngs = new ArrayList<>();
        for (Point p : routePoints) {
            latLngs.add(new LatLng(p.latitude(), p.longitude()));
        }
        return new vn.vietmap.vietmapsdk.geometry.LatLngBounds.Builder().includes(latLngs).build();
    }

    // ===== Implemented interfaces =====

    // Map click
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        // đặt điểm đến = vị trí nhấn
        destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Toast.makeText(this, "Destination set. Tap Start to navigate.", Toast.LENGTH_SHORT).show();
        return false;
    }

    // ProgressChangeListener
    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        // có thể recenter camera tại đây nếu không overview
        if (!isOverviewing && location != null) {
            CameraPosition pos = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(zoom)
                    .tilt(tilt)
                    .build();
            vietmapGL.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 600);
        }
    }

    // NavigationListener
    @Override
    public void onCancelNavigation() {
    }

    @Override
    public void onNavigationFinished() {
    }

    @Override
    public void onNavigationRunning() {
    }

    // NavigationEventListener
    @Override
    public void onRunning(boolean running) {
    }

    // RouteListener
    @Override
    public boolean allowRerouteFrom(Point point) {
        return true;
    }

    @Override
    public void onOffRoute(Point point) { /* có thể gọi fetchRoute(true) nếu muốn */ }

    @Override
    public void onRerouteAlong(DirectionsRoute route) {
        currentRoute = route;
    }

    @Override
    public void onFailedReroute(String s) {
    }

    @Override
    public void onArrival() {
        Toast.makeText(this, "Arrived!", Toast.LENGTH_SHORT).show();
        stopNavigation();
    }

    // SpeechAnnouncementListener
    @Override
    public SpeechAnnouncement willVoice(SpeechAnnouncement speechAnnouncement) {
        return speechAnnouncement; // trả nguyên (không tắt voice)
    }

    // BannerInstructionsListener
    @Override
    public BannerInstructions willDisplay(BannerInstructions bannerInstructions) {
        return bannerInstructions; // trả nguyên (không tắt banner)
    }

    // OffRouteListener, FasterRouteListener, MilestoneEventListener (đã thêm ở phần implements)
    @Override
    public void userOffRoute(Location location) {
    }

    @Override
    public void fasterRouteFound(DirectionsRoute directionsRoute) {
    }

    @Override
    public void onMilestoneEvent(RouteProgress routeProgress, String instruction, vn.vietmap.services.android.navigation.v5.milestone.Milestone milestone) {
    }

    // ===== Lifecycle =====
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
