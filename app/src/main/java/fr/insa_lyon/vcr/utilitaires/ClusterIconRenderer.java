package fr.insa_lyon.vcr.utilitaires;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import fr.insa_lyon.vcr.modele.StationVelov;

/**
 * Created by julien on 04/05/15.
 */
public class ClusterIconRenderer extends DefaultClusterRenderer<StationVelov> {


    public ClusterIconRenderer(Context context, GoogleMap map, ClusterManager<StationVelov> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(StationVelov item,
                                               MarkerOptions markerOptions) {
        markerOptions.icon(item.getIcon());
    }
}
