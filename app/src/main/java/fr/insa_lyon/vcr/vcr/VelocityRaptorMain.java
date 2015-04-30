package fr.insa_lyon.vcr.vcr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import fr.insa_lyon.vcr.modele.MarqueurPerso;
import fr.insa_lyon.vcr.modele.Station;
import fr.insa_lyon.vcr.reseau.FetchStation;
import fr.insa_lyon.vcr.utilitaires.MathsUti;


public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback {

    // ----------------------------------------------------------------------------------- VARIABLES
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private int rayonCercle = 500;
    private Circle cercleCourant;
    private boolean isModeDeposer = false;
    List<Station> stations;
    List<MarqueurPerso> marqueurs;

    // adresse du serveur
    private String server_url = "http://vps165245.ovh.net/station";

    /**
     * Receiver chargé de récupérer les données dans le message broadcasté par
     * le UpdateStation et de les retransformer en JSON avant de les passer à une méthode
     * qui va mettre à jour la carte
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String json_string = bundle.getString(FetchStation.JSON_ARR);
                int resultCode = bundle.getInt(FetchStation.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(VelocityRaptorMain.this,
                            "Update complete",
                            Toast.LENGTH_LONG).show();
                    parserStations(json_string);

                } else {
                    Toast.makeText(VelocityRaptorMain.this, "Update failed",
                            Toast.LENGTH_LONG).show();

                }
            }
        }
    };

    //-------------------------------------------------------------------- Activity LifeCyle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        stations = new ArrayList<>();
        marqueurs = new ArrayList<>();


        if (savedInstanceState == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


        Intent intent = new Intent(this, FetchStation.class);
        intent.putExtra(FetchStation.SERVER_URL, server_url);
        intent.putExtra(FetchStation.URL_PARAM_N1, "limit");
        intent.putExtra(FetchStation.URL_PARAM_V1, "0");
        startService(intent);
        Switch switchDeposerRetirer = (Switch) findViewById(R.id.switchDeposerRetirer);
        switchDeposerRetirer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isModeDeposer = true;
            }
        });
    }


    /**
     * Permet de "s'enregistrer" pour être contacté dans le cas d'un envoi de broadcast par
     * UpdateStaion (l'intent contenant le JSON)
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(FetchStation.NOTIFICATION));
    }


    //----------------------------------------------------------------------- Listenners - CallBacks

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
                        .strokeColor(0xFF00e676)
                        .fillColor(0x734caf50));
                for(MarqueurPerso m : marqueurs){
                    if(MathsUti.getDistance(m.getMarqueur().getPosition(),position)<=rayonCercle){
                        m.getMarqueur().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        m.getMarqueur().setTitle("DETECTE");
                        m.getMarqueur().setAlpha(1f);
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


    //------------------------------------------------------------------------------ General Methods

    private void parserStations(String jsonString) {
        stations.clear();
        String strD = "Taille " + jsonString.length();
        Log.d("JSONString", strD);
        try {
            JSONArray jArrayStations = new JSONArray(jsonString);
            Log.d("##JSONArray##", jArrayStations.get(0).toString());
            for(int i=0; i<jArrayStations.length();i++){
                stations.add(new Station(jArrayStations.getJSONObject(i)));
            }
        }
        catch(JSONException j){
            Log.e("ACTIVITY_MAP", "Problème en parsant le JSON");
        }
        Log.d("STATION 1", stations.get(0).getNom() + " // " + stations.get(0).getSnippetDeposer());
        Log.d("Marqueurs", "Création des marqueurs dans parserStations");
        creerMarqueurs();
    }

    private void creerMarqueurs(){
        marqueurs.clear();
        MarkerOptions optionsCourantes;
        Marker marqueurCourant;
        for(Station s: stations){
            optionsCourantes = new MarkerOptions().position(s.getPosition()).title(s.getNom()).snippet("");
            majMarqueurs();
            marqueurCourant = mMap.addMarker(optionsCourantes);
            marqueurs.add(new MarqueurPerso(s, marqueurCourant));
        }
    }

    private void majMarqueurs(){
        for(MarqueurPerso m : marqueurs){
            if(isModeDeposer){
                m.getMarqueur().setSnippet(m.getStation().getSnippetDeposer());
            }
            else{
                m.getMarqueur().setSnippet(m.getStation().getSnippetRetirer());
            }
        }
    }

}
