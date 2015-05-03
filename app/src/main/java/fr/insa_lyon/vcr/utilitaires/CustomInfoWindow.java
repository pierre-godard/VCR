package fr.insa_lyon.vcr.utilitaires;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import fr.insa_lyon.vcr.vcr.R;

/**
 * Created by julien on 03/05/15.
 */
public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {


    LayoutInflater layoutInflater;
    View customInfoView;

    public CustomInfoWindow(LayoutInflater layInf) {
        customInfoView = layInf.inflate(R.layout.custom_info, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
/*
        TextView textView_title = ((TextView) customInfoView.findViewById(R.id.title));
        textView_title.setText(marker.getTitle());
        TextView textView_BikeAvail = ((TextView) customInfoView.findViewById(R.id.snippet));
        textView_BikeAvail.setText(marker.getSnippet());

        return customInfoView;*/
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView textView_title = ((TextView) customInfoView.findViewById(R.id.title));
        textView_title.setText(marker.getTitle());
        TextView textView_BikeAvail = ((TextView) customInfoView.findViewById(R.id.snippet));
        textView_BikeAvail.setText(marker.getSnippet());

        return customInfoView;
    }
}
