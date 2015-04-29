package fr.insa_lyon.vcr.reseau;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.insa_lyon.vcr.utilitaires.NVP;


public class UpdateStation extends IntentService {


    private int result = Activity.RESULT_CANCELED;

    public static final String SERVER_URL = "Server_url";
    public static final String URL_PARAM = "Url_Param";
    public static final String JSON_OBJ = "JSON";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "fr.insa_lyon.vcr.reseau";

    ServerConnection serverConnection = null;
    JSONObject fetchedResult;
    List<NVP> url_Param;


    public UpdateStation() {
        super("UpdateStation");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "UpdateStation starting", Toast.LENGTH_SHORT).show();

        Bundle extra = intent.getExtras();
        url_Param = (ArrayList<NVP>) extra.getSerializable("extra");
        if (url_Param == null) {
            Log.d("UpdateStation", "url_Param is null");
        } else {
            Log.d("UpdateStation", "url_Param not null");
        }
        serverConnection = new ServerConnection(intent.getStringExtra(SERVER_URL), url_Param);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (serverConnection != null) {
            fetchedResult = serverConnection.makeHttpGet();
            if (fetchedResult != null)
                result = Activity.RESULT_OK;
            publishResults(fetchedResult, result);
        } else {
            publishResults(new JSONObject(), result);
        }
    }

    private void publishResults(JSONObject jsonObj, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        intent.putExtra(JSON_OBJ, jsonObj.toString());
        sendBroadcast(intent);
    }
}
