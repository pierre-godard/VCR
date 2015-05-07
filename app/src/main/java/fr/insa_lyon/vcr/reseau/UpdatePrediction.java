package fr.insa_lyon.vcr.reseau;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lilian on 06/05/2015.
 */
public class UpdatePrediction extends IntentService{
    private int result = Activity.RESULT_CANCELED;

    public static final String SERVER_URL = "Server_url";
    public static final String ID_STATIONS = "Id_Station";
    public static final String PREDICTION_TIME = "Prediction_time";
    public static final String JSON_ARR = "JSON";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "fr.insa_lyon.vcr.reseau.updatePrediction";

    ServerConnection serverConnection = null;
    JSONArray updateResult;
    List<NameValuePair> url_Param;
    String idStations;
    String predictionTime;
    String url;

    public UpdatePrediction() {
        super("UpdatePrediction");
        updateResult = new JSONArray();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url_Param = new ArrayList<NameValuePair>();
        idStations = intent.getStringExtra(ID_STATIONS);
        predictionTime = intent.getStringExtra(PREDICTION_TIME);
        url = intent.getStringExtra(SERVER_URL);

        serverConnection = new ServerConnection();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        work();
    }

    private void publishResults(JSONArray jsonArr, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        if (result == Activity.RESULT_OK)
            intent.putExtra(JSON_ARR, jsonArr.toString());
        sendBroadcast(intent);
    }

    public void work() {
        new Thread(new Runnable() {
            public void run() {
                if (serverConnection != null) {
                    for(String idStation: idStations.split(";")){
                        serverConnection.setUrl(url + "/"+idStation+"/"+predictionTime);
                        updateResult.put(serverConnection.makeHttpGetObject());
                    }
                    if (updateResult != null)
                        result = Activity.RESULT_OK;
                    publishResults(updateResult, result);
                }
            }
        }).start();
    }

}
