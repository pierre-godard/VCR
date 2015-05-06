package fr.insa_lyon.vcr.vcr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONObject;

/**
 * Created by Wirden on 06/05/2015.
 */

public class GraphStation extends DialogFragment {

    private GraphView graph;
    private  LineGraphSeries<DataPoint> seriesBikes;
    private  LineGraphSeries<DataPoint> seriesStands;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.graph_dialog,null);

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

}
