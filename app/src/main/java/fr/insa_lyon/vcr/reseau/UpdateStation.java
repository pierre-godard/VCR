package fr.insa_lyon.vcr.reseau;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julien on 29/04/15.
 */
public class UpdateStation extends IntentService {

    private int result = Activity.RESULT_CANCELED;

    public static final String SERVER_URL = "Server_url";
    public static final String URL_PARAM_N1 = "Url_Param_Name1";
    public static final String URL_PARAM_V1 = "Url_Param_Value1";
    public static final String JSON_ARR = "JSON";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "fr.insa_lyon.vcr.reseau.update";

    ServerConnection serverConnection = null;
    JSONArray updateResult;
    List<NameValuePair> url_Param;


    public UpdateStation() {
        super("UpdateStation");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "UpdateStation starting", Toast.LENGTH_SHORT).show();
        url_Param = new ArrayList<NameValuePair>();
        String name = intent.getStringExtra(URL_PARAM_N1);
        String value = intent.getStringExtra(URL_PARAM_V1);
        String url = intent.getStringExtra(SERVER_URL);

        url_Param.add(new BasicNameValuePair(name, value));
        serverConnection = new ServerConnection(url, url_Param);
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
                    updateResult = serverConnection.makeHttpGet();
                    if (updateResult != null)
                        result = Activity.RESULT_OK;
                    publishResults(updateResult, result);
                }
            }
        }).start();
    }


}