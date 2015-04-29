package fr.insa_lyon.vcr.vcr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.insa_lyon.vcr.modele.MarqueurPerso;
import fr.insa_lyon.vcr.modele.Station;
import fr.insa_lyon.vcr.utilitaires.MathsUti;

public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private ServerConnection serverConnect;
    private int rayonCercle = 500;
    private String serverUrl;
    private List<NameValuePair> requestParams;
    private List<Station> stations;
    private List<MarqueurPerso> marqueurs;
    private JSONObject infoStationsJSON;
    private boolean serverInitOk = false;
    private Circle cercleCourant;
    private boolean isModeDeposer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        requestParams = new ArrayList<>();
        stations = new ArrayList<>();
        marqueurs = new ArrayList<>();

        if (savedInstanceState == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        Switch switchDeposerRetirer = (Switch) findViewById(R.id.switchDeposerRetirer);
        switchDeposerRetirer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isModeDeposer = !isModeDeposer;
                mettreAJourMarqueurs();
            }
        });
        /*if (!(serverInitOk = serverInit())) {
            finish();
        }*/

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng position) {
                if(cercleCourant != null){
                   cercleCourant.remove();
                }
                cercleCourant = mMap.addCircle(new CircleOptions()
                        .center(position)
                        .radius(rayonCercle)
                        .strokeColor(Color.BLUE)
                        .fillColor(0x7333CCFF));
                for(MarqueurPerso m : marqueurs){
                    if(MathsUti.getDistance(m.getMarqueur().getPosition(),position)<=rayonCercle){
                        m.getMarqueur().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        m.getMarqueur().setTitle("DETECTE");
                    }
                }
            }
        });

        creerMarqueurs();
        // Ajouter les marqueurs des stations vélovs, avec info bulles, etc.. : récupérer ça du serveur.
       /* MarkerOptions marOpt1 = new MarkerOptions().position(new LatLng(45.759948, 4.836593)).title("Lyon").snippet("Snippet Lyon");
        Marker m = mMap.addMarker(marOpt1);
        m.setTitle("Title changed");*/
    }


    private void parserStations(){
        stations.clear();
        String jsonString = "stations:"+infoStationsJSON.toString();
        try {
            JSONObject jsonStations = new JSONObject(jsonString);
            JSONArray jArrayStations = jsonStations.getJSONArray("stations");
            for(int i=0; i<jArrayStations.length();i++){
                stations.add(new Station(jArrayStations.getJSONObject(i)));
            }
        }
        catch(JSONException j){
            Log.e("ACTIVITY_MAP", "Y'a eut une couille en parsant le json :(");
        }
    }

    private void mettreAJourMarqueurs(){
        for(MarqueurPerso m : marqueurs){
            if(isModeDeposer) {
                m.getMarqueur().setSnippet(m.getStation().getSnippetDeposerVelo());
            }
            else{
                m.getMarqueur().setSnippet(m.getStation().getSnippetRetirerVelo());
            }
        }
    }

    private void creerMarqueurs(){
        marqueurs.clear();
        MarkerOptions optionsCourantes;
        Marker marqueurCourant;
        for(Station s: stations){
            optionsCourantes = new MarkerOptions().position(s.getPosition()).title(s.getNom()).snippet("");
            marqueurCourant = mMap.addMarker(optionsCourantes);
            marqueurs.add(new MarqueurPerso(s,marqueurCourant));
        }
        mettreAJourMarqueurs();
            /* MarkerOptions marOpt1 = ;
        Marker m = mMap.addMarker(marOpt1);
        m.setTitle("Title changed");*/
    }

}
