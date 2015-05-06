package fr.insa_lyon.vcr.vcr;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.*;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fr.insa_lyon.vcr.modele.ResultatPartiel;
import fr.insa_lyon.vcr.utilitaires.UsefulConstants;



public class ResearchDialog extends DialogFragment {

    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<ResultatPartiel> adp;
    private AutoCompleteTextView t1;
    private GraphView graph;
    private  LineGraphSeries<DataPoint> seriesBikes;
    private  LineGraphSeries<DataPoint> seriesStands;
    //private Activity activite;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.research_dialog,null);
        //instance API
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mGoogleApiClient.connect();
        List<ResultatPartiel> resultList = new ArrayList<>();
        adp = new ArrayAdapter<ResultatPartiel>(this.getActivity(),
                android.R.layout.simple_dropdown_item_1line, resultList);
        adp.setNotifyOnChange(true);

        t1 = (AutoCompleteTextView) v.findViewById(R.id.autoComplete);
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
                new GetPlace().execute(((ResultatPartiel) parent.getAdapter().getItem(position)).getIdentifiantPlace());
            }
        });


     /*   DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpPost httppost = new HttpPost("vps165245.ovh.net/prediction/analysis/10002/0");
        httppost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            // Oops
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
*/
        String result = new String("{\"station\":2040,\"predict_bike_stands\":9.03060393776574,\"predict_bikes\":4.454347597583672,\"predict_state\":\"places available\",\"confidence\":0.06763644755219839,\"time\":1430908836042}");
        graph = (GraphView) v.findViewById(R.id.graph);
        seriesBikes = new LineGraphSeries<DataPoint>(generateData(result, "predict_bikes"));
        seriesStands = new LineGraphSeries<DataPoint>(generateData(result, "predict_bike_stands"));
        seriesStands.setColor(Color.RED);

        graph.addSeries(seriesBikes);
        graph.addSeries(seriesStands);

        builder.setView(v);
        return builder.create();
    }

    private DataPoint[] generateData(String jsonString, String result) {
        int count = 288;
        DataPoint[] values = new DataPoint[2];

      //  for (int i=1; i<count; i++) {
            try {
                JSONObject jObject = new JSONObject(jsonString);
                String S1 = jObject.getString(result);
                DataPoint DP1 = new DataPoint(1, Math.round(Double.parseDouble(S1)));
                DataPoint DP2 = new DataPoint(2, 5);
                values[0] = DP1;
                values[1] = DP2;
            }catch(Exception e){
                // TODO : banane
            }
      //  }
        return values;
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
            ((VelocityRaptorMain)getActivity()).drawCircle(places.get(0).getLatLng());
            dismiss();
            places.release();
        }
    }
}






/**
 * TODO: document your custom view class.




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


    private class GetPredictions extends AsyncTask<String,Void,AutocompletePredictionBuffer> { */
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */

        // Try to understand how AsyncTask works !!!!!!!
     /*   @Override
        protected AutocompletePredictionBuffer doInBackground(String... recherche) {

            PendingResult<AutocompletePredictionBuffer> results  =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, recherche[0],
                            UsefulConstants.rectangleLyon, null );
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await();
            return autocompletePredictions;
        }
*/
        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
   /*     @Override
        protected void onPostExecute(AutocompletePredictionBuffer autocompletePredictions) {
            adp.clear();
            for (AutocompletePrediction prediction : autocompletePredictions) {
                adp.add(new ResultatPartiel(prediction.getDescription(),prediction.getPlaceId()));
            }
            autocompletePredictions.release();
        }
    }

    private class GetPlace extends AsyncTask<String,Void,PlaceBuffer> { */
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */

        // Try to understand how AsyncTask works !!!!!!!
     /*   @Override
        protected PlaceBuffer doInBackground(String... placeId) {
            PendingResult<PlaceBuffer> result  = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId[0]);
            PlaceBuffer place = result.await();
            return place;
        }
*/
        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
     /*   @Override
        protected void onPostExecute(PlaceBuffer places) {
            ((VelocityRaptorMain)activite).drawCircle(places.get(0).getLatLng());
            dismiss();
            places.release();
        }
    }

}
*/