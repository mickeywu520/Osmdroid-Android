package com.android.openstreetmaps;

import android.app.AlertDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity implements MapListener {

    private String TAG = "MainActivity";
    private MapView mMap;
    private IMapController controller;
    private MyLocationNewOverlay mMyLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapDialog();
            }
        });
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE));
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        if (event != null && event.getSource() != null && event.getSource().getMapCenter() != null) {
            // 處理滾動事件
        }
        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        // 處理縮放事件
        return false;
    }

    // 在地圖上釘選一個標記
    private void pinMarker(double setLatitude, double setLongitude) {
        GeoPoint startPoint = new GeoPoint(setLatitude, setLongitude);
        Marker startMarker = new Marker(mMap);
        startMarker.setId("marked");
        startMarker.setPosition(startPoint);
        startMarker.setTitle(startMarker.getPosition().toString());
        // 移除之前的標記
        for (int i = 0; i < mMap.getOverlays().size(); i++) {
            Overlay overlay = mMap.getOverlays().get(i);
            if (overlay instanceof Marker && ((Marker) overlay).getId().equals("marked")) {
                mMap.getOverlays().remove(overlay);
            }
        }
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMap.getOverlays().add(startMarker);
        startMarker.showInfoWindow();
        mMap.invalidate();
    }

    // 觸控監聽器
    public void onTapListener() {
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Toast.makeText(getBaseContext(), "Long Pressed", Toast.LENGTH_SHORT).show();
                pinMarker(p.getLatitude(), p.getLongitude());
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        mMap.getOverlays().add(OverlayEvents);
    }

    private void showMapDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.map_dialog, null);
        builder.setView(dialogView);
        mMap = dialogView.findViewById(R.id.osmmap);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.getMapCenter();
        mMap.setMultiTouchControls(true);
        mMap.getLocalVisibleRect(new Rect());
        mMyLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMap);
        controller = mMap.getController();
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
        mMyLocationOverlay.setDrawAccuracyEnabled(true);
        mMyLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        controller.setCenter(mMyLocationOverlay.getMyLocation());
                        controller.animateTo(mMyLocationOverlay.getMyLocation());
                    }
                });
            }
        });
        controller.setZoom(6.0);
        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.addMapListener(this);
        onTapListener();
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            IGeoPoint geoPoint = mMap.getMapCenter();
            double latitude = geoPoint.getLatitude();
            double longitude = geoPoint.getLongitude();
            // 處理獲取到的經緯度坐標
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}