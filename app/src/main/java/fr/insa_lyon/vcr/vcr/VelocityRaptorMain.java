package fr.insa_lyon.vcr.vcr;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fr.insa_lyon.vcr.modele.StationVelov;
import fr.insa_lyon.vcr.reseau.FetchStation;
import fr.insa_lyon.vcr.reseau.UpdateStation;
import fr.insa_lyon.vcr.utilitaires.CustomAnimatorUpdateListener;
import fr.insa_lyon.vcr.utilitaires.CustomInfoWindow;
import fr.insa_lyon.vcr.utilitaires.FinishWithDialog;
import fr.insa_lyon.vcr.utilitaires.MathsUti;
import fr.insa_lyon.vcr.utilitaires.ServerFailureDialog;


public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback, FinishWithDialog {

    // ----------------------------------------------------------------------------------- VARIABLES
    protected GoogleMap mMap;
    protected int circleRadius = 600; // in meters
    protected Circle currentCircle;
    protected boolean isWithdrawMode = false;
    protected final String SERVER_URL = "http://vps165245.ovh.net";

    HashMap<String, StationVelov> mapStations;

    DialogFragment exitDialogFragment;
    boolean serverFailureDetected = false;

    // Download services intent
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
                        Log.d("RECEIVER_DYNA", "Before call to updateStationValues");
                        updateStationValues(json_string);
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
        mapStations = new HashMap<String, StationVelov>();

        // alert dialog in case of server failure
        exitDialogFragment = new ServerFailureDialog();


        if (savedInstanceState == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


        // fetch static data for all stations
        intentStat = new Intent(this, FetchStation.class);
        intentStat.putExtra(FetchStation.SERVER_URL, SERVER_URL + "/station");
        intentStat.putExtra(FetchStation.URL_PARAM_N1, "limit");
        intentStat.putExtra(FetchStation.URL_PARAM_V1, "30");
        startService(intentStat);

        // fetch dynamic data
        intentDyna = new Intent(this, UpdateStation.class);
        intentDyna.putExtra(UpdateStation.SERVER_URL, SERVER_URL + "/lastmeasure");
        intentDyna.putExtra(UpdateStation.URL_PARAM_N1, "limit");
        intentDyna.putExtra(UpdateStation.URL_PARAM_V1, "30");

        // Ajout du listener sur le switch
        Switch switchWithdrawDeposit = (Switch) findViewById(R.id.switchDeposerRetirer);
        switchWithdrawDeposit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isWithdrawMode = !isWithdrawMode;
                //majMarqueurs();
                Log.d("SWITCH LISTENNER", "BEFORE UPDATE MARKER MODE");
                updateMarkerMode();
            }
        });
    }


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
      /*  if (v == findViewById(R.id.but_recherche)) {
            Intent intent = new Intent(this, UserInput.class);
            startActivityForResult(intent, 1);
        }*/
        ResearchDialog dialog = new ResearchDialog(this);
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // on récupère la map via ce callback
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this.getLayoutInflater()));
        // réglages génraux
        mMap.setMyLocationEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
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
                drawCircle(position);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                drawCircle(marker.getPosition());
                return true;
            }
        });
    }

    //------------------------------------------------------------------------------ General Methods

    public void drawCircle(LatLng position) {
        if (currentCircle != null) {
            currentCircle.remove();
            for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
                if (MathsUti.getDistance(entry.getValue().getPosition(), currentCircle.getCenter()) <= circleRadius) {
                    entry.getValue().setSelected(false);
                }
            }
        }
        Circle c = mMap.addCircle(new CircleOptions()
                .center(position)
                .strokeWidth(6)
                .radius(circleRadius)
                .strokeColor(0xFFFFFFFF)
                .fillColor(0x734caf50));

        ValueAnimator vAnimator = new ValueAnimator();
        vAnimator.setIntValues(0, circleRadius);
        vAnimator.setDuration(100);
        vAnimator.setEvaluator(new IntEvaluator());
        vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        CustomAnimatorUpdateListener caul = new CustomAnimatorUpdateListener();
        caul.setCircle(c);
        vAnimator.addUpdateListener(caul);
        vAnimator.start();
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            if (MathsUti.getDistance(entry.getValue().getMarqueur().getPosition(), position) <= circleRadius) {
                entry.getValue().setSelected(true);
            }
        }
        currentCircle = c;
    }

    /**
     * This method mus be called only once, in order to fill the HashMap of StationVelov
     * It gives a JSONObject and en empty marker to the constructor of StationVelov, which do all
     * the rest.
     *
     * @param jsonString Json String that can be parsed to a json array
     */
    private void fillMapStations(String jsonString) {
        try {
            JSONArray jArrayStations = new JSONArray(jsonString);
            JSONObject currentJSON;
            MarkerOptions currentOpt;
            Marker currentMark;
            LatLng currentPosition;
            for (int i = 0; i < jArrayStations.length(); i++) {
                currentJSON = jArrayStations.getJSONObject(i);
                String id = (String) currentJSON.get("id");
                currentPosition = new LatLng(currentJSON.getDouble("latitude"), currentJSON.getDouble("longitude"));
                currentOpt = new MarkerOptions().position(currentPosition);
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
     * @param jsonString Json String that can be parsed to a json array
     */
    protected void updateStationValues(String jsonString) {
        Log.d("UPDATE_STATION_VALUES", "In method");
        try {
            JSONArray jArrayMeasures = new JSONArray(jsonString);
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
                updatedStation.setBikesAndStands(currentMeasure.getInt("available_bikes"), currentMeasure.getInt("available_bike_stands"));
                mapStations.put(stationId, updatedStation);
                Log.d("UPDATE_STATION_VALUES", "Station " + updatedStation.getName() + " has been updated");
            }
        } catch (JSONException j) {
            Log.e("updateStationValues", "Problem when parsing JSON : " + j);
        }
        Log.d("UPDATE_STATION_VALUES", "-----------DONE-----------");
    }

/*    private void majMarqueurs(){
        for(MarqueurPerso m : marqueurs){
            if (isWithdrawMode) {
                m.getMarqueur().setSnippet(m.getStation().getSnippetDeposer());
            }
            else{
                m.getMarqueur().setSnippet(m.getStation().getSnippetRetirer());
            }
            m.getMarqueur().hideInfoWindow();
        }
    }*/


    public void updateMarkerMode() {
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            entry.getValue().setMode(isWithdrawMode);
            Log.d("UPDATE_MARKER_MODE", "Station " + entry.getValue().getName() + " has mode withdraw = " + entry.getValue().getMode());
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
