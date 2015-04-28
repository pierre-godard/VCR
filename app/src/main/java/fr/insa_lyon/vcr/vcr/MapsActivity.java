package fr.insa_lyon.vcr.vcr;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends FragmentActivity
        implements FragmentManager.OnBackStackChangedListener, OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Est-ce qu'on affiche le user input field ou non (sinon on affiche la map).
    private boolean mShowingBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps) ;

        // ADDED CODE
        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            if (mMap == null) {

                GoogleMapOptions googleMapOptions = new GoogleMapOptions();
                googleMapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL)
                        .compassEnabled(true)
                        .rotateGesturesEnabled(true)
                        .tiltGesturesEnabled(true)
                        .zoomControlsEnabled(true)
                        .camera(new CameraPosition(new LatLng(45.759948,4.836593),13,0,0));

                SupportMapFragment.newInstance(GoogleMapOptions googleMapOptions).getMapAsync(OnMapReadyCallbackCallback);

                ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMapAsync(OnMapReadyCallbackCallback);
                //mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                 //       .getMap();
            }
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }
        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);
        // ADDED CODE _ END
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(OnMapReadyCallbackCallback);
        }
    }


    private void setUpMap() {

//        From Xml to map properties
//        map:cameraTargetLat="45.759948"
//        map:cameraTargetLng="4.836593"
//        map:uiCompass="true"
//        map:uiZoomControls="true"
//        map:cameraZoom="13"


        // Only test code, not needed here
//        MarkerOptions markerOpt = new MarkerOptions();
//        markerOpt.position(new LatLng(45.759948, 4.836593));
//        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        markerOpt.title("Lyon-test");
//        mMap.addMarker(markerOpt);
//        CircleOptions circle1 = new CircleOptions();
//        circle1.center(new LatLng(45.759948, 4.836593));
//        circle1.radius(500);
//        int lightgreen_trans = Color.argb(100, 153, 255, 153);
//        int white_trans = Color.argb(255, 255, 255, 255);
//        circle1.fillColor(lightgreen_trans);
//        circle1.strokeColor(white_trans);
//        circle1.strokeWidth(5);
//        circle1.visible(true);
//        mMap.addCircle(circle1);
        // test code - end
    }

    @Override
    public void onBackStackChanged() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
