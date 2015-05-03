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

import java.util.ArrayList;
import java.util.List;

import fr.insa_lyon.vcr.modele.MarqueurPerso;
import fr.insa_lyon.vcr.modele.Station;
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
    private boolean isModeDeposer = false;
    List<Station> stations;
    List<MarqueurPerso> marqueurs;

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
                    if (json_string.length() >= 2) {
                        parserStations(json_string);
                        stopService(intentStat);
                        startService(intentDyna);
                    }
                } else {
                    if (!serverFailureDetected) {
                        serverFailureDetected = true;
                        stopService(intentDyna);
                        stopService(intentStat);
                        exitDialogFragment.show(getFragmentManager(), "exitdialogDyna");
                    }
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
                    if (json_string.length() >= 2) {
                        //parserStations(json_string);
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
        stations = new ArrayList<>();
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
        Switch switchDeposerRetirer = (Switch) findViewById(R.id.switchDeposerRetirer);
        switchDeposerRetirer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isModeDeposer = !isModeDeposer;
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
                    for (MarqueurPerso m : marqueurs) {
                        if (MathsUti.getDistance(m.getMarqueur().getPosition(), cercleCourant.getCenter()) <= rayonCercle) {
                            m.getMarqueur().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurperso));
                        }
                    }
                }
                cercleCourant = mMap.addCircle(new CircleOptions()
                        .center(position)
                        .radius(rayonCercle)
                        .strokeColor(0xFF00e676)
                        .fillColor(0x734caf50));
                for(MarqueurPerso m : marqueurs){
                    if(MathsUti.getDistance(m.getMarqueur().getPosition(),position)<=rayonCercle){
                        m.getMarqueur().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        m.getMarqueur().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurpersorouge));
                        m.getMarqueur().setTitle("DETECTE");
                        m.getMarqueur().setAlpha(1f);
                    }
                }
            }
        });

        // créer les marqueurs
        creerMarqueurs();
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

    private void updateStations(String jsonString) {
        // do nothing for the moment.
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
            m.getMarqueur().hideInfoWindow();
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
