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
import com.google.android.gms.location.places.PlaceBuffer;
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


public class UserInput extends Activity {

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
