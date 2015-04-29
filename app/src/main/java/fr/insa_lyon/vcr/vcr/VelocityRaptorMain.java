package fr.insa_lyon.vcr.vcr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.List;

public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private ServerConnection serverConnect;
    String serverUrl;
    List<NameValuePair> requestParams;
    JSONObject infoStationsJSON;
    boolean serverInitOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (savedInstanceState == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        if (!(serverInitOk = serverInit())) {
            finish();
        }
    }

    private boolean serverInit() {
        if (!serverInitOk) {
            serverUrl = "";
            requestParams.add(new BasicNameValuePair("toto", "titi"));
            serverConnect = new ServerConnection();
            infoStationsJSON = serverConnect.makeHttpGet(serverUrl, requestParams);
            if (infoStationsJSON != null) {
                return true;
            }
        }
        return false;
    }


    public void onButtonClickedMain(View v) {
        if (v == findViewById(R.id.but_recherche)) {
            Intent intent = new Intent(this, UserInput.class);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // position, marker, etc -> setUpMap est ici
        mMap.setMyLocationEnabled(true);
        mUiSettings = mMap.getUiSettings();

        // Keep the UI Settings state in sync with the checkboxes.
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.763478, 4.835442), 13));


        // Ajouter les marqueurs des stations vélovs, avec info bulles, etc.. : récupérer ça du serveur.
        MarkerOptions marOpt1 = new MarkerOptions().position(new LatLng(45.759948, 4.836593)).title("Lyon").snippet("Snippet Lyon");
        Marker m = mMap.addMarker(marOpt1);
        m.setTitle("Title changed");
    }


}
