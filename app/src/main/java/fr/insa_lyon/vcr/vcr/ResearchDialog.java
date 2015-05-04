package fr.insa_lyon.vcr.vcr;


import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import fr.insa_lyon.vcr.modele.ResultatPartiel;
import fr.insa_lyon.vcr.utilitaires.UsefulConstants;

/**
 * TODO: document your custom view class.
 */
public class ResearchDialog extends Dialog {


    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<ResultatPartiel> adp;
    private AutoCompleteTextView t1;
    private Activity activite;

    public ResearchDialog(Activity a) {
        super(a);
        activite = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.research_dialog);

        //instance API
        mGoogleApiClient = new GoogleApiClient.Builder(this.getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mGoogleApiClient.connect();
        List<ResultatPartiel> resultList = new ArrayList<>();
        adp = new ArrayAdapter<ResultatPartiel>(this.getContext(),
                android.R.layout.simple_dropdown_item_1line, resultList);
        adp.setNotifyOnChange(true);
        t1 = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        t1.setThreshold(1);
        t1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Rien du tout
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new GetPredictions().execute(s.toString());
                t1.setAdapter(adp);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Rien du tout
            }
        });

        t1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new GetPlace().execute(((ResultatPartiel)parent.getAdapter().getItem(position)).getIdentifiantPlace());
            }
        });
    }


    private class GetPredictions extends AsyncTask<String,Void,AutocompletePredictionBuffer> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */

        // Try to understand how AsyncTask works !!!!!!!
        @Override
        protected AutocompletePredictionBuffer doInBackground(String... recherche) {

            PendingResult<AutocompletePredictionBuffer> results  =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, recherche[0],
                            UsefulConstants.rectangleLyon, null );
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await();
            return autocompletePredictions;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        @Override
        protected void onPostExecute(AutocompletePredictionBuffer autocompletePredictions) {
            adp.clear();
            for (AutocompletePrediction prediction : autocompletePredictions) {
                adp.add(new ResultatPartiel(prediction.getDescription(),prediction.getPlaceId()));
            }
            autocompletePredictions.release();
        }
    }

    private class GetPlace extends AsyncTask<String,Void,PlaceBuffer> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */

        // Try to understand how AsyncTask works !!!!!!!
        @Override
        protected PlaceBuffer doInBackground(String... placeId) {
            PendingResult<PlaceBuffer> result  = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId[0]);
            PlaceBuffer place = result.await();
            return place;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        @Override
        protected void onPostExecute(PlaceBuffer places) {
            ((VelocityRaptorMain)activite).drawCircle(places.get(0).getLatLng());
            dismiss();
            places.release();
        }
    }

}
