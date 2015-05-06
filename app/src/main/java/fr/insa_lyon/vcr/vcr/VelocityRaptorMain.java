package fr.insa_lyon.vcr.vcr;

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
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insa_lyon.vcr.modele.StationVelov;
import fr.insa_lyon.vcr.reseau.FetchStation;
import fr.insa_lyon.vcr.reseau.UpdatePrediction;
import fr.insa_lyon.vcr.reseau.UpdateStation;
import fr.insa_lyon.vcr.utilitaires.ClusterIconRenderer;
import fr.insa_lyon.vcr.utilitaires.CustomClusterManager;
import fr.insa_lyon.vcr.utilitaires.CustomInfoWindow;
import fr.insa_lyon.vcr.utilitaires.FinishWithDialog;
import fr.insa_lyon.vcr.utilitaires.MathsUti;
import fr.insa_lyon.vcr.utilitaires.ServerFailureDialog;


public class VelocityRaptorMain extends FragmentActivity implements OnMapReadyCallback, FinishWithDialog {

    public static final String ALARM_NOTIFICATION = "fr.insa_lyon.vcr.alarm";
    public static final int ALARM_DURATION = 120; // in seconds
    protected final String SERVER_URL = "http://vps165245.ovh.net";
    // ----------------------------------------------------------------------------------- VARIABLES
    protected GoogleMap mMap;
    protected int circleRadius = 500; // in meters
    protected Circle currentCircle;
    protected boolean isWithdrawMode = true;
    HashMap<String, StationVelov> mapStations;
    List<String> idStationsSelectionnees;
    LatLng lastCirclePosition;
    DialogFragment exitDialogFragment;
    boolean serverFailureDetected = false;
    SlidingUpPanelLayout slidingUp;
    TextView txt_Predict;
    SeekBar seekbarPredict;
    TextView txt_Radius;
    SeekBar seekbarRadius;
    // Download services intent
    Intent intentDyna;
    Intent intentStat;
    Intent intentAlarm;
    Intent intentPredictions;
    PendingIntent pendingIntentAlarm;
    AlarmManager alarmManager;
    private int numberPredictionsArrived = 0;
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
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ALARM_DURATION * 1000, pendingIntentAlarm);
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

    private BroadcastReceiver receiverUpdatePrediction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("UPDATE_PREDICTIONS", "Ici PapaTango, bien reçu DeltaCharlie");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(UpdateStation.RESULT);
                if (resultCode == RESULT_OK) {
                    String json_string = bundle.getString(UpdatePrediction.JSON_ARR);
                    if (json_string.length() >= 3) {  // Json is not empty or "[]"
                        Log.d("UPDATE_PREDICTIONS", "Before call to updatePredictionsValues");
                        updateStationPredictions(json_string);
                    }
                } else {
                    if (!serverFailureDetected) {
                        Log.e("UPDATE_PREDICTIONS","Et galère...");
                        serverFailureDetected = true;
                        stopService(intentStat);
                        stopService(intentDyna);
                        stopService(intentPredictions);
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
        idStationsSelectionnees = new ArrayList<String>();
        // alert dialog in case of server failure
        exitDialogFragment = new ServerFailureDialog();

        if (savedInstanceState == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


        slidingUp = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUp.setParalaxOffset(50);
        slidingUp.setOverlayed(false);

        try {
            this.getActionBar().hide();
        } catch (NullPointerException e){
            Log.e("ACTION_BAR", "ActionBar hiding failed");
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


        txt_Predict = (TextView) findViewById(R.id.predictValue);
        seekbarPredict = (SeekBar) findViewById(R.id.seekBar);
        seekbarPredict.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0){
                    txt_Predict.setText("Temps réel");
                } else {
                    if (progress > 60){
                        int progressH = (int) Math.floor(((double) progress) / 60);
                        int progressM = progress - (progressH*60);
                        if (progressM < 10) {
                            txt_Predict.setText("Prédiction à "+progressH+"h et 0"+progressM+" min");
                        } else {
                            txt_Predict.setText("Prédiction à "+progressH+"h et "+progressM+" min");
                        }
                    } else {
                        if (progress < 10) {
                            txt_Predict.setText("Prédiction à 0"+progress+" min");
                        } else {
                            txt_Predict.setText("Prédiction à "+progress+" min");
                        }
                    }
                }
                // TODO fetch predictions for the right time if there are stations in the circle
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        txt_Radius = (TextView) findViewById(R.id.radiusValue);
        seekbarRadius = (SeekBar) findViewById(R.id.seekBarRadius);
        seekbarRadius.setProgress(500);
        seekbarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int MIN = 100;
                if (progress < MIN) {
                    progress += MIN;
                    txt_Radius.setText("Rayon de recherche de "+progress+"m");
                } else{
                    txt_Radius.setText("Rayon de recherche de " + progress + "m");
                }
                circleRadius = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (lastCirclePosition != null) {
                    drawCircle(lastCirclePosition);
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiverStat, new IntentFilter(FetchStation.NOTIFICATION));
        registerReceiver(receiverDyna, new IntentFilter(UpdateStation.NOTIFICATION));
        registerReceiver(receiverAlarm, new IntentFilter(ALARM_NOTIFICATION));
        registerReceiver(receiverUpdatePrediction, new IntentFilter(UpdatePrediction.NOTIFICATION));
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
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<StationVelov>() {
            @Override
            public boolean onClusterClick(Cluster<StationVelov> stationVelovCluster) {
                return false;
            }
        });
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
                    reloadMarker(entry.getValue());
                }
            }
        }
        setAlphaStations(false);
        numberPredictionsArrived = 0;
        idStationsSelectionnees.clear();
        Circle c = mMap.addCircle(new CircleOptions()
                .center(position)
                .strokeWidth(0)
                .radius(circleRadius)
                .strokeColor(0xFFFFFFFF)
                .fillColor(0x730080f1));

        lastCirclePosition = position;

        /*ValueAnimator vAnimator = new ValueAnimator();
        vAnimator.setIntValues(0, circleRadius);
        vAnimator.setDuration(100);
        vAnimator.setEvaluator(new IntEvaluator());
        vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        CustomAnimatorUpdateListener caul = new CustomAnimatorUpdateListener();
        caul.setCircle(c);
        vAnimator.addUpdateListener(caul);
        vAnimator.start();
        */
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            if (MathsUti.getDistance(entry.getValue().getPosition(), position) <= circleRadius) {
                entry.getValue().setSelected(true);
                idStationsSelectionnees.add(entry.getValue().getId());
                reloadMarker(entry.getValue());
            }
        }
        currentCircle = c;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        Log.d("DRAW CIRCLE", "Before updatePredictions");
        updatePredictions();
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
                Log.d("UPDATE STATION =>", "Station " + updatedStation.getTitle() + " being updated");
                mapStations.put(stationId, updatedStation);
            }
        } catch (JSONException j) {
            Log.e("updateStationValues", "Problem when parsing JSON : " + j);
        }
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            reloadMarker(entry.getValue());
        }

    }

    protected void updateStationPredictions(String jsonString) {
        try {
            Log.d("UPDATE_PREDICTIONS","Allez on y va on update les prédictions !");
            JSONArray jArrayPredictions = new JSONArray(jsonString);
            JSONObject currentStation;
            String stationId;
            int bikePrediction;
            int bikeStandPrediction;
            float predictionConfidence;
            StationVelov updatedStation;
            for (int i = 0; i < jArrayPredictions.length(); i++) {
                // Find the station whose id is the same as the one in the jsonArray
                currentStation = jArrayPredictions.getJSONObject(i);
                stationId = currentStation.getString("station");
                bikePrediction = currentStation.getInt("predict_bikes");
                bikeStandPrediction = currentStation.getInt("predict_bike_stands");
                predictionConfidence = (float)currentStation.getDouble("confidence");
                updatedStation = mapStations.get(stationId);
                updatedStation.setNumberOfBikes_predict(bikePrediction);
                updatedStation.setNumberOfFreeBikeStands_predict(bikeStandPrediction);
                updatedStation.setPredictionConfidence(predictionConfidence);
                Log.d("UPDATE_PREDICTIONS", "Station " + updatedStation.getTitle() + " being updated");
                mapStations.put(stationId, updatedStation);
            }
        } catch (JSONException j) {
            Log.e("UPDATE_PREDICTIONS", "Problem when parsing JSON : " + j);
        }
        setAlphaStations(true);
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
        stopService(intentAlarm);
        stopService(intentDyna);
        stopService(intentStat);
        stopService(intentPredictions);
        finish();
    }

    public void drawAround(Marker marker){
        Log.d("DRAW AROUND", "MethodStarts");
        boolean isMarkerSelected = false;
        Map.Entry<String,StationVelov> entryMarker = null;
        for (Map.Entry<String, StationVelov> entry : mapStations.entrySet()) {
            // title of current station
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
        if(entryMarker != null) {
            entryMarker.getValue().switchInfoWindowShown();
            if (entryMarker.getValue().isInfoWindowShown()) {
                marker.showInfoWindow();
            } else {
                marker.hideInfoWindow();
            }
        }
        Log.d("DRAW AROUND", "MethodEnds");
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
                Log.d("RELOAD MARKER =>", "Station "+m.getTitle()+" being updated");
                m.setIcon(item.getIcon());
                m.setSnippet(item.getSnippet());
                break;
            }
        }
    }

    public void updatePredictions(){
        Log.d("UPDATE PREDICTION", "In update prediction");
        intentPredictions = new Intent(this, UpdatePrediction.class);
        String idStations="";
        for(String idStation : idStationsSelectionnees) {
            idStations+=idStation+";";
        }
        intentPredictions.putExtra(UpdatePrediction.SERVER_URL, SERVER_URL + "/prediction/analysis");
        intentPredictions.putExtra(UpdatePrediction.ID_STATIONS, idStations);
        intentPredictions.putExtra(UpdatePrediction.PREDICTION_TIME, "5"); //TODO: Remplacer par temps indiqué
        startService(intentPredictions);
    }

    public void setAlphaStations(boolean areInNewCircle){
        StationVelov currentStation;
        if(!areInNewCircle){
            for(String idStation : idStationsSelectionnees){
                currentStation = mapStations.get(idStation);
                for(Marker m: mClusterManager.getMarkerCollection().getMarkers()){
                    if(currentStation.getTitle().equals(m.getTitle())){
                        m.setAlpha(1);
                    }
                }
            }
        }
        else {
            //getPredictions
            float scoreCourant;
            float scoreMax = Integer.MIN_VALUE;
            float scoreMin = Integer.MAX_VALUE;
            for (String idStation : idStationsSelectionnees) {
                currentStation = mapStations.get(idStation);
                if (isWithdrawMode) {
                    scoreCourant = currentStation.getNumberOfBikes_predict() * currentStation.getPredictionConfidence();
                } else {
                    scoreCourant = currentStation.getNumberOfFreeBikeStands_predict() * currentStation.getPredictionConfidence();
                }
                if (scoreCourant < scoreMin) {
                    scoreMin = scoreCourant;
                }
                if (scoreCourant > scoreMax) {
                    scoreMax = scoreCourant;
                }
            }
            for (String idStation : idStationsSelectionnees) {
                currentStation = mapStations.get(idStation);
                if (isWithdrawMode) {
                    scoreCourant = currentStation.getNumberOfBikes_predict() * currentStation.getPredictionConfidence();
                } else {
                    scoreCourant = currentStation.getNumberOfFreeBikeStands_predict() * currentStation.getPredictionConfidence();
                }
                for (Marker m : mClusterManager.getMarkerCollection().getMarkers()) {
                    if (currentStation.getTitle().equals(m.getTitle())) {
                        if(scoreMax != scoreMin){
                            m.setAlpha(((scoreCourant-scoreMin)/(scoreMax-scoreMin)));
                        }
                        else{
                            m.setAlpha(1);
                        }
                    }
                }
            }
        }
    }
}
