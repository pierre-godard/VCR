package fr.insa_lyon.vcr.vcr;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class MapsActivity extends FragmentActivity
        implements FragmentManager.OnBackStackChangedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Est-ce qu'on affiche le user input field ou non (sinon on affiche la map).
    private boolean mShowingBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // ADDED CODE
        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            if (mMap == null) {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();
            }
            if (mMap != null) {
                setUpMap();
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
            // Try to obtain the map from the SupportMapFragment.
            // and
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
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
}
