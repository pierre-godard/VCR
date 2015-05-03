package fr.insa_lyon.vcr.vcr;

import android.app.DialogFragment;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.insa_lyon.vcr.modele.MarqueurPerso;
import fr.insa_lyon.vcr.modele.Station;
import fr.insa_lyon.vcr.modele.StationVelov;
import fr.insa_lyon.vcr.reseau.FetchStation;
import fr.insa_lyon.vcr.reseau.UpdateStation;
import fr.insa_lyon.vcr.utilitaires.FinishWithDialog;
import fr.insa_lyon.vcr.utilitaires.MathsUti;
import fr.insa_lyon.vcr.utilitaires.ServerFailureDialog;


public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback, FinishWithDialog {

    // ----------------------------------------------------------------------------------- VARIABLES
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private int rayonCercle = 500;
    private Circle cercleCourant;
    private boolean isWithdrawMode = false;
    HashMap<String, Station> stations;
    List<MarqueurPerso> marqueurs;


    // NEW STATION:
    HashMap<String, StationVelov> mapStations;

    DialogFragment exitDialogFragment;
    boolean serverFailureDetected = false;

    // adresse du serveur
    private String server_url = "http://vps165245.ovh.net";
    Intent intentDyna;
    Intent intentStat;


    /**
     * Callback from UpdateStation Service. Will receive the static data about all stations
     * at app launching.
     */
    private BroadcastReceiver receiverStat = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(FetchStation.RESULT);
                if (resultCode == RESULT_OK) {
                    String json_string = bundle.getString(FetchStation.JSON_ARR);
                    Toast.makeText(VelocityRaptorMain.this,
                            "Fetch complete",
                            Toast.LENGTH_LONG).show();
                    if (json_string.length() >= 3) {    // Json is not empty or "[]"
                        //parseStations(json_string);
                        fillMapStations(json_string);
                        stopService(intentStat);
                        startService(intentDyna);
                    }
                } else {
                    stopService(intentStat);
                    exitDialogFragment.show(getFragmentManager(), "exitdialogDyna");
                }
            }
        }
    };

    private BroadcastReceiver receiverDyna = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(UpdateStation.RESULT);
                if (resultCode == RESULT_OK) {
                    String json_string = bundle.getString(UpdateStation.JSON_ARR);
                    Toast.makeText(VelocityRaptorMain.this, "Update complete", Toast.LENGTH_LONG).show();
                    if (json_string.length() >= 3) {  // Json is not empty or "[]"
                        //updateValues(json_string);
                    }
                } else {
                    if (!serverFailureDetected) {
                        serverFailureDetected = true;
                        stopService(intentStat);
                        stopService(intentDyna);
                        exitDialogFragment.show(getFragmentManager(), "exitdialogDyna");
                    }
                }
            }
        }
    };
    //-------------------------------------------------------------------- Activity LifeCyle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        stations = new HashMap<String, Station>();
        marqueurs = new ArrayList<>();

        // alert dialog in case of server failure
        exitDialogFragment = new ServerFailureDialog();


        if (savedInstanceState == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


        // fetch static data for all stations
        intentStat = new Intent(this, FetchStation.class);
        intentStat.putExtra(FetchStation.SERVER_URL, server_url + "/station");
        intentStat.putExtra(FetchStation.URL_PARAM_N1, "limit");
        intentStat.putExtra(FetchStation.URL_PARAM_V1, "30");
        startService(intentStat);

        // fetch dynamic data
        intentDyna = new Intent(this, UpdateStation.class);
        intentDyna.putExtra(UpdateStation.SERVER_URL, server_url + "/lastmeasure");
        intentDyna.putExtra(UpdateStation.URL_PARAM_N1, "limit");
        intentDyna.putExtra(UpdateStation.URL_PARAM_V1, "30");
        //startService(intentDyna);


        // Ajout du listener sur le switch
        Switch switchWithdrawDeposit = (Switch) findViewById(R.id.switchDeposerRetirer);
        switchWithdrawDeposit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isWithdrawMode = !isWithdrawMode;
                majMarqueurs();
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
        registerReceiver(receiverStat, new IntentFilter(FetchStation.NOTIFICATION));
        registerReceiver(receiverDyna, new IntentFilter(UpdateStation.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(intentDyna);
        stopService(intentStat);
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
        // on récupère la map via ce callback
        mMap = googleMap;
        // réglages génraux
        mMap.setMyLocationEnabled(true);
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.763478, 4.835442), 12));

        // listenners sur la map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng position) {
                if(cercleCourant != null){
                    cercleCourant.remove();

                    for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
                        if (MathsUti.getDistance(entry.getValue().getPosition(), cercleCourant.getCenter()) <= rayonCercle) {
                            entry.getValue().getMarqueur().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurperso));
                        }
                    }

                    /*for (MarqueurPerso m : marqueurs) {
                        if (MathsUti.getDistance(m.getMarqueur().getPosition(), cercleCourant.getCenter()) <= rayonCercle) {
                            m.getMarqueur().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurperso));
                        }
                    }*/
                }
                cercleCourant = mMap.addCircle(new CircleOptions()
                        .center(position)
                        .radius(rayonCercle)
                        .strokeColor(0xFF00e676)
                        .fillColor(0x734caf50));

                for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
                    if (MathsUti.getDistance(entry.getValue().getMarqueur().getPosition(), position) <= rayonCercle) {
                        entry.getValue().getMarqueur().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurpersorouge));
                        entry.getValue().getMarqueur().setTitle("DETECTE");
                    }
                }
                
      /*          for(MarqueurPerso m : marqueurs){
                    if(MathsUti.getDistance(m.getMarqueur().getPosition(),position)<=rayonCercle){
                        m.getMarqueur().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        m.getMarqueur().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurpersorouge));
                        m.getMarqueur().setTitle("DETECTE");
                        m.getMarqueur().setAlpha(1f);
                    }
                }*/
            }
        });

        // créer les marqueurs
        //creerMarqueurs();
    }


    //------------------------------------------------------------------------------ General Methods
/*
    private void parseStations(String jsonString) {
        stations.clear();
        String strD = "Taille " + jsonString.length();
        Log.d("JSONString", strD);
        try {
            JSONArray jArrayStations = new JSONArray(jsonString);
            Log.d("##JSONArray##", jArrayStations.get(0).toString());
            for(int i=0; i<jArrayStations.length();i++){
                String id = (String) jArrayStations.getJSONObject(i).get("id");
                stations.put(id, new Station(jArrayStations.getJSONObject(i)));
            }
        }
        catch(JSONException j){
            Log.e("parseStations", "Problème en parsant le JSON");
        }
        Log.d("STATION 1", stations.get(0).getNom() + " // " + stations.get(0).getSnippetDeposer());
        Log.d("Marqueurs", "Création des marqueurs dans parserStations");
        creerMarqueurs();
    }*/


    /**
     * This method mus be called only once, in order to fill the HashMap of StationVelov
     * It gives a JSONObject and en empty marker to the constructor of StationVelov, which do all
     * the rest.
     *
     * @param jsonString
     */
    private void fillMapStations(String jsonString) {
        try {
            JSONArray jArrayStations = new JSONArray(jsonString);
            MarkerOptions currentOpt;
            Marker currentMark;
            LatLng currentPos;
            for (int i = 0; i < jArrayStations.length(); i++) {
                String id = (String) jArrayStations.getJSONObject(i).get("id");
                currentOpt = new MarkerOptions();
                currentMark = mMap.addMarker(currentOpt);
                mapStations.put(id, new StationVelov(jArrayStations.getJSONObject(i), currentMark));
            }
        } catch (JSONException j) {
            Log.e("parseStations", "Problem when parsing JSON");
        }
    }

    /**
     * Method uses a json String received from UpdateStations via receiverDyna to update the number
     * of bikes in all stations.
     *
     * @param json_string
     */
    private void updateStationValues(String json_string) {
        try {
            JSONArray jArrayMeasures = new JSONArray(json_string);
            JSONObject currentStation;
            JSONObject currentMeasure;
            String stationId;
            StationVelov updatedStation;
            for (int i = 0; i < jArrayMeasures.length(); i++) {
                // Find the station whose id is the same as the one in the jsonArray
                currentMeasure = jArrayMeasures.getJSONObject(i);
                currentStation = currentMeasure.getJSONObject("station");
                stationId = currentStation.getString("id");
                updatedStation = mapStations.get(stationId);            // will need to catch exception in case id is not found in hashmap
                updatedStation.setNumberOfBikes(currentMeasure.getInt("available_bikes"));
                updatedStation.setNumberOfFreeBikeStands(currentMeasure.getInt("available_bikes_stands"));
                mapStations.put(stationId, updatedStation);
            }
        } catch (JSONException j) {
            Log.e("updateStationValues", "Problem when parsing JSON");
        }
    }


/*    private void creerMarqueurs(){
        marqueurs.clear();
        MarkerOptions optionsCourantes;
        Marker marqueurCourant;
        for(String key : stations.keySet()){
            Station s = stations.get(key);
            optionsCourantes = new MarkerOptions().position(s.getPosition()).title(s.getNom()).snippet("");
            majMarqueurs();
            marqueurCourant = mMap.addMarker(optionsCourantes);
            marqueurs.add(new MarqueurPerso(s, marqueurCourant));
        }
    }*/

    private void majMarqueurs(){
        for(MarqueurPerso m : marqueurs){
            if (isWithdrawMode) {
                m.getMarqueur().setSnippet(m.getStation().getSnippetDeposer());
            }
            else{
                m.getMarqueur().setSnippet(m.getStation().getSnippetRetirer());
            }
            m.getMarqueur().hideInfoWindow();
        }
    }


    public void updateMarkerMode() {
        Iterator it = mapStations.keySet().iterator();
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            entry.getValue().setMode(isWithdrawMode);
        }
    }


    /**
     * Method from interface FinishWithDialog used in order to kill application in case server is
     * not reachable
     */
    @Override
    public void onChoose() {
        // Quite a lot of things to add here on order to finsh the activity in a "cleaner"  way
        finish();
    }
}
