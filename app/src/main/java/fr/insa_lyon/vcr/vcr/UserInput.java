package fr.insa_lyon.vcr.vcr;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceTypes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import fr.insa_lyon.vcr.modele.ResultatPartiel;


public class UserInput extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

   /* private void majAdapter(String recherche, final ArrayAdapter adapter){
        LatLng sudOuestLyon = new LatLng(45.708931, 4.745801);
        LatLng nordEstLyon = new LatLng(45.805918, 4.924447);
        LatLngBounds rectangleLyon = new LatLngBounds(sudOuestLyon,nordEstLyon);

        adapter.clear();
        GoogleApiClient client = new GoogleApiClient.Builder(getApplicationContext()).addApi(Places.GEO_DATA_API).build();
        client.connect();
        PendingResult pResult =
                Places.GeoDataApi.getAutocompletePredictions(client, recherche,
                        rectangleLyon, null);
        pResult.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
            @Override
            public void onResult(AutocompletePredictionBuffer result) {
                for(AutocompletePrediction prediction : result){
                    adapter.add(new ResultatPartiel(prediction.getMatchedSubstrings().get(0).toString(),prediction.getPlaceId()));
                }
            }
        });
    }*/

// début test

    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<String> adp;
    private AutoCompleteTextView t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        //instance API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
        List<String> resultList = new ArrayList<>();
        adp = new ArrayAdapter<String>(this,
        android.R.layout.simple_dropdown_item_1line, resultList);
        adp.setNotifyOnChange(true);
        t1 = (AutoCompleteTextView) findViewById(R.id.autoCompleteDepart);
        t1.setThreshold(1);
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        new GetPredictions().execute("boulevard stalingrad lyon");
        //adp.add("work");
        t1.setAdapter(adp);
    }


    private class GetPredictions extends AsyncTask<String,Void,AutocompletePredictionBuffer> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */

        // Try to understand how AsyncTask works !!!!!!!
        @Override
         protected AutocompletePredictionBuffer doInBackground(String... test) {

            AutoCompleteTextView t1 = (AutoCompleteTextView)
                    findViewById(R.id.autoCompleteDepart);

            LatLng sudOuestLyon = new LatLng(45.708931, 4.745801);
            LatLng nordEstLyon = new LatLng(45.805918, 4.924447);
            LatLngBounds rectangleLyon = new LatLngBounds(sudOuestLyon, nordEstLyon);
            Set<Integer> placeTypes = new TreeSet<>();
            placeTypes.add(Place.TYPE_GEOCODE);
            AutocompleteFilter filtre = AutocompleteFilter.create(placeTypes);
            PendingResult<AutocompletePredictionBuffer> results  =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, test[0],
                            rectangleLyon, filtre );
            Log.d("PATAPON","Debut de await");
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await();
            Log.d("PATAPON","Fin de doInBackground");
            return autocompletePredictions;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        @Override
         protected void onPostExecute(AutocompletePredictionBuffer autocompletePredictions) {
            Log.d("PATAPON","Debut boucle buffer");
            for (AutocompletePrediction prediction : autocompletePredictions) {
                Log.d("PATAPON", prediction.getDescription());
                Log.d("BANDE D'ENCULES", prediction.getPlaceTypes().toString());
                adp.add(prediction.getDescription());
            }
            Log.d("PATAPON","Fin boucle buffer");
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {

        List<String> listeDepart = new ArrayList<>();

        final ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, listeDepart);

        adp.add("fuck");

    //    t1.setThreshold(1);
    //    t1.setAdapter(adp);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        AutoCompleteTextView t1 = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteDepart);


        List<String> listeDepart = new ArrayList<>();

        final ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, listeDepart);

        adp.add("fuck");

      //  t1.setThreshold(1);
      //  t1.setAdapter(adp);

    }

/* fin test

        List<ResultatPartiel> listeDepart = new ArrayList<>();
        List<ResultatPartiel> listeArrivee = new ArrayList<>();

        final ArrayAdapter<ResultatPartiel> adapterDepart = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listeDepart);
        final ArrayAdapter<ResultatPartiel> adapterArrivee = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listeArrivee);
        adapterDepart.setNotifyOnChange(true);
        adapterArrivee.setNotifyOnChange(true);

        final AutoCompleteTextView autoCompleteDepart = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteDepart);
        final AutoCompleteTextView autoCompleteArrivee = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteArrivee);
        autoCompleteDepart.setAdapter(adapterDepart);
        autoCompleteArrivee.setAdapter(adapterArrivee);

        autoCompleteDepart.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                majAdapter(s.toString(), (ArrayAdapter) autoCompleteDepart.getAdapter());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                //rienDuTout
            }

            @Override
            public void afterTextChanged(Editable s) {
                //rienDuTout
            }
        });

        autoCompleteArrivee.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                majAdapter(s.toString(), (ArrayAdapter) autoCompleteArrivee.getAdapter());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                //rienDuTout
            }

            @Override
            public void afterTextChanged(Editable s) {
                //rienDuTout
            }
        });

        autoCompleteDepart.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                ResultatPartiel selected = (ResultatPartiel) arg0.getAdapter().getItem(arg2);
                Log.d("SELECTION", selected.getIdentifiantPlace() + "sélectionné");
            }
        });

    }
*/


}
