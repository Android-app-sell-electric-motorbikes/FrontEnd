package com.example.evshop;

// package your.package.name;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.evshop.domain.models.CurrentCenterPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import com.example.evshop.util.IconUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import vn.vietmap.android.gestures.RotateGestureDetector;
import vn.vietmap.services.android.navigation.ui.v5.camera.CameraOverviewCancelableCallback;
import vn.vietmap.services.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.RouteListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute;
import vn.vietmap.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayer;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider;
import vn.vietmap.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import vn.vietmap.services.android.navigation.v5.milestone.Milestone;
import vn.vietmap.services.android.navigation.v5.milestone.MilestoneEventListener;
import vn.vietmap.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationEventListener;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation;
import vn.vietmap.services.android.navigation.v5.navigation.camera.RouteInformation;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteListener;
import vn.vietmap.services.android.navigation.v5.route.FasterRouteListener;
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;
import vn.vietmap.services.android.navigation.v5.snap.SnapToRoute;
import vn.vietmap.services.android.navigation.v5.utils.RouteUtils;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.geometry.LatLngBounds;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.VietMapGLOptions;

public class VietMapNavigationV2 extends AppCompatActivity
        implements OnMapReadyCallback, ProgressChangeListener,
        OffRouteListener, MilestoneEventListener, NavigationEventListener, NavigationListener,
        FasterRouteListener, SpeechAnnouncementListener, BannerInstructionsListener, RouteListener,
        VietMapGL.OnMapLongClickListener, VietMapGL.OnMapClickListener,
        VietMapGL.OnRotateListener, MapView.OnDidFinishRenderingMapListener {

    private MapView mapView;
    private VietMapGL vietmapGL;

    private DirectionsRoute currentRoute;
    private boolean routeClicked = false;
    private LocationEngine locationEngine;
    private NavigationMapRoute navigationMapRoute;
    private List<DirectionsRoute> directionsRoutes;
    private final SnapToRoute snapEngine = new SnapToRoute();
    private boolean simulateRoute = false;
    private int primaryRouteIndex = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private VietmapNavigation navigation;
    private SpeechPlayerProvider speechPlayerProvider;
    private SpeechPlayer speechPlayer;
    private RouteProgress routeProgress;
    private final RouteUtils routeUtils = new RouteUtils();
    private boolean voiceInstructionsEnabled = true;
    private boolean isBuildingRoute = false;
    private Point origin = Point.fromLngLat(106.675789, 10.759050);
    private Point destination = Point.fromLngLat(106.686777, 10.775056);
    private LocationComponent locationComponent;
    private CurrentCenterPoint currentCenterPoint;
    private boolean isOverviewing = false;
    private boolean animateBuildRoute = true;
    private boolean isNavigationInProgress = false;
    private boolean isNavigationCanceled = false;
    public double zoom = 20.0;
    public double bearing = 0.0;
    public double tilt = 0.0;
    public int[] padding = new int[]{150, 500, 150, 500};
    public boolean isRunning = false;
    private VietMapGLOptions options;

    private static String API_KEY = BuildConfig.VIETMAP_API_KEY;
    private final VietmapNavigationOptions navigationOptions = VietmapNavigationOptions.builder().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Theo Vietmap SDK: gọi trước super.onCreate để tránh crash
        Vietmap.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_nav);

        initLocationEngine();

        speechPlayerProvider = new SpeechPlayerProvider(this, "vi", true);
        speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);

        mapView = findViewById(R.id.ktMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Nếu dùng MapView trong layout, không cần tạo mapView mới:
        options = VietMapGLOptions.createFromAttributes(this)
                .compassEnabled(false)
                .logoEnabled(true);

        navigation = new VietmapNavigation(this, navigationOptions, locationEngine);

        Button btnStopNavigation = findViewById(R.id.btnStopNavigation);
        btnStopNavigation.setOnClickListener(v -> finishNavigation());

        Button btnStartNavigation = findViewById(R.id.btnStartNavigation);
        btnStartNavigation.setOnClickListener(v -> startNavigation());

        Button btnOverview = findViewById(R.id.btnOverview);
        btnOverview.setOnClickListener(v -> overViewRoute());

        Button btnRecenter = findViewById(R.id.btnRecenter);
        btnRecenter.setOnClickListener(v -> recenter());
    }

    // region Map lifecycle/listeners

    @Override
    public void onMapReady(@NonNull VietMapGL map) {
        this.vietmapGL = map;

        vietmapGL.setStyle(new Style.Builder()
                        .fromUri("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=" + API_KEY),
                style -> {
                    initLocationEngine();
                    enableLocationComponent(style);
                    initMapRoute();
                });

        vietmapGL.addOnMapClickListener(this);
        vietmapGL.addOnMapLongClickListener(this);
        vietmapGL.addOnRotateListener(this);

        Toast.makeText(this, "Long tap on the map to place the destination.", Toast.LENGTH_LONG).show();
    }

    private void enableLocationComponent(Style style) {
        locationComponent = vietmapGL.getLocationComponent();
        if (locationComponent != null) {
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style).build()
            );

            // Chỉ tiếp tục nếu ĐÃ có quyền (Kotlin code trước bị ngược logic)
            if (!checkPermission()) return;

            locationComponent.setCameraMode(
                    CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 0.0, 0.0, null);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.zoomWhileTracking(19.0);
            locationComponent.setRenderMode(RenderMode.GPS);
            locationComponent.setLocationEngine(locationEngine);
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        addMarker(point);
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        getCurrentLocation();
        destination = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        if (origin != null) {
            fetchRouteWithBearing(false);
        }
        return false;
    }

    @Override
    public void onDidFinishRenderingMap(boolean fully) {
        // no-op
    }

    @Override
    public void onRotateBegin(@NonNull RotateGestureDetector detector) { }

    @Override
    public void onRotate(@NonNull RotateGestureDetector detector) { }

    @Override
    public void onRotateEnd(@NonNull RotateGestureDetector detector) {
        System.out.println(detector.getDeltaSinceLast() * 360);
    }

    // endregion

    // region UI actions

    private void overViewRoute() {
        isOverviewing = true;
        if (routeProgress != null) {
            showRouteOverview(padding, routeProgress);
        }
    }

    private void clearRoute() {
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        }
        currentRoute = null;
    }

    private void initMapRoute() {
        if (vietmapGL != null) {
            navigationMapRoute = new NavigationMapRoute(mapView, vietmapGL, "vmadmin_province");
        }

        if (navigationMapRoute != null) {
            navigationMapRoute.setOnRouteSelectionChangeListener(route -> {
                routeClicked = true;
                currentRoute = route;

                try {
                    List<Point> routePoints = currentRoute.routeOptions().coordinates();
                    animateVietmapGLForRouteOverview(padding, routePoints);
                } catch (Exception ignore) {}

                try {
                    String idx = route.routeIndex();
                    primaryRouteIndex = (idx != null) ? Integer.parseInt(idx) : 0;
                } catch (Exception e) {
                    primaryRouteIndex = 0;
                }

                if (isRunning) {
                    finishNavigation(true);
                    startNavigation();
                }
            });
        }

        if (vietmapGL != null) {
            vietmapGL.addOnMapClickListener(this);
        }
    }

    private void finishNavigation() {
        finishNavigation(false);
    }

    private void finishNavigation(boolean isOffRouted) {
        zoom = 15.0;
        bearing = 0.0;
        tilt = 0.0;
        isNavigationCanceled = true;

        if (!isOffRouted) {
            isNavigationInProgress = false;
            moveCameraToOriginOfRoute();
        }

        if (currentRoute != null) {
            isRunning = false;
            if (navigation != null) {
                navigation.stopNavigation();
                navigation.removeFasterRouteListener(this);
                navigation.removeMilestoneEventListener(this);
                navigation.removeNavigationEventListener(this);
                navigation.removeOffRouteListener(this);
                navigation.removeProgressChangeListener(this);
            }
        }
    }

    private void moveCameraToOriginOfRoute() {
        if (currentRoute == null) return;
        try {
            List<Point> coords = currentRoute.routeOptions().coordinates();
            if (coords != null && !coords.isEmpty()) {
                Point originCoordinate = coords.get(0);
                if (originCoordinate != null) {
                    LatLng location = new LatLng(originCoordinate.latitude(), originCoordinate.longitude());
                    moveCamera(location, null);
                }
            }
        } catch (Exception e) {
            Timber.i(String.format("moveCameraToOriginOfRoute, Error: %s", e.getMessage()));
        }
    }

    private void moveCamera(@NonNull LatLng location, Float bearingDegrees) {
        CameraPosition.Builder builder = new CameraPosition.Builder()
                .target(location)
                .zoom(zoom)
                .tilt(tilt);

        if (bearingDegrees != null) {
            builder.bearing((double) bearingDegrees);
        }

        int duration = animateBuildRoute ? 3000 : 1;

        if (vietmapGL != null) {
            vietmapGL.animateCamera(
                    CameraUpdateFactory.newCameraPosition(builder.build()),
                    duration
            );
        }
    }

    private void startNavigation() {
        tilt = 60.0;
        zoom = 19.0;
        isOverviewing = false;
        isNavigationCanceled = false;

        if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
            vietmapGL.getLocationComponent().setCameraMode(CameraMode.TRACKING_GPS_NORTH);
        }

        if (currentRoute != null) {
            if (simulateRoute) {
                ReplayRouteLocationEngine mockLocationEngine = new ReplayRouteLocationEngine();
                mockLocationEngine.assign(currentRoute);
                if (navigation != null) {
                    navigation.setLocationEngine(mockLocationEngine);
                }
            } else if (locationEngine != null && navigation != null) {
                navigation.setLocationEngine(locationEngine);
            }

            isRunning = true;

            if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
                vietmapGL.getLocationComponent().setLocationEngine(null);
            }

            if (navigation != null) {
                navigation.addNavigationEventListener(this);
                navigation.addFasterRouteListener(this);
                navigation.addMilestoneEventListener(this);
                navigation.addOffRouteListener(this);
                navigation.addProgressChangeListener(this);
                navigation.setSnapEngine(snapEngine);

                isNavigationInProgress = true;
                navigation.startNavigation(currentRoute);
                recenter();
            }
        }
    }

    private void recenter() {
        isOverviewing = false;
        if (currentCenterPoint != null) {
            LatLng ll = new LatLng(currentCenterPoint.getLatitude(), currentCenterPoint.getLongitude());
            moveCamera(ll, currentCenterPoint.getBearing());
        }
    }

    private void initLocationEngine() {
        if (simulateRoute) {
            locationEngine = new ReplayRouteLocationEngine();
        } else {
            locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        }
    }

    // endregion

    // region Overview helpers

    private void showRouteOverview(int[] paddingVals, @NonNull RouteProgress currentRouteProgress) {
        RouteInformation routeInformation = buildRouteInformationFromProgress(currentRouteProgress);
        animateCameraForRouteOverview(routeInformation, paddingVals);
    }

    private RouteInformation buildRouteInformationFromProgress(RouteProgress rp) {
        if (rp == null) {
            return RouteInformation.create(null, null, null);
        } else {
            return RouteInformation.create(rp.directionsRoute(), null, null);
        }
    }

    private void animateCameraForRouteOverview(RouteInformation routeInformation, int[] paddingVals) {
        if (navigation == null) return;
        List<Point> routePoints = navigation.getCameraEngine().overview(routeInformation);
        if (!routePoints.isEmpty()) {
            animateVietmapGLForRouteOverview(paddingVals, routePoints);
        }
    }

    private void animateVietmapGLForRouteOverview(int[] paddingVals, List<Point> routePoints) {
        if (routePoints.size() <= 1) return;

        CameraUpdate resetUpdate = buildResetCameraUpdate();
        CameraUpdate overviewUpdate = buildOverviewCameraUpdate(paddingVals, routePoints);
        if (vietmapGL != null) {
            vietmapGL.animateCamera(
                    resetUpdate, 150,
                    new CameraOverviewCancelableCallback(overviewUpdate, vietmapGL)
            );
        }
    }

    private CameraUpdate buildResetCameraUpdate() {
        CameraPosition resetPosition = new CameraPosition.Builder()
                .tilt(0.0).bearing(0.0).build();
        return CameraUpdateFactory.newCameraPosition(resetPosition);
    }

    private CameraUpdate buildOverviewCameraUpdate(int[] paddingVals, List<Point> routePoints) {
        LatLngBounds routeBounds = convertRoutePointsToLatLngBounds(routePoints);
        return CameraUpdateFactory.newLatLngBounds(
                routeBounds, paddingVals[0], paddingVals[1], paddingVals[2], paddingVals[3]
        );
    }

    private LatLngBounds convertRoutePointsToLatLngBounds(List<Point> routePoints) {
        List<LatLng> latLngs = new ArrayList<>();
        for (Point rp : routePoints) {
            latLngs.add(new LatLng(rp.latitude(), rp.longitude()));
        }
        return new LatLngBounds.Builder().includes(latLngs).build();
    }

    // endregion

    // region Progress / off-route

    @Override
    public void onProgressChange(Location location, RouteProgress rp) {
        if (!isNavigationCanceled && location != null && rp != null) {
            try {
                boolean noRoutes = (directionsRoutes == null || directionsRoutes.isEmpty());
                boolean newCurrentRoute = !rp.directionsRoute()
                        .equals(noRoutes ? null : directionsRoutes.get(primaryRouteIndex));
                boolean isANewRoute = noRoutes || newCurrentRoute;

                if (!isANewRoute) {
                    currentCenterPoint = new CurrentCenterPoint(location.getLatitude(),
                            location.getLongitude(), location.getBearing());

                    if (!isOverviewing) {
                        this.routeProgress = rp;
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), location.getBearing());
                    }

                    if (!isBuildingRoute) {
                        Location snappedLocation = snapEngine.getSnappedLocation(location, rp);
                        if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
                            vietmapGL.getLocationComponent().forceLocationUpdate(snappedLocation);
                        }
                    }

                    if (simulateRoute && !isBuildingRoute) {
                        if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
                            vietmapGL.getLocationComponent().forceLocationUpdate(location);
                        }
                    }
                }

                handleProgressChange(location, rp);
            } catch (Exception ignored) { }
        }
    }

    private void handleProgressChange(Location location, RouteProgress rp) {
        Double distRemainToNextTurn = null;
        if (rp.currentLegProgress() != null && rp.currentLegProgress().currentStepProgress() != null) {
            distRemainToNextTurn = rp.currentLegProgress().currentStepProgress().distanceRemaining();
        }

        if (distRemainToNextTurn != null && distRemainToNextTurn < 30) {
            CameraPosition resetPosition = new CameraPosition.Builder().tilt(0.0).zoom(17.0).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(resetPosition);
            if (vietmapGL != null) {
                vietmapGL.animateCamera(cameraUpdate, 1000);
            }
        } else if (rp.currentLegProgress() != null
                && rp.currentLegProgress().currentStepProgress() != null
                && rp.currentLegProgress().currentStepProgress().distanceTraveled() > 30
                && !isOverviewing) {
            recenter();
        }
    }

    @Override
    public void userOffRoute(Location location) {
        if (location != null) {
            if (checkIfUserOffRoute(location)) {
                if (speechPlayer != null) speechPlayer.onOffRoute();
                doOnNewRoute(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
            }
        }
    }

    private void doOnNewRoute(Point offRoutePoint) {
        if (!isBuildingRoute) {
            isBuildingRoute = true;

            if (offRoutePoint != null) {
                finishNavigation(true);
                moveCamera(new LatLng(offRoutePoint.latitude(), offRoutePoint.longitude()), null);
            }

            origin = offRoutePoint;
            isNavigationInProgress = true;
            fetchRouteWithBearing(false);
        }
    }

    private boolean checkIfUserOffRoute(Location location) {
        Location snapLocation = snapEngine.getSnappedLocation(location, routeProgress);
        double distance = calculateDistanceBetween2Point(location, snapLocation);
        return distance > 30;
    }

    private double calculateDistanceBetween2Point(Location location1, Location location2) {
        double radius = 6371000.0; // meters
        double dLat = (location2.getLatitude() - location1.getLatitude()) * Math.PI / 180.0;
        double dLon = (location2.getLongitude() - location1.getLongitude()) * Math.PI / 180.0;

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(location1.getLatitude() * Math.PI / 180.0)
                * Math.cos(location2.getLatitude() * Math.PI / 180.0)
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return radius * c;
    }

    // endregion

    // region Milestones / events / reroute

    @Override
    public void onMilestoneEvent(RouteProgress p0, String p1, Milestone p2) {
        if (voiceInstructionsEnabled) {
            playVoiceAnnouncement(p2);
        }
        if (p0 != null && p2 != null) {
            if (routeUtils.isArrivalEvent(p0, p2) && isNavigationInProgress) {
                if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
                    vietmapGL.getLocationComponent().setLocationEngine(locationEngine);
                }
                finishNavigation();
            }
        }
    }

    private void playVoiceAnnouncement(Milestone milestone) {
        if (milestone instanceof VoiceInstructionMilestone) {
            SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                    .voiceInstructionMilestone((VoiceInstructionMilestone) milestone)
                    .build();
            if (speechPlayer != null) speechPlayer.play(announcement);
        }
    }

    @Override public void onRunning(boolean running) { }
    @Override public void onCancelNavigation() { }
    @Override public void onNavigationFinished() { }
    @Override public void onNavigationRunning() { }

    @Override
    public void fasterRouteFound(DirectionsRoute route) {
        if (route != null) {
            currentRoute = route;
            finishNavigation();
            startNavigation();
        }
    }

    @Override
    public SpeechAnnouncement willVoice(SpeechAnnouncement ann) {
        return ann; // trả nguyên nếu không tắt voice
    }

    @Override
    public BannerInstructions willDisplay(BannerInstructions bi) {
        return bi; // trả nguyên nếu không tắt banner
    }

    @Override
    public boolean allowRerouteFrom(Point p0) {
        return true;
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {
        doOnNewRoute(offRoutePoint);
    }

    @Override
    public void onRerouteAlong(DirectionsRoute route) {
        if (route != null) {
            currentRoute = route;
            finishNavigation();
            startNavigation();
        }
    }

    @Override
    public void onFailedReroute(String msg) {
        // handle failed reroute here
    }

    @Override
    public void onArrival() {
        if (vietmapGL != null && vietmapGL.getLocationComponent() != null) {
            vietmapGL.getLocationComponent().setLocationEngine(locationEngine);
        }
        System.out.println("You have arrived at your destination");
    }

    // endregion

    // region Route fetching / location

    private void getCurrentLocation() {
        if (checkPermission() && fusedLocationClient != null) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                }
            });
        }
    }

    private void fetchRouteWithBearing(boolean isStartNavigation) {
        if (checkPermission() && fusedLocationClient != null) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    fetchRoute(isStartNavigation, location.getBearing());
                } else {
                    fetchRoute(isStartNavigation, null);
                }
            });
        } else {
            fetchRoute(isStartNavigation, null);
        }
    }

    private void fetchRoute(boolean isStartNavigation, Float bearingVal) {
        NavigationRoute.Builder builder = NavigationRoute.builder(this)
                .apikey(API_KEY);

        if (bearingVal != null) {
            double b = bearingVal.doubleValue();
            builder.origin(origin, b, b);
            builder.destination(destination, b, b);
        } else {
            builder.origin(origin);
            builder.destination(destination);
        }

        builder.build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call,
                                   @NonNull Response<DirectionsResponse> response) {
                if (response.body() == null) return;

                directionsRoutes = response.body().routes();
                if (directionsRoutes == null || directionsRoutes.isEmpty()) return;

                currentRoute = (directionsRoutes.size() <= primaryRouteIndex)
                        ? directionsRoutes.get(0)
                        : directionsRoutes.get(primaryRouteIndex);

                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                } else if (vietmapGL != null) {
                    navigationMapRoute = new NavigationMapRoute(mapView, vietmapGL, "vmadmin_province");
                }

                if (directionsRoutes.size() > 1) {
                    navigationMapRoute.addRoutes(directionsRoutes);
                } else {
                    navigationMapRoute.addRoute(currentRoute);
                }

                isBuildingRoute = false;

                try {
                    List<Point> routePoints = currentRoute.routeOptions().coordinates();
                    animateVietmapGLForRouteOverview(padding, routePoints);
                } catch (Exception ignored) { }

                if (isNavigationInProgress || isStartNavigation) {
                    startNavigation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                isBuildingRoute = false;
            }
        });
    }

    private Marker addMarker(@NonNull LatLng position) {
        if (vietmapGL == null) return null;
        return vietmapGL.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(new IconUtils().drawableToIcon(
                                this,
                                R.drawable.ic_launcher_foreground,
                                ResourcesCompat.getColor(getResources(), R.color.black, getTheme())
                        ))
        );
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // endregion

    // region Activity lifecycle

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
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
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
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    // endregion
}
