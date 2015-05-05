package fr.insa_lyon.vcr.vcr;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fr.insa_lyon.vcr.modele.StationVelov;
import fr.insa_lyon.vcr.reseau.FetchStation;
import fr.insa_lyon.vcr.reseau.UpdateStation;
import fr.insa_lyon.vcr.utilitaires.ClusterIconRenderer;
import fr.insa_lyon.vcr.utilitaires.CustomAnimatorUpdateListener;
import fr.insa_lyon.vcr.utilitaires.CustomClusterManager;
import fr.insa_lyon.vcr.utilitaires.CustomInfoWindow;
import fr.insa_lyon.vcr.utilitaires.FinishWithDialog;
import fr.insa_lyon.vcr.utilitaires.MathsUti;
import fr.insa_lyon.vcr.utilitaires.ServerFailureDialog;


public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback, FinishWithDialog {

    public static final String ALARM_NOTIFICATION = "fr.insa_lyon.vcr.alarm";
    public static final int ALARM_DURATION = 20; // in seconds
    protected final String SERVER_URL = "http://vps165245.ovh.net";
    // ----------------------------------------------------------------------------------- VARIABLES
    protected GoogleMap mMap;
    protected int circleRadius = 600; // in meters
    protected Circle currentCircle;
    protected boolean isWithdrawMode = true;
    HashMap<String, StationVelov> mapStations;

    DialogFragment exitDialogFragment;
    boolean serverFailureDetected = false;

    // Download services intent
    Intent intentDyna;
    Intent intentStat;
    Intent intentAlarm;
    PendingIntent pendingIntentAlarm;
    AlarmManager alarmManager;

    private ClusterManager<StationVelov> mClusterManager;
    private ClusterIconRenderer mClusterIconRenderer;


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
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 20 * 1000, pendingIntentAlarm);
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

    private BroadcastReceiver receiverAlarm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(VelocityRaptorMain.this, "Reload stations", Toast.LENGTH_LONG).show();
            context.startService(intentDyna);
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


        // ####### Fetch static data for all stations #######
        intentStat = new Intent(this, FetchStation.class);
        intentStat.putExtra(FetchStation.SERVER_URL, SERVER_URL + "/station");
        intentStat.putExtra(FetchStation.URL_PARAM_N1, "limit");
        intentStat.putExtra(FetchStation.URL_PARAM_V1, "0");
        startService(intentStat);

        // ####### Fetch dynamic data #######
        intentDyna = new Intent(this, UpdateStation.class);
        intentDyna.putExtra(UpdateStation.SERVER_URL, SERVER_URL + "/lastmeasure");
        intentDyna.putExtra(UpdateStation.URL_PARAM_N1, "limit");
        intentDyna.putExtra(UpdateStation.URL_PARAM_V1, "0");

        // Setting up the alarm manager that will be used for fetching dynamic data
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        intentAlarm = new Intent(ALARM_NOTIFICATION);
        pendingIntentAlarm = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intentAlarm, 0);

        // Ajout du listener sur le switch
        Switch switchWithdrawDeposit = (Switch) findViewById(R.id.switchDeposerRetirer);
        switchWithdrawDeposit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isWithdrawMode = !isWithdrawMode;
                // Update mode : change snippet and icon.
                updateStationMode();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiverStat, new IntentFilter(FetchStation.NOTIFICATION));
        registerReceiver(receiverDyna, new IntentFilter(UpdateStation.NOTIFICATION));
        registerReceiver(receiverAlarm, new IntentFilter(ALARM_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(intentDyna);
        stopService(intentStat);
    }


    //----------------------------------------------------------------------- Listenners - CallBacks

    /**
     * Click on button "Recherche" in order to display search popup.
     *
     * @param v
     */
    public void onButtonClickedMain(View v) {
      /*  if (v == findViewById(R.id.but_recherche)) {
            Intent intent = new Intent(this, UserInput.class);
            startActivityForResult(intent, 1);
        }*/

        DialogFragment newFragment = new ResearchDialog();
        newFragment.show(getFragmentManager(), "search");

        /* ResearchDialog dialog = new ResearchDialog(this);
         dialog.show();*/
    }

    /**
     * Callback from getMapAsync : set up map and clustering on map
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this.getLayoutInflater()));
        mMap.setMyLocationEnabled(false);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        // Camera above Lyon
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.763478, 4.835442), 12));


        // ######-------------------------------------------------------------------##### Clustering
        MarkerManager markerManager = new MarkerManager(mMap);
        mClusterManager = new CustomClusterManager(this, mMap, this);
        // Grid based display for Clusters
        //mClusterManager.setAlgorithm(new GridBasedAlgorithm<StationVelov>());
        // add custom Icon renderer.
        mClusterIconRenderer = new ClusterIconRenderer(this, mMap, mClusterManager);
        mClusterManager.setRenderer(mClusterIconRenderer);
       // mClusterManager.getMarkerCollection().
        // listenners sur la map
        mMap.setOnMarkerClickListener(mClusterManager);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng position) {
                drawCircle(position);
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
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
                .strokeWidth(0)
                .radius(circleRadius)
                .strokeColor(0xFFFFFFFF)
                .fillColor(0x730080f1));

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
            if (MathsUti.getDistance(entry.getValue().getPosition(), position) <= circleRadius) {
                entry.getValue().setSelected(true);
                reloadMarker(entry.getValue());
            }
        }
        currentCircle = c;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
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
            StationVelov currentStation;
            for (int i = 0; i < jArrayStations.length(); i++) {
                currentJSON = jArrayStations.getJSONObject(i);
                String id = (String) currentJSON.get("id");
                currentPosition = new LatLng(currentJSON.getDouble("latitude"), currentJSON.getDouble("longitude"));
                //currentOpt = new MarkerOptions().position(currentPosition);
                //currentMark = mMap.addMarker(currentOpt);
                //currentStation = new StationVelov(jArrayStations.getJSONObject(i), currentMark);
                currentStation = new StationVelov(jArrayStations.getJSONObject(i));
                mapStations.put(id, currentStation);
            }
        } catch (JSONException j) {
            Log.e("parseStations", "Problem when parsing JSON");
        }

        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            mClusterManager.addItem(entry.getValue());
        }
        mClusterManager.cluster();
    }


    /**
     * Method uses a json String received from UpdateStations via receiverDyna to update the number
     * of bikes in all stations.
     *
     * @param jsonString Json String that can be parsed to a json array
     */
    protected void updateStationValues(String jsonString) {
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
            }
        } catch (JSONException j) {
            Log.e("updateStationValues", "Problem when parsing JSON : " + j);
        }
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            reloadMarker(entry.getValue());
        }

    }


    public void updateMarkerMode() {
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            entry.getValue().setMode(isWithdrawMode);
            reloadMarker(entry.getValue());
            //Log.d("UPDATE_MARKER_MODE", "Station " + entry.getValue().getTitle() + " has mode withdraw = " + entry.getValue().getMode());
        }
    }



    public void updateStationMode() {
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            entry.getValue().setMode(isWithdrawMode);
            reloadMarker(entry.getValue());
            //Log.d("UPDATE_MARKER_MODE", "Station " + entry.getValue().getTitle() + " has mode withdraw = " + entry.getValue().getMode());
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

    public void drawAround(Marker marker){
        boolean isMarkerSelected = false;
        Map.Entry<String,StationVelov> entryMarker = null;
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            // title of current station
            Log.d("PATAPON", "DESSIN AUTOUR DU MARQUEUR");
            String strId = entry.getValue().getTitle();
            if (strId.equals(marker.getTitle())) {
                entryMarker = entry;
                if (entry.getValue().isSelected()) {
                    // marker selected
                    isMarkerSelected = true;
                }
                break;
            }
        }
        // draw circle around marker
        if (!isMarkerSelected) {
            drawCircle(marker.getPosition());
        }

        Log.d("PATAPON", "MARKER TROUVE DANS LA MAP");
        entryMarker.getValue().switchInfoWindowShown();
        if(entryMarker.getValue().isInfoWindowShown()){
            Log.d("PATAPON","ON AFFICHE");
            marker.showInfoWindow();
        }
        else{
            Log.d("PATAPON", "C'EST CA CACHE TOI");
            marker.hideInfoWindow();
        }
    }

    /**
     * Workarround to repaint markers
     *
     * @param item item to repaint
     */
    public void reloadMarker(StationVelov item) {
        MarkerManager.Collection markerCollection = mClusterManager.getMarkerCollection();
        Collection<Marker> markers = markerCollection.getMarkers();
        String strId = item.getTitle();
        for (Marker m : markers) {
            if (strId.equals(m.getTitle())) {
                m.setIcon(item.getIcon());
                m.setSnippet(item.getSnippet());
                break;
            }
        }

    }
}
