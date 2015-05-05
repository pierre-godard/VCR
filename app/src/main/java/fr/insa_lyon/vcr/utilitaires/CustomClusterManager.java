package fr.insa_lyon.vcr.utilitaires;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Map;

import fr.insa_lyon.vcr.modele.StationVelov;
import fr.insa_lyon.vcr.vcr.VelocityRaptorMain;

/**
 * Created by Lilian on 05/05/2015.
 */
public class CustomClusterManager extends ClusterManager<StationVelov> {

    VelocityRaptorMain activity;

    public CustomClusterManager(Context context, GoogleMap map, VelocityRaptorMain activity) {
        super(context, map);
        this.activity = activity;
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        Log.d("PATAPON", "MARQUEUR DECLENCHE");
        activity.drawAround(marker);
        return true;
    }
}
